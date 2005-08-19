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
package org.osaf.cosmo.dav.property;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;

import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.model.Ticket;

/**
 * Represents the WebDAV Tickets ticketdiscovery property.
 */
public class TicketDiscovery extends AbstractDavProperty {

    private CosmoDavResource resource;

    /**
     */
    public TicketDiscovery(CosmoDavResource resource) {
        super(CosmoDavPropertyName.TICKETDISCOVERY, true);
        this.resource = resource;
    }

    /**
     * Returns an <code>Element</code> representing this property.
     */
    public Element toXml() {
        Element element = getName().toXml();
        if (getValue() != null) {
            element.addContent((Set) getValue());
        }
        return element;
    }

    /**
     * (Returns a <code>Set</code> of <code>Element</code>s
     * representing the restrictions of this property.
     */
    public Object getValue() {
        Set elements = new HashSet();
        for (Iterator i=resource.getLoggedInUserTickets().iterator();
             i.hasNext();) {
            Ticket ticket = (Ticket) i.next();
            elements.add(ticketToXml(ticket));
        }
        return elements;
    }

    private Element ticketToXml(Ticket ticket) {
        Element ticketInfo = new Element(CosmoDavConstants.ELEMENT_TICKETINFO,
                                         CosmoDavConstants.NAMESPACE_TICKET);

        Element id = new Element(CosmoDavConstants.ELEMENT_ID,
                                 CosmoDavConstants.NAMESPACE_TICKET);
        id.addContent(ticket.getId());
        ticketInfo.addContent(id);

        Element owner = new Element(CosmoDavConstants.ELEMENT_OWNER,
                                    DavConstants.NAMESPACE);
        Element href = new Element(CosmoDavConstants.ELEMENT_HREF,
                                   DavConstants.NAMESPACE);
        String url =
            resource.getHomedirLocator(ticket.getOwner()).getHref(true);
        href.addContent(url);
        owner.addContent(href);
        ticketInfo.addContent(owner);

        Element timeout = new Element(CosmoDavConstants.ELEMENT_TIMEOUT,
                                      CosmoDavConstants.NAMESPACE_TICKET);
        timeout.addContent(ticket.getTimeout());
        ticketInfo.addContent(timeout);

        // visit limits are not supported; the element remains to
        // comply with the current draft of the spec
        Element visits = new Element(CosmoDavConstants.ELEMENT_VISITS,
                                     CosmoDavConstants.NAMESPACE_TICKET);
        visits.addContent(CosmoDavConstants.VALUE_INFINITY);
        ticketInfo.addContent(visits);

        Element privilege = new Element(CosmoDavConstants.ELEMENT_PRIVILEGE,
                                        DavConstants.NAMESPACE);
        if (ticket.getPrivileges().contains(CosmoDavConstants.PRIVILEGE_READ)) {
            Element read = new Element(CosmoDavConstants.ELEMENT_READ,
                                       DavConstants.NAMESPACE);
            privilege.addContent(read);
        }
        if (ticket.getPrivileges().
            contains(CosmoDavConstants.PRIVILEGE_WRITE)) {
            Element write = new Element(CosmoDavConstants.ELEMENT_WRITE,
                                        DavConstants.NAMESPACE);
            privilege.addContent(write);
        }
        ticketInfo.addContent(privilege);

        return ticketInfo;
    }
}
