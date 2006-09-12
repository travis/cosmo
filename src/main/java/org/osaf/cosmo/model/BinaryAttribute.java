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

/**
 * Attribute with a binary value.
 */
public class BinaryAttribute extends Attribute implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6296196539997344427L;

    private byte[] value;

    /** default constructor */
    public BinaryAttribute() {
    }

    public BinaryAttribute(String name, byte[] value) {
        setName(name);
        this.value = value;
    }

    // Property accessors
    public byte[] getValue() {
        return this.value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public void setValue(Object value) {
        if (value != null && !(value instanceof byte[]))
            throw new ModelValidationException(
                    "attempted to set non binary value on attribute");
        setValue((byte[]) value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.model.Attribute#copy()
     */
    public Attribute copy() {
        BinaryAttribute attr = new BinaryAttribute();
        attr.setName(getName());
        if (value != null)
            attr.setValue(value.clone());
        return attr;
    }

}
