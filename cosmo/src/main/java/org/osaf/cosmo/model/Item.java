/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.w3c.dom.Element;


/**
 * Abstract base class for an item on server.  All
 * content in cosmo extends from Item.
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="item")
@org.hibernate.annotations.Table(
        appliesTo="item", 
        indexes={@Index(name="idx_itemtype", columnNames={"itemtype"})})
@DiscriminatorColumn(
        name="itemtype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Item extends AuditableObject {

    public static final long MAX_BINARY_ATTR_SIZE = 100 * 1024 * 1024;
    public static final long MAX_STRING_ATTR_SIZE = 1 * 1024;

    @Column(name = "uid", nullable = false, unique=true, length=255)
    @NotNull
    @Length(min=1, max=255)
    @Index(name="idx_itemuid")
    private String uid;
    
    @Column(name = "itemname", nullable = false, length=255)
    @NotNull
    @Length(min=1, max=255)
    @Index(name="idx_itemname")
    private String name;
    
    @Column(name = "displayname", length=1024)
    private String displayName;
    
    @Column(name = "clientcreatedate")
    @Type(type="long_timestamp")
    private Date clientCreationDate;
    
    @Column(name = "clientmodifieddate")
    @Type(type="long_timestamp")
    private Date clientModifiedDate;
    
    @Version
    @Column(name="version", nullable = false)
    private Integer version;
    
    private transient Boolean isActive = Boolean.TRUE;
    
    @OneToMany(mappedBy = "item", fetch=FetchType.LAZY)
    // turns out this creates a query that is unoptimized for MySQL
    //@Fetch(FetchMode.SUBSELECT)
    @BatchSize(size=50)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN }) 
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Map<QName, Attribute> attributes = new HashMap<QName, Attribute>(0);
    
    @OneToMany(mappedBy = "item", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN }) 
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Ticket> tickets = new HashSet<Ticket>(0);
    
    @OneToMany(mappedBy = "item", fetch=FetchType.LAZY)
    // turns out this creates a query that is unoptimized for MySQL
    //@Fetch(FetchMode.SUBSELECT)
    @BatchSize(size=50)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Stamp> stamps = new HashSet<Stamp>(0);
    
    @OneToMany(mappedBy="item", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN }) 
    private Set<Tombstone> tombstones = new HashSet<Tombstone>(0);
    
    private transient Map<String, Stamp> stampMap = null;
    
    @ManyToMany(fetch=FetchType.LAZY) 
    @JoinTable(
        name="collection_item",
        joinColumns={@JoinColumn(name="itemid")},
        inverseJoinColumns={@JoinColumn(name="collectionid")}
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<CollectionItem> parents = new HashSet<CollectionItem>(0);
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ownerid", nullable = false)
    @NotNull
    private User owner;
  
    
    /**
     * Return all stamps associated with Item.  Use
     * addStamp() and removeStamp() to manipulate set.
     * @return set of stamps associated with Item
     */
    public Set<Stamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }
    
    /**
     * @return Map of Stamps indexed by Stamp type.
     */
    public Map<String, Stamp> getStampMap() {
        if(stampMap==null) {
            stampMap = new HashMap<String, Stamp>();
            for(Stamp stamp : stamps)
                stampMap.put(stamp.getType(), stamp);
        }
        
        return stampMap;
    }
    
    /**
     * Add stamp to Item
     * @param stamp stamp to add
     */
    public void addStamp(Stamp stamp) {
        if (stamp == null)
            throw new IllegalArgumentException("stamp cannot be null");

        // remove old tombstone if exists
        for(Iterator<Tombstone> it=tombstones.iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof StampTombstone)
                if(((StampTombstone) ts).getStampType().equals(stamp.getType()))
                    it.remove();
        }
        
        stamp.setItem(this);
        stamps.add(stamp);
    }
    
    /**
     * Remove stamp from Item.
     * @param stamp stamp to remove
     */
    public void removeStamp(Stamp stamp) {
        // only remove stamps that belong to item
        if(!stamps.contains(stamp))
            return;
        
        stamps.remove(stamp);
        
        // add tombstone for tracking purposes
        tombstones.add(new StampTombstone(this, stamp));
    }
    
    /**
     * Get the stamp that corresponds to the specified type
     * @param type stamp type to return
     * @return stamp
     */
    public Stamp getStamp(String type) {
        for(Stamp stamp : stamps)
            // only return stamp if it matches class and is active
            if(stamp.getType().equals(type))
                return stamp;
        
        return null;
    }
    
    /**
     * Get the stamp that corresponds to the specified class
     * @param clazz class of stamp to return
     * @return stamp
     */
    public Stamp getStamp(Class clazz) {
        for(Stamp stamp : stamps)
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp))
                return stamp;
        
        return null;
    }

    /**
     * Get all Attributes of Item.  Use addAttribute() and 
     * removeAttribute() to manipulate map.
     * @return
     */
    public Map<QName, Attribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
    
    public void addTicket(Ticket ticket) {
        ticket.setItem(this);
        tickets.add(ticket);
    }
    
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    public void addAttribute(Attribute attribute) {
        if (attribute == null)
            throw new IllegalArgumentException("attribute cannot be null");

        // remove old tombstone if exists
        for(Iterator<Tombstone> it=tombstones.iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof AttributeTombstone)
                if(((AttributeTombstone) ts).getQName().equals(attribute.getQName()))
                    it.remove();
        }
        
        validateAttribute(attribute);
        attribute.setItem(this);
        attributes.put(attribute.getQName(), attribute);
    }
    
    /**
     * Remove attribute in default namespace with local name.
     * @param name local name of attribute to remove
     */
    public void removeAttribute(String name) {
       removeAttribute(new QName(name));
    }
    
    /**
     * Remove attribute.
     * @param qname qualifed name of attribute to remove.
     */
    public void removeAttribute(QName qname) {
        if(attributes.containsKey(qname)) {
            attributes.remove(qname);
            tombstones.add(new AttributeTombstone(this, qname));
        }
    }

    /**
     * Remove all attributes in a namespace.
     * @param namespace namespace of attributes to remove
     */
    public void removeAttributes(String namespace) {
        ArrayList<QName> toRemove = new ArrayList<QName>();
        for (QName qname: attributes.keySet()) {
            if (qname.getNamespace().equals(namespace))
                toRemove.add(qname);
        }
        
        for(QName qname: toRemove)
            removeAttribute(qname);
    }

    /**
     * Get attribute in default namespace with local name.
     * @param name local name of attribute
     * @return attribute in default namespace with given name
     */
    public Attribute getAttribute(String name) {
        return getAttribute(new QName(name));
    }
    
    /**
     * Get attribute with qualified name.
     * @param qname qualified name of attribute to retrieve
     * @return attribute with qualified name.
     */
    public Attribute getAttribute(QName qname) {
        return attributes.get(qname);
    }
    
    /**
     * Get attribute value with local name in default namespace
     * @param name local name of attribute
     * @return attribute value
     */
    public Object getAttributeValue(String name) {
       return getAttributeValue(new QName(name));
    }
    
    /**
     * Get attribute value with qualified name
     * @param qname qualified name of attribute
     * @return attribute value
     */
    public Object getAttributeValue(QName qname) {
        Attribute attr = attributes.get(qname);
        if (attr == null)
            return attr;
        return attr.getValue();
    }

    /**
     * Add new StringAttribute in default namespace
     * @param name local name of attribute to add
     * @param value String value of attribute to add
     */
    public void addStringAttribute(String name, String value) {
        addStringAttribute(new QName(name), value);
    }
    
    /**
     * Add new StringAttribute
     * @param qname qualified name of attribute to add
     * @param value String value of attribute to add
     */
    public void addStringAttribute(QName qname, String value) {
        addAttribute(new StringAttribute(qname, value));
    }
    
    /**
     * Add new IntegerAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Integer value of attribute to add
     */
    public void addIntegerAttribute(String name, Long value) {
        addIntegerAttribute(new QName(name), value);
    }
    
    /**
     * Add new IntegerAttribute
     * @param qname qualified name of attribute to add
     * @param value Integer value of attribute to add
     */
    public void addIntegerAttribute(QName qname, Long value) {
        addAttribute(new IntegerAttribute(qname, value));
    }
    
    /**
     * Add new BooleanAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Boolean value of attribute to add
     */
    public void addBooleanAttribute(String name, Boolean value) {
        addBooleanAttribute(new QName(name), value);
    }
    
    /**
     * Add new BooleanAttribute
     * @param qname qualified name of attribute to addd
     * @param value Boolean value of attribute to add
     */
    public void addBooleanAttribute(QName qname, Boolean value) {
        addAttribute(new BooleanAttribute(qname, value));
    }
    
    /**
     * Add new DateAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Date value of attribute to add
     */
    public void addDateAttribute(String name, Date value) {
        addDateAttribute(new QName(name), value);
    }
    
    /**
     * Add new DateAttribute
     * @param qname qualified name of attribute to add
     * @param value Date value of attribute to add
     */
    public void addDateAttribute(QName qname, Date value) {
        addAttribute(new DateAttribute(qname, value));
    }
    
    /**
     * Add new MultiValueStringAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Set value of attribute to add
     */
    public void addMultiValueStringAttribute(String name, Set<String> value) {
        addMultiValueStringAttribute(new QName(name), value);
    }
    
    /**
     * Add new MultiValueStringAttribute
     * @param qname qualified name of attribute to add
     * @param value Set value of attribute to add
     */
    public void addMultiValueStringAttribute(QName qname, Set<String> value) {
        addAttribute(new MultiValueStringAttribute(qname, value));
    }
    
    /**
     * Add new DictionaryAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Map value of attribute to add
     */
    public void addDictionaryAttribute(String name, Map<String, String> value) {
        addDictionaryAttribute(new QName(name), value);
    }
    
    /**
     * Add new DictionaryAttribute
     * @param qname qualified name of attribute to add
     * @param value Map value of attribute to add
     */
    public void addDictionaryAttribute(QName qname, Map<String, String> value) {
        addAttribute(new DictionaryAttribute(qname, value));
    }

    /**
     * Add new XmlAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Element value of attribute to add
     */
    public void addXmlAttribute(String name, Element value) {
        addXmlAttribute(new QName(name), value);
    }

    /**
     * Add new XmlAttribute
     * @param qname qualified name of attribute to add
     * @param value Element value of attribute to add
     */
    public void addXmlAttribute(QName qname, Element value) {
        addAttribute(new XmlAttribute(qname, value));
    }
    
    /**
     * Set attribute value of attribute with local name in default
     * namespace.
     * @param name local name of attribute
     * @param value value to update attribute
     */
    public void setAttribute(String name, Object value) {
        setAttribute(new QName(name),value);
    }
    
    /**
     * Set attribute value attribute with qualified name
     * @param key qualified name of attribute
     * @param value value to update attribute
     */
    @SuppressWarnings("unchecked")
    public void setAttribute(QName key, Object value) {
        Attribute attr = (Attribute) attributes.get(key);
    
        if(attr==null)
        {
            if(value instanceof String)
                attr = new StringAttribute(key, (String) value);
            else if(value instanceof byte[])
                attr = new BinaryAttribute(key, (byte[]) value);
            else if(value instanceof Long)
                attr = new IntegerAttribute(key, (Long) value);
            else if(value instanceof Boolean)
                attr = new BooleanAttribute(key, (Boolean) value);
            else if(value instanceof Date)
                attr = new DateAttribute(key, (Date) value);
            else if(value instanceof Set)
                attr = new MultiValueStringAttribute(key, (Set) value);
            else if(value instanceof Map)
                attr = new DictionaryAttribute(key, (Map) value);
            else if(value instanceof Element)
                attr = new XmlAttribute(key, (Element) value);
            else
                attr = new StringAttribute(key, value.toString());
            addAttribute(attr);
        } else {
            validateAttribute(attr, value);
            attr.setValue(value);
        }
    }

    /**
     * Return Attributes for a given namespace.  Attributes are returned
     * in a Map indexed by the name of the attribute.
     * @param namespace namespace of the Attributes to return
     * @return map of Attributes indexed by the name of the attribute
     */
    public Map<String, Attribute> getAttributes(String namespace) {
        HashMap<String, Attribute> attrs = new HashMap<String, Attribute>();
        for(Entry<QName, Attribute> e: attributes.entrySet()) {
            if(e.getKey().getNamespace().equals(namespace))
                attrs.put(e.getKey().getLocalName(), e.getValue());
        }
        
        return attrs;
    }
    
    // TODO: move to hibernate validator
    protected void validateAttribute(Attribute attribute,
                                     Object value) {
        if (value == null)
            return;

        if (attribute instanceof BinaryAttribute) {
            byte[] v = (byte[]) value;
            if (v.length > MAX_BINARY_ATTR_SIZE)
                throw new DataSizeException("Binary attribute " + attribute.getQName() + " too large");
        }

        if (attribute instanceof StringAttribute) {
            String v = (String) value;
            if (v.length() > MAX_STRING_ATTR_SIZE)
                throw new DataSizeException("String attribute " + attribute.getQName() + " too large");
        }
    }

    protected void validateAttribute(Attribute attribute) {
        validateAttribute(attribute, attribute.getValue());
    }
    

    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }
    
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Item's human readable name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName Item's human readable name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getVersion() {
        return version;
    }

    public Set<CollectionItem> getParents() {
        return parents;
    }

    /**
     * Return a single parent.
     * @deprecated
     */
    public CollectionItem getParent() {
        if(parents.size()==0)
            return null;
        
        return parents.iterator().next();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Get all Tickets on Item.  
     * @return set of tickets
     */
    public Set<Ticket> getTickets() {
        return tickets;
    }

    public Set<Tombstone> getTombstones() {
        return tombstones;
    }

    public void addTombstone(Tombstone tombstone) {
        tombstone.setItem(this);
        tombstones.add(tombstone);
    }
    
    
    /**
     * Item uid determines equality 
     */
    @Override
    public boolean equals(Object obj) {
        if(obj==null || uid==null)
            return false;
        if( ! (obj instanceof Item))
            return false;
        
        return uid.equals(((Item) obj).getUid());
    }

    @Override
    public int hashCode() {
        if(uid==null)
            return super.hashCode();
        else
            return uid.hashCode();
    }
    
    public abstract Item copy();
   
    
    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
            new Long(getModifiedDate().getTime()).toString() : "-";
        String etag = uid + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }

    protected void copyToItem(Item item) {
        item.setOwner(getOwner());
        item.setName(getName());
        item.setDisplayName(getDisplayName());
        
        // copy attributes
        for(Entry<QName, Attribute> entry: attributes.entrySet())
            item.addAttribute(entry.getValue().copy());
        
        // copy stamps
        for(Stamp stamp: stamps)
            item.addStamp(stamp.copy());
    }
}
