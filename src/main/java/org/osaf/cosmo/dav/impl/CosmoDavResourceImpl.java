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
package org.osaf.cosmo.dav.impl;

import java. io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.id.StringIdentifierGenerator;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.simple.DavResourceImpl;

import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResourceFactory;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 */
public class CosmoDavResourceImpl extends DavResourceImpl 
    implements CosmoDavResource {

    private HashMap tickets = new HashMap();
    private StringIdentifierGenerator ticketIdGenerator;
    private String baseUrl;
    private DavLocatorFactory principalLocatorFactory;

    /**
     */
    public CosmoDavResourceImpl(DavResourceLocator locator,
                                CosmoDavResourceFactory factory,
                                DavSession session)
        throws RepositoryException {
        super(locator, factory, session);
    }

    // CosmoDavResource methods

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    public void saveTicket(Ticket ticket) {
        ticket.setId(ticketIdGenerator.nextStringIdentifier());
        ticket.setOwner(getLoggedInUser().getUsername());

        // XXX save into repository
        getTickets(ticket.getOwner()).add(ticket);
    }

    /**
     * Returns all tickets owned by the named user on this resource,
     * or an empty <code>Set</code> if the user does not own any
     * tickets.
     *
     * @param username
     */
    public Set getTickets(String username) {
        // XXX pull from repository
        Set userTickets = (Set) tickets.get(username);
        if (userTickets == null) {
            userTickets = new HashSet();
            tickets.put(username, userTickets);
        }
        return userTickets;
    }

    /**
     * Returns all tickets owned by the currently logged in user on
     * this resource, or an empty <code>Set</code> if the user does
     * not own any tickets.
     */
    public Set getLoggedInUserTickets() {
        return getTickets(getLoggedInUser().getUsername());
    }

    /**
     * Returns a resource locator for the named principal.
     */
    public DavResourceLocator getPrincipalLocator(String principal) {
        return principalLocatorFactory.
            createResourceLocator(baseUrl, "/" + principal);
    }

    // our methods

    /**
     * Set the generator for ticket identifiers.
     */
    public void setTicketIdGenerator(StringIdentifierGenerator generator) {
        ticketIdGenerator = generator;
    }

    /**
     * Set the base URL for the server on which this resource lives
     * (could be statically configured or dynamically calculated
     * per-request).
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Set the locator factory that generates URLs for principal
     * resources (often used to address the owner of a dav resource).
     */
    public void setPrincipalLocatorFactory(DavLocatorFactory factory) {
        principalLocatorFactory = factory;
    }

    // private methods

    private User getLoggedInUser() {
        CosmoDavResourceFactory cosmoFactory =
            (CosmoDavResourceFactory) getFactory();
        return cosmoFactory.getSecurityManager().getSecurityContext().
            getUser();
    }
}
