/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.BaseDavTestCase;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test case for <code>StandardDavRequest</code>.
 */
public class StandardDavRequestTest extends BaseDavTestCase {
    private static final Log log =
        LogFactory.getLog(StandardDavRequestTest.class);

    public void testNoDepth() throws Exception {
        // no depth => depth infinity

        MockHttpServletRequest httpRequest =
            new MockHttpServletRequest();
        StandardDavRequest request =
            new StandardDavRequest(httpRequest,
                                   testHelper.getResourceLocatorFactory(),
                                   testHelper.getEntityFactory());

        assertEquals("no depth not infinity", DEPTH_INFINITY,
                     request.getDepth());
    }

    public void testBadDepth() throws Exception {
        MockHttpServletRequest httpRequest =
            new MockHttpServletRequest();
        httpRequest.addHeader("Depth", "bad value");
        StandardDavRequest request =
            new StandardDavRequest(httpRequest,
                                   testHelper.getResourceLocatorFactory(),
                                   testHelper.getEntityFactory());

        try {
            int depth = request.getDepth();
            fail("got bad depth " + depth);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testEmptyPropfindBody() throws Exception {
        // empty propfind body => allprop request

        MockHttpServletRequest httpRequest =
            new MockHttpServletRequest();
        httpRequest.setContentType("text/xml");
        StandardDavRequest request =
            new StandardDavRequest(httpRequest,
                                   testHelper.getResourceLocatorFactory(),
                                   testHelper.getEntityFactory());

        assertEquals("propfind type not allprop", PROPFIND_ALL_PROP,
                     request.getPropFindType());
        assertTrue("propnames not empty",
                   request.getPropFindProperties().isEmpty());
    }
}
