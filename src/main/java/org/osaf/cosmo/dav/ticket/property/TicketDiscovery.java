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
package org.osaf.cosmo.dav.ticket.property;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.osaf.cosmo.dav.ExtendedDavResource;
import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.model.Ticket;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * Represents the WebDAV Tickets ticketdiscovery property.
 */
public class TicketDiscovery extends AbstractDavProperty
    implements TicketConstants {

    private ExtendedDavResource resource;

    /**
     */
    public TicketDiscovery(ExtendedDavResource resource) {
        super(TICKETDISCOVERY, true);
        this.resource = resource;
    }

    /**
     * Returns a <code>Set</code> of
     * <code>TicketDiscovery.TicketInfo</code>s for this property.
     */
    public Object getValue() {
        Set elements = new HashSet();
        for (Iterator i=resource.getTickets().iterator();
             i.hasNext();) {
            elements.add(new TicketInfo((Ticket) i.next()));
        }
        return elements;
    }

    public class TicketInfo implements XmlSerializable {
        private Ticket ticket;
  
        public TicketInfo(Ticket ticket) {
            this.ticket = ticket;
        }

        public Element toXml(Document document) {
            Element ticketInfo =
                DomUtil.createElement(document, ELEMENT_TICKET_TICKETINFO,
                                      NAMESPACE_TICKET);

            Element id =
                DomUtil.createElement(document, ELEMENT_TICKET_ID,
                                      NAMESPACE_TICKET);
            DomUtil.setText(id, ticket.getKey());
            ticketInfo.appendChild(id);

            Element owner =
                DomUtil.createElement(document, XML_OWNER, NAMESPACE);
            Element href =
                DomUtil.createElement(document, XML_HREF, NAMESPACE);
            String url =
                resource.getLocator().getFactory().
                createResourceLocator(resource.getLocator().getPrefix(),
                                      "/" + ticket.getOwner()).
                getHref(true);
            DomUtil.setText(href, url);
            owner.appendChild(href);
            ticketInfo.appendChild(owner);

            Element timeout =
                DomUtil.createElement(document, ELEMENT_TICKET_TIMEOUT,
                                      NAMESPACE_TICKET);
            DomUtil.setText(timeout, ticket.getTimeout());
            ticketInfo.appendChild(timeout);
 
            // visit limits are not supported; the element remains to
            // comply with the current draft of the spec
            Element visits =
                DomUtil.createElement(document, ELEMENT_TICKET_VISITS,
                                      NAMESPACE_TICKET);
            DomUtil.setText(visits, VALUE_INFINITY);
            ticketInfo.appendChild(visits);
 
            Element privilege =
                DomUtil.createElement(document, XML_PRIVILEGE, NAMESPACE);
            if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ)) {
                Element read =
                    DomUtil.createElement(document, XML_READ, NAMESPACE);
                privilege.appendChild(read);
            }
            if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_WRITE)) {
                Element write =
                    DomUtil.createElement(document, XML_WRITE, NAMESPACE);
                privilege.appendChild(write);
            }
            if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_FREEBUSY)) {
                Element freebusy =
                    DomUtil.createElement(document, ELEMENT_TICKET_FREEBUSY,
                                          NAMESPACE);
                privilege.appendChild(freebusy);
            }
            ticketInfo.appendChild(privilege);
 
            return ticketInfo;
        }
    }
}
