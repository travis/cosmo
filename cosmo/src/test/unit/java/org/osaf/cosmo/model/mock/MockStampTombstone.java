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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampTombstone;

/**
 * When an Stamp is removed from an item, a tombstone is attached
 * to the item to track when this removal ocurred.
 */
public class MockStampTombstone extends MockTombstone implements StampTombstone {
    
    private String stampType = null;

    public MockStampTombstone() {
    }
    
    public MockStampTombstone(Item item, Stamp stamp) {
        super(item);
        stampType = stamp.getType();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStampTombstone#getStampType()
     */
    public String getStampType() {
        return this.stampType;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStampTombstone#setStampType(java.lang.String)
     */
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
