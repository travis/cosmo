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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.NotNull;

/**
 * Represents a user preference.
 */
@Entity
@Table(name="user_preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames={"userid", "preferencename"})})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Preference extends AuditableObject {

    private static final long serialVersionUID = 1376628118792909420L;
    private User user;
    private String key;
    private String value;
    
    public Preference() {
    }

    public Preference(String key,
                      String value) {
        this.key = key;
        this.value = value;
    }

    @Column(name = "preferencename", nullable = false, length = 255)
    @NotNull
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "preferencevalue", nullable = false, length = 255)
    @NotNull
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    @NotNull
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Transient
    public String getEntityTag() {
        // preference is unique by name for its user
        String uid = (getUser() != null && getUser().getUid() != null) ?
            getUser().getUid() : "-";
        String key = getKey() != null ? getKey() : "-";
        String modTime = getModifiedDate() != null ?
            new Long(getModifiedDate().getTime()).toString() : "-";
        String etag = uid + ":" + key + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }
}
