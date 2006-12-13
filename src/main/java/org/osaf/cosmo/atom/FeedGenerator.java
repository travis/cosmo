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

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Generator;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

import java.util.Calendar;

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
 * @see Item
 */
public class FeedGenerator implements AtomConstants {
    private static final Log log = LogFactory.getLog(FeedGenerator.class);

    private static final String ATOM_10 = "atom_1.0";

    private ServiceLocator locator;

    /** */
    public FeedGenerator(ServiceLocator locator) {
        this.locator = locator;
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
            feed.getEntries().add(createEntry((ContentItem)child));
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
        Feed feed = new Feed(ATOM_10);

        feed.setTitle(collection.getDisplayName());
        feed.setUpdated(Calendar.getInstance().getTime());
        feed.setId(collection.getUid());

        Generator generator = new Generator();
        generator.setUrl(CosmoConstants.PRODUCT_URL);
        generator.setVersion(CosmoConstants.PRODUCT_VERSION);
        generator.setValue(CosmoConstants.PRODUCT_NAME);
        feed.setGenerator(generator);

        Person owner = new Person();
        String name = collection.getOwner().getUsername();
        owner.setName(name);
        feed.getAuthors().add(owner);

        Link selfLink = new Link();
        selfLink.setRel(REL_SELF);
        selfLink.setType(MEDIA_TYPE_ATOM);
        selfLink.setHref(locator.getAtomUrl(collection));
        feed.getAlternateLinks().add(selfLink);

        Link webLink = new Link();
        webLink.setRel(REL_ALTERNATE);
        webLink.setType(MEDIA_TYPE_HTML);
        webLink.setHref(locator.getWebUrl(collection));
        feed.getAlternateLinks().add(webLink);

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
        String id = item.getUid() + "#" + item.getModifiedDate().getTime();

        Entry entry = new Entry();
        entry.setId(id);
        entry.setPublished(item.getCreationDate());
        entry.setTitle(item.getDisplayName());
        entry.setUpdated(item.getModifiedDate());

        if (item.getStamp(EventStamp.class)!=null) {
            EventStamp event = EventStamp.getStamp(item);
            Content content = new Content();
            content.setType(Content.HTML);
            content.setValue(HCalendarFormatter.toHCal(event));
            entry.getContents().add(content);
        }

        return entry;
    }
}
