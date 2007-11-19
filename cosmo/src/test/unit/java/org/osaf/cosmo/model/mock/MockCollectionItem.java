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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Tombstone;

/**
 * Extends {@link Item} to represent a collection of items
 */

public class MockCollectionItem extends MockItem implements CollectionItem {

    /**
     * 
     */
    private static final long serialVersionUID = 2873258323314048223L;
    
    // CollectionItem specific attributes
    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
        new MockQName(CollectionItem.class, "excludeFreeBusyRollup");
    
    public static final QName ATTR_HUE =
        new MockQName(CollectionItem.class, "hue");

    private Set<Item> children = new HashSet<Item>(0);
    
    public MockCollectionItem() {
    };

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#getChildren()
     */
    public Set<Item> getChildren() {
        return children;
    }

    private void setChildren(Set<Item> children) {
        this.children = children;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#getChild(java.lang.String)
     */
    public Item getChild(String uid) {
        for (Item child : children) {
            if (child.getUid().equals(uid))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#getChildByName(java.lang.String)
     */
    public Item getChildByName(String name) {
        for (Item child : children) {
            if (child.getName().equals(name))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#isExcludeFreeBusyRollup()
     */
    public boolean isExcludeFreeBusyRollup() {
        Boolean val =
            (Boolean) getAttributeValue(ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if (val != null)
            return val.booleanValue();
        return false;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#setExcludeFreeBusyRollup(boolean)
     */
    public void setExcludeFreeBusyRollup(boolean flag) {
        setAttribute(ATTR_EXCLUDE_FREE_BUSY_ROLLUP, Boolean.valueOf(flag));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#getHue()
     */
    public Long getHue() {
        return MockIntegerAttribute.getValue(this, ATTR_HUE);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#setHue(java.lang.Long)
     */
    public void setHue(Long value) {
        MockIntegerAttribute.setValue(this, ATTR_HUE, value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#removeTombstone(org.osaf.cosmo.model.copy.Item)
     */
    public boolean removeTombstone(Item item) {
        for(Iterator<Tombstone> it = getTombstones().iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof ItemTombstone) {
                if(((ItemTombstone) ts).getItemUid().equals(item.getUid())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#generateHash()
     */
    public int generateHash() {
        return getVersion();
    }
    
    public Item copy() {
        CollectionItem copy = new MockCollectionItem();
        copyToItem(copy);
        return copy;
    }
}
