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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


/**
 * Attribute that contains a Map<String,String> as its
 * value.
 */
public class DictionaryAttribute extends Attribute
        implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3713980765847199175L;
    
    private Map<String, String> value = new HashMap<String,String>(0);

    /** default constructor */
    public DictionaryAttribute() {
    }

    public DictionaryAttribute(String name, Map<String, String> value)
    {
        setName(name);
        this.value = value;
    }

    // Property accessors
    public Map<String, String> getValue() {
        return this.value;
    }

    public void setValue(Map<String, String> value) {
        this.value = value;
    }
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof Map))
            throw new ModelValidationException(
                    "attempted to set non Map value on attribute");
        setValue((Map<String, String>) value);
    }
    
    public Attribute copy() {
        DictionaryAttribute attr = new DictionaryAttribute();
        attr.setName(getName());
        Map<String, String> newVal = new HashMap<String, String>();
        for(Entry<String, String> entry: value.entrySet())
            newVal.put(entry.getKey(), entry.getValue());
        attr.setValue(newVal);
        return attr;
    }

}
