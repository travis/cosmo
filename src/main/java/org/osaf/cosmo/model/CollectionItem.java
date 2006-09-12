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
    
    private Set<Item> children = new HashSet<Item>(0);
    
    public CollectionItem() {
    };

    public Set<Item> getChildren() {
        return children;
    }

    private void setChildren(Set<Item> children) {
        this.children = children;
    }
}
