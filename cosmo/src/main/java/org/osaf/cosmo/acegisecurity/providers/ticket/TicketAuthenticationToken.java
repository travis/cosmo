/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.acegisecurity.providers.ticket;

import java.io.Serializable;
import java.util.Set;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.AbstractAuthenticationToken;

import org.osaf.cosmo.model.Ticket;

/**
 * Represents a ticket-based
 * {@link org.springframework.security.Authentication}.
 *
 * Before being authenticated, the token contains the ticket id and
 * the path of the ticketed resource. After authentication, the
 * token's principal is the {@link Ticket} itself.
 */
public class TicketAuthenticationToken extends AbstractAuthenticationToken
    implements Serializable {

    private static final GrantedAuthority[] AUTHORITIES = { new GrantedAuthorityImpl(
            "ROLE_TICKET") };

    private boolean authenticated;
    private String path;
    private Set<String> keys;
    private Ticket ticket;

    /**
     * @param path the absolute URI path to the ticketed resource
     * @param keys all ticket keys provided for the resource
     */
    public TicketAuthenticationToken(String path,
                                     Set<String> keys) {
        super(AUTHORITIES);
        if (path == null || path.equals(""))
            throw new IllegalArgumentException("path may not be null or empty");
        if (keys == null || keys.isEmpty())
            throw new IllegalArgumentException("keys may not be null or empty");
        this.path = path;
        this.keys = keys;
        authenticated = false;
    }

    // Authentication methods

    /** */
    public void setAuthenticated(boolean isAuthenticated) {
        authenticated = isAuthenticated;
    }

    /** */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Always returns an empty <code>String</code>.
     */
    public Object getCredentials() {
        return "";
    }

    /**
     * Returns the ticket.
     */
    public Object getPrincipal() {
        return ticket;
    }

    // our methods

    /** */
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /** */
    public String getPath() {
        return path;
    }

    /** */
    public Set<String> getKeys() {
        return keys;
    }

    /** */
    public boolean equals(Object obj) {
        if (! super.equals(obj)) {
            return false;
        }
        if (! (obj instanceof TicketAuthenticationToken)) {
            return false;
        }
        TicketAuthenticationToken test = (TicketAuthenticationToken) obj;
        return ticket.equals(test.getPrincipal());
    }
}
