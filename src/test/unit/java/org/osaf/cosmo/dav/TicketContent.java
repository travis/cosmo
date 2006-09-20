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

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.model.Ticket;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Simple wrapper bean that converts a {@link Ticket} to an XML
 * fragment suitable for sending as DAV request content.
 */
public class TicketContent
    implements XmlSerializable, DavConstants, TicketConstants {
    private static final Log log = LogFactory.getLog(TicketContent.class);

    private Ticket ticket;

    /**
     */
    public TicketContent(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     */
    public Element toXml(Document doc) {
        Element e = DomUtil.createElement(doc, ELEMENT_TICKET_TICKETINFO,
                                          NAMESPACE_TICKET);

        Element timeout = DomUtil.createElement(doc, ELEMENT_TICKET_TIMEOUT,
                                                NAMESPACE_TICKET);
        DomUtil.setText(timeout, ticket.getTimeout());
        e.appendChild(timeout);

        Element privilege = DomUtil.createElement(doc, XML_PRIVILEGE,
                                                  NAMESPACE);
        for (String priv : (Set<String>) ticket.getPrivileges()) {
            if (priv.equals(Ticket.PRIVILEGE_READ)) {
                Element read = DomUtil.createElement(doc, XML_READ,
                                                     NAMESPACE);
                privilege.appendChild(read);
            } else if (priv.equals(Ticket.PRIVILEGE_WRITE)) { 
                Element write = DomUtil.createElement(doc, XML_WRITE,
                                                      NAMESPACE);
                privilege.appendChild(write);
            } else if (priv.equals(Ticket.PRIVILEGE_FREEBUSY)) {
                Element freebusy =
                    DomUtil.createElement(doc, ELEMENT_TICKET_FREEBUSY,
                                          NAMESPACE);
                privilege.appendChild(freebusy);
            } else {
                throw new IllegalStateException("Unrecognized ticket privilege " + priv);
            }
        }
        e.appendChild(privilege);

        return e;
    }
}
