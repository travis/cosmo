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
package org.osaf.cosmo.model.mock;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;

/**
 */
public class MockUser extends MockAuditableObject implements User {

    /**
     */
    private static final long serialVersionUID = -5401963358519490736L;
   
    /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 32;
    /**
     */
    public static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[\\u0020-\\ud7ff\\ue000-\\ufffd&&[^\\u007f\\u003a;/\\\\]]+$");
    
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

    
    private String uid;
    
    private String username;
    
    private transient String oldUsername;
    
    private String password;
    
    private String firstName;
    
    private String lastName;
    
    private String email;
    
    private transient String oldEmail;
   
    private String activationId;
  
    private Boolean admin;
    
    private transient Boolean oldAdmin;
    
    private Boolean locked;
    
    private Set<Preference> preferences = new HashSet<Preference>(0);
    
    private Set<CollectionSubscription> subscriptions = 
        new HashSet<CollectionSubscription>(0);

    /**
     */
    public MockUser() {
        admin = Boolean.FALSE;
        locked = Boolean.FALSE;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getUid()
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setUid(java.lang.String)
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getUsername()
     */
    public String getUsername() {
        return username;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setUsername(java.lang.String)
     */
    public void setUsername(String username) {
        oldUsername = this.username;
        this.username = username;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getOldUsername()
     */
    public String getOldUsername() {
        return oldUsername != null ? oldUsername : username;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#isUsernameChanged()
     */
    public boolean isUsernameChanged() {
        return oldUsername != null && ! oldUsername.equals(username);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getFirstName()
     */
    public String getFirstName() {
        return firstName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setFirstName(java.lang.String)
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getLastName()
     */
    public String getLastName() {
        return lastName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setLastName(java.lang.String)
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getEmail()
     */
    public String getEmail() {
        return email;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setEmail(java.lang.String)
     */
    public void setEmail(String email) {
        oldEmail = this.email;
        this.email = email;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getOldEmail()
     */
    public String getOldEmail() {
        return oldEmail;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#isEmailChanged()
     */
    public boolean isEmailChanged() {
        return oldEmail != null && ! oldEmail.equals(email);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getAdmin()
     */
    public Boolean getAdmin() {
        return admin;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getOldAdmin()
     */
    public Boolean getOldAdmin() {
        return oldAdmin;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#isAdminChanged()
     */
    public boolean isAdminChanged() {
        return oldAdmin != null && ! oldAdmin.equals(admin);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setAdmin(java.lang.Boolean)
     */
    public void setAdmin(Boolean admin) {
        oldAdmin = this.admin;
        this.admin = admin;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getActivationId()
     */
    public String getActivationId() {
        return activationId;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setActivationId(java.lang.String)
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#isOverlord()
     */
    public boolean isOverlord() {
        return username != null && username.equals(USERNAME_OVERLORD);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#isActivated()
     */
    public boolean isActivated() {
        return this.activationId == null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#activate()
     */
    public void activate(){
       this.activationId = null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#isLocked()
     */
    public Boolean isLocked() {
        return locked;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#setLocked(java.lang.Boolean)
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    /**
     * Username determines equality 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || username == null)
            return false;
        if (! (obj instanceof User))
            return false;
        
        return username.equals(((User) obj).getUsername());
    }

    @Override
        public int hashCode() {
        if (username == null)
            return super.hashCode();
        else
            return username.hashCode();
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
            append("locked", locked).
            toString();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#validate()
     */
    public void validate() {
        validateUsername();
        validateFirstName();
        validateLastName();
        validateEmail();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#validateUsername()
     */
    public void validateUsername() {
        if (username == null) {
            throw new ModelValidationException(this,"Username not specified");
        }
        if (username.length() < USERNAME_LEN_MIN ||
            username.length() > USERNAME_LEN_MAX) {
            throw new ModelValidationException(this,"Username must be " +
                                               USERNAME_LEN_MIN + " to " +
                                               USERNAME_LEN_MAX +
                                               " characters in length");
        }
        Matcher m = USERNAME_PATTERN.matcher(username);
        if (! m.matches()) {
            throw new ModelValidationException(this,"Username contains illegal " +
                                               "characters");
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#validateRawPassword()
     */
    public void validateRawPassword() {
        if (password == null) {
            throw new ModelValidationException(this,"Password not specified");
        }
        if (password.length() < PASSWORD_LEN_MIN ||
            password.length() > PASSWORD_LEN_MAX) {
            throw new ModelValidationException(this,"Password must be " +
                                               PASSWORD_LEN_MIN + " to " +
                                               PASSWORD_LEN_MAX +
                                               " characters in length");
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#validateFirstName()
     */
    public void validateFirstName() {
        if (firstName == null) {
            throw new ModelValidationException(this,"First name is null");
        }
        if (firstName.length() < FIRSTNAME_LEN_MIN ||
            firstName.length() > FIRSTNAME_LEN_MAX) {
            throw new ModelValidationException(this,"First name must be " +
                                               FIRSTNAME_LEN_MIN + " to " +
                                               FIRSTNAME_LEN_MAX +
                                               " characters in length");
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#validateLastName()
     */
    public void validateLastName() {
        if (lastName == null) {
            throw new ModelValidationException(this,"Last name is null");
        }
        if (lastName.length() < LASTNAME_LEN_MIN ||
            lastName.length() > LASTNAME_LEN_MAX) {
            throw new ModelValidationException(this,"Last name must be " +
                                               LASTNAME_LEN_MIN + " to " +
                                               LASTNAME_LEN_MAX +
                                               " characters in length");
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#validateEmail()
     */
    public void validateEmail() {
        if (email == null) {
            throw new ModelValidationException(this,"Email is null");
        }
        if (email.length() < EMAIL_LEN_MIN ||
            email.length() > EMAIL_LEN_MAX) {
            throw new ModelValidationException(this,"Email must be " +
                                               EMAIL_LEN_MIN + " to " +
                                               EMAIL_LEN_MAX +
                                               " characters in length");
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getPreferences()
     */
    public Set<Preference> getPreferences() {
        return preferences;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#addPreference(org.osaf.cosmo.model.copy.Preference)
     */
    public void addPreference(Preference preference) {
        preference.setUser(this);
        preferences.add(preference);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getPreference(java.lang.String)
     */
    public Preference getPreference(String key) {
        for (Preference pref : preferences) {
            if (pref.getKey().equals(key))
                return pref;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#removePreference(java.lang.String)
     */
    public void removePreference(String key) {
        removePreference(getPreference(key));
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#removePreference(org.osaf.cosmo.model.copy.Preference)
     */
    public void removePreference(Preference preference) {
        if (preference != null)
            preferences.remove(preference);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getCollectionSubscriptions()
     */
    public Set<CollectionSubscription> getCollectionSubscriptions() {
        return subscriptions;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#addSubscription(org.osaf.cosmo.model.copy.CollectionSubscription)
     */
    public void addSubscription(CollectionSubscription subscription) {
        subscription.setOwner(this);
        subscriptions.add(subscription);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getSubscription(java.lang.String)
     */
    public CollectionSubscription getSubscription(String displayname) {

        for (CollectionSubscription sub : subscriptions) {
            if (sub.getDisplayName().equals(displayname))
                return sub;
        }

        return null;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#getSubscription(java.lang.String, java.lang.String)
     */
    public CollectionSubscription getSubscription(String collectionUid, String ticketKey){
        for (CollectionSubscription sub : subscriptions) {
            if (sub.getCollectionUid().equals(collectionUid)
                    && sub.getTicketKey().equals(ticketKey)) {
                return sub;
            }
        }

        return null;
    }

    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#removeSubscription(java.lang.String, java.lang.String)
     */
    public void removeSubscription(String collectionUid, String ticketKey){
        removeSubscription(getSubscription(collectionUid, ticketKey));
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#removeSubscription(java.lang.String)
     */
    public void removeSubscription(String displayName) {
        removeSubscription(getSubscription(displayName));
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#removeSubscription(org.osaf.cosmo.model.copy.CollectionSubscription)
     */
    public void removeSubscription(CollectionSubscription sub) {
        if (sub != null)
            subscriptions.remove(sub);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#isSubscribedTo(org.osaf.cosmo.model.copy.CollectionItem)
     */
    public boolean isSubscribedTo(CollectionItem collection){
        for (CollectionSubscription sub : subscriptions){
            if (collection.getUid().equals(sub.getCollectionUid())) return true;
        }
        return false;
    }

    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceUser#calculateEntityTag()
     */
    public String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ?
            new Long(getModifiedDate().getTime()).toString() : "-";
        String etag = username + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }
}
