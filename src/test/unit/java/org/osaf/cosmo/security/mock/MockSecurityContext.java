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
package org.osaf.cosmo.security.mock;

import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.util.Iterator;
import java.security.Principal;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A mock implementation of {@link CosmoSecurityContext} that provides
 * dummy instances for use with unit mocks.
 */
public class MockSecurityContext implements CosmoSecurityContext {
    private static final Log log =
        LogFactory.getLog(MockSecurityContext.class);

    private boolean anonymous;
    private Principal principal;
    private boolean admin;
    private Ticket ticket;
    private User user;

    /**
     */
    public MockSecurityContext(Principal principal) {
        this.anonymous = false;
        this.principal = principal;
        this.admin = false;

        processPrincipal();
    }

    /* ----- CosmoSecurityContext methods ----- */

    /**
     * Returns a name describing the principal for this security
     * context (the name of the Cosmo user, the id of the ticket, or
     * some other precise identification.
     */
    public String getName() {
        if (isAnonymous()) {
            return "anonymous";
        }
        if (ticket != null) {
            return ticket.getId();
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
     * administrative user.
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
    protected Principal getPrincipal() {
        return principal;
    }

    private void processPrincipal() {
        if (principal instanceof MockAnonymousPrincipal) {
            anonymous = true;
        }
        else if (principal instanceof MockUserPrincipal) {
            user = ((MockUserPrincipal) principal).getUser();
            admin = user.getAdmin().booleanValue();
        }
        
    }
}
