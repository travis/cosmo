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
 * Test class for {@link ItemProvider#updateMedia()} tests.
 */
public class UpdateItemMediaTest extends BaseItemCollectionAdapterTestCase {
    private static final Log log = LogFactory.getLog(UpdateItemMediaTest.class);

    public void testUpdateMedia() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        RequestContext req = createRequestContext(item, copy);
        log.error("content-type: " + req.getContentType());

        ResponseContext res = adapter.putMedia(req);
        log.error(helper.getContent(res));
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
    }

    public void testUnsupportedMediaType() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        RequestContext req = createRequestContext(item, copy);
        helper.forgetContentTypes();

        ResponseContext res = adapter.putMedia(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 415, res.getStatus());
    }

    public void testInvalidContent() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        RequestContext req = createRequestContext(item, copy);
        helper.enableProcessorValidationError();

        ResponseContext res = adapter.putMedia(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testProcessingError() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        RequestContext req = createRequestContext(item, copy);
        helper.enableProcessorFailure();

        ResponseContext res = adapter.putMedia(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberContentType("text/plain");
    }

    private RequestContext createRequestContext(NoteItem original,
                                                NoteItem update)
        throws Exception {
        MockItemRequestContext rc =
            new MockItemRequestContext(provider, original,
                                       "PUT");
        rc.setPropertiesAsText(serialize(update));
        return rc;
    }
}
