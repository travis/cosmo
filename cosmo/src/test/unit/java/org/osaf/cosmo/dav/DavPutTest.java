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
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test case for <code>PUT</code> requests on
 * <code>DavServlet</code>.
 * <p>
 */
public class DavPutTest extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(DavPutTest.class);

    /** */
    public void testPutContent() throws Exception {
        testHelper.logIn();
        HomeCollectionItem home = testHelper.getHomeCollection();
       
        MockHttpServletRequest request =
            createMockRequest("PUT", toCanonicalPath("testContent"));
        request.setContent("test!".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("PUT content did not return Created",
                     MockHttpServletResponse.SC_CREATED,
                     response.getStatus());
        
        FileItem content = (FileItem)
            testHelper.getContentService().
            findItemByPath(toCanonicalPath("testContent"));
     
        assertNotNull("Content does not exist", content);
        assertEquals("Content name incorrect.","testContent", content.getName());
        assertEquals("Content content incorrect","test!",new String(content.getContent()));
        assertEquals("Content parent not home collection", home, content
                .getParents().iterator().next());
    }
    
    /** */
    public void testPutContentConcurrent() throws Exception {
        testHelper.logIn();
        HomeCollectionItem home = testHelper.getHomeCollection();
       
        // tell the mock dao to throw an exception
        MockContentDao.THROW_CONCURRENT_EXCEPTION = true;
        
        MockHttpServletRequest request =
            createMockRequest("PUT", toCanonicalPath("testConcurrContent"));
        request.setContent("test!".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("PUT content did not return Server Error.",
                     MockHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     response.getStatus());
        
        assertEquals("PUT content did not return Concurrency failure.",
                "Concurrency failure.",
                response.getErrorMessage());
        
        // tell the mock dao to not throw an exception
        MockContentDao.THROW_CONCURRENT_EXCEPTION = false;
    }
}
