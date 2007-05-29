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

import java.security.MessageDigest;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.commons.codec.binary.Base64;

import org.hibernate.annotations.Type;

/**
 * Extends BaseModelObject and adds creationDate, modifiedDate
 * to track when Object was created and modified.
 */
@MappedSuperclass
public abstract class AuditableObject extends BaseModelObject {

    private static final MessageDigest etagDigest;
    private static final Base64 etagEncoder = new Base64();

    static {
        try {
            etagDigest = MessageDigest.getInstance("sha1");
        } catch (Exception e) {
            throw new RuntimeException("Platform does not support sha1?", e);
        }
    }

    private Date creationDate;
    private Date modifiedDate;
    
    /**
     * @return date object was created
     */
    @Column(name = "createdate")
    @Type(type="long_timestamp")
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate 
     *                     date object was created
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return date object was last updated
     */
    @Column(name = "modifydate")
    @Type(type="long_timestamp")
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @param modifiedDate
     *                     date object was last modified
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    protected String encodeEntityTag(byte[] bytes) {
        return new String(etagEncoder.encode(etagDigest.digest(bytes)));
    }
}
