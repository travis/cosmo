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
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 */
public class User extends BaseModelObject {

    private Long id;
    private String username;
    private String oldUsername;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Date dateCreated;
    private Date dateModified;
    private Set roles;

    /**
     */
    public User() {
        roles = new HashSet();
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
    public String getUsername() {
        return username;
    }

    /**
     */
    public void setUsername(String username) {
        oldUsername = this.username;
        this.username = username;
    }

    /**
     */
    public String getOldUsername() {
        return oldUsername;
    }

    /**
     */
    public boolean isUsernameChanged() {
        return oldUsername != null && ! oldUsername.equals(username);
    }

    /**
     */
    public String getPassword() {
        return password;
    }

    /**
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     */
    public String getLastName() {
        return lastName;
    }

    /**
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     */
    public String getEmail() {
        return email;
    }

    /**
     */
    public void setEmail(String email) {
        this.email = email;
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
    public boolean isInRole(String name) {
        for (Iterator i=roles.iterator(); i.hasNext();) {
            Role role = (Role) i.next();
            if (role.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    public Set getRoles() {
        return roles;
    }

    /**
     */
    public void addRole(Role role) {
        roles.add(role);
    }

    /**
     */
    public void removeRole(Role role) {
        roles.remove(role);
    }

    /**
     */
    public void setRoles(Set roles) {
        this.roles = roles;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof User)) {
            return false;
        }
        User it = (User) o;
        return new EqualsBuilder().
            append(username, it.username).
            append(password, it.password).
            append(firstName, it.firstName).
            append(lastName, it.lastName).
            append(email, it.email).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(username).
            append(password).
            append(firstName).
            append(lastName).
            append(email).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("username", username).
            append("password", "xxxxxx").
            append("firstName", firstName).
            append("lastName", lastName).
            append("email", email).
            append("dateCreated", dateCreated).
            append("dateModified", dateModified).
            toString();
    }
}
