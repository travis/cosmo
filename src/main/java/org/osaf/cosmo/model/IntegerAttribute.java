/*
 * Copyright (c) 2006 SimDesk Technologies, Inc.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * SimDesk Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with SimDesk Technologies.
 *
 * SIMDESK TECHNOLOGIES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
 * THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  SIMDESK TECHNOLOGIES
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
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
