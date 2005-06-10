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

import javax.jcr.RepositoryException;

import org.apache.commons.id.random.SessionIdGenerator;

import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.simple.DavResourceImpl;

import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResourceFactory;
import org.osaf.cosmo.model.Ticket;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 *
 * For now, this class is a placeholder.
 */
public class CosmoDavResourceImpl extends DavResourceImpl 
    implements CosmoDavResource {

    private HashMap tickets = new HashMap();
    private static final SessionIdGenerator ticketIdGenerator =
        new SessionIdGenerator();

    /**
     * Create a new {@link DavResource}.
     *
     * @param locator
     * @param factory
     * @param session
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

        // assign owner
        CosmoDavResourceFactory cosmoFactory =
            (CosmoDavResourceFactory) getFactory();
        String owner = cosmoFactory.getSecurityManager().getSecurityContext().
            getUser().getUsername();
        ticket.setOwner(owner);

        // XXX save into repository
        tickets.put(ticket.getId(), ticket);
    }

    /**
     * Returns the ticket with the given id associated with this
     * resource.
     */
    public Ticket getTicket(String id) {
        // XXX pull out of repository
        return (Ticket) tickets.get(id);
    }
}
