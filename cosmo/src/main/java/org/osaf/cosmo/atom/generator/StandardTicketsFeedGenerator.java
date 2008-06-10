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

import java.util.Date;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.text.XhtmlTicketFormat;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * An interface for classes that generate Atom feeds and entries
 * representing collection tickets.
 *
 * @see Entry
 * @see Feed
 * @see CollectionItem
 */
public class StandardTicketsFeedGenerator
    extends BaseItemFeedGenerator
    implements TicketsFeedGenerator, AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardTicketsFeedGenerator.class);

    public StandardTicketsFeedGenerator(StandardGeneratorFactory factory,
                                            ServiceLocator locator) {
        super(factory, locator);
    }

    // TicketFeedGenerator methods

    /**
     * Generates an Atom feed containing entries for a collections's
     * tickets.
     *
     * @param collection the collection
     * @throws GeneratorException
     */
    public Feed generateFeed(CollectionItem collection)
        throws GeneratorException {
        Feed feed = createFeed(collection);

        for (Ticket ticket : visibleTickets(collection))
            feed.addEntry(createEntry(collection, ticket));

        return feed;
    }

    /**
     * Generates an Atom entry representing a specific 
     * ticket.
     *
     * @param ticket the ticket
     * @throws GeneratorException
     */
    public Entry generateEntry(CollectionItem collection, Ticket ticket)
        throws GeneratorException {
        return createEntry(collection, ticket, true);
    }

    // our methods

    /**
     * Creates a <code>Feed</code> with attributes set based on the
     * given collection.
     *
     * @param collection the collection on which the feed is based
     * @throws GeneratorException
     */
    protected Feed createFeed(CollectionItem collection)
        throws GeneratorException {
        Feed feed = newFeed(collection.getUid()); 

        String title = "Tickets on " + collection.getDisplayName(); // XXX: i18n
        feed.setTitle(title);
        feed.setUpdated(new Date());
        feed.setGenerator(newGenerator());
        feed.addAuthor(newPerson(collection.getOwner()));
        feed.addLink(newSelfLink(ticketsIri(collection)));

        return feed;
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given ticket. The entry does not represent a
     * document but is meant to be added to a <code>Feed</code>.
     *
     * @param ticket the ticket
     * @throws GeneratorException
     */
    protected Entry createEntry(CollectionItem collection, Ticket ticket)
        throws GeneratorException {
        return createEntry(collection, ticket, false);
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given preference.
     *
     * @param pref the preference
     * @param isDocument whether or not the entry represents an entire
     * document or is attached to a feed document
     * @throws GeneratorException
     */
    protected Entry createEntry(CollectionItem collection, Ticket ticket,
                                boolean isDocument)
        throws GeneratorException {
        String uid = ticket.getKey();
        Entry entry = newEntry(uid, isDocument);

        String iri = ticketIri(collection, ticket);
        Date now = new Date();

        entry.setTitle(ticket.getKey());
        entry.setUpdated(ticket.getCreated());
        entry.setEdited(ticket.getCreated());
        entry.setPublished(ticket.getCreated());
        if (isDocument)
            entry.addAuthor(newPerson(ticket.getOwner()));
        entry.addLink(newSelfLink(iri));
        entry.addLink(newEditLink(iri));

        XhtmlTicketFormat formatter = new XhtmlTicketFormat();
        entry.setContentAsXhtml(formatter.format(ticket));

        return entry;
    }

    @Override
    protected String getProjection() {
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    protected void setEntryContent(Entry entry, NoteItem item)
            throws GeneratorException {
        // TODO Auto-generated method stub
        
    }

}
