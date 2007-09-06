/*
 * Copyright 2006 Open Source Applications Foundation
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResponse;
import org.osaf.cosmo.dav.impl.DavItemResource;
import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.dav.ticket.property.TicketDiscovery;
import org.osaf.cosmo.model.Ticket;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extends {@link org.apache.jackrabbit.webdav.WebdavResponseImpl} and
 * implements methods for the DAV ticket extension.
 */
public class StandardDavResponse extends WebdavResponseImpl
    implements DavResponse, DavConstants, TicketConstants {
    private static final Log log =
        LogFactory.getLog(StandardDavResponse.class);
    private static final XMLOutputFactory XML_OUTPUT_FACTORY =
        XMLOutputFactory.newInstance();

    /**
     */
    public StandardDavResponse(HttpServletResponse response) {
        super(response);
    }

    // DavResponse methods

    /**
     * Send the <code>ticketdiscovery</code> response to a
     * <code>MKTICKET</code> request.
     *
     * @param resource the resource on which the ticket was created
     * @param ticketId the id of the newly created ticket
     */
    public void sendMkTicketResponse(DavItemResource resource,
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

    public void sendDavError(DavException e)
        throws IOException {
        setStatus(e.getErrorCode());
        if (! e.hasContent())
            return;

        XMLStreamWriter writer = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
            writer.writeStartDocument();
            e.writeTo(writer);
            writer.writeEndDocument();

            setContentType("text/xml; charset=UTF-8");
            byte[] bytes = out.toByteArray();
            setContentLength(bytes.length);
            getOutputStream().write(bytes);
        } catch (Throwable e2) {
            log.error("Error writing XML", e2);
            log.error("Original exception", e);
            setStatus(500);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException e2) {
                    log.warn("Unable to close XML writer", e2);
                }
            }
        }
    }
}
