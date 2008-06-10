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
import org.osaf.cosmo.atom.provider.mock.MockExpandedRequestContext;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test class for {@link ExpandedItemProvider#getFeed()} tests.
 */
public class GetExpandedItemTest extends BaseExpandedItemCollectionAdapterTestCase {
    private static final Log log = LogFactory.getLog(GetExpandedItemTest.class);

    public void testGetFeed() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item, "yyz", "eff");

        ResponseContext res = adapter.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
    }

    public void testUnsupportedProjection() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item, "yyz", "eff");
        helper.forgetProjections();

        ResponseContext res = adapter.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testUnsupportedFormat() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item, "yyz", "eff");
        helper.forgetFormats();

        ResponseContext res = adapter.getFeed(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testGenerationError() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item, "yyz", "eff");
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

    private RequestContext createRequestContext(NoteItem item,
                                                String projection,
                                                String format) {
        return createRequestContext(item, projection, format, true);
    }

    private RequestContext createRequestContext(NoteItem item,
                                                String projection,
                                                String format,
                                                boolean withQueryParams) {
        MockExpandedRequestContext rc =
            new MockExpandedRequestContext(provider, item,
                                           projection, format);
        if (withQueryParams) {
            helper.addParameter(rc, "start", "2007-01-01T00:00:00Z");
            helper.addParameter(rc, "end", "2007-01-31T23:59:59Z");
        }
        return rc;
    }
}
