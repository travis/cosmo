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
package org.osaf.cosmo.atom.generator;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * An interface for classes that generate Atom feeds and entries
 * representing collection subscriptions.
 *
 * @see Entry
 * @see Feed
 * @see CollectionSubscription
 * @see User
 */
public class StandardSubscriptionFeedGenerator
    extends BaseFeedGenerator implements SubscriptionFeedGenerator {
    private static final Log log =
        LogFactory.getLog(StandardSubscriptionFeedGenerator.class);

    public StandardSubscriptionFeedGenerator(StandardGeneratorFactory factory,
                                             ServiceLocator locator) {
        super(factory, locator);
    }

    /**
     * Generates an Atom feed containing entries for a user's
     * collection subscriptions.
     *
     * @param user the user
     * @throws GeneratorException
     */
    public Feed generateFeed(User user)
        throws GeneratorException {
        return newFeed(user.getUsername());
    }

    /**
     * Generates an Atom entry representing a specific collection
     * subscription.
     *
     * @param sub the subscription
     * @throws GeneratorException
     */
    public Entry generateEntry(CollectionSubscription sub)
        throws GeneratorException {
        // XXX: not universally unique .. prefix with username?
        return newEntry(sub.getDisplayName(), false);
    }

    // XXX: perhaps override uuid2uri
}
