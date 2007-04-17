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

import java.util.Date;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

/**
 * Represents an abstract Stamp on an Item. A Stamp is a set of related
 * properties and apis that is associated to an item.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "stamptype", 
                     discriminatorType = DiscriminatorType.STRING, length = 16)
// Unique constraint for stamptype and itemid to prevent items
// having more than one of the same stamp
@Table(name = "stamp", uniqueConstraints = { 
        @UniqueConstraint(columnNames = { "itemid", "stamptype" }) })
// Define index on discriminator
@org.hibernate.annotations.Table(appliesTo = "stamp", 
        indexes = { @Index(name = "idx_stamptype", columnNames = { "stamptype" }) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Stamp extends AuditableObject implements
        java.io.Serializable {

    // Fields
    private Item item;
    private boolean dirty = false;

    // Constructors
    /** default constructor */
    public Stamp() {
    }

    /**
     * @return Item attribute belongs to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "itemid", nullable = false)
    public Item getItem() {
        return item;
    }

    /**
     * @param item
     *            attribute belongs to
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * @return Stamp type
     */
    @Transient
    public abstract String getType();

   
    /**
     * Return a new instance of Stamp containing a copy of the Stamp
     * @param item Item stamp is copied to
     * @return copy of Stamp
     */
    public abstract Stamp copy(Item item);
    
    
    @Transient
    public boolean isDirty() {
        return dirty;
    }
    
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    /**
     * Update stamp's timestamp
     */
    protected void updateTimestamp() {
        setModifiedDate(new Date());
    }
}
