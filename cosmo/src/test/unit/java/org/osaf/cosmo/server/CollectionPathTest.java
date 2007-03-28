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
 * Test case for <code>CollectionPath</code>.
 */
public class CollectionPathTest extends TestCase {

    /** */
    public void testAbsoluteUrlPath() throws Exception {
        String badUrlPath = "http://dead.beef/";
        try {
            CollectionPath cp = CollectionPath.parse(badUrlPath);
            fail("absolute urlPath parsed successfully");
        } catch (IllegalArgumentException e) {}
    }

    /** */
    public void testSuccessfulParse() throws Exception {
        String urlPath = "/collection/deadbeef";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNotNull("path did not parse successfully", cp);
    }

    /** */
    public void testUnsuccessfulParse() throws Exception {
        String urlPath = "/bcm/stuff/deadbeef";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNull("non-collection path parsed successfuly", cp);
    }

    /** */
    public void testParseNoPathInfo() throws Exception {
        String urlPath = "/collection/deadbeef/foobar";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNull("path with disallowed pathInfo parsed successfully", cp);
    }

    /** */
    public void testParseWithPathInfo() throws Exception {
        String urlPath = "/collection/deadbeef/foobar";
        CollectionPath cp = CollectionPath.parse(urlPath, true);
        assertNotNull("path with allowed pathInfo did not parse successfully",
                      cp);
    }

    /** */
    public void testGetUid() throws Exception {
        String uid = "deadbeef";
        String urlPath = "/collection/" + uid;
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNotNull("path did not parse successfully", cp);
        assertNotNull("uid not found", cp.getUid());
        assertEquals("found incorrect uid", uid, cp.getUid());
    }

    /** */
    public void testGetUidWithPathInfo() throws Exception {
        String uid = "deadbeef";
        String pathInfo = "/foobar";
        String urlPath = "/collection/" + uid + pathInfo;
        CollectionPath cp = CollectionPath.parse(urlPath, true);
        assertNotNull("path did not parse successfully", cp);
        assertNotNull("uid not found", cp.getUid());
        assertEquals("found incorrect uid", uid, cp.getUid());
        assertNotNull("path info not found", cp.getPathInfo());
        assertEquals("found incorrect path info", pathInfo, cp.getPathInfo());
    }
}
