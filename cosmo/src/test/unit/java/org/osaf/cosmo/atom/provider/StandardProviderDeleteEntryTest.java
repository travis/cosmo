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

import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test class for {@link StandardProvider#deleteEntry()} tests.
 */
public class StandardProviderDeleteEntryTest extends BaseProviderTestCase {
    private static final Log log =
        LogFactory.getLog(StandardProviderDeleteEntryTest.class);

    public void testDeleteEntry() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = helper.createEntryRequestContext(item, "DELETE");

        ResponseContext res = provider.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNull("Item not removed", helper.findItem(item.getUid()));
    }

    public void testIfMatchAll() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = helper.createEntryRequestContext(item, "DELETE");
        helper.setIfMatch(req, "*");

        ResponseContext res = provider.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNull("Item not removed", helper.findItem(item.getUid()));
    }

    public void testIfMatchOk() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = helper.createEntryRequestContext(item, "DELETE");
        helper.setIfMatch(req, item);

        ResponseContext res = provider.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNull("Item not removed", helper.findItem(item.getUid()));
    }

    public void testIfMatchNotOk() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = helper.createEntryRequestContext(item, "DELETE");
        helper.setIfMatch(req, "aeiou");

        ResponseContext res = provider.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 412, res.getStatus());
        assertNotNull("Item incorrectly removed",
                      helper.findItem(item.getUid()));
    }

    public void testIfNoneMatchAll() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = helper.createEntryRequestContext(item, "DELETE");
        helper.setIfNoneMatch(req, "*");

        ResponseContext res = provider.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 412, res.getStatus());
        assertNotNull("Item incorrectly removed",
                      helper.findItem(item.getUid()));
    }

    public void testIfNoneMatchNotOk() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = helper.createEntryRequestContext(item, "DELETE");
        helper.setIfNoneMatch(req, item);

        ResponseContext res = provider.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 412, res.getStatus());
        assertNotNull("Item incorrectly removed",
                      helper.findItem(item.getUid()));
    }

    public void testIfNoneMatchOk() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = helper.createEntryRequestContext(item, "DELETE");
        helper.setIfNoneMatch(req, "aeiou");

        ResponseContext res = provider.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNull("Item not removed", helper.findItem(item.getUid()));
    }
}
