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
package org.osaf.cosmo.util;

import junit.framework.TestCase;

/**
 * Test case for <code>RepositoryUriParser</code>.
 */
public class RepositoryUriParserTest extends TestCase {

    /** */
    public void testBadUri() throws Exception {
        String badUri = "http://dead.beef/";
        try {
            RepositoryUriParser parser = new RepositoryUriParser(badUri);
            fail("parser instantiated with bad uri");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /** */
    public void testIsCollectionUri() throws Exception {
        String uri = "/collection/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertTrue("uri not flagged as collection uri",
                   parser.isCollectionUri());
    }

    /** */
    public void testIsNotCollectionUri() throws Exception {
        String uri = "/bcm/stuff/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertTrue("uri incorrectly flagged as collection uri",
                   ! parser.isCollectionUri());
    }

    /** */
    public void testIsCollectionUriNoSelectors() throws Exception {
        String uri = "/collection/deadbeef-cafebebe/foobar";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertFalse("uri with selectors incorrectly flagged as collection uri",
                    parser.isCollectionUri());
    }

    /** */
    public void testIsCollectionUriWithSelectors() throws Exception {
        String uri = "/collection/deadbeef-cafebebe/foobar";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertTrue("uri with selectors not flagged as collection uri",
                   parser.isCollectionUri(true));
    }

    /** */
    public void testGetCollectionUid() throws Exception {
        String uid = "deadbeef-cafebebe";
        String uri = "/collection/" + uid;
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertNotNull("uid not found in collection uri",
                      parser.getCollectionUid());
        assertEquals("found incorrect uid", uid, parser.getCollectionUid());
    }

    /** */
    public void testGetUidCollectionUidOnNonCollectionUri() throws Exception {
        String uri = "/bcm/stuff/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertNull("uid incorrectly found in non-collection uri",
                   parser.getCollectionUid());
    }

    /** */
    public void testGetCollectionUidNoSelectors() throws Exception {
        String uri = "/collection/deadbeef-cafebebe/foobar";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertNull("uid incorrectly found in collection uri with selectors",
                   parser.getCollectionUid());
    }

    /** */
    public void testGetCollectionUidWithSelectors() throws Exception {
        String uid = "deadbeef-cafebebe";
        String uri = "/collection/" + uid + "/foobar";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertNotNull("uid not found in collection uri with selectors",
                      parser.getCollectionUid(true));
        assertEquals("found incorrect uid in collection uri with selectors",
                     uid, parser.getCollectionUid(true));
    }

    /** */
    public void testIsPathUri() throws Exception {
        String uri = "/bcm/stuff/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertTrue("uri not flagged as path uri",
                   parser.isPathUri());
    }

    /** */
    public void testIsNotPathUri() throws Exception {
        String uri = "/collection/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertTrue("uri incorrectly flagged as path uri",
                   ! parser.isPathUri());
    }

    /** */
    public void testGetItemPath() throws Exception {
        String uri = "/bcm/stuff/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertNotNull("item path not found in item uri", parser.getItemPath());
    }

    /** */
    public void testGetItemPathOnNonPathUri() throws Exception {
        String uri = "/collection/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertNull("path incorrectly found in non-path uri",
                   parser.getItemPath());
    }

    /** */
    public void testGetPathSelector() throws Exception {
        String selector1 = "foobar";
        String selector2 = "bazquux";
        String uri =
            "/collection/deadbeef-cafebebe/" + selector1 + "/" + selector2;
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertTrue("path selector 1 not found",
                   parser.getPathSelector(selector1));
        assertTrue("path selector 2 not found",
                   parser.getPathSelector(selector2));
        assertFalse("bad path selector found",
                    parser.getPathSelector("not there"));
    }

    /** */
    public void testNoPathSelectors() throws Exception {
        String uri = "/collection/deadbeef-cafebebe";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertFalse("bad path selector found",
                    parser.getPathSelector("not there"));
    }

    /** */
    public void testNoPathSelectorsTrailingSlash() throws Exception {
        String uri = "/collection/deadbeef-cafebebe/";
        RepositoryUriParser parser = new RepositoryUriParser(uri);
        assertFalse("bad path selector found",
                    parser.getPathSelector("not there"));
    }
}
