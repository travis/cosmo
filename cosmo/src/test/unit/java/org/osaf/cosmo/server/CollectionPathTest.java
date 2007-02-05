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
        String urlPath = "/collection/deadbeef-cafebebe";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNotNull("path did not parse successfully", cp);
    }

    /** */
    public void testUnsuccessfulParse() throws Exception {
        String urlPath = "/bcm/stuff/deadbeef-cafebebe";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNull("non-collection path parsed successfuly", cp);
    }

    /** */
    public void testParseNoSelectors() throws Exception {
        String urlPath = "/collection/deadbeef-cafebebe/foobar";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNull("path with disallowed selectors parsed successfully", cp);
    }

    /** */
    public void testParseWithSelectors() throws Exception {
        String urlPath = "/collection/deadbeef-cafebebe/foobar";
        CollectionPath cp = CollectionPath.parse(urlPath, true);
        assertNotNull("path with allowed selectors did not parse successfully",
                      cp);
    }

    /** */
    public void testGetUid() throws Exception {
        String uid = "deadbeef-cafebebe";
        String urlPath = "/collection/" + uid;
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertNotNull("path did not parse successfully", cp);
        assertNotNull("uid not found", cp.getUid());
        assertEquals("found incorrect uid", uid, cp.getUid());
    }

    /** */
    public void testGetUidWithSelectors() throws Exception {
        String uid = "deadbeef-cafebebe";
        String urlPath = "/collection/" + uid + "/foobar";
        CollectionPath cp = CollectionPath.parse(urlPath, true);
        assertNotNull("path did not parse successfully", cp);
        assertNotNull("uid not found", cp.getUid());
        assertEquals("found incorrect uid", uid, cp.getUid());
    }

    /** */
    public void testGetSelector() throws Exception {
        String selector1 = "foobar";
        String selector2 = "bazquux";
        String urlPath =
            "/collection/deadbeef-cafebebe/" + selector1 + "/" + selector2;
        CollectionPath cp = CollectionPath.parse(urlPath, true);
        assertNotNull("path did not parse successfully", cp);
        assertTrue("path selector 1 not found", cp.getSelector(selector1));
        assertTrue("path selector 2 not found", cp.getSelector(selector2));
        assertFalse("bad path selector found", cp.getSelector("not there"));
    }

    /** */
    public void testNoPathSelectors() throws Exception {
        String urlPath = "/collection/deadbeef-cafebebe";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertFalse("bad path selector found", cp.getSelector("not there"));
    }

    /** */
    public void testNoPathSelectorsTrailingSlash() throws Exception {
        String urlPath = "/collection/deadbeef-cafebebe/";
        CollectionPath cp = CollectionPath.parse(urlPath);
        assertFalse("bad path selector found", cp.getSelector("not there"));
    }
}
