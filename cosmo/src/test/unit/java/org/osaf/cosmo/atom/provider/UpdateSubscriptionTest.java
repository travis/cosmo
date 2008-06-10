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
import org.osaf.cosmo.atom.provider.mock.MockSubscriptionRequestContext;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.mock.MockCollectionSubscription;

/**
 * Test class for {@link SubscriptionProvider#updateEntry()} tests.
 */
public class UpdateSubscriptionTest extends BaseSubscriptionCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(UpdateSubscriptionTest.class);

    public void testUpdateEntry() throws Exception {
        MockCollectionSubscription sub = (MockCollectionSubscription) helper.makeAndStoreDummySubscription();
        String oldName = sub.getDisplayName();
        MockCollectionSubscription newsub = (MockCollectionSubscription) newSubscription();
        RequestContext req = createRequestContext(sub, newsub);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertFalse("Matching etags",
                    sub.calculateEntityTag().equals(newsub.calculateEntityTag()));
        assertNotNull("Null last modified", res.getLastModified());

        String username = helper.getUser().getUsername();
        CollectionSubscription saved = helper.getUserService().
            getUser(username).getSubscription(oldName);
        assertNull("Subscription under old name still around", saved);

        saved = helper.getUserService().getUser(username).
            getSubscription(newsub.getDisplayName());
        assertNotNull("Subscription not found under new name", saved);

        assertEquals("Wrong collection uid", newsub.getCollectionUid(),
                     saved.getCollectionUid());
        assertEquals("Wrong ticket key", newsub.getTicketKey(),
                     saved.getTicketKey());
    }

    public void testGenerationError() throws Exception {
        CollectionSubscription sub =
            helper.makeDummySubscription(helper.getUser());
        CollectionSubscription newsub = newSubscription();
        RequestContext req = createRequestContext(sub, newsub);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    public void testEntryExists() throws Exception {
        CollectionSubscription sub1 = helper.makeAndStoreDummySubscription();
        CollectionSubscription sub2 = helper.makeAndStoreDummySubscription();
        CollectionSubscription newsub = newSubscription();
        newsub.setDisplayName(sub2.getDisplayName());
        RequestContext req = createRequestContext(sub1, newsub);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
    }

    public void testNoTicketKey() throws Exception {
        CollectionSubscription sub =
            helper.makeDummySubscription(helper.getUser());
        CollectionSubscription newsub = new MockCollectionSubscription();
        newsub.setCollectionUid("deadbeef");
        RequestContext req = createRequestContext(sub, newsub);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testNoCollectionUid() throws Exception {
        CollectionSubscription sub =
            helper.makeDummySubscription(helper.getUser());
        CollectionSubscription newsub = new MockCollectionSubscription();
        newsub.setTicketKey("deadbeef");
        RequestContext req = createRequestContext(sub, newsub);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    private CollectionSubscription newSubscription() {
        CollectionSubscription sub = new MockCollectionSubscription();
        sub.setDisplayName("new display name");
        sub.setCollectionUid("new uid");
        sub.setTicketKey("new key");
        return sub;
    }

    private RequestContext createRequestContext(CollectionSubscription sub,
                                                CollectionSubscription newsub)
        throws Exception {
        MockSubscriptionRequestContext rc =
            new MockSubscriptionRequestContext(provider,
                                               helper.getUser(), sub, "PUT");
        rc.setXhtmlContentAsEntry(serialize(newsub));
        return rc;
    }
}
