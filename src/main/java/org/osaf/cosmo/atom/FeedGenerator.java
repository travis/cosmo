/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.atom;

import java.util.Calendar;
import javax.activation.MimeTypeParseException;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Generator;
import org.apache.abdera.model.Person;
import org.apache.abdera.model.Link;
import org.apache.abdera.util.iri.IRISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.hcalendar.HCalendarFormatter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * A class that generates Atom 1.0 feeds and entries representing
 * Cosmo collections and items.
 *
 * @see Feed
 * @see Entry
 * @see CollectionItem
 * @see ContentItem
 */
public class FeedGenerator implements AtomConstants {
    private static final Log log = LogFactory.getLog(FeedGenerator.class);

    private ServiceLocator locator;
    private Factory factory;

    /** */
    public FeedGenerator(ServiceLocator locator) {
        this.locator = locator;
        factory = Abdera.getNewFactory();
    }

    /**
     * Generates an Atom feed containing entries for each child item
     * of the collection.
     *
     * @param collection the collection on which the feed is based
     * @param path the path address of the feed
     */
    public Feed generateFeed(CollectionItem collection) {
        Feed feed = createFeed(collection);

        for (Item child : collection.getChildren()) {
            if (child instanceof CollectionItem)
                continue;
            feed.addEntry(createEntry((ContentItem)child));
        }

        return feed;
    }

    /**
     * Creates a <code>Feed</code> with attributes set based on the
     * given collection. The given path is used to construct the
     * various URIs for the feed.
     *
     * @param collection the collection on which the feed is based
     * @param path the path address of the feed
     */
    protected Feed createFeed(CollectionItem collection) {
        Feed feed = factory.newFeed();

        String id = "urn:uuid:" + collection.getUid();
        try {
            feed.setId(id);
        } catch (IRISyntaxException e) {
            throw new IllegalStateException("Attempted to set invalid feed id " + id, e);
        }
        feed.setTitle(collection.getDisplayName());
        feed.setUpdated(Calendar.getInstance().getTime());

        Generator generator = factory.newGenerator();
        try {
            generator.setUri(CosmoConstants.PRODUCT_URL);
        } catch (IRISyntaxException e) {
            throw new IllegalStateException("Attempted to set invalid generator URI " + CosmoConstants.PRODUCT_URL, e);
        }
        generator.setVersion(CosmoConstants.PRODUCT_VERSION);
        generator.setText(CosmoConstants.PRODUCT_NAME);
        feed.setGenerator(generator);

        Person author = factory.newAuthor();
        author.setName(collection.getOwner().getUsername());
        author.setEmail(collection.getOwner().getEmail());
        feed.addAuthor(author);

        Link selfLink = factory.newLink();
        selfLink.setRel(Link.REL_SELF);
        try {
            selfLink.setMimeType(MEDIA_TYPE_ATOM);
        } catch (MimeTypeParseException e) {
            throw new IllegalStateException("Attempted to set invalid link mime type " + MEDIA_TYPE_ATOM, e);
        }
        String selfHref = locator.getAtomUrl(collection);
        try {
            selfLink.setHref(locator.getAtomUrl(collection));
        } catch (IRISyntaxException e) {
            throw new IllegalStateException("Attempted to set invalid link href " + selfHref, e);
        }
        feed.addLink(selfLink);

        Link webLink = factory.newLink();
        webLink.setRel(Link.REL_ALTERNATE);
        try {
            webLink.setMimeType(MEDIA_TYPE_HTML);
        } catch (MimeTypeParseException e) {
            throw new IllegalStateException("Attempted to set invalid link mime type " + MEDIA_TYPE_HTML, e);
        }
        String webHref = locator.getWebUrl(collection);
        try {
            webLink.setHref(webHref);
        } catch (IRISyntaxException e) {
            throw new IllegalStateException("Attempted to set invalid link href " + webHref, e);
        }
        feed.addLink(webLink);

        return feed;
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given item. The given path is used to construct the
     * various URIs for the entry.
     *
     * @param item the item on which the entry is based
     * @param feedPath the path address of the parent feed
     */
    protected Entry createEntry(ContentItem item) {
        // add the timestamp of the item's last modification to the id
        // so that readers which are too smart for their own good will
        // count an item as unread if it has changed since the reader
        // last saw it
        StringBuffer id = new StringBuffer();
        id.append("urn:uuid:").append(item.getUid()).
            append("#").append(item.getModifiedDate().getTime());

        Entry entry = factory.newEntry();
        try {
            entry.setId(id.toString());
        } catch (IRISyntaxException e) {
            throw new IllegalStateException("Attempted to set invalid entry id " + id, e);
        }
        entry.setTitle(item.getDisplayName());
        entry.setUpdated(item.getModifiedDate());
        entry.setPublished(item.getCreationDate());

        EventStamp stamp = EventStamp.getStamp(item);
        if (stamp != null)
            entry.setContentAsHtml(HCalendarFormatter.toHCal(stamp));

        return entry;
    }
}
