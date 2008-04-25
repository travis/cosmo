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
import org.osaf.cosmo.model.DataSizeException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.StringAttribute;


/**
 * Represents an attribute with a string value.
 */
public class MockStringAttribute extends MockAttribute implements
        java.io.Serializable, StringAttribute {

    public static final int VALUE_LEN_MAX = 2048;
    
    /**
     * 
     */
    private static final long serialVersionUID = 2417093506524504993L;
    
    private String value;

    // Constructors

    /** default constructor */
    public MockStringAttribute() {
    }

    public MockStringAttribute(QName qname, String value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStringAttribute#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceStringAttribute#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof String))
            throw new ModelValidationException(
                    "attempted to set non String value on attribute");
        setValue((String) value);
    }
    
    public Attribute copy() {
        StringAttribute attr = new MockStringAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(getValue());
        return attr;
    }
    
    /**
     * Convienence method for returning a String value on a StringAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch StringAttribute from
     * @param qname QName of attribute
     * @return String value of StringAttribute
     */
    public static String getValue(Item item, QName qname) {
        StringAttribute ta = (StringAttribute) item.getAttribute(qname);
        if(ta==null)
            return null;
        else
            return ta.getValue();
    }
    
    /**
     * Convienence method for setting a String value on a StringAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch StringAttribute from
     * @param qname QName of attribute
     * @param value value to set on StringAttribute
     */
    public static void setValue(Item item, QName qname, String value) {
        StringAttribute attr = (StringAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new MockStringAttribute(qname,value);
            item.addAttribute(attr);
            return;
        }
        if(value==null)
            item.removeAttribute(qname);
        else
            attr.setValue(value);
    }
    
    @Override
    public void validate() {
        if (value!= null && value.length() > VALUE_LEN_MAX)
            throw new DataSizeException("String attribute " + getQName() + " too large");
    }
}
