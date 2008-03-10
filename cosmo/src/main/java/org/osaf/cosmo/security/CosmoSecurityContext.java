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
package org.osaf.cosmo.security;

import java.util.Set;

import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * An interface that represents a user-specific context for Cosmo
 * security operations. It provides a facade for the Acegi Security
 * system and applies Cosmo-specific security rules.
 */
public interface CosmoSecurityContext {

    /**
     * Returns a name describing the principal for this security
     * context (the name of the Cosmo user, the id of the ticket, or
     * some other precise identification.
     */
    public String getName();

    /**
     * Determines whether or not the context represents an anonymous
     * Cosmo user.
     */
    public boolean isAnonymous();

    /**
     * Returns an instance of {@link User} describing the user
     * represented by the security context, or <code>null</code> if
     * the context does not represent a user.
     */
    public User getUser();

    /**
     * Returns an instance of {@link Ticket} describing the ticket
     * represented by the security context, or <code>null</code> if
     * the context does not represent a ticket.
     */
    public Ticket getTicket();
    
    /**
     * Returns a set of tickets in addition to the principal.  This
     * set of tickets gives the current principal additional access.
     */
    public Set<Ticket> getTickets();

    /**
     * Determines whether or not the security context represents an
     * administrative user.
     */
    public boolean isAdmin();

    /**
     * Returns the set of tickets granted on the given item that are
     * visible to the current security context.
     */
    public Set<Ticket> findVisibleTickets(Item item);
}
