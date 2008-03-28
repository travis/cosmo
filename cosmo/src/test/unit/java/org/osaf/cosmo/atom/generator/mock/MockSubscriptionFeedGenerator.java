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
package org.osaf.cosmo.atom.generator.mock;

import java.net.URLEncoder;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.SubscriptionFeedGenerator;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Mock implementation of {@link SubscriptionFeedGenerator} that
 * generates dummy feeds for use with atom unit tests.
 *
 * @see MockGeneratorFactory
 * @see Feed
 * @see CollectionSubscription
 * @see User
 */
public class MockSubscriptionFeedGenerator
    implements SubscriptionFeedGenerator {
    private static final Log log =
        LogFactory.getLog(MockSubscriptionFeedGenerator.class);

    private MockGeneratorFactory factory;
    private String projection;
    private String format;
    private ServiceLocator locator;

    /** */
    public MockSubscriptionFeedGenerator(MockGeneratorFactory factory,
                                         ServiceLocator locator) {
        this.factory = factory;
        this.locator = locator;
    }

    // SubscriptionFeedGenerator methods

    public Feed generateFeed(User user)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");
        return factory.getAbdera().getFactory().newFeed();
    }

    public Entry generateEntry(CollectionSubscription sub)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");

        try {
            Entry entry = factory.getAbdera().getFactory().newEntry();

            // when writing entries, we need self links to generate
            // location response headers
            try {
                String encodedUuid = URLEncoder.encode(sub.getDisplayName());
                entry.addLink("urn:uid:" + encodedUuid, "self");
            } catch (Exception e) {
                throw new RuntimeException("Could not encode display name " + sub.getDisplayName(), e);
            }

            return entry;
        } catch (Exception e) {
            throw new GeneratorException(e.getMessage(), e);
        }
    }
}
