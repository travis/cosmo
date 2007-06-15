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
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.provider.mock.MockItemRequestContext;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test class for {@link ItemProvider#updateEntry()} tests.
 */
public class UpdateItemTest extends BaseItemProviderTestCase
    implements AtomConstants {
    private static final Log log = LogFactory.getLog(UpdateItemTest.class);

    public void testUpdateEntry() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setName("this is a new name");
        RequestContext req = createRequestContext(item, copy);

        ResponseContext res = provider.updateEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
    }

    public void testUnsupportedMediaType() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setName("this is a new name");
        RequestContext req = createRequestContext(item, copy);
        helper.forgetMediaTypes();

        ResponseContext res = provider.updateEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 415, res.getStatus());
    }

    public void testInvalidContent() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setName("this is a new name");
        RequestContext req = createRequestContext(item, copy);
        helper.enableProcessorValidationError();

        ResponseContext res = provider.updateEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testUidInUse() throws Exception {
        // XXX our mock dao can't figure out that item2's uid has been
        // changed, so it isn't aware that the update request is
        // addressed to item1
        NoteItem item1 = helper.makeAndStoreDummyItem();
        NoteItem item2 = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item2.copy();
        copy.setName("this is a new name");
        copy.setUid(item1.getUid());
        RequestContext req = createRequestContext(item2, copy);

        ResponseContext res = provider.updateEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
    }

    public void testProcessingError() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setName("this is a new name");
        RequestContext req = createRequestContext(item, copy);
        helper.enableProcessorFailure();

        ResponseContext res = provider.updateEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberMediaType(Content.Type.TEXT.toString());
        helper.rememberProjection(PROJECTION_FULL);
    }

    private RequestContext createRequestContext(NoteItem original,
                                                NoteItem update)
        throws Exception {
        MockItemRequestContext rc =
            new MockItemRequestContext(helper.getServiceContext(), original,
                                       "PUT");
        rc.setContentAsEntry(update.getUid());
        return rc;
    }
}
