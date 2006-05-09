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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class User extends BaseModelObject {

    /**
     */
    public static final String USERNAME_OVERLORD = "root";
    /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 32;
    /**
     */
    public static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[^\\t\\n\\r\\f\\a\\e\\p{Cntrl}/]+$");
    /**
     */
    public static final int PASSWORD_LEN_MIN = 5;
    /**
     */
    public static final int PASSWORD_LEN_MAX = 16;
    /**
     */
    public static final int FIRSTNAME_LEN_MIN = 1;
    /**
     */
    public static final int FIRSTNAME_LEN_MAX = 128;
    /**
     */
    public static final int LASTNAME_LEN_MIN = 1;
    /**
     */
    public static final int LASTNAME_LEN_MAX = 128;
    /**
     */
    public static final int EMAIL_LEN_MIN = 1;
    /**
     */
    public static final int EMAIL_LEN_MAX = 128;

    private String username;
    private String oldUsername;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String oldEmail;
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
        oldUsername = this.username;
        this.username = username;
    }

    /**
     */
    public String getOldUsername() {
        return oldUsername != null ? oldUsername : username;
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
        oldEmail = this.email;
        this.email = email;
    }

    /**
     */
    public String getOldEmail() {
        return oldEmail;
    }

    /**
     */
    public boolean isEmailChanged() {
        return oldEmail != null && ! oldEmail.equals(email);
    }

    /**
     */
    public Boolean getAdmin() {
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

    /**
     */
    public void validate() {
        validateUsername();
        validateFirstName();
        validateLastName();
        validateEmail();
    }

    /**
     */
    public void validateUsername() {
        if (username == null) {
            throw new ModelValidationException("Username not specified");
        }
        if (username.length() < USERNAME_LEN_MIN ||
            username.length() > USERNAME_LEN_MAX) {
            throw new ModelValidationException("Username must be " +
                                               USERNAME_LEN_MIN + " to " +
                                               USERNAME_LEN_MAX +
                                               " characters in length");
        }
        Matcher m = USERNAME_PATTERN.matcher(username);
        if (! m.matches()) {
            throw new ModelValidationException("Username contains illegal " +
                                               "characters");
        }
    }

    /**
     */
    public void validateRawPassword() {
        if (password == null) {
            throw new ModelValidationException("Password not specified");
        }
        if (password.length() < PASSWORD_LEN_MIN ||
            password.length() > PASSWORD_LEN_MAX) {
            throw new ModelValidationException("Password must be " +
                                               PASSWORD_LEN_MIN + " to " +
                                               PASSWORD_LEN_MAX +
                                               " characters in length");
        }
    }

    /**
     */
    public void validateFirstName() {
        if (firstName == null) {
            throw new ModelValidationException("First name is null");
        }
        if (firstName.length() < FIRSTNAME_LEN_MIN ||
            firstName.length() > FIRSTNAME_LEN_MAX) {
            throw new ModelValidationException("First name must be " +
                                               FIRSTNAME_LEN_MIN + " to " + 
                                               FIRSTNAME_LEN_MAX +
                                               " characters in length");
        }
    }

    /**
     */
    public void validateLastName() {
        if (lastName == null) {
            throw new ModelValidationException("Last name is null");
        }
        if (lastName.length() < LASTNAME_LEN_MIN ||
            lastName.length() > LASTNAME_LEN_MAX) {
            throw new ModelValidationException("Last name must be " +
                                               LASTNAME_LEN_MIN + " to " +
                                               LASTNAME_LEN_MAX +
                                               " characters in length");
        }
    }

    /**
     */
    public void validateEmail() {
        if (email == null) {
            throw new ModelValidationException("Email is null");
        }
        if (email.length() < EMAIL_LEN_MIN ||
            email.length() > EMAIL_LEN_MAX) {
            throw new ModelValidationException("Email must be " +
                                               EMAIL_LEN_MIN + " to " +
                                               EMAIL_LEN_MAX +
                                               " characters in length");
        }
    }
}
