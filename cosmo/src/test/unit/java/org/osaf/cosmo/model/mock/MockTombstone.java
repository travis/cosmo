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
package org.osaf.cosmo.model.mock;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Tombstone;

/**
 * When an Item is removed from a collection, a tombstone is attached
 * to the collection to track when this removal ocurred.
 */
public abstract class MockTombstone implements Tombstone {
    
   
    private Date timestamp = null;
    
    private Item item = null;

    public MockTombstone() {
    }
    
    public MockTombstone(Item item) {
        this.item = item;
        this.timestamp = new Date(System.currentTimeMillis());
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceTombstone#getTimestamp()
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceTombstone#setTimestamp(java.util.Date)
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceTombstone#getItem()
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceTombstone#setItem(org.osaf.cosmo.model.copy.Item)
     */
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
