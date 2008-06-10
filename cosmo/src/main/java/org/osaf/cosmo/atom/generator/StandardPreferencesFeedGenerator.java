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
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.text.XhtmlPreferenceFormat;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * An interface for classes that generate Atom feeds and entries
 * representing collection preferencess.
 *
 * @see Entry
 * @see Feed
 * @see User
 */
public class StandardPreferencesFeedGenerator
    extends BaseFeedGenerator
    implements PreferencesFeedGenerator, AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardPreferencesFeedGenerator.class);

    public StandardPreferencesFeedGenerator(StandardGeneratorFactory factory,
                                            ServiceLocator locator) {
        super(factory, locator);
    }

    // PreferencesFeedGenerator methods

    /**
     * Generates an Atom feed containing entries for a user's
     * preferences.
     *
     * @param user the user
     * @throws GeneratorException
     */
    public Feed generateFeed(User user)
        throws GeneratorException {
        Feed feed = createFeed(user);

        for (Preference pref : findPreferences(user))
            feed.addEntry(createEntry(pref));

        return feed;
    }

    /**
     * Generates an Atom entry representing a specific user
     * preference.
     *
     * @param pref the preference
     * @throws GeneratorException
     */
    public Entry generateEntry(Preference pref)
        throws GeneratorException {
        return createEntry(pref, true);
    }

    // our methods

    /**
     * <p>
     * Returns a sorted set of preference keys to include as
     * entries in the feed.
     * </p>
     * <p>
     * This implementation returns all of the user's preference keys
     * sorted in natural order.
     * </p>
     *
     * @param user the user whose preferences are to be listed
     */
    protected SortedSet<Preference> findPreferences(User user) {
        // XXX sort
        // XXX page

        TreeSet<Preference> prefs =
            new TreeSet<Preference>(new AuditableComparator(true));

        for (Preference pref : user.getPreferences())
            prefs.add(pref);

        return prefs;
    }

    /**
     * Creates a <code>Feed</code> with attributes set based on the
     * given user.
     *
     * @param user the user on which the feed is based
     * @throws GeneratorException
     */
    protected Feed createFeed(User user)
        throws GeneratorException {
        Feed feed = newFeed(user.getUsername()); // XXX: sufficiently unique?

        String title = user.getUsername() + "'s Preferences"; // XXX: i18n
        feed.setTitle(title);
        feed.setUpdated(new Date());
        feed.setGenerator(newGenerator());
        feed.addAuthor(newPerson(user));
        feed.addLink(newSelfLink(preferencesIri(user)));

        return feed;
    }

    /**
     * Creates a <code>Entry</code> with attributes and content based
     * on the given preference. The entry does not represent a
     * document but is meant to be added to a <code>Feed</code>.
     *
     * @param pref the preference
     * @throws GeneratorException
     */
    protected Entry createEntry(Preference pref)
        throws GeneratorException {
        return createEntry(pref, false);
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
    protected Entry createEntry(Preference pref,
                                boolean isDocument)
        throws GeneratorException {
        String uid = pref.getUser().getUsername() + "-" + pref.getKey();
        Entry entry = newEntry(uid, isDocument);

        String iri = preferenceIri(pref);
        Date now = new Date();

        entry.setTitle(pref.getKey());
        entry.setUpdated(pref.getModifiedDate());
        entry.setEdited(pref.getModifiedDate());
        entry.setPublished(pref.getCreationDate());
        if (isDocument)
            entry.addAuthor(newPerson(pref.getUser()));
        entry.addLink(newSelfLink(iri));
        entry.addLink(newEditLink(iri));

        XhtmlPreferenceFormat formatter = new XhtmlPreferenceFormat();
        entry.setContentAsXhtml(formatter.format(pref));

        return entry;
    }

    /**
     * Returns the IRI of the given user's preferences collection.
     *
     * @param user the user
     */
    protected String preferencesIri(User user) {
        StringBuffer iri = new StringBuffer(personIri(user));
        iri.append("/preferences");
        return iri.toString();
    }

    /**
     * Returns the IRI of the given preference.
     *
     * @param pref the preference
     */
    protected String preferenceIri(Preference pref) {
        try {
            StringBuffer iri = new StringBuffer(personIri(pref.getUser()));
            iri.append("/preference/").
                append(URLEncoder.encode(pref.getKey(), "UTF-8"));
            return iri.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode preferences display name " + pref.getKey(), e);
        }
    }
}
