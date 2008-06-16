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

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.provider.mock.MockCollectionRequestContext;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test class for {@link ItemProvider#addItemToCollection()} tests.
 */
public class AddItemToCollectionTest extends BaseItemCollectionAdapterTestCase
    implements AtomConstants {
    private static final Log log = LogFactory.getLog(AddItemToCollectionTest.class);

    public void testAddItemToCollection() throws Exception {
        CollectionItem collection1 = helper.makeAndStoreDummyCollection();
        CollectionItem collection2 = helper.makeAndStoreDummyCollection();
        NoteItem item = helper.makeAndStoreDummyItem(collection1);
        RequestContext req = createRequestContext(collection2, item.getUid());

        ResponseContext res = ((ItemCollectionAdapter)adapter).addItemToCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertTrue("item not in collection 1", collection1.getChildren().contains(item));
        assertTrue("item not in collection 2", collection2.getChildren().contains(item));
    }

    public void testNoUuid() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection, null);

        ResponseContext res = ((ItemCollectionAdapter)adapter).addItemToCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 411, res.getStatus());
    }

    public void testBadUuid() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection, "garbage");

        ResponseContext res = ((ItemCollectionAdapter)adapter).addItemToCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testNonNoteUuid() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        RequestContext req = createRequestContext(collection, collection.getUid());

        ResponseContext res = ((ItemCollectionAdapter)adapter).addItemToCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void DONTtestLockedError() throws Exception {
        // XXX re-enable when i figure out how to lock the collection
        // from another thread
        CollectionItem collection1 = helper.makeAndStoreDummyCollection();
        CollectionItem collection2 = helper.makeAndStoreDummyCollection();
        NoteItem item = helper.makeAndStoreDummyItem(collection1);
        RequestContext req = createRequestContext(collection2, item.getUid());
        helper.lockCollection(collection2);

        ResponseContext res = ((ItemCollectionAdapter)adapter).addItemToCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 423, res.getStatus());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberContentType(MEDIA_TYPE_URLENCODED);
    }

    private RequestContext createRequestContext(CollectionItem collection,
                                                String uid)
        throws Exception {
        MockCollectionRequestContext rc =
            new MockCollectionRequestContext(provider,
                                             collection, "POST");
        if (uid != null)
            rc.setContent("uuid=" + uid, MEDIA_TYPE_URLENCODED);
        
        rc.setParameter("uuid", uid);
        
        return rc;
    }
}
