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
package org.osaf.cosmo.feed;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.hcalendar.HCalendarFormatter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Generator;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

/**
 * A class that generates Atom 1.0 feeds and entries representing
 * Cosmo collections and items.
 *
 * @see Feed
 * @see Entry
 * @see CollectionItem
 * @see Item
 */
public class FeedGenerator {
    private static final Log log = LogFactory.getLog(FeedGenerator.class);

    private String atomBase;
    private String davBase;
    private String webBase;

    /**
     *
     * @param atomBase the base for constructing atom URIs
     * @param davBase the base for constructing dav URIs
     * @param webBase the base for constructing web URIs
     */
    public FeedGenerator(String atomBase,
                         String davBase,
                         String webBase) {
        this.atomBase = atomBase;
        this.davBase = davBase;
        this.webBase = webBase;
    }

    /**
     * Generates an Atom feed containing entries for each child item
     * of the collection.
     *
     * @param collection the collection on which the feed is based
     * @param path the path address of the feed
     */
    public Feed generateFeed(CollectionItem collection,
                             String path) {
        if (! path.equals("/") && path.endsWith("/"))
            path = path.substring(0, path.length()-1);

        Feed feed = createFeed(collection, path);

        for (Item child : collection.getChildren()) {
            feed.getEntries().add(createEntry(child, path));
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
    protected Feed createFeed(CollectionItem collection,
                              String path) {
        Feed feed = new Feed("atom_1.0");

        feed.setTitle(collection.getDisplayName());
        feed.setUpdated(Calendar.getInstance().getTime());
        feed.setId(davHref(path));

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
        selfLink.setRel("self");
        selfLink.setType("application/atom+xml");
        selfLink.setHref(atomHref(path));
        feed.getAlternateLinks().add(selfLink);

        Link webLink = new Link();
        webLink.setRel("alternate");
        webLink.setType("text/html");
        webLink.setHref(webHref(path));
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
    protected Entry createEntry(Item item,
                                String feedPath) {
        String entryPath = feedPath + "/" + item.getName();

        // add the timestamp of the item's last modification to the id
        // so that readers which are too smart for their own good will
        // count an item as unread if it has changed since the reader
        // last saw it
        String id = davHref(entryPath);
        id = id + "#" + item.getModifiedDate().getTime();

        Entry entry = new Entry();
        entry.setId(id);
        entry.setPublished(item.getCreationDate());
        entry.setTitle(item.getDisplayName());
        entry.setUpdated(item.getModifiedDate());

        Link webLink = new Link();
        webLink.setRel("alternate");
        webLink.setType("text/html");
        webLink.setHref(webHref(entryPath));
        entry.getAlternateLinks().add(webLink);

        if (item.getStamp(EventStamp.class)!=null) {
            EventStamp event = EventStamp.getStamp(item);
            Content content = new Content();
            content.setType(Content.HTML);
            content.setValue(HCalendarFormatter.toHCal(event));
            entry.getContents().add(content);
        }

        return entry;
    }

    /**
     * Calculates the full dav URI for the given feed or entry path.
     */
    protected String davHref(String href) {
        return davBase + href;
    }

    /**
     * Calculates the full web URI for the given feed or entry path.
     */
    protected String webHref(String href) {
        return webBase + href;
    }

    /**
     * Calculates the full atom URI for the given feed or entry path.
     */
    protected String atomHref(String href) {
        return atomBase + href;
    }
}
