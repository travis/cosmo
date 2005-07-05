/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.acegisecurity.ticket;

import java.io.Serializable;

import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.providers.AbstractAuthenticationToken;

/**
 * Represents a ticket-based {@link net.sf.acegisecurity.Authentication}.
 *
 * Ticket tokens are always assigned a single
 * {@link net.sf.acegisecurity.GrantedAuthority} named
 * <code>ROLE_TICKET</code>.
 */
public class TicketAuthenticationToken extends AbstractAuthenticationToken
    implements Serializable {

    private static final GrantedAuthority[] AUTHORITIES = {
        new GrantedAuthorityImpl("ROLE_TICKET")
    };

    private boolean authenticated;
    private String path;
    private String id;

    /**
     * @param path the absolute path to the ticketed resource
     * @param id the ticket id
     */
    public TicketAuthenticationToken(String path, String id) {
        if (path == null || path.equals("")) {
            throw new IllegalArgumentException("path may not be null or empty");
        }
        if (id == null || id.equals("")) {
            throw new IllegalArgumentException("id may not be null or empty");
        }
        this.path = path;
        this.id = id;
        authenticated = false;
    }

    // Authentication methods

    /**
     */
    public void setAuthenticated(boolean isAuthenticated) {
        authenticated = isAuthenticated;
    }

    /**
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     */
    public GrantedAuthority[] getAuthorities() {
        return AUTHORITIES;
    }

    /**
     * Always returns an empty <code>String</code>.
     */
    public Object getCredentials() {
        return "";
    }

    /**
     * Returns the path and id separated by a colon.
     */
    public Object getPrincipal() {
        return path + ":" + id;
    }

    // our methods

    /**
     */
    public String getPath() {
        return path;
    }

    /**
     */
    public String getId() {
        return id;
    }

    /**
     */
    public boolean equals(Object obj) {
        if (! super.equals(obj)) {
            return false;
        }

        if (! (obj instanceof TicketAuthenticationToken)) {
            return false;
        }

        TicketAuthenticationToken test = (TicketAuthenticationToken) obj;
        return id.equals(test.getPrincipal());
    }
}
