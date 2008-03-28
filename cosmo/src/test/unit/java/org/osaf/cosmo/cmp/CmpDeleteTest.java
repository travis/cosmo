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

import org.osaf.cosmo.model.User;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Case for CMP <code>DELETE</code> operations.
 */
public class CmpDeleteTest extends BaseCmpServletTestCase {
    private static final Log log = LogFactory.getLog(CmpDeleteTest.class);

    /**
     */
    public void testDeleteUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        userService.createUser(u1);

        MockHttpServletRequest request =
            createMockRequest("DELETE", "/user/" + u1.getUsername());
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NO_CONTENT);
        User test = userService.getUser(u1.getUsername());
        assertNull(test);
    }

    /**
     */
    public void testDeleteNonExistentUser() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("DELETE", "/user/deadbeef");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NO_CONTENT);
    }

    /**
     */
    public void testDeleteOverlord() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("DELETE", "/user/" + User.USERNAME_OVERLORD);
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_FORBIDDEN);
    }

    /**
     */
    public void testDeleteNoUsername() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("DELETE", "/user/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NOT_FOUND);
    }

    /**
     */
    public void testDeleteBadCommand() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("DELETE", "/deadbeef");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NOT_FOUND);
    }
}
