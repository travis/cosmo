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

import java.util.Set;


/**
 * Represents attribute with an integer value.
 */
public class IntegerAttribute extends Attribute
		implements java.io.Serializable {
	  
	/**
     * 
     */
    private static final long serialVersionUID = -7110319771835652090L;
    
    private Long value;

	/** default constructor */
	public IntegerAttribute() {
	}
	
	public IntegerAttribute(String name, Long value)
	{
		setName(name);
		this.value = value;
	}

	// Property accessors
	public Long getValue() {
		return this.value;
	}
    
    public Attribute copy() {
        IntegerAttribute attr = new IntegerAttribute();
        attr.setName(getName());
        attr.setValue(getValue());
        return attr;
    }

	public void setValue(Long value) {
		this.value = value;
	}
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof Long))
            throw new ModelValidationException(
                    "attempted to set non Long value on attribute");
        setValue((Long) value);
    }
    
    

}
