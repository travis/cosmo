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
import org.osaf.cosmo.atom.provider.TicketsTarget;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Mock implementation of {@link RequestContext} representing requests
 * to the ticket feed of a collection.
 */
public class MockTicketsRequestContext extends BaseMockRequestContext {
    private static final Log log =
        LogFactory.getLog(MockTicketsRequestContext.class);

    public MockTicketsRequestContext(Provider provider,
                                        CollectionItem collection) {
        this(provider, collection, "GET");
    }

    public MockTicketsRequestContext(Provider provider,
                                        CollectionItem collection,
                                        String method) {
        super(provider, method, toRequestUri(collection));
        this.target = new TicketsTarget(this, collection);
    }

    private static String toRequestUri(CollectionItem collection){
        return TEMPLATE_TICKETS.bind(collection.getUid());
    }
}
