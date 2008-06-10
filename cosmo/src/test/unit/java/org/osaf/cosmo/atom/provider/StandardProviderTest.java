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
package org.osaf.cosmo.atom.provider;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomHelper;
import org.osaf.cosmo.atom.provider.mock.MockCollectionRequestContext;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test class for {@link StandardProvider}.
 */
public class StandardProviderTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(StandardProviderTest.class);

    private AtomHelper helper;
    private StandardProvider provider;

    public void testIfMatchAll() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.setIfMatch(req, "*");
       
        ResponseContext rc = provider.preconditions(req);
        assertNull("Preconditions failed", rc);
    }

    public void testIfMatchOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.setIfMatch(req, collection);
       
        ResponseContext rc = provider.preconditions(req);
        assertNull("Preconditions failed", rc);
    }

    public void testIfMatchNotOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.setIfMatch(req, "aeiou");
      
        ResponseContext rc = provider.preconditions(req);
        assertNotNull("Preconditions passed", rc);
        assertEquals("Incorrect response status", 412, rc.getStatus());
        assertNotNull("Null ETag header", rc.getEntityTag());
    }

    public void testIfNoneMatchAll() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.setIfNoneMatch(req, "*");
       
        ResponseContext rc = provider.preconditions(req);
        assertNotNull("Preconditions passed", rc);
        assertEquals("Incorrect response status", 304, rc.getStatus());
        assertNotNull("Null ETag header", rc.getEntityTag());
    }

    public void testIfNoneMatchNotOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.setIfNoneMatch(req, collection);
       
        ResponseContext rc = provider.preconditions(req);
        assertNotNull("Preconditions passed", rc);
        assertEquals("Incorrect response status", 304, rc.getStatus());
        assertNotNull("Null ETag header", rc.getEntityTag());
    }

    public void testIfNoneMatchOk() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.setIfNoneMatch(req, "aeiou");
      
        ResponseContext rc = provider.preconditions(req);
        assertNull("Preconditions failed", rc);
    }

    public void testIfModifiedSinceAfter() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        Date date = new Date(System.currentTimeMillis()-1000000);
        helper.setIfModifiedSince(req, date);
        
        ResponseContext rc = provider.preconditions(req);
        assertNull("Preconditions failed", rc);
    }

    public void testIfModifiedSinceBefore() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        Date date = new Date(System.currentTimeMillis()+1000000);
        helper.setIfModifiedSince(req, date);
        ResponseContext rc = provider.preconditions(req);
        
        assertNotNull("Preconditions failed", rc);
        assertEquals("Incorrect response status", 304, rc.getStatus());
    }

    public void testIfUnmodifiedSinceAfter() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        Date date = new Date(System.currentTimeMillis()-1000000);
        helper.setIfUnmodifiedSince(req, date);
        ResponseContext rc = provider.preconditions(req);
        
        assertNotNull("Preconditions failed", rc);
        assertEquals("Incorrect response status", 412, rc.getStatus());
    }

    public void testIfUnmodifiedSinceBefore() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        Date date = new Date(System.currentTimeMillis()+1000000);
        helper.setIfUnmodifiedSince(req, date);
        ResponseContext rc = provider.preconditions(req);
        
        assertNull("Preconditions failed", rc);
    }

    protected void setUp() throws Exception {
        helper = new AtomHelper();
        helper.setUp();

        provider = new StandardProvider();
        provider.init(new Abdera(), null);
    }

    protected void tearDown() throws Exception {
        helper.tearDown();
    }

    public RequestContext createRequestContext(CollectionItem collection) {
        return new MockCollectionRequestContext(provider,
                                                collection, "GET", "yyz",
                                                "eff");
    }

    public RequestContext createPutRequestContext(CollectionItem collection) {
        return new MockCollectionRequestContext(provider,
                                                collection, "PUT");
    }
}
