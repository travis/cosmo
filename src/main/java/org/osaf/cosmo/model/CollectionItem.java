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

/**
 * Extends {@link Item} to represent a collection of items
 */
public class CollectionItem extends Item {

    /**
     * 
     */
    private static final long serialVersionUID = 2873258323314048223L;
    
    // CollectionItem specific attributes
    public static final String ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
        "cosmo:excludeFreeBusyRollup";

    private Set<Item> children = new HashSet<Item>(0);
    
    public CollectionItem() {
    };

    public Set<Item> getChildren() {
        return children;
    }

    private void setChildren(Set<Item> children) {
        this.children = children;
    }

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
