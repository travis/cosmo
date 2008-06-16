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
import org.osaf.cosmo.atom.provider.NewCollectionTarget;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.User;

/**
 * Mock implementation of {@link RequestContext} for import collection
 * requests.
 */
public class MockImportCollectiontContext extends BaseMockRequestContext {
    private static final Log log =
        LogFactory.getLog(MockImportCollectiontContext.class);

    public MockImportCollectiontContext(Provider provider, User user,
            HomeCollectionItem home, String displayName) {
        super(provider, "POST", toRequestUri(user, displayName));
        this.target = new NewCollectionTarget(this, user, home, displayName);
    }

    private static String toRequestUri(User user, String displayName) {
        return TEMPLATE_IMPORT_COLLECTION.bind(user.getUsername(), displayName);
    }
}
