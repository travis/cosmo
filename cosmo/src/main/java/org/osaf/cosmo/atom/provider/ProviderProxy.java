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

import org.apache.abdera.model.Service;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.ServiceGenerator;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;

public class ProviderProxy extends BaseProvider {
    private static final Log log = LogFactory.getLog(ProviderProxy.class);

    private ItemProvider itemProvider;
    private SubscriptionProvider subscriptionProvider;

    // Provider methods

    public ResponseContext createEntry(RequestContext request) {
        if (request.getTarget() instanceof SubscribedTarget)
            return subscriptionProvider.createEntry(request);
        return itemProvider.createEntry(request);
    }

    public ResponseContext deleteEntry(RequestContext request) {
        if (request.getTarget() instanceof SubscriptionTarget)
            return subscriptionProvider.deleteEntry(request);
        return itemProvider.deleteEntry(request);
    }

    public ResponseContext deleteMedia(RequestContext request) {
        if (request.getTarget() instanceof SubscriptionTarget)
            return subscriptionProvider.deleteMedia(request);
        return itemProvider.deleteMedia(request);
    }

    public ResponseContext updateEntry(RequestContext request) {
        if (request.getTarget() instanceof SubscriptionTarget)
            return subscriptionProvider.updateEntry(request);
        return itemProvider.updateEntry(request);
    }
  
    public ResponseContext updateMedia(RequestContext request) {
        if (request.getTarget() instanceof SubscriptionTarget)
            return subscriptionProvider.updateMedia(request);
        return itemProvider.updateMedia(request);
    }
  
    public ResponseContext getService(RequestContext request) {
        UserTarget target = (UserTarget) request.getTarget();
        User user = target.getUser();
        if (log.isDebugEnabled())
            log.debug("getting service for user " + user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            ServiceGenerator generator = createServiceGenerator(locator);
            Service service =
                generator.generateService(target.getUser());

            return createResponseContext(service.getDocument());
        } catch (GeneratorException e) {
            String reason = "Unknown service generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }

    public ResponseContext getFeed(RequestContext request) {
        if (request.getTarget() instanceof SubscribedTarget)
            return subscriptionProvider.getFeed(request);
        return itemProvider.getFeed(request);
    }

    public ResponseContext getEntry(RequestContext request) {
        if (request.getTarget() instanceof SubscriptionTarget)
            return subscriptionProvider.getEntry(request);
        return itemProvider.getEntry(request);
    }
  
    public ResponseContext getMedia(RequestContext request) {
        if (request.getTarget() instanceof SubscriptionTarget)
            return subscriptionProvider.getMedia(request);
        return itemProvider.getMedia(request);
    }
  
    public ResponseContext getCategories(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext entryPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    // ExtendedProvider methods

    public ResponseContext updateCollection(RequestContext request) {
        if (request.getTarget() instanceof CollectionTarget)
            return itemProvider.updateCollection(request);
        throw new UnsupportedOperationException();
    }

    // our methods

    public ItemProvider getItemProvider() {
        return itemProvider;
    }

    public void setItemProvider(ItemProvider provider) {
        this.itemProvider = provider;
    }

    public SubscriptionProvider getSubscriptionProvider() {
        return subscriptionProvider;
    }

    public void setSubscriptionProvider(SubscriptionProvider provider) {
        this.subscriptionProvider = provider;
    }

    public void init() {
        super.init();
        if (itemProvider == null)
            throw new IllegalStateException("itemProvider is required");
        if (subscriptionProvider == null)
            throw new IllegalStateException("subscriptionProvider is required");
    }

    protected ServiceGenerator createServiceGenerator(ServiceLocator locator) {
        return getGeneratorFactory().createServiceGenerator(locator);
    }
}
