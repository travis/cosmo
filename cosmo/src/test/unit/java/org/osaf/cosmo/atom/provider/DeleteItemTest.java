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
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.mock.MockCollectionItem;

/**
 * Test class for {@link ItemProvider#deleteEntry()} tests.
 */
public class DeleteItemTest extends BaseItemCollectionAdapterTestCase {
    private static final Log log = LogFactory.getLog(DeleteItemTest.class);

    public void testDeleteEntry() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        RequestContext req = createRequestContext(item);

        ResponseContext res = adapter.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNull("Item not deleted", helper.findItem(item.getUid()));
    }

    public void testRemoveEntry() throws Exception {
        // store an item in two collections
        CollectionItem collection1 = helper.makeAndStoreDummyCollection();
        CollectionItem collection2 = helper.makeAndStoreDummyCollection();
        NoteItem item = helper.makeAndStoreDummyItem(collection1);
        ((MockCollectionItem) collection2).addChild(item);
        helper.getContentService().updateContent(item);
        helper.getContentService().updateCollection(collection2);

        RequestContext req = createRequestContext(item);
        helper.addParameter(req, "uuid", collection2.getUid());

        ResponseContext res = adapter.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNotNull("Item incorrectly removed",
                      helper.findItem(item.getUid()));
        assertNotNull("Item not in collection 1",
                      collection1.getChild(item.getUid()));
        assertNull("Item in collection 2",
                   collection2.getChild(item.getUid()));
    }

    public void testRemoveEntryCollectionNotFound() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();

        RequestContext req = createRequestContext(item);
        helper.addParameter(req, "uuid", "deadbeef");

        ResponseContext res = adapter.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
        assertNotNull("Item incorrectly removed",
                      helper.findItem(item.getUid()));
    }

    public void testRemoveEntryNotACollection() throws Exception {
        NoteItem item1 = helper.makeAndStoreDummyItem();
        NoteItem item2 = helper.makeAndStoreDummyItem();

        RequestContext req = createRequestContext(item1);
        helper.addParameter(req, "uuid", item2.getUid());

        ResponseContext res = adapter.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
        assertNotNull("Item incorrectly removed",
                      helper.findItem(item1.getUid()));
    }

    public RequestContext createRequestContext(NoteItem item) {
        return new MockItemRequestContext(provider, item,
                                          "DELETE");
    }
}
