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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * When an Stamp is removed from an item, a tombstone is attached
 * to the item to track when this removal ocurred.
 */
@Entity
@DiscriminatorValue("stamp")
public class StampTombstone extends Tombstone {
    
    @Column(name="stamptype", length=255)
    private String stampType = null;

    public StampTombstone() {
    }
    
    public StampTombstone(Item item, Stamp stamp) {
        super(item);
        stampType = stamp.getType();
    }
    
    public String getStampType() {
        return this.stampType;
    }

    public void setStampType(String stampType) {
        this.stampType = stampType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StampTombstone))
            return false;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(
                stampType, ((StampTombstone) obj).getStampType()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 25).appendSuper(super.hashCode())
                .append(stampType.hashCode()).toHashCode();
    }
    
    
}
