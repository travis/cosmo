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
package org.osaf.cosmo.atom.servlet;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomHelper;
import org.osaf.cosmo.atom.provider.mock.MockProvider;
import org.osaf.cosmo.model.CollectionItem;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test class for {@link StandardRequestHandler}.
 */
public class StandardRequestHandlerTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(StandardRequestHandlerTest.class);

    private AtomHelper helper;
    private StandardRequestHandler handler;

    public void testIfMatchAll() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", "eff");
        helper.setIfMatch(req, "*");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean rv = handler.preconditions(helper.getProvider(), req, res);
        assertTrue("Preconditions failed", rv);
        assertEquals("Incorrect response status", 200, res.getStatus());
    }

    public void testIfMatchOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", "eff");
        helper.setIfMatch(req, collection);
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean rv = handler.preconditions(helper.getProvider(), req, res);
        assertTrue("Preconditions failed", rv);
        assertEquals("Incorrect response status", 200, res.getStatus());
    }

    public void testIfMatchNotOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", "eff");
        helper.setIfMatch(req, "aeiou");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean rv = handler.preconditions(helper.getProvider(), req, res);
        assertFalse("Preconditions passed", rv);
        assertEquals("Incorrect response status", 412, res.getStatus());
        assertNotNull("Null ETag header", res.getHeader("ETag"));
    }

    public void testIfNoneMatchAll() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET");
        helper.setIfNoneMatch(req, "*");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean rv = handler.preconditions(helper.getProvider(), req, res);
        assertFalse("Preconditions passed", rv);
        assertEquals("Incorrect response status", 304, res.getStatus());
        assertNotNull("Null ETag header", res.getHeader("ETag"));
    }

    public void testIfNoneMatchNotOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET");
        helper.setIfNoneMatch(req, collection);
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean rv = handler.preconditions(helper.getProvider(), req, res);
        assertFalse("Preconditions passed", rv);
        assertEquals("Incorrect response status", 304, res.getStatus());
        assertNotNull("Null ETag header", res.getHeader("ETag"));
    }

    public void testIfNoneMatchOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET");
        helper.setIfNoneMatch(req, "aeiou");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean rv = handler.preconditions(helper.getProvider(), req, res);
        assertTrue("Preconditions failed", rv);
        assertEquals("Incorrect response status", 200, res.getStatus());
    }

    public void testProcessCollectionUpdate() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createUpdateRequestContext(collection);
        helper.setContentType(req, "application/x-www-form-urlencoded");

        ResponseContext res = handler.process(helper.getProvider(), req);
        assertNotNull("Null response context", res);
        assertTrue("Collection not updated",
                   ((MockProvider)helper.getProvider()).isUpdated(collection));
    }

    protected void setUp() throws Exception {
        helper = new AtomHelper();
        helper.setUp();

        handler = new StandardRequestHandler();
    }

    protected void tearDown() throws Exception {
        helper.tearDown();
    }
}
