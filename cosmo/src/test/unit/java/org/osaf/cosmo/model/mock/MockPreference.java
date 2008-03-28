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

import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;

/**
 * Represents a user preference.
 */
public class MockPreference extends MockAuditableObject implements Preference {

    private static final long serialVersionUID = 1376628118792909420L;
    
   
    private User user;
    
    
    private String key;
    
    
    private String value;
    
    public MockPreference() {
    }

    public MockPreference(String key,
                      String value) {
        this.key = key;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePreference#getKey()
     */
    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePreference#setKey(java.lang.String)
     */
    public void setKey(String key) {
        this.key = key;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePreference#getValue()
     */
    public String getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePreference#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePreference#getUser()
     */
    public User getUser() {
        return user;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfacePreference#setUser(org.osaf.cosmo.model.copy.User)
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    public String calculateEntityTag() {
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
