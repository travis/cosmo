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
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Where;

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
    private Set<Item> allChildren = new HashSet<Item>(0);
    
    public CollectionItem() {
    };

    /**
     * Return active children items (those with isActive=true).
     * @return active children items
     */
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
    @Where(clause = "isactive=1")
    public Set<Item> getChildren() {
        return children;
    }

    private void setChildren(Set<Item> children) {
        this.children = children;
    }
    
    /**
     * Return all children, including those with isActive=false.
     * @return all children items
     */
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE }) 
    public Set<Item> getAllChildren() {
        return allChildren;
    }

    // Only used for cascade delete by Hibernate
    private void setAllChildren(Set<Item> allChildren) {
        this.allChildren = allChildren;
    }

    /**
     * Return child item with matching uid
     * @return identified child item, or null if no child with that
     * uid exists
     */
    public Item getChild(String uid) {
        for (Item child : allChildren) {
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
     * Generate alternative hash code for collection.
     * This hash code will return a different value if
     * collection or any child items in the collection
     * has changed since the last hash code was generated.
     * @return
     */
    public int generateHash() {
        int hash = getVersion();
        for(Item item : getAllChildren()) {
            // account for version starting with 0
            hash += (item.getVersion() + 1);
        }
        return hash;
    }
}
