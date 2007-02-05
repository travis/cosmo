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
package org.osaf.cosmo.cmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.User;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Case for CMP server usage operations.
 */
public class CmpServerUsageTest extends BaseCmpServletTestCase {
    private static final Log log = LogFactory.getLog(CmpServerUsageTest.class);

    /**
     */
    public void testGetSpaceUsageAllUsers() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);
        HomeCollectionItem h1 = contentService.getRootItem(u1);
        ContentItem c1 = testHelper.makeDummyContent(u1);
        c1 = contentService.createContent(h1, c1);
        ContentItem c2 = testHelper.makeDummyContent(u1);
        c2 = contentService.createContent(h1, c2);
        CollectionItem l1 = testHelper.makeDummyCollection(u1);
        l1 = contentService.createCollection(h1, l1);
        ContentItem c3 = testHelper.makeDummyContent(u1);
        c3 = contentService.createContent(l1, c3);
        ContentItem c4 = testHelper.makeDummyContent(u1);
        c4 = contentService.createContent(l1, c4);

        User u2 = testHelper.makeDummyUser();
        u2 = userService.createUser(u2);
        HomeCollectionItem h2 = contentService.getRootItem(u2);
        ContentItem c5 = testHelper.makeDummyContent(u2);
        c5 = contentService.createContent(h2, c5);
        ContentItem c6 = testHelper.makeDummyContent(u2);
        c6 = contentService.createContent(h2, c6);

        MockHttpServletRequest request =
            createMockRequest("GET", "/server/usage/space");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(MockHttpServletResponse.SC_OK, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("UTF-8", response.getCharacterEncoding());

        // XXX: verify output
        // log.error(response.getContentAsString());

        userService.removeUser(u1);
        userService.removeUser(u2);
    }

    /**
     */
    public void testGetSpaceUsageOneUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);
        HomeCollectionItem h1 = contentService.getRootItem(u1);
        ContentItem c1 = testHelper.makeDummyContent(u1);
        c1 = contentService.createContent(h1, c1);
        ContentItem c2 = testHelper.makeDummyContent(u1);
        c2 = contentService.createContent(h1, c2);
        CollectionItem l1 = testHelper.makeDummyCollection(u1);
        l1 = contentService.createCollection(h1, l1);
        ContentItem c3 = testHelper.makeDummyContent(u1);
        c3 = contentService.createContent(l1, c3);
        ContentItem c4 = testHelper.makeDummyContent(u1);
        c4 = contentService.createContent(l1, c4);

        MockHttpServletRequest request =
            createMockRequest("GET", "/server/usage/space/" + u1.getUsername());
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(MockHttpServletResponse.SC_OK, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("UTF-8", response.getCharacterEncoding());

        // XXX: verify output
        // log.error(response.getContentAsString());

        userService.removeUser(u1);
    }

    /**
     */
    public void testGetSpaceUsageBadUser() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("GET", "/server/usage/space/deadbeef");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(MockHttpServletResponse.SC_NOT_FOUND,
                     response.getStatus());
    }
}
