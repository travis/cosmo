package org.osaf.cosmo.model;


/**
 * Extends {@link CollectionItem} to represent a users home collection.
 */
public class HomeCollectionItem extends CollectionItem {

    /**
     * 
     */
    private static final long serialVersionUID = -4301319758735788800L;

    public void setName(String name) {
        // Prevent name changes to home collection
        if(getName()==null)
            super.setName(name);
    }
}
