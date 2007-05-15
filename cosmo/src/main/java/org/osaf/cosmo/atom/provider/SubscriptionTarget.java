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

import org.apache.abdera.protocol.server.provider.AbstractTarget;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.TargetType;

import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.User;

/**
 * <p>
 * A target that identifies the subscriptions for a user, or one
 * particular subscription.
 * </p>
 * <p>
 * A target with a null subscription is considered to identify all of
 * a user's subscriptions.
 * </p>
 */
public class SubscriptionTarget extends AbstractTarget {

    private User user;
    private CollectionSubscription subscription;

    public SubscriptionTarget(RequestContext request,
                              User user) {
        this(request, user, null);
    }

    public SubscriptionTarget(RequestContext request,
                              User user,
                              CollectionSubscription subscription) {
        super(determineTargetType(subscription), request);
        this.user = user;
        this.subscription = subscription;
    }

    public User getUser() {
        return user;
    }

    public CollectionSubscription getSubscription() {
        return subscription;
    }

    private static TargetType determineTargetType(CollectionSubscription sub) {
        return sub != null ? TargetType.TYPE_ENTRY : TargetType.TYPE_COLLECTION;
    }
}
