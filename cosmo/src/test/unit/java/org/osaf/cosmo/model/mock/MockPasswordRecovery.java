/*
 * Copyright 2007 Open Source Applications Foundation
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

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.User;

/**
 * An entity representing a password change request.
 * 
 * There should be a single password change request corresponding
 * to each password recovery request in the system. 
 */
public class MockPasswordRecovery implements PasswordRecovery {

    private static final long serialVersionUID = 854107654491442548L;

    private static final long DEFAULT_TIMEOUT = 1000*60*60*24*3; // 3 days
   
    private String key;
    
    
    private Date created;
    
    private long timeout;
    
    private User user;
    
    public MockPasswordRecovery(){
        this(null, null);
    }
    
    /**
     */
    public MockPasswordRecovery(User user, String key) {
        this(user, key, DEFAULT_TIMEOUT);
    }
    
    /**
     * 
     */
    public MockPasswordRecovery(User user, String key, long timeout) {
        this.user = user;
        this.key = key;
        this.timeout = timeout;
        this.created = new Date();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#getKey()
     */
    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#setKey(java.lang.String)
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#getTimeout()
     */
    public long getTimeout() {
        return timeout;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#setTimeout(long)
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#getCreated()
     */
    public Date getCreated() {
        return created;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#setCreated(java.util.Date)
     */
    public void setCreated(Date created) {
        this.created = created;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#getUser()
     */
    public User getUser() {
        return user;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#setUser(org.osaf.cosmo.model.copy.User)
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePasswordRecovery#hasExpired()
     */
    public boolean hasExpired() {
        Date now = new Date();
        return now.after(new Date(created.getTime() + timeout));
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof MockPasswordRecovery)) {
            return false;
        }
        MockPasswordRecovery it = (MockPasswordRecovery) o;
        return new EqualsBuilder().
            append(key, it.key).
            append(user, it.user).
            append(created, it.created).
            append(timeout, it.timeout).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(key).
            append(user).
            append(created).
            append(timeout).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("key", key).
            append("user", user).
            append("created", created).
            append("timeout", timeout).
            toString();
    }

}
