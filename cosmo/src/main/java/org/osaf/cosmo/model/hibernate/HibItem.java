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
package org.osaf.cosmo.model.hibernate;

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
import org.hibernate.annotations.MapKey;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.AttributeTombstone;
import org.osaf.cosmo.model.BinaryAttribute;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.DataSizeException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampTombstone;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.Tombstone;
import org.osaf.cosmo.model.User;
import org.w3c.dom.Element;


/**
 * Hibernate persistent Item.
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
public abstract class HibItem extends HibAuditableObject implements Item {

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
    
    @OneToMany(targetEntity=HibAttribute.class, mappedBy = "item", fetch=FetchType.LAZY)
    @MapKey(targetElement=HibQName.class)
    // turns out this creates a query that is unoptimized for MySQL
    //@Fetch(FetchMode.SUBSELECT)
    @BatchSize(size=50)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN }) 
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Map<QName, Attribute> attributes = new HashMap<QName, Attribute>(0);
    
    @OneToMany(targetEntity=HibTicket.class, mappedBy = "item", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN }) 
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Ticket> tickets = new HashSet<Ticket>(0);
    
    @OneToMany(targetEntity=HibStamp.class, mappedBy = "item", fetch=FetchType.LAZY)
    // turns out this creates a query that is unoptimized for MySQL
    //@Fetch(FetchMode.SUBSELECT)
    @BatchSize(size=50)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Stamp> stamps = new HashSet<Stamp>(0);
    
    @OneToMany(targetEntity=HibTombstone.class, mappedBy="item", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN }) 
    protected Set<Tombstone> tombstones = new HashSet<Tombstone>(0);
    
    private transient Map<String, Stamp> stampMap = null;
    
    @ManyToMany(targetEntity=HibCollectionItem.class, fetch=FetchType.LAZY) 
    @JoinTable(
        name="collection_item",
        joinColumns={@JoinColumn(name="itemid")},
        inverseJoinColumns={@JoinColumn(name="collectionid")}
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<CollectionItem> parents = new HashSet<CollectionItem>(0);
    
    @ManyToOne(targetEntity=HibUser.class, fetch=FetchType.LAZY)
    @JoinColumn(name="ownerid", nullable = false)
    @NotNull
    private User owner;
  
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getStamps()
     */
    public Set<Stamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getStampMap()
     */
    public Map<String, Stamp> getStampMap() {
        if(stampMap==null) {
            stampMap = new HashMap<String, Stamp>();
            for(Stamp stamp : stamps)
                stampMap.put(stamp.getType(), stamp);
        }
        
        return stampMap;
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addStamp(org.osaf.cosmo.model.Stamp)
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
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#removeStamp(org.osaf.cosmo.model.Stamp)
     */
    public void removeStamp(Stamp stamp) {
        // only remove stamps that belong to item
        if(!stamps.contains(stamp))
            return;
        
        stamps.remove(stamp);
        
        // add tombstone for tracking purposes
        tombstones.add(new HibStampTombstone(this, stamp));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getStamp(java.lang.String)
     */
    public Stamp getStamp(String type) {
        for(Stamp stamp : stamps)
            // only return stamp if it matches class and is active
            if(stamp.getType().equals(type))
                return stamp;
        
        return null;
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getStamp(java.lang.Class)
     */
    public Stamp getStamp(Class clazz) {
        for(Stamp stamp : stamps)
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp))
                return stamp;
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getAttributes()
     */
    public Map<QName, Attribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addTicket(org.osaf.cosmo.model.Ticket)
     */
    public void addTicket(Ticket ticket) {
        ticket.setItem(this);
        tickets.add(ticket);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#removeTicket(org.osaf.cosmo.model.Ticket)
     */
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addAttribute(org.osaf.cosmo.model.Attribute)
     */
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
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
       removeAttribute(new HibQName(name));
    }
  
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#removeAttribute(org.osaf.cosmo.model.QName)
     */
    public void removeAttribute(QName qname) {
        if(attributes.containsKey(qname)) {
            attributes.remove(qname);
            tombstones.add(new HibAttributeTombstone(this, qname));
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#removeAttributes(java.lang.String)
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

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getAttribute(java.lang.String)
     */
    public Attribute getAttribute(String name) {
        return getAttribute(new HibQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getAttribute(org.osaf.cosmo.model.QName)
     */
    public Attribute getAttribute(QName qname) {
        return attributes.get(qname);
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getAttributeValue(java.lang.String)
     */
    public Object getAttributeValue(String name) {
       return getAttributeValue(new HibQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getAttributeValue(org.osaf.cosmo.model.QName)
     */
    public Object getAttributeValue(QName qname) {
        Attribute attr = attributes.get(qname);
        if (attr == null)
            return attr;
        return attr.getValue();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addStringAttribute(java.lang.String, java.lang.String)
     */
    public void addStringAttribute(String name, String value) {
        addStringAttribute(new HibQName(name), value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addStringAttribute(org.osaf.cosmo.model.QName, java.lang.String)
     */
    public void addStringAttribute(QName qname, String value) {
        addAttribute(new HibStringAttribute(qname, value));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addIntegerAttribute(java.lang.String, java.lang.Long)
     */
    public void addIntegerAttribute(String name, Long value) {
        addIntegerAttribute(new HibQName(name), value);
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addIntegerAttribute(org.osaf.cosmo.model.QName, java.lang.Long)
     */
    public void addIntegerAttribute(QName qname, Long value) {
        addAttribute(new HibIntegerAttribute(qname, value));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addBooleanAttribute(java.lang.String, java.lang.Boolean)
     */
    public void addBooleanAttribute(String name, Boolean value) {
        addBooleanAttribute(new HibQName(name), value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addBooleanAttribute(org.osaf.cosmo.model.QName, java.lang.Boolean)
     */
    public void addBooleanAttribute(QName qname, Boolean value) {
        addAttribute(new HibBooleanAttribute(qname, value));
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addDateAttribute(java.lang.String, java.util.Date)
     */
    public void addDateAttribute(String name, Date value) {
        addDateAttribute(new HibQName(name), value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addDateAttribute(org.osaf.cosmo.model.QName, java.util.Date)
     */
    public void addDateAttribute(QName qname, Date value) {
        addAttribute(new HibDateAttribute(qname, value));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addMultiValueStringAttribute(java.lang.String, java.util.Set)
     */
    public void addMultiValueStringAttribute(String name, Set<String> value) {
        addMultiValueStringAttribute(new HibQName(name), value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addMultiValueStringAttribute(org.osaf.cosmo.model.QName, java.util.Set)
     */
    public void addMultiValueStringAttribute(QName qname, Set<String> value) {
        addAttribute(new HibMultiValueStringAttribute(qname, value));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addDictionaryAttribute(java.lang.String, java.util.Map)
     */
    public void addDictionaryAttribute(String name, Map<String, String> value) {
        addDictionaryAttribute(new HibQName(name), value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addDictionaryAttribute(org.osaf.cosmo.model.QName, java.util.Map)
     */
    public void addDictionaryAttribute(QName qname, Map<String, String> value) {
        addAttribute(new HibDictionaryAttribute(qname, value));
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addXmlAttribute(java.lang.String, org.w3c.dom.Element)
     */
    public void addXmlAttribute(String name, Element value) {
        addXmlAttribute(new HibQName(name), value);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addXmlAttribute(org.osaf.cosmo.model.QName, org.w3c.dom.Element)
     */
    public void addXmlAttribute(QName qname, Element value) {
        addAttribute(new HibXmlAttribute(qname, value));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        setAttribute(new HibQName(name),value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setAttribute(org.osaf.cosmo.model.QName, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setAttribute(QName key, Object value) {
        Attribute attr = (Attribute) attributes.get(key);
    
        if(attr==null)
        {
            if(value instanceof String)
                attr = new HibStringAttribute(key, (String) value);
            else if(value instanceof byte[])
                attr = new HibBinaryAttribute(key, (byte[]) value);
            else if(value instanceof Long)
                attr = new HibIntegerAttribute(key, (Long) value);
            else if(value instanceof Boolean)
                attr = new HibBooleanAttribute(key, (Boolean) value);
            else if(value instanceof Date)
                attr = new HibDateAttribute(key, (Date) value);
            else if(value instanceof Set)
                attr = new HibMultiValueStringAttribute(key, (Set) value);
            else if(value instanceof Map)
                attr = new HibDictionaryAttribute(key, (Map) value);
            else if(value instanceof Element)
                attr = new HibXmlAttribute(key, (Element) value);
            else
                attr = new HibStringAttribute(key, value.toString());
            addAttribute(attr);
        } else {
            validateAttribute(attr, value);
            attr.setValue(value);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getAttributes(java.lang.String)
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
    

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getClientCreationDate()
     */
    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setClientCreationDate(java.util.Date)
     */
    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getClientModifiedDate()
     */
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setClientModifiedDate(java.util.Date)
     */
    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getOwner()
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setOwner(org.osaf.cosmo.model.User)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getUid()
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setUid(java.lang.String)
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getVersion()
     */
    public Integer getVersion() {
        return version;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getParents()
     */
    public Set<CollectionItem> getParents() {
        return parents;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getParent()
     */
    public CollectionItem getParent() {
        if(parents.size()==0)
            return null;
        
        return parents.iterator().next();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getIsActive()
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setIsActive(java.lang.Boolean)
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getTickets()
     */
    public Set<Ticket> getTickets() {
        return Collections.unmodifiableSet(tickets);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getTombstones()
     */
    public Set<Tombstone> getTombstones() {
        return Collections.unmodifiableSet(tombstones);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#addTombstone(org.osaf.cosmo.model.Tombstone)
     */
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
        item.setDisplayName(getDisplayName());
        
        // copy attributes
        for(Entry<QName, Attribute> entry: attributes.entrySet())
            item.addAttribute(entry.getValue().copy());
        
        // copy stamps
        for(Stamp stamp: stamps)
            item.addStamp(stamp.copy());
    }
}
