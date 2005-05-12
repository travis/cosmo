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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 */
public class Role extends BaseModelObject {

    private Long id;
    private String name;
    private Date dateCreated;
    private Date dateModified;
    private Set users;

    /**
     */
    public Role() {
        users = new HashSet();
    }

    /**
     */
    public Long getId() {
        return id;
    }

    /**
     */
    public void setId(Long id) {
        this.id = id;
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
    public Date getDateModified() {
        return dateModified;
    }

    /**
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /**
     */
    public Set getUsers() {
        return users;
    }

    /**
     */
    public void setUsers(Set users) {
        this.users = users;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof Role)) {
            return false;
        }
        Role it = (Role) o;
        return new EqualsBuilder().
            append(name, it.name).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(name).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("name", name).
            append("dateCreated", dateCreated).
            append("dateModified", dateModified).
            toString();
    }
}
