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

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * An abstract base class for shared content resources that are stored
 * in the repository. 
 */
public abstract class Resource extends BaseModelObject {

    private String path;
    private String displayName;
    private HashMap properties;
    private Date dateCreated;

    /**
     */
    public Resource() {
        properties = new HashMap();
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
    public Set getProperties() {
        return properties.keySet();
    }

    /**
     */
    public String getProperty(String name) {
        return (String) properties.get(name);
    }

    /**
     */
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     */
    public void removeProperty(String name) {
        properties.remove(name);
    }

    /**
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof Resource)) {
            return false;
        }
        Resource it = (Resource) o;
        return new EqualsBuilder().
            append(path, it.path).
            append(displayName, it.displayName).
            append(properties, it.properties).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(7, 11).
            append(path).
            append(displayName).
            append(properties).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("path", path).
            append("displayName", displayName).
            append("properties", properties).
            toString();
    }
}
