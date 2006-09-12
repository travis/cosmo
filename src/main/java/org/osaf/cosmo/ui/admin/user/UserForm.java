/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.ui.admin.user;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.struts.validator.ValidatorForm;
import org.apache.struts.action.ActionMapping;

import org.osaf.cosmo.util.PageCriteria;

/**
 * Action for managing users.
 */
public class UserForm extends ValidatorForm {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirm;
    private boolean admin;

    /**
     */
    public UserForm() {
        initialize();
    }

    /**
     * Used to uniquely identify an existing user. The id should
     * remain fixed even if the username is changed. Is irrelevant for
     * new users (those that have not yet been persistent).
     */
    public String getId() {
        return id;
    }

    /**
     */
    public void setId(String id) {
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
        this.username = username;
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
    public String getConfirm() {
        return confirm;
    }

    /**
     */
    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    /**
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     */
    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        super.reset(mapping, request);
        initialize();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("username", username).
            append("firstName", firstName).
            append("lastName", lastName).
            append("email", email).
            append("admin", admin).
            toString();
    }

    /**
     */
    private void initialize() {
        id = null;
        username = null;
        firstName = null;
        lastName = null;
        email = null;
        password = null;
        confirm = null;
        admin = false;
    }
}
