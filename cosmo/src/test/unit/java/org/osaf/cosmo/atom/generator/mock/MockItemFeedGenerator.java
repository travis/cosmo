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

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.generator.ItemFeedGenerator;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Mock implementation of {@link ItemFeedGenerator} that generates dummy
 * feeds for use with atom unit tests.
 *
 * @see MockGeneratorFactory
 * @see Feed
 * @see CollectionItem
 */
public class MockItemFeedGenerator implements ItemFeedGenerator {
    private static final Log log =
        LogFactory.getLog(MockItemFeedGenerator.class);

    private MockGeneratorFactory factory;
    private String projection;
    private String format;
    private ServiceLocator locator;
    private NoteItemFilter filter;

    /** */
    public MockItemFeedGenerator(MockGeneratorFactory factory,
                                 String projection,
                                 String format,
                                 ServiceLocator locator) {
        this.factory = factory;
        this.projection = projection;
        this.format = format;
        this.locator = locator;
    }

    // ItemFeedGenerator methods

    public void setFilter(NoteItemFilter filter) {
        this.filter = filter;
    }

    public Feed generateFeed(CollectionItem item)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");
        return factory.getAbdera().getFactory().newFeed();
    }

    public Feed generateFeed(NoteItem item)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");
        return factory.getAbdera().getFactory().newFeed();
    }

    public Entry generateEntry(NoteItem item)
        throws GeneratorException {
        if (factory.isFailureMode())
            throw new GeneratorException("Failure mode");

        try {
            Entry entry = factory.getAbdera().getFactory().newEntry();

            // when writing entries, we need self links to generate
            // location response headers
            String href= "urn:uid:" + item.getUid().replaceAll("\\s", "_");;
            entry.addLink(href, "self");

            return entry;
        } catch (Exception e) {
            throw new GeneratorException(e.getMessage(), e);
        }
    }
}
