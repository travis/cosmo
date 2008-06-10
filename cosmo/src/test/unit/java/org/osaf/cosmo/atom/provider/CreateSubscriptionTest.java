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

import org.osaf.cosmo.atom.provider.mock.MockSubscriptionsRequestContext;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.hibernate.HibCollectionSubscription;

/**
 * Test class for {@link SubscriptionProvider#createEntry()} tests.
 */
public class CreateSubscriptionTest extends BaseSubscriptionCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(CreateSubscriptionTest.class);

    public void testCreateEntry() throws Exception {
        CollectionSubscription sub =
            helper.makeDummySubscription(helper.getUser());
        RequestContext req = createRequestContext(sub);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        // disable last modified check til mock dao bumps modified date
        //assertNotNull("Null last modified", res.getLastModified());
        assertNotNull("Null Location header", res.getHeader("Location"));
        assertNotNull("Null Content-Location header",
                      res.getHeader("Content-Location"));

        String username = helper.getUser().getUsername();
        CollectionSubscription saved = helper.getUserService().
            getUser(username).getSubscription(sub.getDisplayName());
        assertNotNull("Subscription not saved", saved);
        assertEquals("Wrong display name", sub.getDisplayName(),
                     saved.getDisplayName());
        assertEquals("Wrong collection uid", sub.getCollectionUid(),
                     saved.getCollectionUid());
        assertEquals("Wrong ticket key", sub.getTicketKey(),
                     saved.getTicketKey());
    }

    public void testGenerationError() throws Exception {
        CollectionSubscription sub =
            helper.makeDummySubscription(helper.getUser());
        RequestContext req = createRequestContext(sub);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    public void testEntryExists() throws Exception {
        CollectionSubscription sub = helper.makeAndStoreDummySubscription();
        RequestContext req = createRequestContext(sub);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
    }

    public void testNoTicketKey() throws Exception {
        CollectionSubscription sub = new HibCollectionSubscription();
        sub.setCollectionUid("deadbeef");
        RequestContext req = createRequestContext(sub);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testNoCollectionUid() throws Exception {
        CollectionSubscription sub = new HibCollectionSubscription();
        sub.setTicketKey("deadbeef");
        RequestContext req = createRequestContext(sub);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    private RequestContext createRequestContext(CollectionSubscription sub)
        throws Exception {
        MockSubscriptionsRequestContext rc =
            new MockSubscriptionsRequestContext(provider,
                                                helper.getUser(), "POST");
        rc.setXhtmlContentAsEntry(serialize(sub));
        return rc;
    }
}
