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

/**
 * Test class for {@link ItemProvider#updateCollection()} tests.
 */
public class UpdateCollectionTest extends BaseItemCollectionAdapterTestCase
    implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(UpdateCollectionTest.class);

    public void testUpdateCollection() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        String oldName = collection.getName();
        CollectionItem copy = (CollectionItem) collection.copy();
        copy.setDisplayName("New Name");

        RequestContext req = createRequestContext(collection, copy);

        ResponseContext res = adapter.putCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
        assertEquals("Display name not updated", collection.getDisplayName(),
                     "New Name");
    }

    public void testUpdateCollectionNullName() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        CollectionItem copy = (CollectionItem) collection.copy();
        copy.setDisplayName(null);

        RequestContext req = createRequestContext(collection, copy);

        ResponseContext res = adapter.putCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testUpdateCollectionEmptyName() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        CollectionItem copy = (CollectionItem) collection.copy();
        copy.setDisplayName("");

        RequestContext req = createRequestContext(collection, copy);

        ResponseContext res = adapter.putCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public RequestContext createRequestContext(CollectionItem collection,
                                               CollectionItem update)
        throws Exception {
        MockCollectionRequestContext rc =
            new MockCollectionRequestContext(provider,
                                             collection, "PUT");
        if (update != null)
            rc.setContentAsXhtml(serialize(update));
        return rc;
    }
}
