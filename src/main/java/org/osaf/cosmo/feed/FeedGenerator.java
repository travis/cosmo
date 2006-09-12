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

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Generator;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;

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

    private String feedBaseUrl;
    private String homeBaseUrl;
    private String browseBaseUrl;

    /** */
    public FeedGenerator(String feedBaseUrl,
                         String homeBaseUrl,
                         String browseBaseUrl) {
        this.feedBaseUrl = feedBaseUrl;
        this.homeBaseUrl = homeBaseUrl;
        this.browseBaseUrl = browseBaseUrl;
    }

    /**
     */
    public Feed generateFeed(CollectionItem collection,
                             Collection children,
                             String path) {
        Feed feed = createFeed(collection, path);

        for (Iterator i=children.iterator(); i.hasNext();) {
            Item child = (Item) i.next();
            Entry entry = createEntry(child, path);
            feed.getEntries().add(entry);
        }

        return feed;
    }

    /** */
    protected Feed createFeed(CollectionItem collection,
                              String path) {
        Feed feed = new Feed("atom_1.0");

        feed.setTitle(collection.getName());
        feed.setUpdated(Calendar.getInstance().getTime());
        feed.setId(homeHref(path));

        Generator generator = new Generator();
        generator.setUrl(CosmoConstants.PRODUCT_URL);
        generator.setVersion(CosmoConstants.PRODUCT_VERSION);
        generator.setValue(CosmoConstants.PRODUCT_NAME);
        feed.setGenerator(generator);

        Person owner = new Person();
        String name = collection.getOwner() != null ?
            collection.getOwner().getUsername() :
            User.USERNAME_OVERLORD;
        owner.setName(name);
        feed.getAuthors().add(owner);

        Link selfLink = new Link();
        selfLink.setRel("self");
        selfLink.setType("application/atom+xml");
        selfLink.setHref(feedHref(path));
        feed.getAlternateLinks().add(selfLink);

        Link browseLink = new Link();
        browseLink.setRel("alternate");
        browseLink.setType("text/html");
        browseLink.setHref(browseHref(path));
        feed.getAlternateLinks().add(browseLink);

        return feed;
    }

    /** */
    protected Entry createEntry(Item item,
                                String feedPath) {
        String entryPath = feedPath + "/" + item.getName();

        Entry entry = new Entry();
        entry.setId(homeHref(entryPath));
        entry.setPublished(item.getCreationDate());
        entry.setTitle(item.getName());
        entry.setUpdated(item.getModifiedDate());

        Link browseLink = new Link();
        browseLink.setRel("alternate");
        browseLink.setType("text/html");
        browseLink.setHref(browseHref(entryPath));
        entry.getAlternateLinks().add(browseLink);

        return entry;
    }

    /** */
    protected String homeHref(String href) {
        return homeBaseUrl + href;
    }

    /** */
    protected String browseHref(String href) {
        return browseBaseUrl + href;
    }

    /** */
    protected String feedHref(String href) {
        return feedBaseUrl + href;
    }
}
