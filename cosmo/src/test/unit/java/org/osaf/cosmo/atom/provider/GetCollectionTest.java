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

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.provider.mock.MockCollectionRequestContext;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test class for {@link ItemProvider#getFeed()} tests.
 */
public class GetCollectionTest extends BaseItemCollectionAdapterTestCase {
    private static final Log log = LogFactory.getLog(GetCollectionTest.class);

    public void testGetFeed() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
    }

    public void testUnsupportedProjection() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.forgetProjections();

        ResponseContext res = adapter.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testUnsupportedFormat() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.forgetFormats();

        ResponseContext res = adapter.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testGenerationError() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberProjection("yyz");
        helper.rememberFormat("eff");
    }

    public RequestContext createRequestContext(CollectionItem collection) {
        return new MockCollectionRequestContext(provider,
                                                collection, "GET", "yyz",
                                                "eff");
    }
}
