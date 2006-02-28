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

import java.io.StringReader;
import java.util.Iterator;
import javax.jcr.Node;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Case for Bug 5175.
 */
public class Bug5175Test extends BaseReportTestCase {
    private static final Log log = LogFactory.getLog(Bug5175Test.class);

    /**
     */
    protected void setUpDavData(Node node) throws Exception {
        Node resourceNode =
            testHelper.addCalendarResourceNode(node, "reports/bugs/5175.ics");
    }

    public void testBug5175() throws Exception {
        Document content = testHelper.loadXml("reports/bugs/5175.xml");

        MockHttpServletRequest request = createMockRequest("REPORT", testUri);
        sendXmlRequest(request, content);

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        assertEquals("response status not MULTI_STATUS",
                     CosmoDavResponse.SC_MULTI_STATUS, response.getStatus());
        // log.debug(new String(response.getContentAsByteArray()));

        MultiStatus ms = readMultiStatusResponse(response);
        assertEquals("expected 1 response but got " + ms.getResponses().size(),
                     1, ms.getResponses().size());

        MultiStatus.MultiStatusResponse msr = (MultiStatus.MultiStatusResponse)
            ms.getResponses().iterator().next();
        assertNotNull("null dav response", msr);
        assertNotNull("null href for dav response", msr.getHref());
        assertTrue("dav response href does not end with 5175.ics",
                   msr.getHref().endsWith("5175.ics"));

        MultiStatus.PropStat ps = (MultiStatus.PropStat)
            msr.getPropStats().iterator().next();
        assertNotNull("null propstat", ps);
        assertNotNull("null propstat status", ps.getStatus());
        assertEquals("propstat status not OK", CosmoDavResponse.SC_OK,
                     ps.getStatus().getCode());

        boolean foundCalendarData = false;
        boolean foundEtag = false;
        for (Iterator i=ps.getProps().iterator(); i.hasNext();) {
            Element prop = (Element) i.next();
            if (prop.getName().equals("calendar-data")) {
                foundCalendarData = true;
                String calendarData = prop.getTextTrim();
                assertNotNull("null calendar-data text", calendarData);
                // log.debug("calendar data:\n" + calendarData);

                CalendarBuilder builder = new CalendarBuilder();
                Calendar calendar =
                    builder.build(new StringReader(calendarData));

                Component event =
                    calendar.getComponents().getComponent(Component.VEVENT);
                assertNotNull("null event", event);

                Property uid = event.getProperties().getProperty(Property.UID);
                assertNotNull("null uid", uid);
                assertEquals("NEW_UID", uid.getValue());
            }
            else if (prop.getName().equals("getetag")) {
                foundEtag = true;
                String etag = prop.getTextTrim();
                assertNotNull("null getetag text", etag);
                // can't check etag equality since it's different for
                // every test run
            }
            else {
                fail("unknown dav prop " + prop.getName());
            }
        }
        assertTrue("calendar-data prop not found", foundCalendarData);
        assertTrue("getetag prop not found", foundEtag);
    }
}
