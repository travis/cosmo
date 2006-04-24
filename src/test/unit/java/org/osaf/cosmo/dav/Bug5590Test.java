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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.osaf.cosmo.model.User;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * Bug5590Test - a Unit test to test which reports we advertise from the server.
 * User: towns
 * Date: Apr 20, 2006
 * Time: 11:35:30 AM
 *
 * @see https://bugzilla.osafoundation.org/show_bug.cgi?id=5590
 */
public class Bug5590Test extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(Bug5590Test.class);
    private static final Namespace NS = Namespace.getNamespace("D", "DAV:");

    /**
     */
    public void testBug5590() throws Exception {
        // Create a test user, and login her in.
        User u1 = testHelper.makeDummyUser();
        userService.createUser(u1);
        logInUser(u1);

        // Setup our DAV request and get its response.
        String path = "/";
        MockHttpServletRequest request = createMockRequest("PROPFIND", path);
        String xml = new String("<?xml version=\"1.0\"?><a:propfind xmlns:a=\"DAV:\"><a:prop><a:supported-report-set/></a:prop></a:propfind>");
        request.setContent(xml.getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        // Parse the returned XML to pull out the supported report names
        // and add them to a HashMap.
        HashMap reports = new HashMap();
        Document xmlDoc = readXmlResponse(response);

        NodeList nodes = xmlDoc.getElementsByTagName("D:report");
        for(int i=0; i < nodes.getLength(); i++) {
            Node reportItem = nodes.item(i).getChildNodes().item(1);
            // The substring here trims the name from C:<report-name> to <report-name>
            reports.put(reportItem.getNodeName().substring(2), reportItem);
        }

        // Build up the expected results.
        String[] expectedResults = new String[2];
        expectedResults[0] = CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_QUERY;
        expectedResults[1] = CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_MULTIGET;

        // Assert that we have the reports we expect and that we have the number of
        // reports we expect.
        for(int i=0; i < expectedResults.length; i++) {
            assertTrue(reports.get(expectedResults[i]) != null);
        }

        // Make sure we have the right number of reports.
        assertTrue(reports.size() == 2);
    }
}
