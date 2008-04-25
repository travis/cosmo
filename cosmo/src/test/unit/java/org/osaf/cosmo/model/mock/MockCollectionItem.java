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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionItemDetails;
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

    private Set<CollectionItemDetails> childDetails = new HashSet<CollectionItemDetails>(0);
    
    
    public MockCollectionItem() {
    };

    
    public void addChild(Item item) {
        MockCollectionItemDetails cid = new MockCollectionItemDetails(this, item);
        childDetails.add(cid);
    }
    
    public void removeChild(Item item) {
        CollectionItemDetails cid = getChildDetails(item);
        if(cid!=null)
            childDetails.remove(cid);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildren()
     */
    public Set<Item> getChildren() {
        Set<Item> children = new HashSet<Item>();
        for(CollectionItemDetails cid: childDetails)
            children.add(cid.getItem());
        
        return Collections.unmodifiableSet(children);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildDetails(org.osaf.cosmo.model.Item)
     */
    public CollectionItemDetails getChildDetails(Item item) {
        for(CollectionItemDetails cid: childDetails)
            if(cid.getItem().equals(item))
                return cid;
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChild(java.lang.String)
     */
    public Item getChild(String uid) {
        for (Item child : getChildren()) {
            if (child.getUid().equals(uid))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CollectionItem#getChildByName(java.lang.String)
     */
    public Item getChildByName(String name) {
        for (Item child : getChildren()) {
            if (child.getName().equals(name))
                return child;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#isExcludeFreeBusyRollup()
     */
    public boolean isExcludeFreeBusyRollup() {
        Boolean bv =  MockBooleanAttribute.getValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if(bv==null)
            return false;
        else
            return bv.booleanValue();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCollectionItem#setExcludeFreeBusyRollup(boolean)
     */
    public void setExcludeFreeBusyRollup(boolean flag) {
        MockBooleanAttribute.setValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP, flag);
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
