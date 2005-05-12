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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Base class for model objects.
 *
 * @author Brian Moseley
 */
public abstract class BaseModelObject implements Comparable, Serializable {

    /**
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     */
    public int compareTo(Object o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }

    /**
     */
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     */
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
