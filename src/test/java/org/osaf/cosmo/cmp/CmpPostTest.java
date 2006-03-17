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
package org.osaf.cosmo.cmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.cmp.CmpConstants;
import org.osaf.cosmo.cmp.UserResource;
import org.osaf.cosmo.model.User;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test Case for CMP <code>POST</code> operations.
 *
 * As <code>POST</code> is treated exactly the same as
 * <code>PUT</code> in CMP, we only test a few cases here, to avoid
 * redundancy with {@link CmpPutTest}.
 */
public class CmpPostTest extends BaseCmpServletTestCase {
    private static final Log log = LogFactory.getLog(CmpPostTest.class);

    /**
     */
    public void testSignup() throws Exception {
        User u1 = testHelper.makeDummyUser();

        MockHttpServletRequest request = createMockRequest("POST", "/signup");
        sendXmlRequest(request, new UserContent(u1));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_CREATED,
                     response.getStatus());
        assertNotNull("null Content-Location",
                      response.getHeader("Content-Location"));
        assertNotNull("null ETag", response.getHeader("ETag"));
    }

    /**
     */
    public void testBadlyFormattedSignup() throws Exception {
        Document doc = BUILDER_FACTORY.newDocumentBuilder().newDocument();
        Element e = DomUtil.createElement(doc, "deadbeef", UserResource.NS_CMP);
        doc.appendChild(e);

        MockHttpServletRequest request = createMockRequest("POST", "/signup");
        sendXmlRequest(request, doc);

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status",
                     MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /**
     */
    public void testSignupDuplicateUsername() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);

        // duplicate u1's username
        User u2 = testHelper.makeDummyUser();
        u2.setUsername(u1.getUsername());

        MockHttpServletRequest request = createMockRequest("POST", "/signup");
        sendXmlRequest(request, new UserContent(u2));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", CmpConstants.SC_USERNAME_IN_USE,
                     response.getStatus());
    }
}
