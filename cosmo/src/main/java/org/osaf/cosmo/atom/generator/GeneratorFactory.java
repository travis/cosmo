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

import org.osaf.cosmo.server.ServiceLocator;

/**
 * An interface for factories that create {@link FeedGenerator}
 * instances.
 */
public interface GeneratorFactory {

    /**
     * Creates an instance of <code>ServiceGenerator</code>.
     *
     * @param locator the service locator from which collection
     * URLs are calculated
     * @return the service generator
     */
    public ServiceGenerator
        createServiceGenerator(ServiceLocator locator);

    /**
     * Creates an instance of <code>ItemFeedGenerator</code> that can
     * service the given projection and format.
     *
     * @param projection the projection name
     * @param format the format name
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator, or null if no generator is
     * supported for the named projection
     */
    public ItemFeedGenerator createItemFeedGenerator(String projection,
                                                     String format,
                                                     ServiceLocator locator)
        throws UnsupportedProjectionException, UnsupportedFormatException;

    /**
     * Creates an instance of <code>SubscriptionFeedGenerator</code>.
     *
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator
     */
    public SubscriptionFeedGenerator
        createSubscriptionFeedGenerator(ServiceLocator locator);

    /**
     * Creates an instance of <code>PreferencesFeedGenerator</code>.
     *
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator
     */
    public PreferencesFeedGenerator
        createPreferencesFeedGenerator(ServiceLocator locator);

    /**
     * Creates an instance of <code>TicketsFeedGenerator</code>.
     *
     * @param locator the service locator from which feed URLs
     * are calculated
     * @return the feed generator
     */
    public TicketsFeedGenerator
        createTicketsFeedGenerator(ServiceLocator locator);
}
