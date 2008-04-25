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

import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;

/**
 * Represents an attribute associated with an Item.
 * An attribute consists of a QName (qualified name)
 * and a value.  The QName is composed from a namespace
 * and a localname.  The QName and Item determine
 * attribute uniqueness.  This means for a given Item
 * and QName, there can be only one attribute.
 * 
 * There are many different types of attributes 
 * (String, Integer, Binary, Boolean, etc.)
 * 
 */
public abstract class MockAttribute extends MockAuditableObject implements java.io.Serializable, Attribute {

    // Fields
    private QName qname;
    
    private Item item;

    // Constructors
    /** default constructor */
    public MockAttribute() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#getQName()
     */
    public QName getQName() {
        return qname;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#setQName(org.osaf.cosmo.model.copy.QName)
     */
    public void setQName(QName qname) {
        this.qname = qname;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#getName()
     */
    public String getName() {
        if(qname==null)
            return null;
        
        return qname.getLocalName();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#getItem()
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#setItem(org.osaf.cosmo.model.copy.Item)
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#getValue()
     */
    public abstract Object getValue();

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#setValue(java.lang.Object)
     */
    public abstract void setValue(Object value);

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAttribute#copy()
     */
    public abstract Attribute copy();
    
    /**
     * Return string representation
     */
    public String toString() {
        Object value = getValue();
        if(value==null)
            return "null";
        return value.toString();
    }
    
    public void validate() {
        
    }

}
