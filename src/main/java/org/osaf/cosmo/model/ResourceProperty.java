/*
 * Copyright 2005 Open Source Applications Foundation
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * An arbitrary piece of data associated with a repository resource.
 */
public class ResourceProperty extends BaseModelObject {

    private String name;
    private String value;

    /**
     */
    public ResourceProperty() {
    }

    /**
     */
    public String getName() {
        return name;
    }

    /**
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     */
    public String getValue() {
        return value;
    }

    /**
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof ResourceProperty)) {
            return false;
        }
        ResourceProperty it = (ResourceProperty) o;
        return new EqualsBuilder().
            append(name, it.name).
            append(value, it.value).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(7, 11).
            append(name).
            append(value).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("name", name).
            append("value", value).
            toString();
    }
}
