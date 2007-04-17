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

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.AbstractServiceContext;
import org.apache.abdera.protocol.server.auth.SubjectResolver;
import org.apache.abdera.protocol.server.provider.ProviderManager;
import org.apache.abdera.protocol.server.provider.TargetResolver;
import org.apache.abdera.protocol.server.servlet.RequestHandlerManager;

public class StandardServiceContext extends AbstractServiceContext {

    public Abdera getAbdera() {
        return abdera;
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }

    public ProviderManager getProviderManager() {
        return providerManager;
    }

    public void setProviderManager(ProviderManager manager) {
        this.providerManager = manager;
    }

    public RequestHandlerManager getRequestHandlerManager() {
        return handlerManager;
    }

    public void setRequestHandlerManager(RequestHandlerManager manager) {
        this.handlerManager = manager;
    }

    public SubjectResolver getSubjectResolver() {
        return subjectResolver;
    }

    public void setSubjectResolver(SubjectResolver resolver) {
        this.subjectResolver = resolver;
    }

    /**
     * A single target resolver is used regardless of context path.
     */
    public TargetResolver getTargetResolver(String contextPath) {
        return getTargetResolver();
    }

    public TargetResolver getTargetResolver() {
        return targetResolver;
    }

    public void setTargetResolver(TargetResolver resolver) {
        this.targetResolver = resolver;
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
}
