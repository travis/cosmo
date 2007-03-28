/*
 * Copyright 2007 Open Source Applications Foundation
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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

/**
 * When an Item is removed from a collection, a tombstone is attached
 * to the collection to track when this removal ocurred.
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="tombstones")
@DiscriminatorColumn(
        name="tombstonetype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
public abstract class Tombstone extends BaseModelObject {
    
    private Date timestamp = null;
    private Item item = null;

    public Tombstone() {
    }
    
    public Tombstone(Item item) {
        this.item = item;
        this.timestamp = new Date(System.currentTimeMillis());
    }
    
    @Column(name = "removedate", nullable = false)
    @Type(type="long_timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemid", nullable = false)
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null || !(obj instanceof Tombstone))
            return false;
        return new EqualsBuilder().append(item, ((Tombstone) obj).getItem()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 23).append(item).toHashCode();
    }
}
