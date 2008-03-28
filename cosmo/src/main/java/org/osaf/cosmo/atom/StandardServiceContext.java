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
package org.osaf.cosmo.atom;

import javax.security.auth.Subject;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.ItemManager;
import org.apache.abdera.protocol.Resolver;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestHandler;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.impl.AbstractServiceContext;

public class StandardServiceContext extends AbstractServiceContext {

    // ServiceContext methods

    public Abdera getAbdera() {
        return abdera;
    }

    public void init() {
        if (abdera == null)
            throw new IllegalStateException("abdera is required");
        if (providerManager == null)
            throw new IllegalStateException("providerManager is required");
        if (handlerManager == null)
            throw new IllegalStateException("requestHandlerManager is required");
        if (targetResolver == null)
            throw new IllegalStateException("targetResolver is required");
    }

    public ItemManager<RequestHandler> getRequestHandlerManager() {
        return handlerManager;
    }

    public ItemManager<Provider> getProviderManager() {
        return providerManager;
    }

    public Resolver<Subject> getSubjectResolver() {
        return subjectResolver;
    }

    /**
     * A single target resolver is used regardless of context path.
     */
    public Resolver<Target> getTargetResolver(String contextPath) {
        return getTargetResolver();
    }

    // our methods

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }

    public Resolver<Target> getTargetResolver() {
        return targetResolver;
    }
}
