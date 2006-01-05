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
package org.osaf.cosmo.dav;

import java.io.InputStream;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;

import org.osaf.cosmo.dav.ticket.TicketDavRequest;
import org.osaf.cosmo.model.Ticket;

/**
 * An interface providing resource functionality required by WebDAV
 * extensions implemented by Cosmo.
 */
public interface CosmoDavResource extends DavResource {

    /**
     * String constant representing the WebDAV 1 and 2 compliance
     * classes as well as the Cosmo extended classes.
     */
    public String COMPLIANCE_CLASS =
        DavResource.COMPLIANCE_CLASS + ", calendar-access, ticket";

    /**
     * String constant representing the WebDAV 1 and 2 method set as
     * well as the Cosmo extended method set (excluding MKCALENDAR,
     * which is only supported for certain resources).
     */
    public String METHODS = DavResource.METHODS + ", MKTICKET, DELTICKET";

    /**
     * Returns true if this resource represents a ticketable dav
     * resource.
     */
    public boolean isTicketable();

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isCalendarCollection();

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    public void saveTicket(Ticket ticket) throws DavException;

    /**
     * Removes the association between the ticket and this resource
     * and deletes the ticket from persistent storage.
     */
    public void removeTicket(Ticket ticket) throws DavException;

    /**
     * Returns the ticket with the given id on this resource.
     */
    public Ticket getTicket(String id);

    /**
     * Returns all tickets owned by the named user on this resource,
     * or an empty <code>Set</code> if the user does not own any
     * tickets.
     *
     * @param username
     */
    public Set getTickets(String username);

    /**
     * Returns all tickets owned by the currently logged in user on
     * this resource, or an empty <code>Set</code> if the user does
     * not own any tickets.
     */
    public Set getLoggedInUserTickets();

    /**
     * Returns a resource locator for the named principal's homedir.
     */
    public DavResourceLocator getHomedirLocator(String principal);
}
