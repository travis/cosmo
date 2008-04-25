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
import org.osaf.cosmo.model.BooleanAttribute;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.QName;

/**
 * Represents attribute with an Boolean value.
 */
public class MockBooleanAttribute extends MockAttribute implements java.io.Serializable, BooleanAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = -8393344132524216261L;
    
    private Boolean value;

    /** default constructor */
    public MockBooleanAttribute() {
    }

    public MockBooleanAttribute(QName qname, Boolean value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceBooleanAttribute#getValue()
     */
    public Boolean getValue() {
        return this.value;
    }

    public Attribute copy() {
        BooleanAttribute attr = new MockBooleanAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(new Boolean(value));
        return attr;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceBooleanAttribute#setValue(java.lang.Boolean)
     */
    public void setValue(Boolean value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceBooleanAttribute#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Boolean))
            throw new ModelValidationException(
                    "attempted to set non Boolean value on attribute");
        setValue((Boolean) value);
    }
    
    /**
     * Convienence method for returning a Boolean value on a BooleanAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch BooleanAttribute from
     * @param qname QName of attribute
     * @return Boolean value of IntegerAttribute
     */
    public static Boolean getValue(Item item, QName qname) {
        BooleanAttribute ba = (BooleanAttribute) item.getAttribute(qname);
        if(ba==null)
            return null;
        else
            return ba.getValue();
    }
    
    /**
     * Convienence method for setting a Boolean value on a BooleanAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch BooleanAttribute from
     * @param qname QName of attribute
     * @param value value to set on BooleanAttribute
     */
    public static void setValue(Item item, QName qname, Boolean value) {
        BooleanAttribute attr = (BooleanAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new MockBooleanAttribute(qname,value);
            item.addAttribute(attr);
            return;
        }
        if(value==null)
            item.removeAttribute(qname);
        else
            attr.setValue(value);
    }

}
