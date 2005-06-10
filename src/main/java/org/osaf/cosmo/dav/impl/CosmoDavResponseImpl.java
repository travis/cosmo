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

import java.io.IOException;

import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.model.Ticket;

/**
 * The standard implementation of {@link CosmoDavResponse}. Wraps a
 * {@link org.apache.jackrabbit.webdav.WebdavResponse}.
 */
public class CosmoDavResponseImpl
    implements CosmoDavResponse, CosmoDavConstants {
    private static final Logger log =
        Logger.getLogger(CosmoDavResponseImpl.class);

    private WebdavResponse webdavResponse;

    /**
     */
    public CosmoDavResponseImpl(WebdavResponse response) {
        webdavResponse = response;
    }

    // TicketDavResponse methods

    /**
     * Send the response body for a {@link Ticket} creation event. The
     * given id specifies which ticket was created, as a resource may
     * have multiple tickets associated with it.
     */
    public void sendMkTicketResponse(CosmoDavResource resource,
                                     String ticketId)
        throws IOException {
        Ticket ticket = resource.getTicket(ticketId);

        webdavResponse.setHeader(HEADER_TICKET, ticketId);

        Element ticketDiscovery =
            new Element(ELEMENT_TICKETDISCOVERY, NAMESPACE);
        ticketDiscovery.addContent(ticketToXml(ticket));
        webdavResponse.sendXmlResponse(new Document(ticketDiscovery),
                                       WebdavResponse.SC_OK);
    }

    // private methods

    private Element ticketToXml(Ticket ticket) {
        Element ticketInfo = new Element(ELEMENT_TICKETINFO, NAMESPACE);

        Element id = new Element(ELEMENT_ID, NAMESPACE);
        id.addContent(ticket.getId());
        ticketInfo.addContent(id);

        Element owner = new Element(ELEMENT_OWNER, NAMESPACE);
        Element href = new Element(ELEMENT_HREF, NAMESPACE);
        // XXX: full href
        href.addContent(ticket.getOwner());
        owner.addContent(href);
        ticketInfo.addContent(owner);

        Element timeout = new Element(ELEMENT_TIMEOUT, NAMESPACE);
        // XXX: convert from seconds
        timeout.addContent(ticket.getTimeout());
        ticketInfo.addContent(timeout);

        Element visits = new Element(ELEMENT_VISITS, NAMESPACE);
        visits.addContent(ticket.getVisits().intValue() == Integer.MAX_VALUE ?
                          VALUE_INFINITY :
                          ticket.getVisits().toString());
        ticketInfo.addContent(visits);

        Element privilege = new Element(ELEMENT_PRIVILEGE, NAMESPACE);
        if (ticket.isRead().booleanValue()) {
            Element read = new Element(ELEMENT_READ, NAMESPACE);
            privilege.addContent(read);
        }
        if (ticket.isWrite().booleanValue()) {
            Element write = new Element(ELEMENT_WRITE, NAMESPACE);
            privilege.addContent(write);
        }
        ticketInfo.addContent(privilege);

        return ticketInfo;
    }
}
