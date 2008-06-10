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

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.atom.provider.mock.MockTicketRequestContext;

/**
 * Test class for {@link TicketProvider#getEntry()} tests.
 */
public class GetTicketTest extends BaseTicketsCollectionAdapterTestCase {
    private static final Log log = LogFactory.getLog(GetTicketTest.class);

    public void testGetTicketEntry() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        Ticket ticket = helper.makeAndStoreDummyTicket(collection);
        RequestContext req = newRequestContext(collection, ticket);

        ResponseContext res = adapter.getEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
    }

    public void testGenerationError() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        Ticket ticket = helper.makeAndStoreDummyTicket(collection);
        RequestContext req = newRequestContext(collection, ticket);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.getEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    private RequestContext newRequestContext(CollectionItem collection, Ticket ticket) {
        return new MockTicketRequestContext(provider,
                                                collection, ticket);
    }
}
