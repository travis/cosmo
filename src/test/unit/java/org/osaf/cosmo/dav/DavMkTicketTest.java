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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Ticket;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test case for <code>MKTICKET</code> requests on
 * <code>DavServlet</code>.
 */
public class DavMkTicketTest extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(DavMkTicketTest.class);

    /** */
    public void testMkTicket() throws Exception {
        HomeCollectionItem home = contentService.getRootItem(user);

        Ticket ticket = testHelper.makeDummyTicket();

        MockHttpServletRequest request =
            createMockRequest("MKTICKET", toCanonicalPath("/"));
        sendXmlRequest(request, new TicketContent(ticket));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("MKTICKET did not return OK",
                     MockHttpServletResponse.SC_OK,
                     response.getStatus());

        String id = (String) response.getHeader("Ticket");
        boolean found = false;
        for (Ticket test : home.getTickets()) {
            if (test.getKey().equals(id)) {
                found = true;
                break;
            }
        }
        assertTrue("Ticket with key " + id + " not found on home collection",
                   found);
    }

    /** */
    public void testNonExistentResource() throws Exception {
        Ticket ticket = testHelper.makeDummyTicket();

        MockHttpServletRequest request =
            createMockRequest("MKTICKET", toCanonicalPath("/ugh"));
        sendXmlRequest(request, new TicketContent(ticket));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("MKTICKET did not return Not Found",
                     MockHttpServletResponse.SC_NOT_FOUND,
                     response.getStatus());
    }

    /** */
    public void testBadRequest() throws Exception {
        HomeCollectionItem home = contentService.getRootItem(user);

        Ticket ticket = testHelper.makeDummyTicket();

        MockHttpServletRequest request =
            createMockRequest("MKTICKET", toCanonicalPath("/"));
        request.setContentType("text/xml");
        request.setContent("deadbeef".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("MKTICKET did not return Bad Request",
                     MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }
}
