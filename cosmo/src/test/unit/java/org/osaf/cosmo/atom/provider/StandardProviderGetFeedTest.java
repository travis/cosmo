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

import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;

/**
 * Test class for {@link StandardProvider#getFeed()} tests.
 */
public class StandardProviderGetFeedTest extends BaseProviderTestCase {
    private static final Log log =
        LogFactory.getLog(StandardProviderGetFeedTest.class);

    public void testGetFeed() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET");

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        // XXX look at content?
    }

    public void testNotFound() throws Exception {
        RequestContext req = helper.createFeedRequestContext("deadbeef", "GET");

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 404, res.getStatus());
    }

    public void testUnsupportedProjection() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", null);

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 404, res.getStatus());
    }

    public void testUnsupportedFormat() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "full", "yyz");

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 404, res.getStatus());
    }

    public void testGenerationError() throws Exception {
        // XXX
    }
}
