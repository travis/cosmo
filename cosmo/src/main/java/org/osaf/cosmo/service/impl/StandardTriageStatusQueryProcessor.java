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
package org.osaf.cosmo.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.Instance;
import org.osaf.cosmo.calendar.InstanceList;
import org.osaf.cosmo.calendar.RecurrenceExpander;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteItemTriageStatusComparator;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.NoteOccurrenceUtil;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.filter.ContentItemFilter;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.Restrictions;
import org.osaf.cosmo.service.triage.TriageStatusQueryContext;
import org.osaf.cosmo.service.triage.TriageStatusQueryProcessor;

/**
 * Standard implementation of TriageStatusQueryProcessor that
 * uses NoteItemFilters and custom logic to process a 
 * TriageStatus query.
 */
public class StandardTriageStatusQueryProcessor implements
        TriageStatusQueryProcessor {

    private ContentDao contentDao = null;
    
    private static final Log log = LogFactory.getLog(StandardTriageStatusQueryProcessor.class);
    private static final Comparator<NoteItem> COMPARE_ASC = new NoteItemTriageStatusComparator(false);
    private static final Comparator<NoteItem> COMPARE_DESC = new NoteItemTriageStatusComparator(true);
    
    // Duration to search forward/backward for recurring events
    // is set to 30 days by default
    private Dur laterDur = new Dur("P30D");
    private Dur doneDur = new Dur("-P30D");
    private int maxDone = 25;
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.service.triage.TriageStatusQueryProcessor#processTriageStatusQuery(org.osaf.cosmo.model.CollectionItem, java.lang.String, java.util.Date, net.fortuna.ical4j.model.TimeZone)
     */
    public SortedSet<NoteItem>
        processTriageStatusQuery(CollectionItem collection,
                                 TriageStatusQueryContext context) {
        if (context.isAll())
            return getAll(collection, context).merge();
        if (context.isDone())
            return getDone(collection, context).merge();
        else if (context.isNow())
            return getNow(collection, context).merge();
        else if (context.isLater())
            return getLater(collection, context).merge();
        else
            throw new IllegalArgumentException("invalid status: " + context.getTriageStatus());
    }
    
    public SortedSet<NoteItem>
        processTriageStatusQuery(NoteItem note,
                                 TriageStatusQueryContext context) {
        if (context.isAll())
            return getAll(note, context).merge();
        if (context.isDone())
            return getDone(note, context).merge();
        else if (context.isNow())
            return getNow(note, context).merge();
        else if (context.isLater())
            return getLater(note, context).merge();
        else
            throw new IllegalArgumentException("invalid status: " + context.getTriageStatus());
    }

    private QueryResult getAll(CollectionItem collection,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();

        qr.add(getNow(collection, context));
        qr.add(getDone(collection, context));
        qr.add(getLater(collection, context));

        return qr;
    }

    private QueryResult getAll(NoteItem note,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();

        qr.add(getNow(note, context));
        qr.add(getDone(note, context));
        qr.add(getLater(note, context));

        return qr;
    }

    /**
     * NOW Query:<br/>
     *   - Non-recurring with no or null triage status<br/>
     *   - Non-recurring with triage status NOW<br/>
     *   - Modifications with triage status NOW<br/>
     *   - Occurrences whose period overlaps the current point in time 
     *   - Modifications with triage status null and whose period
     *     overlaps the current point in time.
     */
    private QueryResult getNow(CollectionItem collection,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();

        // filter for NOW triage notes
        NoteItemFilter nowFilter =
            getTriageStatusFilter(collection, TriageStatus.CODE_NOW);
        
        // filter for no (null) triage status
        NoteItemFilter noTriageStatusFilter =
            getTriageStatusFilter(collection, -1);
        noTriageStatusFilter.setIsModification(false);
        
        // recurring event filter
        NoteItemFilter eventFilter =
            getRecurringEventFilter(collection, context.getPointInTime(),
                                    context.getPointInTime(),
                                    context.getTimeZone());
        
        // Add all non-recurring items that are have an explicit NOW triage,
        // modifications with NOW triage, or no triage (null triage)
        ItemFilter[] filters = new ItemFilter[] {
            nowFilter, noTriageStatusFilter
        };
        for(Item item : contentDao.findItems(filters)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = StampUtils.getEventStamp(note);
            
            // Don't add recurring events
            if(eventStamp==null || eventStamp.isRecurring()==false) {
                qr.getResults().add(note);
                // keep track of master
                if(note.getModifies()!=null)
                    qr.getMasters().add(note.getModifies());
            }
        }
        
        // Now process recurring events, returning only occurrences that overlap
        // current instant in time
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null)
                continue;
            Set<NoteItem> occurrences =
                getNowFromRecurringNote(note, context);
            if(occurrences.size()>0) {
                qr.getResults().addAll(occurrences);
                qr.getMasters().add(note);
            }
        }

        return qr;
    }
    
    /**
     * NOW Query for a specific master NoteItem:<br/>
     *   - Modifications with triage status NOW<br/>
     *   - Occurrences whose period overlaps the current point in time 
     *   - Modifications with triage status null and whose period
     *     overlaps the current point in time.
     */
    private QueryResult getNow(NoteItem master,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();
        Set<NoteItem> mods = null;

        mods = getModificationsByTriageStatus(master, TriageStatus.CODE_NOW);
        qr.getResults().addAll(mods);
        
        // add all occurrences that occur NOW
        mods = getNowFromRecurringNote(master, context);
        qr.getResults().addAll(mods);

        // add master if necessary
        if (! qr.getResults().isEmpty())
            qr.getMasters().add(master);

        return qr;
    }
    
    /**
     * Get all instances that are occuring during a given point in time
     */
    private Set<NoteItem>
        getNowFromRecurringNote(NoteItem note,
                                TriageStatusQueryContext context) {
        EventStamp eventStamp = StampUtils.getEventStamp(note);
        DateTime currentDate = new DateTime(context.getPointInTime()); 
        RecurrenceExpander expander = new RecurrenceExpander();
        HashSet<NoteItem> results = new HashSet<NoteItem>();
        
        // Get all occurrences that overlap current instance in time
        InstanceList occurrences = expander.getOcurrences(
                eventStamp.getEvent(), eventStamp.getExceptions(), currentDate,
                currentDate, context.getTimeZone());
        
        for(Instance instance: (Collection<Instance>) occurrences.values()) {
            // Not interested in modifications
            if(!instance.isOverridden()) {
                // add occurrence
                results.add(NoteOccurrenceUtil.createNoteOccurrence(instance.getRid(), note));
            } else {
                // return modification if it has no triage-status
                ModificationUid modUid = new ModificationUid(note, instance.getRid());
                NoteItem mod = (NoteItem) contentDao.findItemByUid(modUid.toString());
                if(mod.getTriageStatus()==null || mod.getTriageStatus().getCode()==null)
                    results.add(mod);
            }
        }
        
        return results;
    }
    
    /**
     * LATER Query:<br/>
     *   - Non-recurring with triage status LATER<br/>
     *   - For each recurring item, either the next occurring modification 
     *     with triage status LATER or the next occurrence, whichever occurs sooner 
     */
    private QueryResult getLater(CollectionItem collection,
                                 TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult(false, -1);

        // filter for LATER triage status 
        NoteItemFilter laterFilter =
            getTriageStatusFilter(collection, TriageStatus.CODE_LATER);
       
        // recurring event filter
        NoteItemFilter eventFilter =
            getRecurringEventFilter(collection, context.getPointInTime(),
                                    laterDur.getTime(context.getPointInTime()),
                                    context.getTimeZone());

        // Add all items that are have an explicit LATER triage
        for(Item item : contentDao.findItems(laterFilter)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = StampUtils.getEventStamp(note);

            // Don't add recurring events
            if(eventStamp==null || eventStamp.isRecurring()==false) {
                qr.getResults().add(note);
                // keep track of masters
                if(note.getModifies()!=null)
                    qr.getMasters().add(note.getModifies());
            }
        }

        // Now process recurring events
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            
            // per bug 10623:
            // return all modifications for later
            if(note.getModifies()!=null) {
                qr.getResults().add(note);
                qr.getMasters().add(note.getModifies());
                continue;
            }
           
            NoteItem laterItem =
                getLaterFromRecurringNote(note, context);
           
            // add laterItem and master if present
            if(laterItem!=null) {
                qr.getResults().add(laterItem);
                qr.getMasters().add(note);
            }
        }

        return qr;
    }
    
    /**
     * LATER Query for a specific master NoteItem:<br/>
     *   - the next occurring modification 
     *     with triage status LATER or the next occurrence, whichever occurs sooner
     */
    private QueryResult getLater(NoteItem master,
                                 TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult(false, -1);

        // get the next occurring modification or occurrence
        NoteItem result = getLaterFromRecurringNote(master, context);
        
        // add result and master if present
        if(result!=null) {
            qr.getMasters().add(master);
            qr.getResults().add(result);
        }
        
        // add all modifications with trigaeStatus LATER
        Set<NoteItem> mods =
            getModificationsByTriageStatus(master, TriageStatus.CODE_LATER);
        if(qr.getResults().addAll(mods))
            qr.getMasters().add(master);
        
        return qr;
    }
    
    /**
     * Get the next occurrence or modification for a recurring event, whichever
     * occurrs sooner relative to a point in time.
     */
    private NoteItem
        getLaterFromRecurringNote(NoteItem note,
                                  TriageStatusQueryContext context) {
        EventStamp eventStamp = StampUtils.getEventStamp(note);
        Date currentDate = context.getPointInTime();
        Date futureDate = laterDur.getTime(currentDate);

        // calculate the next occurrence or LATER modification
        NoteItem first = getFirstInstanceOrModification(eventStamp,
                currentDate, futureDate, context.getTimeZone());

        return first;
    }
    
    /**
     * DONE Query:<br/>
     *   - Non-recurring with triage status DONE<br/>
     *   - For each recurring item, either the most recently occurring 
     *     modification with triage status DONE or the most recent occurrence,
     *     whichever occurred most recently 
     *   - Limit to maxDone results
     */
    private QueryResult getDone(CollectionItem collection,
                                TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult(true, maxDone);

        // filter for DONE triage status
        NoteItemFilter doneFilter =
            getTriageStatusFilter(collection, TriageStatus.CODE_DONE);

        // Limit the number of items with DONE status so we don't load
        // tons of items on the server before merging with the recurring
        // item occurrences and sorting.  Anything over this number will
        // be thrown away during the limit/sorting phase so no need to pull
        // more than maxDone items as long as they are sorted by rank.
        doneFilter.setMaxResults(maxDone);
        doneFilter.addOrderBy(ContentItemFilter.ORDER_BY_TRIAGE_STATUS_RANK_ASC);
        
        // filter for recurring events
        NoteItemFilter eventFilter =
            getRecurringEventFilter(collection,
                                    doneDur.getTime(context.getPointInTime()),
                                    context.getPointInTime(),
                                    context.getTimeZone());

        // Add all items that are have an explicit DONE triage
        for(Item item : contentDao.findItems(doneFilter)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = StampUtils.getEventStamp(note);
            
            // Don't add recurring events
            if(eventStamp==null || eventStamp.isRecurring()==false) {
                qr.getResults().add(note);
            }
        }

        // Now process recurring events
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null)
                continue;
            
            NoteItem doneItem = getDoneFromRecurringNote(note, context);
            // add doneItem and master if present
            if(doneItem!=null) {
                qr.getResults().add(doneItem);
            }
        }
        
        // add masters for all ocurrences and modifications
        for(NoteItem note: qr.getResults())
            if(note instanceof NoteOccurrence)
                qr.getMasters().add(((NoteOccurrence) note).getMasterNote());
            else if(note.getModifies()!=null)
                qr.getMasters().add(note.getModifies());
        
        return qr;
    }
    
    /**
     * DONE Query for a specific master NoteItem:<br/>
     *   - the last occurring modification 
     *     with triage status DONE or the last occurrence, whichever occurred
     *     most recently
     */
    private QueryResult getDone(NoteItem master,
                                TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();
        
        // get the most recently occurred modification or occurrence
        NoteItem result = getDoneFromRecurringNote(master, context);
        
        // add result and master if present
        if(result!=null) {
            qr.getMasters().add(master);
            qr.getResults().add(result);
        }
        
        // add all modifications with trigaeStatus DONE
        Set<NoteItem> mods =
            getModificationsByTriageStatus(master, TriageStatus.CODE_DONE);
        if(qr.getResults().addAll(mods))
            qr.getMasters().add(master);
        
        return qr;
    }
    
    /**
     * Get the last occurring modification or occurrence, whichever occurred
     * last.
     */
    private NoteItem
        getDoneFromRecurringNote(NoteItem note,
                                 TriageStatusQueryContext context) {
        EventStamp eventStamp = StampUtils.getEventStamp(note);
        Date currentDate = context.getPointInTime();
        Date pastDate = doneDur.getTime(currentDate);
       
        // calculate the previous occurrence or modification
        NoteItem latest = getLatestInstanceOrModification(eventStamp, pastDate,
                currentDate, context.getTimeZone());
    
        return latest;
    }
    
    
    /**
     * Calculate and return the latest ocurring instance or modification for the 
     * specified master event and date range.
     * The instance must end before the end of the range.
     * If the latest instance is a modification, then the modification must
     * have a triageStatus of DONE
     *
     */
    private NoteItem getLatestInstanceOrModification(EventStamp event, Date rangeStart, Date rangeEnd,
            TimeZone timezone) {
        NoteItem note = (NoteItem) event.getItem();
        RecurrenceExpander expander = new RecurrenceExpander();
        
        InstanceList instances = expander.getOcurrences(event.getEvent(), event.getExceptions(),
                new DateTime(rangeStart), new DateTime(rangeEnd), timezone);

        // Find the latest occurrence that ends before the end of the range
        while (instances.size() > 0) {
            String lastKey = (String) instances.lastKey();
            Instance instance = (Instance) instances.remove(lastKey);
            if (instance.getEnd().before(rangeEnd)) {
                if(instance.isOverridden()) {
                    ModificationUid modUid = new ModificationUid(note, instance.getRid());
                    NoteItem mod = (NoteItem) contentDao.findItemByUid(modUid.toString());
                    // shouldn't happen, but log and continue if it does
                    if(mod==null) {
                        log.error("no modification found for uid: " + modUid.toString());
                        continue;
                    }
                    TriageStatus status = mod.getTriageStatus();
                    if(status==null || status.getCode().equals(TriageStatus.CODE_DONE))
                        return mod;
                } else {
                    return NoteOccurrenceUtil.createNoteOccurrence(instance.getRid(), note);
                }
            }
                
        }

        return null;
    }
    
    
    /**
     * Calculate and return the first ocurring instance or modification
     * for the specified master event and date range.
     * The instance must begin after the start of the range and if it
     * is a modification it must have a triageStatus of LATER.
     * 
     */
    private NoteItem getFirstInstanceOrModification(EventStamp event, Date rangeStart, Date rangeEnd, TimeZone timezone) {
        NoteItem note = (NoteItem) event.getItem();
        RecurrenceExpander expander = new RecurrenceExpander();
        
        InstanceList instances = expander.getOcurrences(event.getEvent(), event.getExceptions(), new DateTime(rangeStart), new DateTime(rangeEnd), timezone );
     
        // Find the first occurrence that begins after the start range
        while(instances.size()>0) {
            String firstKey = (String) instances.firstKey();
            Instance instance = (Instance) instances.remove(firstKey);
            if(instance.getStart().after(rangeStart)) {
                if(instance.isOverridden()) {
                    ModificationUid modUid = new ModificationUid(note, instance.getRid());
                    NoteItem mod = (NoteItem) contentDao.findItemByUid(modUid.toString());
                    // shouldn't happen, but log and continue if it does
                    if(mod==null) {
                        log.error("no modification found for uid: " + modUid.toString());
                        continue;
                    }
                    TriageStatus status = mod.getTriageStatus();
                    if(status==null || status.getCode().equals(TriageStatus.CODE_LATER))
                        return mod;
                } else {
                    return NoteOccurrenceUtil.createNoteOccurrence(instance.getRid(), note);
                }
            }   
        }
        
        return null;
    }
    
    
    private Set<NoteItem> getModificationsByTriageStatus(NoteItem master, Integer triageStatus) {
        
        HashSet<NoteItem> mods = new HashSet<NoteItem>();
        
        for(NoteItem mod: master.getModifications()) {
            if (mod.getTriageStatus() == null
                    || mod.getTriageStatus().getCode() == null
                    || mod.getTriageStatus().getCode().equals(triageStatus))
                continue;
            
           mods.add(mod);
        }
        
        return mods;
    }
    
    /**
     * Create NoteItemFilter that matches a parent collection and a specific
     * TriageStatus code.  The filter matches only master events (no modifications).
     */
    private NoteItemFilter getTriageStatusFilter(CollectionItem collection, int code) {
        NoteItemFilter triageStatusFilter = new NoteItemFilter();
        triageStatusFilter.setParent(collection);
        if(code==-1)
            triageStatusFilter.setTriageStatusCode(Restrictions.isNull());
        else
            triageStatusFilter.setTriageStatusCode(Restrictions.eq(new Integer(code)));
        return triageStatusFilter;
    }
    
    
    /**
     * Create NoteItemFilter that matches all recurring event NoteItems that belong
     * to a specified parent collection.
     */
    private NoteItemFilter getRecurringEventFilter(CollectionItem collection, Date start, Date end, TimeZone timezone) {
        NoteItemFilter eventNoteFilter = new NoteItemFilter();
        eventNoteFilter.setFilterProperty(EventStampFilter.PROPERTY_DO_TIMERANGE_SECOND_PASS, "false");
        EventStampFilter eventFilter = new EventStampFilter();
        eventFilter.setIsRecurring(true);
        eventFilter.setTimeRange(new DateTime(start), new DateTime(end));
        eventFilter.setTimezone(timezone);
        eventNoteFilter.setParent(collection);
        eventNoteFilter.getStampFilters().add(eventFilter);
        return eventNoteFilter;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    public void setDoneDuration(String doneDuration) {
        doneDur = new Dur(doneDuration);
    }

    public void setLaterDuration(String laterDuration) {
        laterDur = new Dur(laterDuration);
    }

    public void setMaxDone(int maxDone) {
        this.maxDone = maxDone;
    }

    private class QueryResult {
        private ArrayList<NoteItem> results = new ArrayList<NoteItem>();
        private HashSet<NoteItem> masters = new HashSet<NoteItem>();
        private Comparator<NoteItem> comparator;
        private int limit;

        public QueryResult() {
            this(true, -1);
        }

        public QueryResult(boolean ascending, int limit) {
            results = new ArrayList<NoteItem>();
            masters = new HashSet<NoteItem>();
            comparator = ascending ? COMPARE_ASC : COMPARE_DESC;
            this.limit = limit;
        }

        public ArrayList<NoteItem> getResults() {
            return results;
        }
        
        public void processResults() {
            
            // sort
            Collections.sort(results, comparator);

            // trim based on limit
            if(limit!=-1 && results.size() > limit) {
                while(results.size() > limit)
                    results.remove(results.size()-1);
                
                // rebuild masters list as it may have changed
                masters.clear();
                for(NoteItem note: results) {
                    if(note instanceof NoteOccurrence)
                        masters.add(((NoteOccurrence) note).getMasterNote());
                    else if(note.getModifies()!=null)
                        masters.add(note.getModifies());
                }
            }
        }
        
        public HashSet<NoteItem> getMasters() {
            return masters;
        }

        public void add(QueryResult qr) {
            qr.processResults();
            results.addAll(qr.getResults());
            masters.addAll(qr.getMasters());
        }

        public SortedSet<NoteItem> merge() {
            
            TreeSet<NoteItem> merged = new TreeSet<NoteItem>(comparator);
            merged.addAll(results);
            merged.addAll(masters);

            return merged;
        }
    }
}
