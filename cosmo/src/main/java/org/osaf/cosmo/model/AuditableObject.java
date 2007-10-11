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

    private static final ThreadLocal<MessageDigest> etagDigestLocal = new ThreadLocal<MessageDigest>();
    private static final Base64 etagEncoder = new Base64();
    
    private Date creationDate;
    private Date modifiedDate;
    private String etag = "";
    
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

    /**
     * Update modifiedDate with current system time.
     */
    public void updateTimestamp() {
        modifiedDate = new Date();
    }
    
    /**
     * <p>
     * Returns a string representing the state of the object. Entity tags can
     * be used to compare two objects in remote environments where the
     * equals method is not available.
     * </p>
     */
    @Column(name="etag")
    public String getEntityTag() {
        return etag;
    }
    
    public void setEntityTag(String etag) {
        this.etag = etag;
    }
    
    /**
     * <p>
     * Calculates updates object's entity tag.
     * Returns calculated entity tag.  
     * </p>
     * <p>
     * This implementation simply returns the empty string. Subclasses should
     * override it when necessary.
     * </p>
     * 
     * Subclasses should override
     * this.
     */
    public String calculateEntityTag() {
        return "";
    }

    /**
     * <p>
     * Returns a Base64-encoded SHA-1 digest of the provided bytes.
     * </p>
     */
    protected static String encodeEntityTag(byte[] bytes) {
        
        // Use MessageDigest stored in threadlocal so that each
        // thread has its own instance.
        MessageDigest md = etagDigestLocal.get();
        
        if(md==null) {
            try {
                // initialize threadlocal
                md = MessageDigest.getInstance("sha1");
                etagDigestLocal.set(md);
            } catch (Exception e) {
                throw new RuntimeException("Platform does not support sha1?", e);
            }
        }
        
        return new String(etagEncoder.encode(md.digest(bytes)));
    }
}
