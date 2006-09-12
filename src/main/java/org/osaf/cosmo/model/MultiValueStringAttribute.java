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
