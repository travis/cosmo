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

/**
 * Represents a subscription to a shared collection.
 * A subscription belongs to a user and consists of 
 * a ticket key and a collection uid.
 */
@Entity
//Define a unique constraint on user and name
//because we don't want two subscriptions with the same name
//to be associated with the same user
@Table(name="subscription", uniqueConstraints = {
        @UniqueConstraint(columnNames={"ownerid", "displayname"})})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CollectionSubscription extends AuditableObject {

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
    public CollectionSubscription() {
    }

    /**
     * Return the uid of the shared collection.  
     * Note, it is possible that the Collection with this uid is not
     * present in the system.  This will happen if a collection is 
     * shared and then the owner deletes the collection.
     * @return Collection uid
     */
    @Column(name = "collectionuid", nullable = false, length = 255)
    @NotNull
    public String getCollectionUid() {
        return collectionUid;
    }

    public void setCollectionUid(String collectionUid) {
        this.collectionUid = collectionUid;
    }
    
    public void setCollection(CollectionItem collection) {
        this.collectionUid = collection.getUid();
    }

    @Column(name = "displayname", nullable = false, length = 255)
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerid", nullable = false)
    @NotNull
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Return the ticket key used to subscribe to the shared collection.
     * Note, it is possible that the Ticket represented by this key
     * is not present in the system.  This happens when a ticket is
     * created for a shared collection, and then removed by the owner.
     * @return
     */
    @Column(name = "ticketkey", nullable = false, length = 255)
    @NotNull
    public String getTicketKey() {
        return ticketKey;
    }

    public void setTicketKey(String ticketKey) {
        this.ticketKey = ticketKey;
    }  
    
    public void setTicket(Ticket ticket) {
        this.ticketKey = ticket.getKey();
    }
}
