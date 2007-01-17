/*
 * Copyright 2006 Open Source Applications Foundation
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKey;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Size;

/**
 */
@Entity
@Table(name="users")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends BaseModelObject {

    /**
     */
    private static final long serialVersionUID = -5401963358519490736L;
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

    // Sort Strings

    /**
     * A String indicating the results should be sorted by Last Name then First Name
     */
    public static final String NAME_SORT_STRING = "Name";
    /**
     * A String indicating the results should be sorted by Username
     */
    public static final String USERNAME_SORT_STRING = "Username";
    /**
     * A String indicating the results should be sorted by Administrator
     */
    public static final String ADMIN_SORT_STRING = "Administrator";
    /**
     * A String indicating the results should be sorted by Email
     */
    public static final String EMAIL_SORT_STRING = "Email";
    /**
     * A String indicating the results should be sorted by Date Created
     */
    public static final String CREATED_SORT_STRING = "Created";
    /**
     * A String indicating the results should be sorted by Date last Modified
     */
    public static final String LAST_MODIFIED_SORT_STRING = "Last Modified";

    /**
     * The Default Sort Type
     */
    public static final String DEFAULT_SORT_STRING = NAME_SORT_STRING;

    public static final String NAME_URL_STRING = "name";
    public static final String USERNAME_URL_STRING = "username";
    public static final String ADMIN_URL_STRING = "admin";
    public static final String EMAIL_URL_STRING = "email";
    public static final String CREATED_URL_STRING = "created";
    public static final String LAST_MODIFIED_URL_STRING = "modified";

    private String uid;
    private String username;
    private String oldUsername;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String oldEmail;
    private String activationId;
    private Boolean admin;
    private Boolean oldAdmin;
    private Date dateCreated;
    private Date dateModified;
    private Set<Item> items = new HashSet<Item>(0);
    private Map<String, String> preferences = new HashMap<String, String>(0);
    private Set<CollectionSubscription> subscriptions = 
        new HashSet<CollectionSubscription>(0);

    /**
     */
    public User() {
        admin = Boolean.FALSE;
    }

    /**
     */
    @Column(name = "uid", nullable=false, unique=true, length=255)
    @NotNull
    @Length(min=1, max=255)
    @Index(name="idx_useruid")
    public String getUid() {
        return uid;
    }

    /**
     * @param uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     */
    @Column(name = "username", nullable=false, unique=true)
    @Index(name="idx_username")
    @NotNull
    @Length(min=USERNAME_LEN_MIN, max=USERNAME_LEN_MAX)
    @org.hibernate.validator.Pattern(regex="^[^\\t\\n\\r\\f\\a\\e\\p{Cntrl}/]+$")
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
    @Transient
    public String getOldUsername() {
        return oldUsername != null ? oldUsername : username;
    }

    /**
     */
    @Transient
    public boolean isUsernameChanged() {
        return oldUsername != null && ! oldUsername.equals(username);
    }

    /**
     */
    @Column(name = "password")
    @NotNull
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
    @Column(name = "firstname")
    @Length(min=FIRSTNAME_LEN_MIN, max=FIRSTNAME_LEN_MAX)
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
    @Column(name = "lastname")
    @Length(min=LASTNAME_LEN_MIN, max=LASTNAME_LEN_MAX)
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
    @Column(name = "email", nullable=false, unique=true)
    @Index(name="idx_useremail")
    @NotNull
    @Length(min=EMAIL_LEN_MIN, max=EMAIL_LEN_MAX)
    @Email
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
    @Transient
    public String getOldEmail() {
        return oldEmail;
    }

    /**
     */
    @Transient
    public boolean isEmailChanged() {
        return oldEmail != null && ! oldEmail.equals(email);
    }

    /**
     */
    @Column(name = "admin")
    public Boolean getAdmin() {
        return admin;
    }
    
    @Transient
    public Boolean getOldAdmin() {
        return oldAdmin;
    }

    /**
     */
    @Transient
    public boolean isAdminChanged() {
        return oldAdmin != null && ! oldAdmin.equals(admin);
    }

    /**
     */
    public void setAdmin(Boolean admin) {
        oldAdmin = this.admin;
        this.admin = admin;
    }

    /**
     */
    @Column(name = "activationid", nullable=true, length=255)
    @Length(min=1, max=255)
    @Index(name="idx_activationid")
    public String getActivationId() {
        return activationId;
    }

    /**
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /**
     */
    @Column(name = "createdate")
    @Type(type="long_timestamp")
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
    @Column(name = "modifydate")
    @Type(type="long_timestamp")
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
    @Transient
    public boolean isOverlord() {
        return username != null && username.equals(USERNAME_OVERLORD);
    }

    /**
     */
    @Transient
    public boolean isActivated() {
        return this.activationId == null;
    }

    /**
     *
     *
     */
    @Transient
    public void activate(){
       this.activationId = null;
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
            append(activationId, it.activationId).
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
            append(activationId).
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
            append("activationId", activationId).
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

    @OneToMany(mappedBy="owner", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE })
    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }
    
    @CollectionOfElements
    @JoinTable(
            name="user_preferences",
            joinColumns = @JoinColumn(name="userid")
    )
    @MapKey(columns=@Column(name="preferencename", length=255))
    @Column(name="preferencevalue", length=255)
    //@Size(min=0, max=255)
    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }
    
    @Transient
    public void setMultiplePreferences(Map<String, String> preferences){
        this.preferences.putAll(preferences);
    }
    
    @Transient
    public String getPreference(String key){
        return preferences.get(key);
    }
    
    @Transient
    public void setPreference(String key, String value){
        preferences.put(key, value);
    }
    
    @Transient
    public void removePreference(String key){
        preferences.remove(key);
    }
    
    @OneToMany(mappedBy = "owner", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN }) 
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public Set<CollectionSubscription> getCollectionSubscriptions() {
        return subscriptions;
    }

    // Used by hibernate
    private void setCollectionSubscriptions(
            Set<CollectionSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void addSubscription(CollectionSubscription subscription) {
        subscription.setOwner(this);
        subscriptions.add(subscription);
    }

    /**
     * Get the CollectionSubscription with the specified displayName
     * @param displayname display name of subscription to return
     * @return subscription with specified display name
     */
    @Transient
    public CollectionSubscription getSubscription(String displayname) {

        for (CollectionSubscription sub : subscriptions) {
            if (sub.getDisplayName().equals(displayname))
                return sub;
        }

        return null;
    }
    
    /**
     * Get the CollectionSubscription with the specified collectionUid 
     * and ticketKey
     * @param collectionUid collection uid of subscription to return
     * @param ticketKey ticketKey of subscription to return
     * @return subscription with specified collectionUid and ticketKey
     */
    @Transient
    public CollectionSubscription getSubscription(String collectionUid, String ticketKey){
        for (CollectionSubscription sub : subscriptions) {
            if (sub.getCollectionUid().equals(collectionUid)
                    && sub.getTicketKey().equals(ticketKey)) {
                return sub;
            }
        }

        return null;
    }

    
    /**
     * Remove the CollectionSubscription with the specifed collectionUid and ticketKey
     * @param collectionUid collection uid of subscription to remove
     * @param ticketKey ticketKey of subscription to remove
     */
    public void removeSubscription(String collectionUid, String ticketKey){
        removeSubscription(getSubscription(collectionUid, ticketKey));
    }
    
    /**
     * Remove the CollectionSubscription with the specifed displayName
     * @param name display name of the subscription to remove
     */
    public void removeSubscription(String displayName) {
        removeSubscription(getSubscription(displayName));
    }

    /** */
    public void removeSubscription(CollectionSubscription sub) {
        if (sub != null)
            subscriptions.remove(sub);
    }


    /*
     * I'm not sure about putting this enum here, but it seems weird in other
     * places too. Since sort information is already here, in the *_SORT_STRING
     * constants, I think this is appropriate.
     */
    public enum SortType {
        NAME (NAME_URL_STRING, NAME_SORT_STRING),
        USERNAME (USERNAME_URL_STRING, USERNAME_SORT_STRING),
        ADMIN (ADMIN_URL_STRING, ADMIN_SORT_STRING),
        EMAIL (EMAIL_URL_STRING, EMAIL_SORT_STRING),
        CREATED (CREATED_URL_STRING, CREATED_SORT_STRING),
        LAST_MODIFIED (LAST_MODIFIED_URL_STRING, LAST_MODIFIED_SORT_STRING);

        private final String urlString;
        private final String titleString;

        SortType(String urlString, String titleString){
            this.urlString = urlString;
            this.titleString = titleString;
        }

        public String getTitleString() {
            return titleString;
        }

        public String getUrlString() {
            return urlString;
        }

        public static SortType getByUrlString(String string) {
            if (string.equals(NAME_URL_STRING)){
                return NAME;
            } else if (string.equals(USERNAME_URL_STRING)){
                return USERNAME;
            } else if (string.equals(ADMIN_URL_STRING)){
                return ADMIN;
            } else if (string.equals(EMAIL_URL_STRING)){
                return EMAIL;
            } else if (string.equals(CREATED_URL_STRING)){
                return CREATED;
            } else if (string.equals(LAST_MODIFIED_URL_STRING)){
                return LAST_MODIFIED;
            } else {
                return null;
            }
        }
    }

}
