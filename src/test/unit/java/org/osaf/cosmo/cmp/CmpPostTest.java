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

import java.net.URLEncoder;

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
 * <code>PUT</code> for most CMP ops, we only test a few cases here,
 * to avoid redundancy with {@link CmpPutTest}.
 */
public class CmpPostTest extends BaseCmpServletTestCase {
    private static final Log log = LogFactory.getLog(CmpPostTest.class);

    /**
     */
    public void testServerGc() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("POST", "/server/gc");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());
    }

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
     * Ensure users cannot sign up as administrators.
     */
    public void testSignupAdmin() throws Exception{
        // Make sure user can't sign up w/ admin privs
        
        User u1 = testHelper.makeDummyUser();
        u1.setAdmin(true);

        MockHttpServletRequest request = createMockRequest("POST", "/signup");
        sendXmlRequest(request, new UserContent(u1));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        this.assertEquals("user was allowed to sign up as admin", 
                MockHttpServletResponse.SC_FORBIDDEN ,response.getStatus());
        
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
    
    /**
     * 
     * @throws Exception
     */
    public void testDeleteUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        userService.createUser(u1);

        MockHttpServletRequest request =
            createMockRequest("POST", "/user/" + u1.getUsername() + "/delete");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NO_CONTENT);
        User test = userService.getUser(u1.getUsername());
        assertNull(test);
    }
    
    /**
     * 
     * @throws Exception
     */
    public void testDeleteMultiUser() throws Exception {
        //basic deletion test
        User u1 = testHelper.makeDummyUser();
        User u2 = testHelper.makeDummyUser();
        User u3 = testHelper.makeDummyUser();
        userService.createUser(u1);
        userService.createUser(u2);
        userService.createUser(u3);

        MockHttpServletRequest request =
            createMockRequest("POST", "/user/delete");
        request.setContentType("application/x-www-form-urlencoded");
        request.addParameter("user", u1.getUsername());
        request.addParameter("user", u2.getUsername());
        request.addParameter("user", u3.getUsername());
                
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        assertTrue(response.getStatus() ==
            MockHttpServletResponse.SC_NO_CONTENT);

        User test1 = userService.getUser(u1.getUsername());
        assertNull(test1);
        User test2 = userService.getUser(u2.getUsername());
        assertNull(test2);
        User test3 = userService.getUser(u3.getUsername());
        assertNull(test3);
        
    }
}
