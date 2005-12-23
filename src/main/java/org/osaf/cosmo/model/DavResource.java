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
 * A bean encapsulating the information about a dav resource.
 */
public class DavResource extends BaseModelObject {

    private String displayName;
    private String path;

    /**
     */
    public DavResource() {
    }

    /**
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     */
    public String getPath() {
        return path;
    }

    /**
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof DavResource)) {
            return false;
        }
        DavResource it = (DavResource) o;
        return new EqualsBuilder().
            append(displayName, it.displayName).
            append(path, it.path).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(7, 11).
            append(displayName).
            append(path).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("displayName", displayName).
            append("path", path).
            toString();
    }
}
