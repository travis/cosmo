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
import java.util.List;
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
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.filter.ContentItemFilter;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
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
    public SortedSet<NoteItem> processTriageStatusQuery(CollectionItem collection,
            String triageStatusLabel, Date pointInTime, TimeZone timezone) {
        if(TriageStatus.LABEL_DONE.equalsIgnoreCase(triageStatusLabel))
            return getDone(collection, pointInTime, timezone);
        else if(TriageStatus.LABEL_NOW.equalsIgnoreCase(triageStatusLabel))
            return getNow(collection, pointInTime, timezone);
        else if(TriageStatus.LABEL_LATER.equalsIgnoreCase(triageStatusLabel))
            return getLater(collection, pointInTime, timezone);
        else
            throw new IllegalArgumentException("invalid status: " + triageStatusLabel);
    }
    
    public SortedSet<NoteItem> processTriageStatusQuery(NoteItem note,
            String triageStatusLabel, Date pointInTime, TimeZone timezone) {
        if(TriageStatus.LABEL_DONE.equalsIgnoreCase(triageStatusLabel))
            return getDone(note, pointInTime, timezone);
        else if(TriageStatus.LABEL_NOW.equalsIgnoreCase(triageStatusLabel))
            return getNow(note, pointInTime, timezone);
        else if(TriageStatus.LABEL_LATER.equalsIgnoreCase(triageStatusLabel))
            return getLater(note, pointInTime, timezone);
        else
            throw new IllegalArgumentException("invalid status: " + triageStatusLabel);
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
    private SortedSet<NoteItem> getNow(CollectionItem collection, Date pointInTime, TimeZone timezone) {
        
        // filter for NOW triage notes
        NoteItemFilter nowFilter = getTriageStatusFilter(collection, TriageStatus.CODE_NOW);
        nowFilter.setIsModification(null);
        
        // filter for no (null) triage status
        NoteItemFilter noTriageStatusFilter = getTriageStatusFilter(collection, -1);
        
        // recurring event filter
        NoteItemFilter eventFilter = getRecurringEventFilter(collection, pointInTime, pointInTime, timezone);
        
        // store results here
        ArrayList<NoteItem> results = new ArrayList<NoteItem>();
        
        // keep track of masters as we have to include them in the result set
        HashSet<NoteItem> masters = new HashSet<NoteItem>();
        
        // Add all non-recurring items that are have an explicit NOW triage,
        // modifications with NOW triage, or no triage (null triage)
        for(Item item : contentDao.findItems(new ItemFilter[] { nowFilter, noTriageStatusFilter })) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = EventStamp.getStamp(note);
            
            // Don't add recurring events
            if(eventStamp==null || eventStamp.isRecurring()==false) {
                results.add(note);
                // keep track of master
                if(note.getModifies()!=null)
                    masters.add(note.getModifies());
            }
        }
        
        // Now process recurring events, returning only occurrences that overlap
        // current instant in time
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null)
                continue;
            Set<NoteItem> occurrences = getNowFromRecurringNote(note, pointInTime, timezone);
            if(occurrences.size()>0) {
                results.addAll(occurrences);
                masters.add(note);
            }
        }
        
        // sort results before returning
        SortedSet<NoteItem> sortedResults =  sortResults(results, COMPARE_ASC, -1); 
        // add masters
        sortedResults.addAll(masters);
        return sortedResults;
    }
    
    /**
     * NOW Query for a specific master NoteItem:<br/>
     *   - Modifications with triage status NOW<br/>
     *   - Occurrences whose period overlaps the current point in time 
     *   - Modifications with triage status null and whose period
     *     overlaps the current point in time.
     */
    private SortedSet<NoteItem> getNow(NoteItem master, Date pointInTime, TimeZone timezone) {
        
        // filter for NOW modifications
        NoteItemFilter nowFilter = getTriageStatusFilter(master, TriageStatus.CODE_NOW);
        
        // store all results here
        ArrayList<NoteItem> results = new ArrayList<NoteItem>();
        
        // Add all modifications triaged as NOW
        for(Item item : contentDao.findItems(nowFilter)) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null)
                results.add(note);
        }
        
        // add all occurrences that occur NOW
        Set<NoteItem> occurrences = getNowFromRecurringNote(master, pointInTime, timezone);
        results.addAll(occurrences);
        
        // sort results before returning
        SortedSet<NoteItem> sortedResults =  sortResults(results, COMPARE_ASC, -1); 
        // add master if necessary
        if(sortedResults.size()>0)
            sortedResults.add(master);
            
        return sortedResults; 
    }
    
    /**
     * Get all instances that are occuring during a given point in time
     */
    private Set<NoteItem> getNowFromRecurringNote(NoteItem note, Date pointInTime, TimeZone timezone) {
        EventStamp eventStamp = EventStamp.getStamp(note);
        DateTime currentDate = new DateTime(pointInTime); 
        RecurrenceExpander expander = new RecurrenceExpander();
        HashSet<NoteItem> results = new HashSet<NoteItem>();
        
        // Get all occurrences that overlap current instance in time
        InstanceList occurrences = expander.getOcurrences(
                eventStamp.getEvent(), eventStamp.getExceptions(), currentDate,
                currentDate, timezone);
        
        for(Instance instance: (Collection<Instance>) occurrences.values()) {
            // Not interested in modifications
            if(!instance.isOverridden()) {
                // add occurrence
                results.add(new NoteOccurrence(instance.getRid(), note));
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
    private SortedSet<NoteItem> getLater(CollectionItem collection, Date pointInTime, TimeZone timezone) {
       
       // filter for LATER triage status 
       NoteItemFilter laterFilter = getTriageStatusFilter(collection, TriageStatus.CODE_LATER);
       
       // recurring event filter
       NoteItemFilter eventFilter = getRecurringEventFilter(collection, pointInTime, laterDur.getTime(pointInTime), timezone);
       
       // store results here
       ArrayList<NoteItem> results = new ArrayList<NoteItem>();
       HashSet<NoteItem> masters = new HashSet<NoteItem>();
       
       // Add all items that are have an explicit LATER triage
       for(Item item : contentDao.findItems(laterFilter)) {
           NoteItem note = (NoteItem) item;
           EventStamp eventStamp = EventStamp.getStamp(note);
           
           // Don't add recurring events
           if(eventStamp==null || eventStamp.isRecurring()==false) {
               results.add(note);
               // keep track of masters
               if(note.getModifies()!=null)
                   masters.add(note.getModifies());
           }
       }
       
       // Now process recurring events
       for(Item item: contentDao.findItems(eventFilter)) {
           NoteItem note = (NoteItem) item;
           if(note.getModifies()!=null)
               continue;
           
           NoteItem laterItem = getLaterFromRecurringNote(note, pointInTime, timezone);
           
           // add laterItem and master if present
           if(laterItem!=null) {
               results.add(laterItem);
               masters.add(note);
           }
           
           // add all modifications with trigaeStatus LATER
           if(results.addAll(getModificationsByTriageStatus(note, TriageStatus.CODE_LATER)))
               results.add(note);
           
       }
       
       // sort results before returning
       SortedSet<NoteItem> sortedResults =  sortResults(results, COMPARE_DESC, -1); 
       // add masters
       sortedResults.addAll(masters);
       return sortedResults;
    }
    
    /**
     * LATER Query for a specific master NoteItem:<br/>
     *   - the next occurring modification 
     *     with triage status LATER or the next occurrence, whichever occurs sooner
     */
    private SortedSet<NoteItem> getLater(NoteItem master, Date pointInTime, TimeZone timezone) {
        TreeSet<NoteItem> results = new TreeSet<NoteItem>(COMPARE_DESC);
        // get the next occurring modification or occurrence
        NoteItem result = getLaterFromRecurringNote(master, pointInTime, timezone);
        
        // add result and master if present
        if(result!=null) {
            results.add(master);
            results.add(result);
        }
        
        // add all modifications with trigaeStatus LATER
        if(results.addAll(getModificationsByTriageStatus(master, TriageStatus.CODE_LATER)))
            results.add(master);
        
        return results;
    }
    
    /**
     * Get the next occurrence or modification for a recurring event, whichever
     * occurrs sooner relative to a point in time.
     */
    private NoteItem getLaterFromRecurringNote(NoteItem note, Date pointInTime, TimeZone timezone) {
        EventStamp eventStamp = EventStamp.getStamp(note);
        Date currentDate = pointInTime;
        Date futureDate = laterDur.getTime(currentDate);
        
        // calculate the next occurrence or LATER modification
        NoteItem first = getFirstInstanceOrModification(eventStamp,
                currentDate, futureDate, timezone);
    
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
    private SortedSet<NoteItem> getDone(CollectionItem collection, Date pointInTime, TimeZone timezone) {
        
        // filter for DONE triage status
        NoteItemFilter doneFilter = getTriageStatusFilter(collection, TriageStatus.CODE_DONE);
        
        // Limit the number of items with DONE status so we don't load
        // tons of items on the server before merging with the recurring
        // item occurrences and sorting.  Anything over this number will
        // be thrown away during the limit/sorting phase so no need to pull
        // more than maxDone items as long as they are sorted by rank.
        doneFilter.setMaxResults(maxDone);
        doneFilter.addOrderBy(ContentItemFilter.ORDER_BY_TRIAGE_STATUS_RANK,
                ItemFilter.ORDER_ASC);
        
        // filter for recurring events
        NoteItemFilter eventFilter = getRecurringEventFilter(collection, doneDur.getTime(pointInTime), pointInTime, timezone);
        
        List<NoteItem> results = new ArrayList<NoteItem>();
        
        // Add all items that are have an explicit DONE triage
        for(Item item : contentDao.findItems(doneFilter)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = EventStamp.getStamp(note);
            
            // Don't add recurring events
            if(eventStamp==null || eventStamp.isRecurring()==false) {
                results.add(note);
            }
        }
        
        // Now process recurring events
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null)
                continue;
            
            NoteItem doneItem = getDoneFromRecurringNote(note, pointInTime, timezone);
            // add doneItem and master if present
            if(doneItem!=null) {
                results.add(doneItem);
            }
            
            // add all modifications with trigaeStatus DONE
            if(results.addAll(getModificationsByTriageStatus(note, TriageStatus.CODE_DONE)))
                results.add(note);
        }
        
        // sort results before returning
        SortedSet<NoteItem> sortedResults =  sortResults(results, COMPARE_ASC, maxDone); 
        Set<NoteItem> masters = new HashSet<NoteItem>();
        
        // add masters for all ocurrences and modifications
        for(NoteItem note: sortedResults)
            if(note instanceof NoteOccurrence)
                masters.add(((NoteOccurrence) note).getMasterNote());
            else if(note.getModifies()!=null)
                masters.add(note.getModifies());
        
        sortedResults.addAll(masters);
        
        return sortedResults;
    }
    
    /**
     * DONE Query for a specific master NoteItem:<br/>
     *   - the last occurring modification 
     *     with triage status DONE or the last occurrence, whichever occurred
     *     most recently
     */
    private SortedSet<NoteItem> getDone(NoteItem master, Date pointInTime, TimeZone timezone) {
        TreeSet<NoteItem> results = new TreeSet<NoteItem>(COMPARE_ASC);
        
        // get the most recently occurred modification or occurrence
        NoteItem result = getDoneFromRecurringNote(master, pointInTime, timezone);
        
        // add result and master if present
        if(result!=null) {
            results.add(master);
            results.add(result);
        }
        
        // add all modifications with trigaeStatus DONE
        if(results.addAll(getModificationsByTriageStatus(master, TriageStatus.CODE_DONE)))
            results.add(master);
        
        return results;
    }
    
    /**
     * Get the last occurring modification or occurrence, whichever occurred
     * last.
     */
    private NoteItem getDoneFromRecurringNote(NoteItem note, Date pointInTime, TimeZone timezone) {
        EventStamp eventStamp = EventStamp.getStamp(note);
        Date currentDate = pointInTime;
        Date pastDate = doneDur.getTime(currentDate);
       
        // calculate the previous occurrence or modification
        NoteItem latest = getLatestInstanceOrModification(eventStamp, pastDate,
                currentDate, timezone);
    
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
                    TriageStatus status = mod.getTriageStatus();
                    if(status==null || status.getCode().equals(TriageStatus.CODE_DONE))
                        return mod;
                } else {
                    return new NoteOccurrence(instance.getRid(), note);
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
                    TriageStatus status = mod.getTriageStatus();
                    if(status==null || status.getCode().equals(TriageStatus.CODE_LATER))
                        return mod;
                } else {
                    return new NoteOccurrence(instance.getRid(), note);
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
     *Sort results using rank calculated from triageStatusRank, eventStart,
     *or lastModified date.  Limit results.
     */
    private SortedSet<NoteItem> sortResults(List<NoteItem> results, Comparator<NoteItem> comparator, int limit) {
        TreeSet<NoteItem> sortedResults = new TreeSet<NoteItem>(comparator);
        Collections.sort(results, comparator);
        
        int toAdd = results.size();
        if(limit > 0 && toAdd > limit)
            toAdd = limit;
        
        for(int i=0;i<toAdd;i++) {
            sortedResults.add(results.get(i));
        }
        
        return sortedResults;
    }
    
    /**
     * Create NoteItemFilter that matches a parent collection and a specific
     * TriageStatus code.  The filter matches only master events (no modifications).
     */
    private NoteItemFilter getTriageStatusFilter(CollectionItem collection, int code) {
        NoteItemFilter triageStatusFilter = new NoteItemFilter();
        triageStatusFilter.setParent(collection);
        triageStatusFilter.setIsModification(Boolean.FALSE);
        triageStatusFilter.setTriageStatus(code);
        return triageStatusFilter;
    }
    
    /**
     * Create NoteItemFilter that matches modifications for a master item with
     * a specific triageStatus
     */
    private NoteItemFilter getTriageStatusFilter(NoteItem master, int code) {
        NoteItemFilter triageStatusFilter = new NoteItemFilter();
        triageStatusFilter.setMasterNoteItem(master);
        triageStatusFilter.setIsModification(Boolean.TRUE);
        triageStatusFilter.setTriageStatus(code);
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

}
