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

import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.ProviderManager;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.Target;

public class StandardProviderManager implements ProviderManager {

    private ItemProvider itemProvider;
    private SubscriptionProvider subscriptionProvider;
    private PreferencesProvider preferencesProvider;
    private UserProvider userProvider;

    // ProviderManager methods

    public Provider getProvider() {
        throw new UnsupportedOperationException();
    }

    public Provider getProvider(RequestContext request) {
        Target target = request.getTarget();
        if (target instanceof BaseItemTarget)
            return itemProvider;
        if (target instanceof SubscribedTarget ||
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
