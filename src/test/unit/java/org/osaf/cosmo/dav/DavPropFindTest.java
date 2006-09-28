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
package org.osaf.cosmo.dav;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Ticket;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for <code>PROPFIND</code> requests on
 * <code>DavServlet</code>.
 */
public class DavPropFindTest extends BaseDavServletTestCase
    implements DavConstants, TicketConstants {
    private static final Log log = LogFactory.getLog(DavPropFindTest.class);

    /** */
    public void testPropFindTicketDiscoveryByUser() throws Exception {
        logInUser(user);

        HomeCollectionItem home = contentService.getRootItem(user);

        Ticket ticket = testHelper.makeDummyTicket();
        ticket.setOwner(user);
        contentService.createTicket(home, ticket);

        PropFindContent content = new PropFindContent();
        content.addPropertyName(TICKETDISCOVERY);

        String path = toCanonicalPath("/");
        MockHttpServletRequest request = createMockRequest("PROPFIND", path);
        sendXmlRequest(request, content);
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("PROPFIND did not return MultiStatus",
                     DavServletResponse.SC_MULTI_STATUS,
                     response.getStatus());

        Set<TicketContent> contents =
            createTicketContents(readMultiStatusResponse(response),
                                 request.getRequestURL().toString());
        assertTrue("No tickets in response", ! contents.isEmpty());
        assertEquals("More than one ticket in response", contents.size(), 1);
        assertTicketEquality(ticket, contents.iterator().next(),
                             request.getRequestURL().toString());
    }

    /** */
    public void testPropFindTicketDiscoveryByTicket() throws Exception {
//         HomeCollectionItem home = contentService.getRootItem(user);

//         Ticket ticket = testHelper.makeDummyTicket();
//         ticket.setOwner(user);
//         contentService.createTicket(home, ticket);

//         // XXX log in ticket

//         PropFindContent content = new PropFindContent();
//         content.addPropertyName(TICKETDISCOVERY);

//         String path = toCanonicalPath("/");
//         MockHttpServletRequest request = createMockRequest("PROPFIND", path);
//         sendXmlRequest(request, content);
//         MockHttpServletResponse response = new MockHttpServletResponse();
//         servlet.service(request, response);

//         assertEquals("PROPFIND did not return MultiStatus",
//                      DavServletResponse.SC_MULTI_STATUS,
//                      response.getStatus());

//         Set<TicketContent> contents =
//             createTicketContents(readMultiStatusResponse(response),
//                                  request.getRequestURL().toString());
//         assertTrue("No tickets in response", ! contents.isEmpty());
//         assertEquals("More than one ticket in response", contents.size(), 1);
//         assertTicketEquality(ticket, contents.iterator().next(),
//                              request.getRequestURL().toString());
    }

    private void assertTicketEquality(Ticket ticket,
                                      TicketContent test,
                                      String ownerHref) {
        assertEquals("Returned id not equal to original id",
                     ticket.getKey(), test.getTicket().getKey());
        assertEquals("Returned owner href not equal to original href",
                     ownerHref, test.getOwnerHref());
        assertEquals("Returned timeout not equal to original timeout",
                     ticket.getTimeout(), test.getTicket().getTimeout());
        for (String priv : (Set<String>) test.getTicket().getPrivileges()) {
            if (! ticket.getPrivileges().contains(priv))
                fail("Returned privilege " + priv + " not on original");
        }
        for (String priv : (Set<String>) ticket.getPrivileges()) {
            if (! test.getTicket().getPrivileges().contains(priv))
                fail("Original privilege " + priv + " not on returned");
        }
    }

    private Set<TicketContent> createTicketContents(MultiStatus ms,
                                                    String href)
        throws Exception {
        Element prop = findProp(ms, href, PROPERTY_TICKET_TICKETDISCOVERY,
                                NAMESPACE_TICKET);
        if (prop == null)
            throw new Exception("no ticketdiscovery property");

        Set<TicketContent> contents = new HashSet<TicketContent>();

        for (ElementIterator i= DomUtil.getChildren(prop,
                                                    ELEMENT_TICKET_TICKETINFO,
                                                    NAMESPACE_TICKET);
             i.hasNext();) {
            contents.add(TicketContent.createFromXml(i.nextElement()));
        }

        return contents;
    }
}
