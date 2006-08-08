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
package org.osaf.cosmo.rpc.model.user;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 */
public class User implements Serializable {

    /**
     */
    public static final String USERNAME_OVERLORD = "root";

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean admin;
    private Date dateCreated;
    private Date dateModified;

    /**
     */
    public User() {
        admin = Boolean.FALSE;
    }

    /**
     */
    public String getUsername() {
        return username;
    }

    /**
     */
    public void setUsername(String username) {
        this.username = username;
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
    public Boolean isAdmin() {
        return admin;
    }

    /**
     */
    public void setAdmin(Boolean admin) {
        this.admin = admin;
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
    public boolean isOverlord() {
        return username != null && username.equals(USERNAME_OVERLORD);
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
            append(admin, it.admin).
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
            append(admin).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("username", username).
            append("password", "xxxxxx").
            append("firstName", firstName).
            append("lastName", lastName).
            append("email", email).
            append("admin", admin).
            append("dateCreated", dateCreated).
            append("dateModified", dateModified).
            toString();
    }
 
}
