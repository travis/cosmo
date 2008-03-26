/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.wsse;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Represents WSSE Username Token, where
 * username = username, nonce = a cryptographically random string,
 * created = "creation timestamp", in W3DTF format, and
 * passwordDigest =  Base64 \ (SHA1 (Nonce + CreationTimestamp + Password))
 */
public class UsernameToken {
    
    private String username;
    private String nonce;
    private String passwordDigest;
    private String created;
    
    public UsernameToken(String username, String nonce, String passwordDigest, String created) {
        this.username = username;
        this.nonce = nonce;
        this.passwordDigest = passwordDigest;
        this.created = created;
    }
    
    public String getUsername() {
        return username;
    }
    public String getNonce() {
        return nonce;
    }
    public String getPasswordDigest() {
        return passwordDigest;
    }
    public String getCreated() {
        return created;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsernameToken == false) {
            return false;
          }
          if (this == obj) {
            return true;
          }
          UsernameToken rhs = (UsernameToken) obj;
          return new EqualsBuilder()
                        .append(username, rhs.username)
                        .append(nonce, rhs.nonce)
                        .append(passwordDigest, rhs.passwordDigest)
                        .append(created, rhs.created)
                        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(username).
            append(nonce).
            append(passwordDigest).
            append(created).
            toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
        append("username", username).
        append("passwordDigest", passwordDigest).
        append("nonce", nonce).
        append("created", created).
        toString();
    }
    
    
    
}
