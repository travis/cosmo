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

import java.util.HashSet;
import java.util.Set;


/**
 * Represents attribute with a List<String> as its value
 */
public class MultiValueStringAttribute extends Attribute
        implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8518583717902318228L;
    private Set<String> value = new HashSet<String>(0);

    /** default constructor */
    public MultiValueStringAttribute() {
    }

    public MultiValueStringAttribute(String name, Set<String> value)
    {
        setName(name);
        this.value = value;
    }

    // Property accessors
    public Set<String> getValue() {
        return this.value;
    }

    public void setValue(Set<String> value) {
        this.value = value;
    }
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof Set))
            throw new ModelValidationException(
                    "attempted to set non Set value on attribute");
        setValue((Set<String>) value);
    }
    
    public Attribute copy() {
        MultiValueStringAttribute attr = new MultiValueStringAttribute();
        attr.setName(getName());
        Set<String> newValue = new HashSet<String>(value);
        attr.setValue(newValue);
        return attr;
    }

}
