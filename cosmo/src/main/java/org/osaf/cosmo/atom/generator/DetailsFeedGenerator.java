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

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * A class that generates an Atom feed which conveys the full set of
 * information stored in the database for a collection but that
 * contains no entries.
 *
 * @see Feed
 * @see CollectionItem
 */
public class DetailsFeedGenerator extends BaseItemFeedGenerator {
    private static final Log log =
        LogFactory.getLog(DetailsFeedGenerator.class);

    /** */
    public DetailsFeedGenerator(StandardGeneratorFactory factory,
                                ServiceLocator locator) {
        super(factory, locator);
    }

    // BaseFeedGenerator methods

    /**
     * Extends the superclass method to add collection details to the
     * created feed.
     *
     * @param collection the collection on which the feed is based
     * @throws GeneratorException
     */
    protected Feed createFeed(CollectionItem collection)
        throws GeneratorException {
        Feed feed = super.createFeed(collection);

        feed.addLink(newEditLink(collection));
        feed.addLink(newMorseCodeLink(collection));
        feed.addLink(newDavLink(collection));
        feed.addLink(newWebcalLink(collection));
        feed.addLink(newPimLink(collection));
        feed.addLink(newTicketsLink(collection));

        for (Ticket ticket : visibleTickets(collection))
            addTicket(feed, collection, ticket);

        return feed;
    }

    /**
     * Returns an empty set of items.
     *
     * @param the collection whose contents are to be listed
     */
    protected SortedSet<NoteItem> findContents(CollectionItem collection) {
        return new TreeSet<NoteItem>();
    }

    /**
     * Does nothing since there are no entries in this feed.
     */
    protected void setEntryContent(Entry entry,
                                   NoteItem item)
        throws GeneratorException {
    }

    /**
     * Returns {@link AtomConstants#PROJECTION_DETAILS}.
     */
    protected String getProjection() {
        return PROJECTION_DETAILS;
    }

    private void addTicket(Feed feed,
                           CollectionItem collection,
                           Ticket ticket)
        throws GeneratorException {
        Element extension = getFactory().getAbdera().getFactory().
            newExtensionElement(QN_TICKET);
        extension.setAttributeValue(AtomConstants.QN_TYPE,
                                    ticket.getType().toString());
        extension.setText(ticket.getKey());
        extension.setAttributeValue("href", ticketIri(collection, ticket)); 
        feed.addExtension(extension);
    }
}
