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

import org.apache.abdera.model.Content;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test class for {@link ItemProvider#createEntry()} tests.
 */
public class CreateItemTest extends BaseItemProviderTestCase
    implements AtomConstants {
    private static final Log log = LogFactory.getLog(CreateItemTest.class);

    public void testCreateEntry() throws Exception {
        CollectionItem item = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(item, "POST");

        ResponseContext res = provider.createEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null Location header", res.getHeader("Location"));
        assertNotNull("Null Content-Location header",
                      res.getHeader("Content-Location"));
    }

    public void testUnsupportedMediaType() throws Exception {
        CollectionItem item = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(item, "POST");
        helper.forgetMediaTypes();

        ResponseContext res = provider.createEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 415, res.getStatus());
    }

    public void testInvalidContent() throws Exception {
        CollectionItem item = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(item, "POST");
        helper.enableProcessorValidationError();

        ResponseContext res = provider.createEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testProcessingError() throws Exception {
        CollectionItem item = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(item, "POST");
        helper.enableProcessorFailure();

        ResponseContext res = provider.createEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    public void DONTtestLockedError() throws Exception {
        // XXX re-enable when i figure out how to lock the collection
        // from another thread
        CollectionItem item = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(item, "POST");
        helper.lockCollection(item);

        ResponseContext res = provider.createEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 423, res.getStatus());
    }

    public void testGenerationError() throws Exception {
        CollectionItem item = helper.makeAndStoreDummyCollection();
        RequestContext req = helper.createFeedRequestContext(item, "POST");
        helper.enableGeneratorFailure();

        ResponseContext res = provider.createEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberProjection(PROJECTION_FULL);
        helper.rememberMediaType(Content.Type.TEXT.toString());
    }
}
