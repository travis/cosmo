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
package org.osaf.cosmo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

/**
 * An entity representing a password change request.
 * 
 * There should be a single password change request corresponding
 * to each password recovery request in the system. 
 */
@Entity
@Table(name="pwrecovery")
public class PasswordRecovery extends BaseModelObject {

    private static final long serialVersionUID = 854107654491442548L;

    private static final long DEFAULT_TIMEOUT = 1000*60*60*24*3; // 3 days
    
    @Column(name = "pwrecoverykey", unique = true, nullable = false, length = 255)
    @NotNull
    @Index(name = "idx_pwrecoverykey")
    private String key;
    
    @Column(name = "creationdate")
    @Type(type="timestamp")
    private Date created;
    
    @Column(name = "timeout")
    @Type(type = "long")
    private long timeout;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;
    
    public PasswordRecovery(){
        this(null, null);
    }
    
    /**
     */
    public PasswordRecovery(User user, String key) {
        this(user, key, DEFAULT_TIMEOUT);
    }
    
    /**
     * 
     */
    public PasswordRecovery(User user, String key, long timeout) {
        this.user = user;
        this.key = key;
        this.timeout = timeout;
        this.created = new Date();
    }
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     */
    public long getTimeout() {
        return timeout;
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     */
    public Date getCreated() {
        return created;
    }
    
    /**
     */
    public void setCreated(Date created) {
        this.created = created;
    }
    
    /**
     */
    public User getUser() {
        return user;
    }
    
    /**
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     */
    public boolean hasExpired() {
        Date now = new Date();
        return now.after(new Date(created.getTime() + timeout));
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof PasswordRecovery)) {
            return false;
        }
        PasswordRecovery it = (PasswordRecovery) o;
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
