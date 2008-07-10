/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * A class that generates full Atom feeds.
 *
 * @see Feed
 * @see CollectionItem
 */
public class FullFeedGenerator extends BaseItemFeedGenerator {
    private static final Log log = LogFactory.getLog(FullFeedGenerator.class);

    private String format;

    /** */
    public FullFeedGenerator(StandardGeneratorFactory factory,
                             ServiceLocator locator,
                             String format)
        throws UnsupportedFormatException {
        super(factory, locator);
        if (format != null) {
            if (! factory.getContentFactory().supports(format))
                throw new UnsupportedFormatException(format);
            this.format = format;
        } else {
            this.format = FORMAT_EIM_JSON;
        }
    }

    /**
     * Extends the superclass method to add an edit link.
     *
     * @param collection the collection on which the feed is based
     * @throws GeneratorException
     */
    protected Feed createFeed(CollectionItem collection)
        throws GeneratorException {
        Feed feed = super.createFeed(collection);

        feed.addLink(newEditLink(collection));

        return feed;
    }

    /**
     * Extends the superclass method to add an edit link and links for
     * the parent collection (only if the entry is enclosed in its own
     * document), the item modified by this item (if any), and any
     * items modified by this item.
     *
     * @param item the item on which the entry is based
     * @param isDocument whether or not the entry represents an entire
     * document or is attached to a feed document
     * @throws GeneratorException
     */
    protected Entry createEntry(NoteItem item,
                                boolean isDocument)
        throws GeneratorException {
        Entry entry = super.createEntry(item, isDocument);

        if (isDocument) {
            for (CollectionItem parent : item.getParents())
                entry.addLink(newParentLink(parent));
        }

        if (item.getModifies() != null)
            entry.addLink(newModifiesLink(item.getModifies()));

        for (NoteItem modification : item.getModifications())
            entry.addLink(newModificationLink(modification));

        EventStamp stamp = StampUtils.getEventStamp(item);
        // Add expanded link only for master events
        if (!(item instanceof NoteOccurrence) && stamp != null
                && stamp.isRecurring())
            entry.addLink(newExpandedLink(item));

        if (item instanceof NoteOccurrence)
            entry.addLink(newMasterLink((NoteOccurrence)item));
        else
            entry.addLink(newEditLink(item));
        
        if (item.getModifies() != null || item instanceof NoteOccurrence)
            entry.addLink(newDetachedLink(item));

        return entry;
    }

    /**
     * Sets the entry content based on the EIM recordset translation
     * of the given item in this generator's data format.
     */
    protected void setEntryContent(Entry entry,
                                   NoteItem item)
        throws GeneratorException {
        ContentBean content =
            getFactory().getContentFactory().createContent(format, item);
        entry.setContent(content.getValue(), content.getMediaType());
    }

    /**
     * Returns {@link AtomConstants#PROJECTION_FULL}.
     */
    protected String getProjection() {
        return PROJECTION_FULL;
    }

    /**
     * Returns the IRI of the given item.
     *
     * @param item the item
     */
    protected String selfIri(Item item,
                             boolean withPathInfo) {
        StringBuffer iri = new StringBuffer(super.selfIri(item, withPathInfo));
        if (withPathInfo && format != null)
            addPathInfo(iri, format);
        return iri.toString();
    }

    protected String detachedIri(NoteItem item) {
        StringBuffer iri = new StringBuffer(super.detachedIri(item));
        if (format != null)
            addPathInfo(iri, format);
        return iri.toString();
    }
}
