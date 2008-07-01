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

import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

import net.fortuna.ical4j.model.TimeZone;

import org.apache.abdera.model.Feed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.TriageStatusUtil;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.triage.TriageStatusQueryContext;

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
        if (triageStatus != -1)
            TriageStatusUtil.label(triageStatus); // validates legitimacy
        this.triageStatus = triageStatus;
    }

    /**
     * @throws IllegalArgumentException if the provided triage status
     * is not known
     */
    public DashboardFeedGenerator(StandardGeneratorFactory factory,
                                  ServiceLocator locator,
                                  String format)
        throws UnsupportedFormatException {
        this(factory, locator, format, -1);
    }

    // our methods

    /**
     * <p>
     * Returns a sorted set of items from the given collection to
     * include as entries in the feed.
     * </p>
     * <p>
     * Calls {@link ContentService#findNotesByTriageStatus(CollectionItem, String, Date, TimeZone)} to find all relevant <code>NoteItem</code>s for the
     * generator's triage status at the current point in time.
     * </p>
     *
     * @param collection the collection whose contents are to be listed
     */
    protected SortedSet<NoteItem> findContents(CollectionItem collection) {
        String label = triageStatus == -1 ?
            null : TriageStatusUtil.label(triageStatus);
        Date now = Calendar.getInstance().getTime();
        TimeZone tz = null;
        
        // Look for timezone in EventStampFilter if present
        if (getFilter() != null) {
            EventStampFilter esf = (EventStampFilter)
                getFilter().getStampFilter(EventStampFilter.class);
            if(esf!=null)
                tz = esf.getTimezone();
        }

        TriageStatusQueryContext context =
            new TriageStatusQueryContext(label, now, tz);
        return getFactory().getContentService().
            findNotesByTriageStatus(collection, context);
    }

    /**
     * <p>
     * Returns a sorted set of occurrences for the given item to
     * include as entries in the feed.
     * </p>
     * <p>
     * Calls {@link ContentService#findNotesByTriageStatus(NoteItem, String, Date, TimeZone)} to find all relevant <code>NoteItem</code>s for the
     * generator's triage status at the current point in time.
     * </p>
     *
     * @param item the item whose contents are to be listed
     */
    protected SortedSet<NoteItem> findOccurrences(NoteItem item) {
        String label = triageStatus == -1 ?
            null : TriageStatusUtil.label(triageStatus);
        Date now = Calendar.getInstance().getTime();
        TimeZone tz = null;
        if (getFilter() != null) {
            EventStampFilter esf = (EventStampFilter)
                getFilter().getStampFilter(EventStampFilter.class);
            tz = esf.getTimezone();
        }

        TriageStatusQueryContext context =
            new TriageStatusQueryContext(label, now, tz);
        return getFactory().getContentService().
            findNotesByTriageStatus(item, context);
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
        if (triageStatus == TriageStatus.CODE_NOW)
            return PROJECTION_DASHBOARD_NOW;
        return PROJECTION_DASHBOARD;
    }
}
