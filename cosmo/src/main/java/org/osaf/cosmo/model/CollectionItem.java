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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

/**
 * Extends {@link Item} to represent a collection of items
 */
@Entity
@DiscriminatorValue("collection")
public class CollectionItem extends Item {

    /**
     * 
     */
    private static final long serialVersionUID = 2873258323314048223L;
    
    // CollectionItem specific attributes
    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
        new QName(CollectionItem.class, "excludeFreeBusyRollup");

    private Set<Item> children = new HashSet<Item>(0);
    
    public CollectionItem() {
    };

    /**
     * Return active children items (those with isActive=true).
     * @return active children items
     */
    @ManyToMany(mappedBy="parents",fetch=FetchType.LAZY)
    public Set<Item> getChildren() {
        return children;
    }

    private void setChildren(Set<Item> children) {
        this.children = children;
    }
    
    /**
     * Return child item with matching uid
     * @return identified child item, or null if no child with that
     * uid exists
     */
    public Item getChild(String uid) {
        for (Item child : children) {
            if (child.getUid().equals(uid))
                return child;
        }
        return null;
    }

    @Transient
    public boolean isExcludeFreeBusyRollup() {
        Boolean val =
            (Boolean) getAttributeValue(ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if (val != null)
            return val.booleanValue();
        return false;
    }

    public void setExcludeFreeBusyRollup(boolean flag) {
        setAttribute(ATTR_EXCLUDE_FREE_BUSY_ROLLUP, Boolean.valueOf(flag));
    }
    
    /**
     * Remove ItemTombstone with an itemUid equal to a given Item's uid
     * @param item
     * @return true if a tombstone was removed
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
    
    /**
     * Generate alternative hash code for collection.
     * This hash code will return a different value if
     * collection or any child items in the collection
     * has changed since the last hash code was generated.
     * @return
     */
    public int generateHash() {
        return getVersion();
    }
    
    public Item copy() {
        CollectionItem copy = new CollectionItem();
        copyToItem(copy);
        return copy;
    }
}
