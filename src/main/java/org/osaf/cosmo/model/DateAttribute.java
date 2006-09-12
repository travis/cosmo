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

import java.util.Date;
import java.util.Set;

/**
 * Represents an attribute with a date value
 */
public class DateAttribute extends Attribute implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5263977785074085449L;
    
    private Date value;

    /** default constructor */
    public DateAttribute() {
    }

    public DateAttribute(String name, Date value) {
        setName(name);
        this.value = value;
    }

    // Property accessors
    public Date getValue() {
        return this.value;
    }

    public void setValue(Date value) {
        this.value = value;
    }
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof Date))
            throw new ModelValidationException(
                    "attempted to set non Date value on attribute");
        setValue((Date) value);
    }
    
    public Attribute copy() {
        DateAttribute attr = new DateAttribute();
        attr.setName(getName());
        attr.setValue(getValue());
        return attr;
    }

}
