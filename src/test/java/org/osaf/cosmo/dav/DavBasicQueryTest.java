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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;

import org.osaf.cosmo.dav.CosmoDavResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Case for DAV <code>REPORT</code> operations.
 *
 * Note: this is essentially an integration test that requires the
 * full jcr-server + cosmo dav, io, and jackrabbit.query layers to be
 * set up. a real unit test would mock up the repository and the
 * dav request, response and resource classes.
 */
public class DavBasicQueryTest extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(DavBasicQueryTest.class);

    private String testpath;

    private static final String[] REPORT_EVENTS = {
        "report-event1.ics",
        "report-event2.ics",
        "report-event3.ics",
        "report-event4.ics",
        "report-event5.ics",
        "report-event6.ics",
        "report-event7.ics"
    };

     /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        // make a calendar collection to put events into
        Node calendarCollectionNode =
            testHelper.addCalendarCollectionNode(getName());
        // XXX: not sure why we have to save, since theoretically
        // testHelper and the servlet are using the same jcr session
        calendarCollectionNode.getParent().save();

        testpath = calendarCollectionNode.getPath();

        // put all test events in the calendar collection
        for (int i=0; i<REPORT_EVENTS.length; i++) {
            Node resourceNode =
                testHelper.addCalendarResourceNode(calendarCollectionNode,
                                                   REPORT_EVENTS[i]);
        }
        calendarCollectionNode.save();
    }

   /**
     */
    public void testBasicEvent() throws Exception {
        Document content = testHelper.loadXml("report-basic1.xml");
        MockHttpServletRequest request = createMockRequest("REPORT", testpath);
        sendXmlRequest(request, content);

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        assertEquals(CosmoDavResponse.SC_MULTI_STATUS, response.getStatus());
        //        log.debug(new String(response.getContentAsByteArray()));

        MultiStatus ms = readMultiStatusResponse(response);
        assertEquals(7, ms.getResponses().size());
    }
}
