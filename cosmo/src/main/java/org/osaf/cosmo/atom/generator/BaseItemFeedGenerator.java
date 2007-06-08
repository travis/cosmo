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

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.AuditableComparator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.util.MimeUtil;

/**
 * A base class for feed generators that work with collectoins and items.
 *
 * @see Feed
 * @see Entry
 * @see CollectionItem
 * @see NoteItem
 */
public abstract class BaseItemFeedGenerator
    extends BaseFeedGenerator
    implements ItemFeedGenerator, AtomConstants, EimmlConstants,
               ICalendarConstants {
    private static final Log log =
        LogFactory.getLog(BaseItemFeedGenerator.class);

    private NoteItemFilter filter;

    /** */
    public BaseItemFeedGenerator(StandardGeneratorFactory factory,
                                 ServiceLocator locator) {
        super(factory, locator);
    }

    // ItemFeedGenerator methods

    /**
     * Sets a query filter used by the generator to find the specific
     * items that will be represented in the feed.
     *
     * @param filter the query filter
     */
    public void setFilter(NoteItemFilter filter) {
        this.filter = filter;
    }

    /**
     * Generates an Atom feed containing entries for each child item
     * of the collection.
     *
     * @param collection the collection on which the feed is based
     * @throws GeneratorException
     */
    public Feed generateFeed(CollectionItem collection)
        throws GeneratorException {
        Feed feed = createFeed(collection);

        for (NoteItem item : findContents(collection))
            feed.addEntry(createEntry(item));

        return feed;
    }

    /**
     * Generates an Atom feed containing entries for an expanded
     * recurring item. Requires a query filter to have been set.
     *
     * @param master the item on which the feed is based
     * @throws GeneratorException
     */
    public Feed generateFeed(NoteItem master)
        throws GeneratorException {
        Feed feed = createFeed(master);

        for (NoteItem item : findOccurrences(master))
            feed.addEntry(createEntry(item));

        return feed;
    }

    /**
     * Generates an Atom entry representing the item.
     *
     * @param item the item which the entry describes
     * @throws GeneratorException
     */
    public Entry generateEntry(NoteItem item)
        throws GeneratorException {
        return createEntry(item, true);
    }

    // our methods

    /**
     * <p>
     * Returns a sorted set of items from the given collection to
     * include as entries in the feed.
     * </p>
     * <p>
     * If a query filter has been provided, this method finds all of
     * the <code>NoteItem</code>s in the collection that match the
     * query filter. Otherwise, it includes all of the *
     * <code>NoteItem</code>s in the collection.
     * </p>
     * <p>
     * The set is sorted with the most recently modified item first.
     * </p>
     *
     * @param the collection whose contents are to be listed
     */
    protected SortedSet<NoteItem> findContents(CollectionItem collection) {
        TreeSet<NoteItem> contents =
            new TreeSet<NoteItem>(new AuditableComparator(true));

        // XXX sort
        // XXX page

        if (filter != null) {
            filter.setParent(collection);
            for (Item item : getFactory().getContentService().
                     findItems(filter))
                contents.add((NoteItem)item);
        } else {
            for (Item child : collection.getChildren()) {
                if (child instanceof CollectionItem)
                    continue;
                if (! (child instanceof NoteItem))
                    continue;
                contents.add((NoteItem)child);
            }
        }

        return contents;
    }
  
    /**
     * <p>
     * Returns a sorted set of items representing modifications and
     * occurrences of a recurring event.
     * </p>
     * <p>
     * The set is sorted with the most recently modified item first.
     * </p>
     *
     * @param the collection whose contents are to be listed
     * @throws IllegalStateException if a filter has not been provided
     */
    protected SortedSet<NoteItem> findOccurrences(NoteItem item) {
        if (filter == null)
            throw new IllegalStateException("filter must be set");

        TreeSet<NoteItem> contents =
            new TreeSet<NoteItem>(new AuditableComparator(true));

        filter.setMasterNoteItem(item);
        for (Item occurrence : getFactory().getContentService().
                 findItems(filter))
            contents.add((NoteItem)occurrence);

        return contents;
    }
  
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

        feed.setTitle(collection.getDisplayName());
        feed.setUpdated(collection.getClientModifiedDate());
        feed.setGenerator(newGenerator());
        feed.addAuthor(newPerson(collection.getOwner()));
        feed.addLink(newSelfLink(collection));
        feed.addLink(newPimLink(collection));

        return feed;
    }

    /**
     * Creates a <code>Feed</code> with attributes set based on the
     * given item.
     *
     * @param item the item on which the feed is based
     * @throws GeneratorException
     */
    protected Feed createFeed(NoteItem item)
        throws GeneratorException {
        Feed feed = newFeed(item.getUid());

        feed.setTitle(item.getDisplayName());
        feed.setUpdated(item.getClientModifiedDate());
        feed.setGenerator(newGenerator());
        feed.addAuthor(newPerson(item.getOwner()));
        feed.addLink(newSelfLink(item));
        feed.addLink(newPimLink(item));

        return feed;
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given item.
     *
     * @param item the item on which the entry is based
     * @throws GeneratorException
     */
    protected Entry createEntry(NoteItem item)
        throws GeneratorException {
        return createEntry(item, false);
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given item.
     *
     * @param item the item on which the entry is based
     * @param isDocument whether or not the entry represents an entire
     * document or is attached to a feed document
     * @throws GeneratorException
     */
    protected Entry createEntry(NoteItem item,
                                boolean isDocument)
        throws GeneratorException {
        Entry entry = newEntry(item.getUid(), isDocument);

        entry.setTitle(item.getDisplayName());
        entry.setUpdated(item.getClientModifiedDate());
        entry.setPublished(item.getClientCreationDate());
        entry.addLink(newSelfLink(item));
        if (isDocument)
            entry.addAuthor(newPerson(item.getOwner()));

        setEntryContent(entry, item);

        return entry;
    }

    /**
     * Returns the projection name that this generator provides.
     */
    protected abstract String getProjection();

    /**
     * Sets the entry content based on the given item.
     *
     * @param entry the entry
     * @param item the item on which the entry is based
     * @throws GeneratorException
     */
    protected abstract void setEntryContent(Entry entry,
                                            NoteItem item)
        throws GeneratorException;

    /**
     * Creates a <code>Link</code> for the pim IRI of the given item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newPimLink(Item item)
        throws GeneratorException {
        return newLink(Link.REL_ALTERNATE, MEDIA_TYPE_HTML,
                       getLocator().getPimUrl(item, true));
    }

    /**
     * Creates a <code>Link</code> for the Morse Code IRI of the given
     * collection.
     *
     * @param collection the collection to link
     * @throws GeneratorException
     */
    protected Link newMorseCodeLink(CollectionItem collection)
        throws GeneratorException {
        return newLink(REL_MORSE_CODE, MEDIA_TYPE_EIMML,
                       getLocator().getMorseCodeUrl(collection));
    }

    /**
     * Creates a <code>Link</code> for the dav IRI of the given item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newDavLink(Item item)
        throws GeneratorException {
        return newLink(REL_DAV, MEDIA_TYPE_XML,
                       getLocator().getDavUrl(item));
    }

    /**
     * Creates a <code>Link</code> for the Webal IRI of the given
     * collection.
     *
     * @param collection the collection to link
     * @throws GeneratorException
     */
    protected Link newWebcalLink(CollectionItem collection)
        throws GeneratorException {
        return newLink(REL_WEBCAL, ICALENDAR_MEDIA_TYPE,
                       getLocator().getWebcalUrl(collection));
    }

    /**
     * Creates a <code>Link</code> for the self IRI of the given item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newSelfLink(Item item)
        throws GeneratorException {
        return newLink(Link.REL_SELF, MEDIA_TYPE_ATOM, selfIri(item));
    }

    /**
     * Creates a <code>Link</code> for the expanded IRI of the given item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newExpandedLink(NoteItem item)
        throws GeneratorException {
        return newLink(REL_EXPANDED, MEDIA_TYPE_ATOM, expandedIri(item));
    }

    /**
     * Creates a <code>Link</code> for the edit IRI of the given
     * collection.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newEditLink(CollectionItem collection)
        throws GeneratorException {
        return newLink(Link.REL_EDIT, MimeUtil.MEDIA_TYPE_FORM_ENCODED,
                       selfIri(collection, false));
    }

    /**
     * Creates a <code>Link</code> for the edit IRI of the given
     * item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newEditLink(NoteItem item)
        throws GeneratorException {
        return newLink(Link.REL_EDIT, MEDIA_TYPE_ATOM, selfIri(item, false));
    }

    /**
     * Creates a <code>Link</code> for the parent IRI of the given
     * collection.
     *
     * @param collection the collection to link
     * @throws GeneratorException
     */
    protected Link newParentLink(CollectionItem collection)
        throws GeneratorException {
        return newLink(REL_PARENT, MEDIA_TYPE_ATOM, selfIri(collection));
    }

    /**
     * Creates a <code>Link</code> for the modifies IRI of the given
     * item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newModifiesLink(NoteItem item)
        throws GeneratorException {
        return newLink(REL_MODIFIES, MEDIA_TYPE_ATOM, selfIri(item));
    }

    /**
     * Creates a <code>Link</code> for the modification IRI of the given
     * item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newModificationLink(NoteItem item)
        throws GeneratorException {
        return newLink(REL_MODIFICATION, MEDIA_TYPE_ATOM, selfIri(item));
    }

    /**
     * Returns the IRI of the given item including path info.
     *
     * @param item the item
     */
    protected String selfIri(Item item) {
        return selfIri(item, true);
    }

    /**
     * Returns the IRI of the given item. Path info (projection, etc)
     * can optionally be included; this is usually done for read links
     * but not edit links.
     *
     * @param item the item
     * @param withPathInfo if path info should be provided
     */
    protected String selfIri(Item item,
                             boolean withPathInfo) {
        StringBuffer iri =
            new StringBuffer(getLocator().getAtomUrl(item, false));
        if (withPathInfo && getProjection() != null)
            iri.append("/").append(getProjection());
        return iri.toString();
    }

    /**
     * Returns the expanded IRI of the given item including path info.
     *
     * @param item the item
     */
    protected String expandedIri(Item item) {
        String selfIri = selfIri(item);
        return selfIri.replaceFirst("item", "expanded");
    }

    public NoteItemFilter getFilter() {
        return filter;
    }
}
