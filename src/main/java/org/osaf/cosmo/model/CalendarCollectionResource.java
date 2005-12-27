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
 * Extends {@link CollectionResource} to represent a collection of
 * calendar resources.
 */
public class CalendarCollectionResource extends CollectionResource {

    private String description;
    private String language;

    /**
     */
    public CalendarCollectionResource() {
        super();
    }

    /**
     */
    public String getDescription() {
        return description;
    }

    /**
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     */
    public String getLanguage() {
        return language;
    }

    /**
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof CalendarCollectionResource)) {
            return false;
        }
        CalendarCollectionResource it = (CalendarCollectionResource) o;
        return new EqualsBuilder().
            appendSuper(super.equals(o)).
            append(description, it.description).
            append(language, it.language).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(13, 15).
            appendSuper(super.hashCode()).
            append(description).
            append(language).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            appendSuper(super.toString()).
            append("description", description).
            append("language", language).
            toString();
    }
}
