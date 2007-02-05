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
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test case for <code>DELETE</code> requests on
 * <code>DavServlet</code>.
 * <p>
 * Unsupported from spec:
 * <ul>
 * <li>Returning multistatus response when a collection member or
 * descendent cannot be deleted</li>
 * </ul>
 */
public class DavDeleteTest extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(DavDeleteTest.class);

    /** */
    public void testDeleteContent() throws Exception {
        HomeCollectionItem home = testHelper.getHomeCollection();
        ContentItem content =
            testHelper.makeDummyContent(testHelper.getUser());
        testHelper.getContentService().createContent(home, content);

        MockHttpServletRequest request =
            createMockRequest("DELETE", toCanonicalPath(content.getName()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("DELETE content did not return No Content",
                     MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());
        assertNull("DELETEd content still exists in storage",
                   testHelper.getContentService().
                   findItemByUid(content.getUid()));
        assertChildNotReferencedByParent(home, content);
    }

    /** */
    public void testDeleteCollection() throws Exception {
        HomeCollectionItem home = testHelper.getHomeCollection();
        CollectionItem collection =
            testHelper.makeDummyCollection(testHelper.getUser());
        testHelper.getContentService().createCollection(home, collection);

        MockHttpServletRequest request =
            createMockRequest("DELETE", toCanonicalPath(collection.getName()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("DELETE collection did not return No Content",
                     MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());
        assertNull("DELETEd collection still exists in storage",
                   testHelper.getContentService().
                   findItemByUid(collection.getUid()));
        assertChildNotReferencedByParent(home, collection);
    }

    /** */
    public void testDeleteCollectionDepthInfinity() throws Exception {
        // should behave exactly the same as testDeleteCollection()

        HomeCollectionItem home = testHelper.getHomeCollection();
        CollectionItem collection =
            testHelper.makeDummyCollection(testHelper.getUser());
        testHelper.getContentService().createCollection(home, collection);

        MockHttpServletRequest request =
            createMockRequest("DELETE", toCanonicalPath(collection.getName()));
        request.addHeader("Depth", "infinity");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("DELETE collection did not return No Content",
                     MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());
        assertNull("DELETEd collection still exists in storage",
                   testHelper.getContentService().
                   findItemByUid(collection.getUid()));
        assertChildNotReferencedByParent(home, collection);
    }

    /** */
    public void testDeleteCollectionDepth0() throws Exception {
        // should fail with unknown status

        HomeCollectionItem home = testHelper.getHomeCollection();
        CollectionItem collection =
            testHelper.makeDummyCollection(testHelper.getUser());
        testHelper.getContentService().createCollection(home, collection);

        MockHttpServletRequest request =
            createMockRequest("DELETE", toCanonicalPath(collection.getName()));
        request.addHeader("Depth", "0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("DELETE collection did not return Bad Request",
                     MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /** */
    public void testDeleteHomeCollection() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("DELETE", toCanonicalPath("/"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("DELETE home collection did not return Forbidden",
                     MockHttpServletResponse.SC_FORBIDDEN,
                     response.getStatus());
    }

    private void assertChildNotReferencedByParent(CollectionItem parent,
                                                  Item item) {
        for (Item child : parent.getChildren()) {
            if (child.getUid().equals(item.getUid()))
                fail("DELETEd item still referenced by parent collection");
        }
        assertTrue(true);
    }
}
