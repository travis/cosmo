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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItemComparator;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
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
     * Performs a dashboard query for the generator's triage status
     * and returns the resulting <code>NoteItem</code>s sorted
     * according to triage status rank.
     * </p>
     *
     * @param the collection whose contents are to be listed
     */
    protected SortedSet<NoteItem> findContents(CollectionItem collection) {
        // stupid grouping and sorting of items by triage
        // status. replace with service queries.

        TreeSet<NoteItem> contents =
            new TreeSet<NoteItem>(new ContentItemComparator());

        for (Item child : collection.getChildren()) {
            if (! (child instanceof NoteItem))
                continue;
            NoteItem note = (NoteItem) child;
            if (triageStatus == TriageStatus.CODE_NOW)
                addIfNow(contents, note);
            else if (triageStatus == TriageStatus.CODE_LATER)
                addIfLater(contents, note);
            else
                addIfDone(contents, note);
        }

        if (triageStatus == TriageStatus.CODE_DONE) {
            // limit to 25 entries
            if (contents.size() > MAX_DONE) {
                Iterator it = contents.iterator();
                int i = 0;
                while (it.hasNext()) {
                    i++;
                    if (i > MAX_DONE)
                        it.remove();
                }
            }
        }

        return contents;
    }

    private void addIfNow(SortedSet contents,
                          NoteItem note) {
        EventStamp stamp = EventStamp.getStamp(note);
        if (stamp != null && stamp.isRecurring()) {
            // XXX: occurrences whose period overlaps the current
            // point in time
        } else {
            // non-recurring with no or null triage status
            if (note.getTriageStatus() == null ||
                note.getTriageStatus().getCode() == null ||
                // non-recurring or modifications with triage status NOW
                note.getTriageStatus().getCode() == TriageStatus.CODE_NOW)
                contents.add(note);
        }
    }

    private void addIfLater(SortedSet contents,
                            NoteItem note) {
        EventStamp stamp = EventStamp.getStamp(note);
        if (stamp != null && stamp.isRecurring()) {
            // XXX: either the next occurring modification with triage
            // status LATER or the next occurrence, whichever occurs
            // sooner
        }
        // non-recurring with triage status LATER
        if (note.getTriageStatus() == null)
            return;
        if (note.getTriageStatus().getCode() == null)
            return;
        if (note.getTriageStatus().getCode() == TriageStatus.CODE_LATER)
            contents.add(note);
    }

    private void addIfDone(SortedSet contents,
                           NoteItem note) {
        EventStamp stamp = EventStamp.getStamp(note);
        if (stamp != null && stamp.isRecurring()) {
            // XXX: either the most recently occurring modification
            // with triage status DONE or the most recent occurrence,
            // whichever occurred more recently
        }
        // non-recurring with triage status DONE
        if (note.getTriageStatus() == null)
            return;
        if (note.getTriageStatus().getCode() == null)
            return;
        if (note.getTriageStatus().getCode() == TriageStatus.CODE_DONE)
            contents.add(note);
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
