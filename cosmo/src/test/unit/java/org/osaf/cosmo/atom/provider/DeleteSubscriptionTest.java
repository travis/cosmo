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

/**
 * Test class for {@link SubscriptionProvider#deleteEntry()} tests.
 */
public class DeleteSubscriptionTest
    extends BaseSubscriptionCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(DeleteSubscriptionTest.class);

    public void testDeleteSubscriptionEntry() throws Exception {
        CollectionSubscription sub = helper.makeAndStoreDummySubscription();
        RequestContext req = createRequestContext(sub);

        ResponseContext res = adapter.deleteEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 204, res.getStatus());
        assertNull("Subscription not removed",
                   helper.findSubscription(sub.getDisplayName()));
    }

    private RequestContext createRequestContext(CollectionSubscription sub) {
        return new MockSubscriptionRequestContext(provider,
                                                  helper.getUser(), sub,
                                                  "DELETE");
    }
}
