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
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.apache.log4j.Logger;

import org.osaf.cosmo.dav.ExtendedDavResource;
import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.dav.ticket.TicketDavResponse;
import org.osaf.cosmo.dav.ticket.property.TicketDiscovery;
import org.osaf.cosmo.model.Ticket;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extends {@link org.apache.jackrabbit.webdav.WebdavResponseImpl} and
 * implements {@link CosmoDavResponse}.
 */
public class StandardDavResponse extends WebdavResponseImpl
    implements TicketDavResponse, DavConstants, TicketConstants {
    private static final Logger log =
        Logger.getLogger(StandardDavResponse.class);

    /**
     */
    public StandardDavResponse(HttpServletResponse response,
                               boolean noCache) {
        super(response, noCache);
    }

    // TicketDavResponse methods

    /**
     * Send the <code>ticketdiscovery</code> response to a
     * <code>MKTICKET</code> request.
     *
     * @param resource the resource on which the ticket was created
     * @param ticketId the id of the newly created ticket
     */
    public void sendMkTicketResponse(ExtendedDavResource resource,
                                     String ticketId)
        throws DavException, IOException {
        setHeader(HEADER_TICKET, ticketId);

        TicketDiscovery ticketdiscovery = (TicketDiscovery)
            resource.getProperties().get(TICKETDISCOVERY);
        MkTicketInfo info = new MkTicketInfo(ticketdiscovery);

        sendXmlResponse(info, SC_OK);
    }

    private class MkTicketInfo implements XmlSerializable {
        private TicketDiscovery td;

        public MkTicketInfo(TicketDiscovery td) {
            this.td = td;
        }

        public Element toXml(Document document) {
            Element prop =
                DomUtil.createElement(document, XML_PROP, NAMESPACE);
            prop.appendChild(td.toXml(document));
            return prop;
        }
    }

    /**
     * Send the response to a <code>DELTICKET</code> request.
     *
     * @param resource the resource on which the ticket was deleted
     * @param ticketId the id of the deleted ticket
     */
    public void sendDelTicketResponse(ExtendedDavResource resource,
                                      String ticketId)
        throws DavException, IOException {
        setStatus(SC_NO_CONTENT);
    }
}
