/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.server;

import junit.framework.TestCase;

/**
 * Test case for <code>UserPath</code>.
 */
public class UserPathTest extends TestCase {

    /** */
    public void testAbsoluteUrlPath() throws Exception {
        String badUrlPath = "http://dead.beef/";
        try {
            UserPath up = UserPath.parse(badUrlPath);
            fail("absolute urlPath parsed successfully");
        } catch (IllegalArgumentException e) {}
    }

    /** */
    public void testSuccessfulParse() throws Exception {
        String urlPath = "/user/jonez";
        UserPath up = UserPath.parse(urlPath);
        assertNotNull("path did not parse successfully", up);
    }

    /** */
    public void testUnsuccessfulParse() throws Exception {
        String urlPath = "/bcm/stuff/jonez";
        UserPath up = UserPath.parse(urlPath);
        assertNull("non-user path parsed successfuly", up);
    }

    /** */
    public void testParseNoPathInfo() throws Exception {
        String urlPath = "/user/jonez/foobar";
        UserPath up = UserPath.parse(urlPath);
        assertNull("path with disallowed pathInfo parsed successfully", up);
    }

    /** */
    public void testParseWithPathInfo() throws Exception {
        String urlPath = "/user/jonez/foobar";
        UserPath up = UserPath.parse(urlPath, true);
        assertNotNull("path with allowed pathInfo did not parse successfully",
                      up);
    }

    /** */
    public void testGetUsername() throws Exception {
        String username = "jonez";
        String urlPath = "/user/" + username;
        UserPath up = UserPath.parse(urlPath);
        assertNotNull("path did not parse successfully", up);
        assertNotNull("username not found", up.getUsername());
        assertEquals("found incorrect username", username, up.getUsername());
    }

    /** */
    public void testGetUsernameWithPathInfo() throws Exception {
        String username = "jonez";
        String pathInfo = "/foobar";
        String urlPath = "/user/" + username + pathInfo;
        UserPath up = UserPath.parse(urlPath, true);
        assertNotNull("path did not parse successfully", up);
        assertNotNull("username not found", up.getUsername());
        assertEquals("found incorrect username", username, up.getUsername());
        assertNotNull("path info not found", up.getPathInfo());
        assertEquals("found incorrect path info", pathInfo, up.getPathInfo());
    }
}
