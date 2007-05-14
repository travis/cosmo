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

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test class for {@link StandardProvider#updateCollection()} tests.
 */
public class StandardProviderUpdateCollectionTest extends BaseProviderTestCase
    implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardProviderUpdateCollectionTest.class);

    public void testUpdateCollection() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        String oldName = collection.getName();
        String newName = "New Name";

        RequestContext req = helper.createUpdateRequestContext(collection);
        helper.addParameter(req, "name", newName);

        ResponseContext res = provider.updateCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertEquals("Display name not updated", collection.getName(), newName);
    }

    public void testUpdateCollectionNoName() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        String oldName = collection.getName();

        RequestContext req = helper.createUpdateRequestContext(collection);

        ResponseContext res = provider.updateCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertTrue("Display name changed",
                   collection.getName().equals(oldName));
    }

    public void testUpdateCollectionNullName() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        String oldName = collection.getName();

        RequestContext req = helper.createUpdateRequestContext(collection);
        helper.addParameter(req, "name", null);

        ResponseContext res = provider.updateCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertTrue("Display name changed",
                   collection.getName().equals(oldName));
    }

    public void testUpdateCollectionEmptyName() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        String oldName = collection.getName();

        RequestContext req = helper.createUpdateRequestContext(collection);
        helper.addParameter(req, "name", "");

        ResponseContext res = provider.updateCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertTrue("Display name changed",
                   collection.getName().equals(oldName));
    }
}
