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
import org.osaf.cosmo.atom.provider.mock.MockServiceRequestContext;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test class for {@link ItemProvider#createCollection()} tests.
 */
public class CreateCollectionTest extends BaseItemCollectionAdapterTestCase
    implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(CreateCollectionTest.class);

    public void testCreateCollection() throws Exception {
        CollectionItem collection =
            helper.makeDummyCalendarCollection(helper.getUser());

        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
        assertNotNull("Null Location header", res.getLocation());
        assertNull("Non-null Content-Location header", res.getContentLocation());

        CollectionItem stored = helper.findCollection(collection.getUid());
        assertNotNull("Collection not stored", stored);
        assertEquals("Incorrect uid", collection.getUid(), stored.getUid());
        assertEquals("Incorrect name", collection.getUid(), stored.getName());
        assertEquals("Incorrect display name", collection.getDisplayName(), stored.getDisplayName());
        assertEquals("Incorrect owner", collection.getOwner(), stored.getOwner());
    }    

    public void testInsaneName() throws Exception {
        CollectionItem collection =
            helper.makeDummyCalendarCollection(helper.getUser());
        collection.setDisplayName("test / ? # ; = name");

        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
        assertNotNull("Null Location header", res.getLocation());
        assertNull("Non-null Content-Location header", res.getContentLocation());

        CollectionItem stored = helper.findCollection(collection.getUid());
        assertNotNull("Collection not stored", stored);
        assertEquals("Incorrect name", collection.getUid(), stored.getName());
        assertEquals("Incorrect display name", collection.getDisplayName(), stored.getDisplayName());
    }

    public void testNullName() throws Exception {
        CollectionItem collection =
            helper.makeDummyCalendarCollection(helper.getUser());
        collection.setDisplayName(null);

        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testEmptyName() throws Exception {
        CollectionItem collection =
            helper.makeDummyCalendarCollection(helper.getUser());
        collection.setDisplayName("");

        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testNullUuid() throws Exception {
        CollectionItem collection =
            helper.makeDummyCalendarCollection(helper.getUser());
        collection.setUid(null);

        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
        assertNotNull("Null Location header", res.getLocation());
        assertNull("Non-null Content-Location header", res.getContentLocation());
    }

    public void testEmptyUuid() throws Exception {
        CollectionItem collection =
            helper.makeDummyCalendarCollection(helper.getUser());
        collection.setUid("");

        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testDuplicateUuid() throws Exception {
        CollectionItem original =
            helper.makeAndStoreDummyCalendarCollection();
        CollectionItem collection =
            helper.makeDummyCalendarCollection(helper.getUser());
        collection.setUid(original.getUid());

        RequestContext req = createRequestContext(collection);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
    }

    public RequestContext createRequestContext(CollectionItem collection)
        throws Exception {
        MockServiceRequestContext rc =
            new MockServiceRequestContext(provider,
                                          helper.getUser(),
                                          helper.getHomeCollection());
        if (collection != null)
            rc.setContentAsXhtml(serialize(collection));
        return rc;
    }
}
