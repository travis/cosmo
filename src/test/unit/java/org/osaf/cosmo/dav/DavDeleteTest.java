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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test case for <code>DELETE</code> requests on
 * <code>DavServlet</code>.
 */
public class DavDeleteTest extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(DavDeleteTest.class);

    /** */
    public void testDeleteContent() throws Exception {
        HomeCollectionItem home = contentService.getRootItem(user);
        ContentItem content = testHelper.makeDummyContent(user);
        contentService.createContent(home, content);

        MockHttpServletRequest request =
            createMockRequest("DELETE", toCanonicalPath(content.getName()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("DELETE content did not return No Content",
                     MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());

        assertNull("DELETEd content still exists in storage",
                   contentService.findItemByUid(content.getUid()));
    }

    /** */
    public void testDeleteCollection() throws Exception {
        HomeCollectionItem home = contentService.getRootItem(user);
        CollectionItem collection = testHelper.makeDummyCollection(user);
        contentService.createCollection(home, collection);

        MockHttpServletRequest request =
            createMockRequest("DELETE", toCanonicalPath(collection.getName()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("DELETE collection did not return No Content",
                     MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());

        assertNull("DELETEd collection still exists in storage",
                   contentService.findItemByUid(collection.getUid()));
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
}
