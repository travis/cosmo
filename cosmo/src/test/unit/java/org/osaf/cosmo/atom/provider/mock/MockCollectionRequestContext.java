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
import org.apache.abdera.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.provider.CollectionTarget;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;

/**
 * Mock implementation of {@link RequestContext}.
 */
public class MockCollectionRequestContext extends BaseMockRequestContext
    implements Constants {
    private static final Log log =
        LogFactory.getLog(MockCollectionRequestContext.class);

    public MockCollectionRequestContext(Provider provider,
                                        CollectionItem collection,
                                        String method) {
        this(provider, collection, method, null, null);
    }

    public MockCollectionRequestContext(Provider provider,
                                        CollectionItem collection,
                                        String method,
                                        String projection,
                                        String format) {
        super(provider, method, toRequestUri(collection, projection, format));
        this.target = new CollectionTarget(this, collection, projection,
                                           format);
    }

    public MockCollectionRequestContext(Provider provider,
                                        String uid,
                                        String method) {
        this(provider, uid, method, null, null);
    }

    public MockCollectionRequestContext(Provider provider,
                                        String uid,
                                        String method,
                                        String projection,
                                        String format) {
        this(provider, newCollection(uid), method, projection, format);
    }

    private static String toRequestUri(CollectionItem collection,
                                       String projection,
                                       String format) {
        return TEMPLATE_COLLECTION.bind(collection.getUid(), projection,
                                        format);
    }

    private static CollectionItem newCollection(String uid) {
        CollectionItem collection = new HibCollectionItem();
        collection.setUid(uid);
        return collection;
    }
}
