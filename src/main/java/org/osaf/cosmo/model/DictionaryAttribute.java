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
