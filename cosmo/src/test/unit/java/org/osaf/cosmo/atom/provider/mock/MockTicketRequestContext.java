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
package org.osaf.cosmo.atom.provider.mock;

import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.provider.TicketTarget;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;

/**
 * Mock implementation of {@link RequestContext} representing requests
 * to a collection's ticket feed.
 */
public class MockTicketRequestContext extends BaseMockRequestContext {
    private static final Log log =
        LogFactory.getLog(MockTicketRequestContext.class);

    public MockTicketRequestContext(Provider provider,
            CollectionItem collection,
            Ticket ticket) {
        this(provider, collection, ticket, "GET");
    }

    public MockTicketRequestContext(Provider context,
            CollectionItem collection,
            Ticket ticket,
            String method) {
        super(context, method, toRequestUri(collection, ticket));
        this.target = new TicketTarget(this, collection, ticket);
    }

    private static String toRequestUri(CollectionItem collection,
            Ticket ticket){
        return TEMPLATE_TICKET.bind(collection.getUid(), ticket.getKey());
    }
}
