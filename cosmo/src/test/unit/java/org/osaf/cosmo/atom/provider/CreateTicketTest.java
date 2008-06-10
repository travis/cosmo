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

import org.osaf.cosmo.atom.provider.mock.MockTicketsRequestContext;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.TicketType;
import org.osaf.cosmo.model.hibernate.HibTicket;

/**
 * Test class for {@link TicketProvider#createEntry()} tests.
 */
public class CreateTicketTest extends BaseTicketsCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(CreateTicketTest.class);

    public void testCreateEntry() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        Ticket ticket = helper.makeDummyTicket();
        ticket.setKey("createEntryTicket");
        
        RequestContext req = createRequestContext(collection, ticket);

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
        Ticket saved = helper.getContentService().
            getTicket(collection, ticket.getKey());
        assertNotNull("Ticket not saved", saved);
        assertEquals("Wrong key", ticket.getKey(), saved.getKey());
        assertEquals("Wrong type", ticket.getType(), saved.getType());
    }

    public void testGenerationError() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        Ticket ticket = helper.makeDummyTicket();
        ticket.setKey("generationTicket");
        RequestContext req = createRequestContext(collection, ticket);
        helper.enableGeneratorFailure();

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    public void testEntryExists() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        Ticket ticket = helper.makeAndStoreDummyTicket(collection);
        RequestContext req = createRequestContext(collection, ticket);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 409, res.getStatus());
    }

    public void testInvalidEntryNoKey() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        Ticket ticket = new HibTicket(TicketType.READ_ONLY);
        RequestContext req = createRequestContext(collection, ticket);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testInvalidEntryNoType() throws Exception {
        CollectionItem collection = helper.makeAndStoreDummyCollection();
        Ticket ticket = new HibTicket();
        ticket.setKey("invalidEntryNoTypeTicket");
        ticket.setKey("foo");
        RequestContext req = createRequestContext(collection, ticket);

        ResponseContext res = adapter.postEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    private RequestContext createRequestContext(CollectionItem collection, Ticket ticket)
        throws Exception {
        MockTicketsRequestContext rc =
            new MockTicketsRequestContext(provider,
                    collection, "POST");
        rc.setXhtmlContentAsEntry(serialize(ticket));
        return rc;
    }
}
