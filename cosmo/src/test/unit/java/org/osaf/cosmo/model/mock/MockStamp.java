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

import java.util.Date;

import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;

/**
 * Represents an abstract Stamp on an Item. A Stamp is a set of related
 * properties and apis that is associated to an item.
 */
public abstract class MockStamp extends MockAuditableObject implements
        java.io.Serializable, Stamp {

    // Fields
    
    private Item item;
    
    // Constructors
    /** default constructor */
    public MockStamp() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStamp#getItem()
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStamp#setItem(org.osaf.cosmo.model.copy.Item)
     */
    public void setItem(Item item) {
        this.item = item;
    }

    
    /**
     * Convenience method for retrieving an attribute on the underlying
     * item.
     * @param qname QName of attribute
     * @return attribute value
     */
    protected Attribute getAttribute(QName qname) {
        return getItem().getAttribute(qname);
    }
    
    /**
     * Convenience method for adding an attribute to the underlying item
     * @param attribute attribute to add
     */
    protected void addAttribute(Attribute attribute) {
        getItem().addAttribute(attribute);
    }
    
    /**
     * Convenience method for removing an attribute to the underlying item
     * @param qname QName of attribute to remove
     */
    protected void removeAttribute(QName qname) {
        getItem().removeAttribute(qname);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStamp#getType()
     */
    public abstract String getType();

   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStamp#copy()
     */
    public abstract Stamp copy();
    
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStamp#updateTimestamp()
     */
    public void updateTimestamp() {
        setModifiedDate(new Date());
    }
}
