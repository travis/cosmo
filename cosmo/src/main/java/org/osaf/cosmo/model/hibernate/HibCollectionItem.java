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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;
import org.osaf.cosmo.model.QName;

/**
 * Hibernate persistent CollectionItem.
 */
@Entity
@DiscriminatorValue("collection")
public class HibCollectionItem extends HibItem implements CollectionItem {

    /**
     * 
     */
    private static final long serialVersionUID = 2873258323314048223L;
    
    // CollectionItem specific attributes
    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
        new HibQName(CollectionItem.class, "excludeFreeBusyRollup");
    
    public static final QName ATTR_HUE =
        new HibQName(CollectionItem.class, "hue");

    @ManyToMany(targetEntity=HibItem.class, mappedBy="parents",fetch=FetchType.LAZY)
    private Set<Item> children = new HashSet<Item>(0);
    
    public HibCollectionItem() {
    };

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildren()
     */
    public Set<Item> getChildren() {
        return children;
    }
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChild(java.lang.String)
     */
    public Item getChild(String uid) {
        for (Item child : children) {
            if (child.getUid().equals(uid))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildByName(java.lang.String)
     */
    public Item getChildByName(String name) {
        for (Item child : children) {
            if (child.getName().equals(name))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#isExcludeFreeBusyRollup()
     */
    public boolean isExcludeFreeBusyRollup() {
        Boolean val =
            (Boolean) getAttributeValue(ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if (val != null)
            return val.booleanValue();
        return false;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#setExcludeFreeBusyRollup(boolean)
     */
    public void setExcludeFreeBusyRollup(boolean flag) {
        setAttribute(ATTR_EXCLUDE_FREE_BUSY_ROLLUP, Boolean.valueOf(flag));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getHue()
     */
    public Long getHue() {
        return HibIntegerAttribute.getValue(this, ATTR_HUE);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#setHue(java.lang.Long)
     */
    public void setHue(Long value) {
        HibIntegerAttribute.setValue(this, ATTR_HUE, value);
    }
    
    /**
     * Remove ItemTombstone with an itemUid equal to a given Item's uid
     * @param item
     * @return true if a tombstone was removed
     */
    public boolean removeTombstone(Item item) {
        ItemTombstone ts = new HibItemTombstone(this, item);
        return tombstones.remove(ts);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#generateHash()
     */
    public int generateHash() {
        return getVersion();
    }
    
    public Item copy() {
        CollectionItem copy = new HibCollectionItem();
        copyToItem(copy);
        return copy;
    }
}
