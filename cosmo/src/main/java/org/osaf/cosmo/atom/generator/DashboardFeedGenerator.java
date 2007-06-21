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

import net.fortuna.ical4j.model.TimeZone;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItemComparator;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * <p>
 * Generates a dashboard feed for a collection.
 * </p>
 * <p>
 * The contents of a dashboard feed are determined by a provided
 * triage status. For a given triage status, the generator implements
 * specific rules that determine which items, modifications and
 * occurrences are included in the feed.
 * </p>
 *
 * @see Feed
 * @see CollectionItem
 * @see TriageStatus
 */
public class DashboardFeedGenerator extends FullFeedGenerator {
    private static final Log log =
        LogFactory.getLog(DashboardFeedGenerator.class);
    private static final int MAX_DONE = 25;

    private int triageStatus;

    /**
     * @throws IllegalArgumentException if the provided triage status
     * is not known
     */
    public DashboardFeedGenerator(StandardGeneratorFactory factory,
                                  ServiceLocator locator,
                                  String format,
                                  int triageStatus)
        throws UnsupportedFormatException {
        super(factory, locator, format);
        TriageStatus.label(triageStatus); // validates legitimacy
        this.triageStatus = triageStatus;
    }

    // our methods

    /**
     * <p>
     * Returns a sorted set of items from the given collection to
     * include as entries in the feed.
     * </p>
     * <p>
     * Calls {@link ContentService#findNotesByTriageStatus(CollectionItem, String, Date)} to find all relevant <code>NoteItem</code>s for the
     * generator's triage status at the current point in time. Returns
     * the notes sorted according to triage status rank. Limits
     * the number of <code>DONE</code> items to {@link #MAX_DONE}.
     * Performs a dashboard query for the generator's triage status
     * </p>
     *
     * @param the collection whose contents are to be listed
     */
    protected SortedSet<NoteItem> findContents(CollectionItem collection) {
        String label = TriageStatus.label(triageStatus);
        Date now = Calendar.getInstance().getTime();
        TimeZone tz = null;
        if (getFilter() != null) {
            EventStampFilter esf = (EventStampFilter)
                getFilter().getStampFilter(EventStampFilter.class);
            tz = esf.getTimezone();
        }

        Set<NoteItem> notes = getFactory().getContentService().
            findNotesByTriageStatus(collection, label, now, tz);

        // sort by triage rank
        TreeSet<NoteItem> contents =
            new TreeSet<NoteItem>(new ContentItemComparator());
        contents.addAll(notes);

        if (triageStatus == TriageStatus.CODE_DONE) {
            // limit to 25 entries
            if (contents.size() > MAX_DONE) {
                int size = contents.size();
                for(int i=0;i<(size-25);i++)
                    contents.remove(contents.first());
            }
        }

        return contents;
    }

    /**
     * Returns one of {@link AtomConstants#PROJECTION_DASHBOARD_NOW},
     * {@link AtomConstants#PROJECTION_DASHBOARD_LATER}, or
     * {link AtomConstants#PROJECTION_DASHBOARD_DONE}.
     */
    protected String getProjection() {
        if (triageStatus == TriageStatus.CODE_DONE)
            return PROJECTION_DASHBOARD_DONE;
        if (triageStatus == TriageStatus.CODE_LATER)
            return PROJECTION_DASHBOARD_LATER;
        return PROJECTION_DASHBOARD_NOW;
    }
}
