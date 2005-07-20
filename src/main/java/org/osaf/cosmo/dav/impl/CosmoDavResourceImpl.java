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
import java.util.ArrayList;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.id.StringIdentifierGenerator;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.simple.DavResourceImpl;

import org.apache.log4j.Logger;

import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResourceFactory;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 */
public class CosmoDavResourceImpl extends DavResourceImpl 
    implements CosmoDavResource {
    private static final Logger log = Logger.getLogger(CosmoDavResource.class);

    private boolean isCollection;
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

    // DavResource methods

    /**
     */
    public boolean isCollection() {
        try {
            // required because super.isCollection is private
            if (getNode().isNodeType(CosmoJcrConstants.NT_TICKET)) {
                return false;
            }
            if (getNode().
                isNodeType(CosmoJcrConstants.NT_CALENDAR_COLLECTION)) {
                return true;
            }
        } catch (RepositoryException e) {
            // XXX
        }
        return super.isCollection();
    }

    /**
     */
    public String getSupportedMethods() {
        // can only make a calendar collection inside a regular
        // collection (NEVER inside another calendar collection).
        if (exists () && isCollection() && ! isCalendarCollection()) {
            return CosmoDavResource.METHODS + ", MKCALENDAR";
        }
        return CosmoDavResource.METHODS;
    }

    /**
     */
    public DavResourceIterator getMembers() {
        // wholly copied from DavResourceImpl in order to filter out
        // ticket nodes
        ArrayList list = new ArrayList();
        if (exists() && isCollection()) {
            try {
                NodeIterator it = getNode().getNodes();
                while(it.hasNext()) {
                    Node childNode = it.nextNode();
                    if (childNode.getPrimaryNodeType().getName().
                        equals(CosmoJcrConstants.NT_TICKET)) {
                        continue;
                    }
                    list.add(buildResourceFromItem(childNode));
                }
            } catch (RepositoryException e) {
                // should not occure
            } catch (DavException e) {
                // should not occure
            }
        }
        return new DavResourceIteratorImpl(list);
    }

    // CosmoDavResource methods

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isCalendarCollection() {
        try {
            return getNode().
                isNodeType(CosmoJcrConstants.NT_CALENDAR_COLLECTION);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the given resoure as an internal member to this resource.
     *
     * @param resource {@link CosmoDavResource} to be added
     */
    public void addCalendarCollection(CosmoDavResource resource)
        throws DavException {
    }

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    public void saveTicket(Ticket ticket)
        throws DavException {
        ticket.setId(ticketIdGenerator.nextStringIdentifier());
        ticket.setOwner(getLoggedInUser().getUsername());

        try {
            Node resourceNode = getNode();
            JCRUtils.ticketToNode(resourceNode, ticket);
            resourceNode.save();
        } catch (RepositoryException e) {
            log.error("cannot save ticket", e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Removes the association between the ticket and this resource
     * and deletes the ticket from persistent storage.
     */
    public void removeTicket(Ticket ticket)
        throws DavException {
        try {
            Node resourceNode = getNode();
            Node ticketNode =
                JCRUtils.findChildTicketNode(resourceNode, ticket);
            if (ticketNode == null) {
                return;
            }
            ticketNode.remove();
            resourceNode.save();
        } catch (RepositoryException e) {
            log.error("cannot remove ticket " + ticket.getId(), e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Returns the ticket with the given id on this resource. Does not
     * execute any security checks.
     */
    public Ticket getTicket(String id)
        throws DavException {
        try {
            Node ticketNode = JCRUtils.findChildTicketNode(getNode(), id);
            if (ticketNode == null) {
                return null;
            }
            return JCRUtils.nodeToTicket(ticketNode);
        } catch (RepositoryException e) {
            log.error("cannot get ticket " + id, e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Returns all tickets owned by the named user on this resource,
     * or an empty <code>Set</code> if the user does not own any
     * tickets.
     *
     * @param username
     */
    public Set getTickets(String username)
        throws DavException {
        try {
            return JCRUtils.findTickets(getNode(), username);
        } catch (RepositoryException e) {
            log.error("cannot get tickets owned by " + username, e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Returns all tickets owned by the currently logged in user on
     * this resource, or an empty <code>Set</code> if the user does
     * not own any tickets.
     */
    public Set getLoggedInUserTickets()
        throws DavException {
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
     */
    protected User getLoggedInUser() {
        CosmoDavResourceFactory cosmoFactory =
            (CosmoDavResourceFactory) getFactory();
        return cosmoFactory.getSecurityManager().getSecurityContext().
            getUser();
    }

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
}
