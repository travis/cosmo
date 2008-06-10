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

import org.osaf.cosmo.atom.provider.mock.MockItemRequestContext;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test class for {@link ItemProvider#getEntry()} tests.
 */
public class GetItemTest extends BaseItemCollectionAdapterTestCase {
    private static final Log log = LogFactory.getLog(GetItemTest.class);

    public void testGetEntry() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item);

        ResponseContext res = adapter.getEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
    }

    public void testUnsupportedProjection() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item);
        helper.forgetProjections();

        ResponseContext res = adapter.getEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testUnsupportedFormat() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item);
        helper.forgetFormats();

        ResponseContext res = adapter.getEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testGenerationError() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.getEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberProjection("yyz");
        helper.rememberFormat("eff");
    }

    private RequestContext createRequestContext(NoteItem item) {
        return new MockItemRequestContext(provider, item,
                                          "GET", "yyz", "eff");
    }
}
