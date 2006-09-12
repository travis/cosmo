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
package org.osaf.cosmo.security.impl;

import org.osaf.cosmo.acegisecurity.providers.ticket.TicketAuthenticationToken;
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.util.Iterator;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default implementation of {@link CosmoSecurityContext}. Wraps
 * an instance of Acegi Security's
 * {@link org.acegisecurity.Authentication}.
 *
 * XXX: consider removing the direct dependency on Acegi Security,
 * instead possibly having separate implementations of
 * CosmoSecurityContext for users and tickets that don't use an
 * Authentication at all
 */
public class CosmoSecurityContextImpl implements CosmoSecurityContext {
    private static final Log log =
        LogFactory.getLog(CosmoSecurityContextImpl.class);

    private boolean admin;
    private boolean anonymous;
    private Authentication authentication;
    private Ticket ticket;
    private User user;

    /**
     */
    public CosmoSecurityContextImpl(Authentication authentication) {
        this.anonymous = false;
        this.authentication = authentication;
        this.admin = false;

        processAuthentication();
    }

    /* ----- CosmoSecurityContext methods ----- */

    /**
     * Returns a name describing the principal for this security
     * context (the name of the Cosmo user, the id of the ticket, or
     * the string "anonymous".
     */
    public String getName() {
        if (isAnonymous()) {
            return "anonymous";
        }
        if (ticket != null) {
            return ticket.getKey();
        }
        return user.getUsername();
    }

    /**
     * Determines whether or not the context represents an anonymous
     * Cosmo user.
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Returns an instance of {@link User} describing the user
     * represented by the security context, or <code>null</code> if
     * the context does not represent a user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns an instance of {@link Ticket} describing the ticket
     * represented by the security context, or <code>null</code> if
     * the context does not represent a ticket.
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Determines whether or not the security context represents an
     * administrator
     */
    public boolean isAdmin() {
        return admin;
    }

    /* ----- our methods ----- */

    /**
     */
    public String toString() {
        return ToStringBuilder.
            reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     */
    protected Authentication getAuthentication() {
        return authentication;
    }

    private void processAuthentication() {
        //anonymous principals do not have CosmoUserDetails and by
        //definition are not running as other principals
        if (authentication instanceof AnonymousAuthenticationToken) {
            anonymous = true;
            return;
        }

        if (authentication instanceof TicketAuthenticationToken) {
            ticket = (Ticket)
                ((TicketAuthenticationToken) authentication).getPrincipal();
            return;
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            user = ((CosmoUserDetails) authentication.getPrincipal()).getUser();
            admin = user.getAdmin().booleanValue();
        }
    }
}
