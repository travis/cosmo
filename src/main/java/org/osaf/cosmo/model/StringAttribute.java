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


/**
 * Represents an attribute with a string value.
 */
public class StringAttribute extends Attribute implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2417093506524504993L;
    private String value;

    // Constructors

    /** default constructor */
    public StringAttribute() {
    }

    public StringAttribute(String name, String value) {
        setName(name);
        this.value = value;
    }

    // Property accessors
    public String getValue() {
        return this.value;
    }

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
        StringAttribute attr = new StringAttribute();
        attr.setName(getName());
        attr.setValue(getValue());
        return attr;
    }
}
