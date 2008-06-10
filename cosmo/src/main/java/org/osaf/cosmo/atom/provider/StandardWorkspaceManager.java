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

import java.util.Collection;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.WorkspaceManager;

public class StandardWorkspaceManager
    implements WorkspaceManager {

    private DetachedItemCollectionAdapter detachedItemAdapter;
    private ExpandedItemCollectionAdapter expandedItemAdapter;
    private ItemCollectionAdapter itemAdapter;
    private SubscriptionCollectionAdapter subscriptionAdapter;
    private PreferencesCollectionAdapter preferencesAdapter;
    private TicketsCollectionAdapter ticketsAdapter;
    private UserCollectionAdapter userAdapter;

    // Manager methods

    public CollectionAdapter getCollectionAdapter(RequestContext request) {
        RequestContext context = (RequestContext) request;
        Target target = context.getTarget();
        if (target == null)
            return null;
        if (target instanceof ExpandedItemTarget)
            return expandedItemAdapter;
        if (target instanceof DetachedItemTarget)
            return detachedItemAdapter;
        if (target instanceof BaseItemTarget ||
            target instanceof NewCollectionTarget)
            return itemAdapter;
        if (target instanceof SubscriptionsTarget ||
            target instanceof SubscriptionTarget)
            return subscriptionAdapter;
        if (target instanceof PreferencesTarget ||
            target instanceof PreferenceTarget)
                return preferencesAdapter;
        if (target instanceof TicketsTarget ||
            target instanceof TicketTarget)
                return ticketsAdapter;
        if (target instanceof UserTarget)
            return userAdapter;
        throw new IllegalArgumentException("No adapter for " + target.getClass().getName());
    }

    public void release(Provider provider) {}

    // our methods

    public DetachedItemCollectionAdapter getDetachedItemAdapter() {
        return detachedItemAdapter;
    }

    public void setDetachedItemAdapter(DetachedItemCollectionAdapter adapter) {
        this.detachedItemAdapter = adapter;
    }

    public ExpandedItemCollectionAdapter getExpandedItemAdapter() {
        return expandedItemAdapter;
    }

    public void setExpandedItemAdapter(ExpandedItemCollectionAdapter adapter) {
        this.expandedItemAdapter = adapter;
    }

    public ItemCollectionAdapter getItemAdapter() {
        return itemAdapter;
    }

    public void setItemAdapter(ItemCollectionAdapter adapter) {
        this.itemAdapter = adapter;
    }

    public SubscriptionCollectionAdapter getSubscriptionAdapter() {
        return subscriptionAdapter;
    }

    public void setSubscriptionAdapter(SubscriptionCollectionAdapter adapter) {
        this.subscriptionAdapter = adapter;
    }

    public PreferencesCollectionAdapter getPreferencesAdapter() {
        return preferencesAdapter;
    }

    public void setPreferencesAdapter(PreferencesCollectionAdapter adapter) {
        this.preferencesAdapter = adapter;
    }

    public TicketsCollectionAdapter getTicketsAdapter() {
        return ticketsAdapter;
    }

    public void setTicketsAdapter(TicketsCollectionAdapter adapter) {
        this.ticketsAdapter = adapter;
    }

    public UserCollectionAdapter getUserAdapter() {
        return userAdapter;
    }

    public void setUserAdapter(UserCollectionAdapter adapter) {
        this.userAdapter = adapter;
    }

    public void init() {
        if (expandedItemAdapter == null)
            throw new IllegalStateException("expandedItemAdapter is required");
        if (detachedItemAdapter == null)
            throw new IllegalStateException("detachedItemAdapter is required");
        if (itemAdapter == null)
            throw new IllegalStateException("itemAdapter is required");
        if (subscriptionAdapter == null)
            throw new IllegalStateException("subscriptionAdapter is required");
        if (preferencesAdapter == null)
            throw new IllegalStateException("preferencesAdapter is required");
        if (preferencesAdapter == null)
            throw new IllegalStateException("ticketsAdapter is required");
        if (userAdapter == null)
            throw new IllegalStateException("userAdapter is required");
    }
    
    public Collection<WorkspaceInfo> getWorkspaces(RequestContext request) {
        throw new RuntimeException("getWorkspaces() not implemented");
    }
}
