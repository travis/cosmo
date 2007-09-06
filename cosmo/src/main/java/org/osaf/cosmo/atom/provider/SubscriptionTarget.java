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

import java.util.Date;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.util.EntityTag;

import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.User;

/**
 * <p>
 * A target that identifies a particular collection subscription.
 * </p>
 */
public class SubscriptionTarget extends UserTarget implements AuditableTarget {

    private CollectionSubscription subscription;

    public SubscriptionTarget(RequestContext request,
                              User user,
                              CollectionSubscription subscription) {
        super(TargetType.TYPE_ENTRY, request, user);
        this.subscription = subscription;
    }

    // AuditableTarget methods

    public EntityTag getEntityTag() {
        if (subscription == null)
            return null;
        return new EntityTag(subscription.getEntityTag());
    }

    public Date getLastModified() {
        if (subscription == null)
            return null;
        return subscription.getModifiedDate();
    }

    // our methods

    public CollectionSubscription getSubscription() {
        return subscription;
    }
}
