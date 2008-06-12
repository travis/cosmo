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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.AuditableComparator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.text.XhtmlSubscriptionFormat;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * An interface for classes that generate Atom feeds and entries
 * representing collection subscriptions.
 *
 * @see Entry
 * @see Feed
 * @see CollectionSubscription
 * @see User
 */
public class StandardSubscriptionFeedGenerator
    extends BaseFeedGenerator
    implements SubscriptionFeedGenerator, AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardSubscriptionFeedGenerator.class);

    public StandardSubscriptionFeedGenerator(StandardGeneratorFactory factory,
                                             ServiceLocator locator) {
        super(factory, locator);
    }

    // SubscriptionFeedGenerator methods

    /**
     * Generates an Atom feed containing entries for a user's
     * collection subscriptions.
     *
     * @param user the user
     * @throws GeneratorException
     */
    public Feed generateFeed(User user)
        throws GeneratorException {
        Feed feed = createFeed(user);

        for (CollectionSubscription sub : findSubscriptions(user))
            feed.addEntry(createEntry(sub));

        return feed;
    }

    /**
     * Generates an Atom entry representing a specific collection
     * subscription.
     *
     * @param sub the subscription
     * @throws GeneratorException
     */
    public Entry generateEntry(CollectionSubscription sub)
        throws GeneratorException {
        return createEntry(sub, true);
    }

    // our methods

    /**
     * <p>
     * Returns a sorted set of collection subscriptions to include as
     * entries in the feed.
     * </p>
     * <p>
     * This implementation returns all of the user's subscriptions
     * sorted with the most recently modified subscription first.
     * </p>
     *
     * @param user the user whose subscriptions are to be listed
     */
    protected SortedSet<CollectionSubscription> findSubscriptions(User user) {
        // XXX sort
        // XXX page

        TreeSet<CollectionSubscription> subs =
            new TreeSet<CollectionSubscription>(new AuditableComparator(true));

        for (CollectionSubscription sub : user.getCollectionSubscriptions())
            subs.add(sub);

        return subs;
    }

    /**
     * Creates a <code>Feed</code> with attributes set based on the
     * given user.
     *
     * @param subscription the subscription on which the feed is based
     * @throws GeneratorException
     */
    protected Feed createFeed(User user)
        throws GeneratorException {
        Feed feed = newFeed(user.getUsername() + "-subscriptions");

        String title = user.getUsername() + "'s Subscriptions"; // XXX: i18n
        feed.setTitle(title);
        feed.setUpdated(new Date());
        feed.setGenerator(newGenerator());
        feed.addAuthor(newPerson(user));
        feed.addLink(newSelfLink(subscriptionsIri(user)));

        return feed;
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given subscription. The entry does not represent a
     * document but is meant to be added to a <code>Feed</code>.
     *
     * @param sub the subscription on which the entry is based
     * @throws GeneratorException
     */
    protected Entry createEntry(CollectionSubscription sub)
        throws GeneratorException {
        return createEntry(sub, false);
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given subscription.
     *
     * @param sub the subscription on which the entry is based
     * @param isDocument whether or not the entry represents an entire
     * document or is attached to a feed document
     * @throws GeneratorException
     */
    protected Entry createEntry(CollectionSubscription sub,
                                boolean isDocument)
        throws GeneratorException {
        String uid = sub.getOwner().getUsername() + "-" + sub.getDisplayName();
        Entry entry = newEntry(uid, isDocument);

        CollectionItem collection = null;
        
        try {
            collection = (CollectionItem) getFactory().getContentService()
                    .findItemByUid(sub.getCollectionUid());
        } catch (ItemSecurityException e) {
            // user no longer has access to collection, so treat as non
            // existing collection
            collection = null;
        }
        
        Ticket ticket = collection != null ?
            getFactory().getContentService()
            .getTicket(collection, sub.getTicketKey()) : null;

        String iri = subscriptionIri(sub);

        entry.setTitle(sub.getDisplayName());
        entry.setUpdated(sub.getModifiedDate());
        entry.setEdited(sub.getModifiedDate());
        entry.setPublished(sub.getCreationDate());
        if (isDocument)
            entry.addAuthor(newPerson(sub.getOwner()));
        entry.addLink(newSelfLink(iri));
        entry.addLink(newEditLink(iri));

        XhtmlSubscriptionFormat formatter = new XhtmlSubscriptionFormat();
        entry.setContentAsXhtml(formatter.format(sub, collection, ticket));

        return entry;
    }

    /**
     * Returns the IRI of the given user's subscriptions collection.
     *
     * @param user the user
     */
    protected String subscriptionsIri(User user) {
        StringBuffer iri = new StringBuffer(personIri(user));
        iri.append("/subscriptions");
        return iri.toString();
    }

    /**
     * Returns the IRI of the given subscription.
     *
     * @param sub the subscription
     */
    protected String subscriptionIri(CollectionSubscription sub) {
        try {
            StringBuffer iri = new StringBuffer(personIri(sub.getOwner()));
            iri.append("/subscription/");
            iri.append(URLEncoder.encode(sub.getDisplayName(), "UTF-8"));
            return iri.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode subscription display name " + sub.getDisplayName(), e);
        }
    }
}
