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
package org.osaf.cosmo.dav;

import java.io.InputStream;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;

import org.osaf.cosmo.dav.caldav.report.CaldavReport;
import org.osaf.cosmo.dav.ticket.TicketDavRequest;
import org.osaf.cosmo.model.Ticket;

/**
 * An interface providing resource functionality required by WebDAV
 * extensions implemented by Cosmo.
 */
public interface ExtendedDavResource extends DavResource {

    /**
     * String constant representing the WebDAV 1 compliance
     * class as well as the Cosmo extended classes.
     */
    // see bug 5137 for why we don't include class 2
    public String COMPLIANCE_CLASS = "1, calendar-access, ticket";

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isCalendarCollection();

    /**
     * Returns true if this resource represents a home collection.
     */
    public boolean isHomeCollection();

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
     * Returns all visible tickets (those owned by the currently
     * authenticated user) on this resource, or an empty
     * <code>Set</code> if there are no visible tickets.
     */
    public Set<Ticket> getTickets();

    /**
     * Adds a new member to this resource and set the member properties.
     */
    public MultiStatusResponse addMember(DavResource member,
                                         InputContext inputContext,
                                         DavPropertySet setProperties)
        throws DavException;

    /**
     * Returns the member resource at the given absolute href.
     */
    public DavResource findMember(String href)
        throws DavException;

    /**
     * Return the report that matches the given report info if it is
     * supported by this resource.
     */
    public Report getReport(ReportInfo info)
        throws DavException;
}
