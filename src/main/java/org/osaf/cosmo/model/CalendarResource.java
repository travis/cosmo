/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this calendar except in compliance with the License.
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
 * Abstract base class that extends {@link FileResource} to represent
 * a calendar resource.
 */
public abstract class CalendarResource extends FileResource {

    private String uid;

    /**
     */
    public CalendarResource() {
        super();
    }

    /**
     */
    public String getUid() {
        return uid;
    }

    /**
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof CalendarResource)) {
            return false;
        }
        CalendarResource it = (CalendarResource) o;
        return new EqualsBuilder().
            appendSuper(super.equals(o)).
            append(uid, it.uid).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(17, 19).
            appendSuper(super.hashCode()).
            append(uid).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            appendSuper(super.toString()).
            append("uid", uid).
            toString();
    }
}
