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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Extends {@link Resource} to represent a collection of
 * resources.
 */
public class CollectionResource extends Resource {

    private Set resources;

    /**
     */
    public CollectionResource() {
        super();
        resources = new HashSet();
    }

    /**
     */
    public Set getResources() {
        return resources;
    }

    /**
     */
    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof CollectionResource)) {
            return false;
        }
        CollectionResource it = (CollectionResource) o;
        return new EqualsBuilder().
            appendSuper(super.equals(o)).
            append(resources, it.resources).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(13, 15).
            appendSuper(super.hashCode()).
            append(resources).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            appendSuper(super.toString()).
            append("resources", resources).
            toString();
    }
}
