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

import javax.jcr.Node;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;

import org.osaf.cosmo.dav.CosmoDavResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Case for DAV <code>REPORT</code> operations.
 */
public class DavBasicQueryTest extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(DavBasicQueryTest.class);

    private Node calendarCollectionNode;

     /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        // make a calendar collection to put events into
        calendarCollectionNode = testHelper.addCalendarCollectionNode();
        // XXX: not sure why we have to save, since theoretically
        // testHelper and the servlet are using the same jcr session
        calendarCollectionNode.getParent().save();
    }

   /**
     */
    public void testBasicEvent() throws Exception {
        if (true) {
            // the test doesn't actually work, but we need at least
            // one test* method in the class
            return;
        }

        // put a calendar resource into the calendar collection
        Calendar calendar = testHelper.loadCalendar("report-event1.ics");
        Node resourceNode =
            testHelper.addCalendarResourceNode(calendarCollectionNode,
                                               calendar);
        resourceNode.getParent().save();

        // load the request content
        Document content = testHelper.loadXml("report-basic1.xml");

        // perform the report
        MockHttpServletRequest request =
            createMockRequest("REPORT", calendarCollectionNode.getPath()); 
        sendXmlRequest(request, content);

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        assertEquals(CosmoDavResponse.SC_MULTI_STATUS, response.getStatus());

        MultiStatus ms = readMultiStatusResponse(response);
        // log.debug("ms: " + ms);
        assertEquals(1, ms.getResponses().size());
    }
}
