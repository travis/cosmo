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
import org.osaf.cosmo.atom.provider.UserTarget;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.User;

/**
 * Mock implementation of {@link RequestContext} for service requests.
 */
public class MockServiceRequestContext extends BaseMockRequestContext {
    private static final Log log =
        LogFactory.getLog(MockServiceRequestContext.class);

    public MockServiceRequestContext(Provider provider,
                                     User user) {
        super(provider, "GET", toRequestUri(user));
        this.target = new UserTarget(this, user);
    }    

    public MockServiceRequestContext(Provider provider,
                                     User user,
                                     HomeCollectionItem home) {
        super(provider, "POST", toRequestUri(user));
        this.target = new NewCollectionTarget(this, user, home);
    }

    private static String toRequestUri(User user) {
        return TEMPLATE_SERVICE.bind(user.getUsername());
    }
}
