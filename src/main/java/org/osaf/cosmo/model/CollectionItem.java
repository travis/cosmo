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
    public static final String ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
        "cosmo:excludeFreeBusyRollup";

    private Set<Item> children = new HashSet<Item>(0);
    private Set<Item> allChildren = null;
    
    public CollectionItem() {
    };

    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
    @Where(clause = "isactive=1")
    public Set<Item> getChildren() {
        return children;
    }

    private void setChildren(Set<Item> children) {
        this.children = children;
    }
    
    // Only used for cascade delete by Hibernate
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE }) 
    private Set<Item> getAllChildren() {
        return allChildren;
    }

    // Only used for cascade delete by Hibernate
    private void setAllChildren(Set<Item> allChildren) {
        this.allChildren = allChildren;
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
}
