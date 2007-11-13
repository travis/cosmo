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

import org.apache.abdera.protocol.ItemManager;
import org.apache.abdera.protocol.Request;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;

public class StandardProviderManager
    implements ItemManager<Provider> {

    private DetachedItemProvider detachedItemProvider;
    private ExpandedItemProvider expandedItemProvider;
    private ItemProvider itemProvider;
    private SubscriptionProvider subscriptionProvider;
    private PreferencesProvider preferencesProvider;
    private UserProvider userProvider;

    // Manager methods

    public Provider get(Request request) {
        RequestContext context = (RequestContext) request;
        Target target = context.getTarget();
        if (target == null)
            return null;
        if (target instanceof ExpandedItemTarget)
            return expandedItemProvider;
        if (target instanceof DetachedItemTarget)
            return detachedItemProvider;
        if (target instanceof BaseItemTarget ||
            target instanceof NewCollectionTarget)
            return itemProvider;
        if (target instanceof SubscriptionsTarget ||
            target instanceof SubscriptionTarget)
            return subscriptionProvider;
        if (target instanceof PreferencesTarget ||
            target instanceof PreferenceTarget)
            return preferencesProvider;
        if (target instanceof UserTarget)
            return userProvider;
        throw new IllegalArgumentException("No provider for " + target.getClass().getName());
    }

    public void release(Provider provider) {}

    // our methods

    public DetachedItemProvider getDetachedItemProvider() {
        return detachedItemProvider;
    }

    public void setDetachedItemProvider(DetachedItemProvider provider) {
        this.detachedItemProvider = provider;
    }

    public ExpandedItemProvider getExpandedItemProvider() {
        return expandedItemProvider;
    }

    public void setExpandedItemProvider(ExpandedItemProvider provider) {
        this.expandedItemProvider = provider;
    }

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

    public PreferencesProvider getPreferencesProvider() {
        return preferencesProvider;
    }

    public void setPreferencesProvider(PreferencesProvider provider) {
        this.preferencesProvider = provider;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    public void setUserProvider(UserProvider provider) {
        this.userProvider = provider;
    }

    public void init() {
        if (expandedItemProvider == null)
            throw new IllegalStateException("expandedItemProvider is required");
        if (detachedItemProvider == null)
            throw new IllegalStateException("detachedItemProvider is required");
        if (itemProvider == null)
            throw new IllegalStateException("itemProvider is required");
        if (subscriptionProvider == null)
            throw new IllegalStateException("subscriptionProvider is required");
        if (preferencesProvider == null)
            throw new IllegalStateException("preferencesProvider is required");
        if (userProvider == null)
            throw new IllegalStateException("userProvider is required");
    }
}
