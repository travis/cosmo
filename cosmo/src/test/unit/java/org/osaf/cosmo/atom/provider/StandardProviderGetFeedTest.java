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
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.Ticket;

/**
 * Test class for {@link StandardProvider#getFeed()} tests.
 */
public class StandardProviderGetFeedTest extends BaseProviderTestCase {
    private static final Log log =
        LogFactory.getLog(StandardProviderGetFeedTest.class);

    public void testGetCollectionFeed() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", "eff");

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
    }

    public void testGetSubscriptionFeed() throws Exception {
        RequestContext req = helper.createSubscribedRequestContext();

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        // subscribed feeds have no etag
        assertNull("Invalid etag", res.getEntityTag());
    }

    public void testUnsupportedProjection() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", "eff");
        helper.forgetProjections();

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
        log.error(helper.getContent(res));
    }

    public void testUnsupportedFormat() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", "eff");
        helper.forgetFormats();

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
        log.error(helper.getContent(res));
    }

    public void testGenerationError() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(collection, "GET",
                                                             "yyz", "eff");
        helper.enableGeneratorFailure();

        ResponseContext res = provider.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
        log.error(helper.getContent(res));
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberProjection("yyz");
        helper.rememberFormat("eff");
    }
}
