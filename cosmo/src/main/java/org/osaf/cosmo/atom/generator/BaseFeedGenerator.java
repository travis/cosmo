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

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.activation.MimeTypeParseException;

import org.apache.abdera.i18n.iri.IRISyntaxException;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Generator;
import org.apache.abdera.model.Person;
import org.apache.abdera.model.Link;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * A base class for feed generators.
 *
 * @see Feed
 * @see CollectionItem
 */
public abstract class BaseFeedGenerator
    implements FeedGenerator, AtomConstants {
    private static final Log log = LogFactory.getLog(BaseFeedGenerator.class);

    private Factory abderaFactory;
    private ContentFactory contentFactory;
    private ServiceLocator serviceLocator;

    // FeedGenerator methods

    /** */
    public BaseFeedGenerator(Factory abderaFactory,
                             ContentFactory contentFactory,
                             ServiceLocator serviceLocator) {
        this.abderaFactory = abderaFactory;
        this.contentFactory = contentFactory;
        this.serviceLocator = serviceLocator;
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
     * This implementation includes all <code>NoteItem</code>s
     * sorted with the most recently modified item first.
     * </p>
     *
     * @param the collection whose contents are to be listed
     */
    protected SortedSet<NoteItem> findContents(CollectionItem collection) {
        TreeSet<NoteItem> contents =
            new TreeSet<NoteItem>(new ItemModifiedComparator(true));
        // XXX sort
        // XXX page
        // XXX query
        for (Item child : collection.getChildren()) {
            if (child instanceof CollectionItem)
                continue;
            if (! (child instanceof NoteItem))
                continue;
            contents.add((NoteItem)child);
        }
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
     * @param item the item on which the entry is based
     * @throws GeneratorException
     */
    protected abstract void setEntryContent(Entry entry,
                                            NoteItem item)
        throws GeneratorException;

    /**
     * Creates a <code>Feed</code> with id set to an IRI based on the
     * given collection uuid.
     *
     * @param uuid the collection uuid to use for the entry id
     * @throws GeneratorException
     */
    protected Feed newFeed(String uuid)
        throws GeneratorException {
        Feed feed = abderaFactory.newFeed();

        String id = uuid2Iri(uuid);
        try {
            feed.setId(id);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid feed id " + id, e);
        }

        String baseUri = serviceLocator.getAtomBase();
        try {
            feed.setBaseUri(baseUri);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid base URI " + baseUri, e);
        }

        return feed;
    }

    /**
     * Creates a <code>Generator</code> specifying Cosmo product
     * information.
     *
     * @throws GeneratorException
     */
    protected Generator newGenerator()
        throws GeneratorException {
        Generator generator = abderaFactory.newGenerator();
        try {
            generator.setUri(CosmoConstants.PRODUCT_URL);
            generator.setVersion(CosmoConstants.PRODUCT_VERSION);
            generator.setText(CosmoConstants.PRODUCT_NAME);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid generator URI " + CosmoConstants.PRODUCT_URL, e);
        }
        return generator;
    }

    /**
     * Creates a <code>Person</code> based on the given user.
     *
     * @param user the user
     * @throws GeneratorException
     */
    protected Person newPerson(User user)
        throws GeneratorException {
        Person author = abderaFactory.newAuthor();

        author.setName(user.getUsername());
        author.setEmail(user.getEmail());

        return author;
    }

    /**
     * Creates a <code>Entry</code> with id set to an IRI based on the
     * given item uuid.
     *
     * @param uuid the item uuid to use for the entry id
     * @param isDocument whether or not the entry represents an entire
     * document or is attached to a feed document
     * @throws GeneratorException
     */
    protected Entry newEntry(String uuid,
                             boolean isDocument)
        throws GeneratorException {
        Entry entry = abderaFactory.newEntry();

        String id = uuid2Iri(uuid);
        try {
            entry.setId(id);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid entry id " + id, e);
        }

        if (isDocument) {
            String baseUri = serviceLocator.getAtomBase();
            try {
                entry.setBaseUri(baseUri);
            } catch (IRISyntaxException e) {
                throw new GeneratorException("Attempted to set invalid base URI " + baseUri, e);
            }
        }

        return entry;
    }

    /**
     * Creates a <code>Link</code> for the pim IRI of the given item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newPimLink(Item item)
        throws GeneratorException {
        return newLink(Link.REL_ALTERNATE, MEDIA_TYPE_HTML,
                       serviceLocator.getPimUrl(item, true));
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
     * Creates a <code>Link</code> for the edit IRI of the given
     * item.
     *
     * @param item the item to link
     * @throws GeneratorException
     */
    protected Link newEditLink(Item item)
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
     * Creates a <code>Link</code> using the given parameters.
     *
     * @param rel the relation between the linked resource and the
     * linking resource
     * @param mimeType the mime type of the linked resource
     * @param href the href of the linked content
     * @throws GeneratorException
     */
    protected Link newLink(String rel,
                           String mimeType,
                           String href)
        throws GeneratorException {
        try {
            Link link = abderaFactory.newLink();
            link.setRel(rel);
            link.setMimeType(mimeType);
            link.setHref(href);
            return link;
        } catch (MimeTypeParseException e) {
            throw new GeneratorException("Attempted to set invalid link mime type " + MEDIA_TYPE_ATOM, e);
        } catch (IRISyntaxException e) {
            throw new GeneratorException("Attempted to set invalid link href " + href, e);
        }
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
            new StringBuffer(serviceLocator.getAtomUrl(item, false));
        if (withPathInfo && getProjection() != null)
            iri.append("/").append(getProjection());
        return iri.toString();
    }

    /**
     * Returns an IRI incorporating the given uuid.
     *
     * @param uuid the uuid
     */
    protected String uuid2Iri(String uuid) {
        return "urn:uuid:" + uuid;
    }

    public Factory getAbderaFactory() {
        return abderaFactory;
    }

    public ContentFactory getContentFactory() {
        return contentFactory;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    private class ItemModifiedComparator implements Comparator<NoteItem> {
        private boolean reverse;

        public ItemModifiedComparator() {
            this.reverse = false;
        }

        public ItemModifiedComparator(boolean reverse) {
            this.reverse = reverse;
        }

        public int compare(NoteItem o1,
                           NoteItem o2) {
            if (o1.getModifiedDate().getTime() ==
                o2.getModifiedDate().getTime())
                return 0;
            if (o1.getModifiedDate().after(o2.getModifiedDate()))
                return reverse ? -1 : 1;
            return reverse ? 1 : -1;
        }

        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj instanceof ItemModifiedComparator)
                return true;
            return super.equals(obj);
        }
    }
}
