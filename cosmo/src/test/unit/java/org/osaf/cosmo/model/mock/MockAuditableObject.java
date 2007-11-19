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

import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.osaf.cosmo.model.AuditableObject;
import org.osaf.cosmo.model.EntityFactory;

/**
 * Extends BaseModelObject and adds creationDate, modifiedDate
 * to track when Object was created and modified.
 */
public abstract class MockAuditableObject implements AuditableObject {

    private static final ThreadLocal<MessageDigest> etagDigestLocal = new ThreadLocal<MessageDigest>();
    private static final Base64 etagEncoder = new Base64();
    private static final EntityFactory FACTORY = new MockEntityFactory();
    
    private Date creationDate;
    private Date modifiedDate;
    private String etag = "";
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#getCreationDate()
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#setCreationDate(java.util.Date)
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#getModifiedDate()
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#setModifiedDate(java.util.Date)
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#updateTimestamp()
     */
    public void updateTimestamp() {
        modifiedDate = new Date();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#getEntityTag()
     */
    public String getEntityTag() {
        return etag;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#setEntityTag(java.lang.String)
     */
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
    
    public EntityFactory getFactory() {
        return FACTORY;
    }
}
