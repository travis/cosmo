/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.model;

import java.util.Calendar;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Generator;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.jdom.Document;

import org.osaf.cosmo.CosmoConstants;

/**
 * Extends {@link Resource} to represent a collection of
 * resources.
 */
public class CollectionResource extends Resource {

    private Set resources;

    /**
     */
    public CollectionResource() {
        super();
        resources = new HashSet();
    }

    /**
     */
    public Set getResources() {
        return resources;
    }

    /**
     */
    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /**
     * Expects the caller to set the <code>xmlBase</code>,
     * <code>alternateLinks</code>, and <code>otherLinks</code>
     * properties.
     */
    public Feed getAtomFeed()
        throws FeedException {
        Feed feed = new Feed("atom_1.0");

        feed.setTitle(getDisplayName());
        feed.setUpdated(Calendar.getInstance().getTime());
        feed.setId(getPath());

        Generator generator = new Generator();
        generator.setUrl(CosmoConstants.PRODUCT_URL);
        generator.setVersion(CosmoConstants.PRODUCT_VERSION);
        feed.setGenerator(generator);

        Link feedLink = new Link();
        feedLink.setRel("self");
        feedLink.setType("application/atom+xml");
        feedLink.setHref(getPath());
        feed.getAlternateLinks().add(feedLink);

        Link viewLink = new Link();
        viewLink.setRel("alternate");
        viewLink.setType("text/html");
        viewLink.setHref(getPath());
        feed.getAlternateLinks().add(viewLink);

        addAtomFeedEntries(feed);

        return feed;
    }

    /**
     */
    protected void addAtomFeedEntries(Feed feed)
        throws FeedException {
        for (Iterator i=resources.iterator(); i.hasNext();) {
            Resource resource = (Resource) i.next();
            if (resource instanceof FileResource) {
                feed.getEntries().add(getAtomEntry((FileResource) resource));
            }
        }
    }

    /**
     */
    protected Entry getAtomEntry(FileResource file)
        throws FeedException {
        Entry entry = new Entry();
        entry.setId(file.getPath());
        entry.setPublished(file.getDateCreated());
        entry.setTitle(file.getDisplayName());
        entry.setUpdated(file.getDateModified());

        Link viewLink = new Link();
        viewLink.setRel("alternate");
        viewLink.setType("text/html");
        viewLink.setHref(file.getPath());
        entry.getAlternateLinks().add(viewLink);

        return entry;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof CollectionResource)) {
            return false;
        }
        CollectionResource it = (CollectionResource) o;
        return new EqualsBuilder().
            appendSuper(super.equals(o)).
            append(resources, it.resources).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(13, 15).
            appendSuper(super.hashCode()).
            append(resources).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            appendSuper(super.toString()).
            append("resources", resources).
            toString();
    }
}
