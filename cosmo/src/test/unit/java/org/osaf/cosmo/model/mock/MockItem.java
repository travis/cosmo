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
package org.osaf.cosmo.model.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.AttributeTombstone;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionItemDetails;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampTombstone;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.Tombstone;
import org.osaf.cosmo.model.User;


/**
 * Abstract base class for an item on server.  All
 * content in cosmo extends from Item.
 */
public abstract class MockItem extends MockAuditableObject implements Item {

   
    private String uid;
    
   
    private String name;
    
    
    private String displayName;
    
    
    private Date clientCreationDate;
    
    
    private Date clientModifiedDate;
    
    private Integer version = 0;
    
    private transient Boolean isActive = Boolean.TRUE;
    
    
    private Map<QName, Attribute> attributes = new HashMap<QName, Attribute>(0);
    
    
    private Set<Ticket> tickets = new HashSet<Ticket>(0);
    
    
    private Set<Stamp> stamps = new HashSet<Stamp>(0);
    
    
    private Set<Tombstone> tombstones = new HashSet<Tombstone>(0);
    
    private transient Map<String, Stamp> stampMap = null;
    
    private Set<CollectionItemDetails> parentDetails = new HashSet<CollectionItemDetails>(0);
    
    private User owner;
  
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getStamps()
     */
    public Set<Stamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }
    
    
    
    public void setVersion(Integer version) {
        this.version = version;
    }



    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getStampMap()
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
     * @see org.osaf.cosmo.model.copy.InterfaceItem#addStamp(org.osaf.cosmo.model.copy.Stamp)
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
     * @see org.osaf.cosmo.model.copy.InterfaceItem#removeStamp(org.osaf.cosmo.model.copy.Stamp)
     */
    public void removeStamp(Stamp stamp) {
        // only remove stamps that belong to item
        if(!stamps.contains(stamp))
            return;
        
        stamps.remove(stamp);
        
        // add tombstone for tracking purposes
        tombstones.add(new MockStampTombstone(this, stamp));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getStamp(java.lang.String)
     */
    public Stamp getStamp(String type) {
        for(Stamp stamp : stamps)
            // only return stamp if it matches class and is active
            if(stamp.getType().equals(type))
                return stamp;
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getStamp(java.lang.Class)
     */
    public Stamp getStamp(Class clazz) {
        for(Stamp stamp : stamps)
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp))
                return stamp;
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getAttributes()
     */
    public Map<QName, Attribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#addTicket(org.osaf.cosmo.model.copy.Ticket)
     */
    public void addTicket(Ticket ticket) {
        ticket.setItem(this);
        tickets.add(ticket);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#removeTicket(org.osaf.cosmo.model.copy.Ticket)
     */
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#addAttribute(org.osaf.cosmo.model.copy.Attribute)
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
        
        ((MockAttribute) attribute).validate();
        attribute.setItem(this);
        attributes.put(attribute.getQName(), attribute);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
       removeAttribute(new MockQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#removeAttribute(org.osaf.cosmo.model.copy.QName)
     */
    public void removeAttribute(QName qname) {
        if(attributes.containsKey(qname)) {
            attributes.remove(qname);
            tombstones.add(new MockAttributeTombstone(this, qname));
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#removeAttributes(java.lang.String)
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
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getAttribute(java.lang.String)
     */
    public Attribute getAttribute(String name) {
        return getAttribute(new MockQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getAttribute(org.osaf.cosmo.model.copy.QName)
     */
    public Attribute getAttribute(QName qname) {
        return attributes.get(qname);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getAttributeValue(java.lang.String)
     */
    public Object getAttributeValue(String name) {
       return getAttributeValue(new MockQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getAttributeValue(org.osaf.cosmo.model.copy.QName)
     */
    public Object getAttributeValue(QName qname) {
        Attribute attr = attributes.get(qname);
        if (attr == null)
            return attr;
        return attr.getValue();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        setAttribute(new MockQName(name),value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#setAttribute(org.osaf.cosmo.model.QName, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setAttribute(QName key, Object value) {
        MockAttribute attr = (MockAttribute) attributes.get(key);
    
        if(attr!=null) {
            attr.setValue(value);
            attr.validate();
        }
        else
           throw new IllegalArgumentException("attribute " + key + " not found");
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getAttributes(java.lang.String)
     */
    public Map<String, Attribute> getAttributes(String namespace) {
        HashMap<String, Attribute> attrs = new HashMap<String, Attribute>();
        for(Entry<QName, Attribute> e: attributes.entrySet()) {
            if(e.getKey().getNamespace().equals(namespace))
                attrs.put(e.getKey().getLocalName(), e.getValue());
        }
        
        return attrs;
    }
    

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getClientCreationDate()
     */
    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#setClientCreationDate(java.util.Date)
     */
    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getClientModifiedDate()
     */
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#setClientModifiedDate(java.util.Date)
     */
    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getOwner()
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#setOwner(org.osaf.cosmo.model.copy.User)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getUid()
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#setUid(java.lang.String)
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getVersion()
     */
    public Integer getVersion() {
        return version;
    }

    public void addParent(CollectionItem parent) {
        MockCollectionItemDetails cid = new MockCollectionItemDetails(parent, this);
        parentDetails.add(cid);
    }

    public void removeParent(CollectionItem parent) {
        CollectionItemDetails cid = getParentDetails(parent);
        if(cid!=null)
            parentDetails.remove(cid);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getParents()
     */
    public Set<CollectionItem> getParents() {
        
        Set<CollectionItem> parents = new HashSet<CollectionItem>();
        for(CollectionItemDetails cid: parentDetails)
            parents.add(cid.getCollection());
        
        return Collections.unmodifiableSet(parents);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getParent()
     */
    public CollectionItem getParent() {
        if(getParents().size()==0)
            return null;
        
        return getParents().iterator().next();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Item#getParentDetails(org.osaf.cosmo.model.CollectionItem)
     */
    public CollectionItemDetails getParentDetails(CollectionItem parent) {
        for(CollectionItemDetails cid: parentDetails)
            if(cid.getCollection().equals(parent))
                return cid;
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getIsActive()
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#setIsActive(java.lang.Boolean)
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getTickets()
     */
    public Set<Ticket> getTickets() {
        return tickets;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#getTombstones()
     */
    public Set<Tombstone> getTombstones() {
        return tombstones;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#addTombstone(org.osaf.cosmo.model.copy.Tombstone)
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
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceItem#copy()
     */
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
