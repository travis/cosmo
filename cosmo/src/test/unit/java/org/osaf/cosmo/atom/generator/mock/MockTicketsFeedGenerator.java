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
import org.osaf.cosmo.atom.generator.TicketsFeedGenerator;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Mock implementation of {@link TicketsFeedGenerator} that
 * generates dummy feeds for use with atom unit tests.
 *
 * @see MockGeneratorFactory
 * @see Feed
 * @see CollectionItem
 */
public class MockTicketsFeedGenerator
    implements TicketsFeedGenerator {
    private static final Log log =
        LogFactory.getLog(MockTicketsFeedGenerator.class);

    private MockGeneratorFactory factory;
    private ServiceLocator locator;

    /** */
    public MockTicketsFeedGenerator(MockGeneratorFactory factory,
                                         ServiceLocator locator) {
        this.factory = factory;
        this.locator = locator;
    }

    // TicketsFeedGenerator methods

    public Feed generateFeed(CollectionItem collection)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");
        return factory.getAbdera().getFactory().newFeed();
    }

    public Entry generateEntry(CollectionItem collection, Ticket ticket)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");

        try {
            Entry entry = factory.getAbdera().getFactory().newEntry();

            // when writing entries, we need self links to generate
            // location response headers
            try {
                String encodedUuid = URLEncoder.encode(ticket.getKey());
                entry.addLink("urn:uid:" + encodedUuid, "self");
            } catch (Exception e) {
                throw new RuntimeException("Could not encode key" + ticket.getKey(), e);
            }

            return entry;
        } catch (Exception e) {
            throw new GeneratorException(e.getMessage(), e);
        }
    }
}
