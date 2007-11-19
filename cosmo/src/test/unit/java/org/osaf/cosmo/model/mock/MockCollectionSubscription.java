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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.NotNull;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * Represents a subscription to a shared collection.
 * A subscription belongs to a user and consists of 
 * a ticket key and a collection uid.
 */
public class MockCollectionSubscription extends MockAuditableObject implements CollectionSubscription {

    /**
     * 
     */
    private static final long serialVersionUID = 1376628118792909419L;
    
   
    private User owner;
    
    
    private String displayName;
    
    private String ticketKey;
    
    private String collectionUid;
    
    /**
     */
    public MockCollectionSubscription() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#getCollectionUid()
     */
    public String getCollectionUid() {
        return collectionUid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#setCollectionUid(java.lang.String)
     */
    public void setCollectionUid(String collectionUid) {
        this.collectionUid = collectionUid;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#setCollection(org.osaf.cosmo.model.copy.CollectionItem)
     */
    public void setCollection(CollectionItem collection) {
        this.collectionUid = collection.getUid();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#getOwner()
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#setOwner(org.osaf.cosmo.model.copy.User)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#getTicketKey()
     */
    public String getTicketKey() {
        return ticketKey;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#setTicketKey(java.lang.String)
     */
    public void setTicketKey(String ticketKey) {
        this.ticketKey = ticketKey;
    }  
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionSubscription#setTicket(org.osaf.cosmo.model.copy.Ticket)
     */
    public void setTicket(Ticket ticket) {
        this.ticketKey = ticket.getKey();
    }

    public String calculateEntityTag() {
        // subscription is unique by name for its owner
        String uid = (getOwner() != null && getOwner().getUid() != null) ?
            getOwner().getUid() : "-";
        String name = getDisplayName() != null ? getDisplayName() : "-";
        String modTime = getModifiedDate() != null ?
            new Long(getModifiedDate().getTime()).toString() : "-";
        String etag = uid + ":" + name + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }
}
