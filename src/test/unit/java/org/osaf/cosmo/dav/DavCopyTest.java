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
 * Test case for <code>COPY</code> requests on
 * <code>DavServlet</code>.
 */
public class DavCopyTest extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(DavCopyTest.class);

    /** */
    public void testCopyContent() throws Exception {
        HomeCollectionItem home = testHelper.getHomeCollection();
        ContentItem content =
            testHelper.makeDummyContent(testHelper.getUser());
        content.setAttribute("test:test", "test value");
        testHelper.getContentService().createContent(home, content);

        String oldPath = toCanonicalPath(content.getName());
        String newPath = toCanonicalPath("testcopy");

        MockHttpServletRequest request = createMockRequest("COPY", oldPath);
        request.addHeader("Destination", toAbsoluteUrl(request, newPath));
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("COPY content did not return Created",
                     MockHttpServletResponse.SC_CREATED,
                     response.getStatus());
        assertNotNull("COPYd content does not exist at old location",
                      testHelper.getContentService().findItemByPath(oldPath));

        ContentItem newContent = (ContentItem)
            testHelper.getContentService().findItemByPath(newPath);
        assertNotNull("COPYd content does not exist at new location",
                      newContent);
        assertNotNull("COPYd content does not retain test property",
                      newContent.getAttribute("test:test"));
        assertEquals("COPYd content property does not have original value",
                     "test value",
                     newContent.getAttributeValue("test:test").toString());
    }
}
