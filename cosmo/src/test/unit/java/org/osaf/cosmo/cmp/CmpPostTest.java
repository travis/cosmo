/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
import org.osaf.cosmo.model.User;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
        
        assertEquals(MockHttpServletResponse.SC_NO_CONTENT, 
                response.getStatus());

        User test1 = userService.getUser(u1.getUsername());
        assertNull(test1);
        User test2 = userService.getUser(u2.getUsername());
        assertNull(test2);
        User test3 = userService.getUser(u3.getUsername());
        assertNull(test3);
        
    }
    
    public void testRecoverPassword() throws Exception {
        User u1 = testHelper.makeDummyUser();
        userService.createUser(u1);

        // test with username
        MockHttpServletRequest request =
            createMockRequest("POST", "/account/password/recover");
        request.setContentType("application/x-www-form-urlencoded");
        request.addParameter("username", u1.getUsername());
                
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        assertEquals(MockHttpServletResponse.SC_OK, 
                response.getStatus());
        
        // test with email
        request =
            createMockRequest("POST", "/account/password/recover");
        request.setContentType("application/x-www-form-urlencoded");
        request.addParameter("username", u1.getUsername());
                
        response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        assertEquals(MockHttpServletResponse.SC_OK, 
                response.getStatus());

        // test with nothing
        request =
            createMockRequest("POST", "/account/password/recover");
        request.setContentType("application/x-www-form-urlencoded");
                        
        response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        assertEquals(MockHttpServletResponse.SC_NOT_FOUND, 
                response.getStatus());

        

    }
}
