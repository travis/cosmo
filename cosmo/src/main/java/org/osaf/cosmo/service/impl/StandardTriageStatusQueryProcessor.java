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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;

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
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.TriageStatus;
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
    
    // Duration to search forward/backward for recurring events
    // is set to 30 days by default
    private Dur laterDur = new Dur("P30D");
    private Dur doneDur = new Dur("-P30D");
    private int maxDone = 25;
    
    public Set<NoteItem> processTriageStatusQuery(CollectionItem collection,
            String triageStatusLabel, Date pointInTime) {
        if(TriageStatus.LABEL_DONE.equalsIgnoreCase(triageStatusLabel))
            return getDone(collection, pointInTime);
        else if(TriageStatus.LABEL_NOW.equalsIgnoreCase(triageStatusLabel))
            return getNow(collection, pointInTime);
        else if(TriageStatus.LABEL_LATER.equalsIgnoreCase(triageStatusLabel))
            return getLater(collection, pointInTime);
        else
            throw new IllegalArgumentException("invalid status: " + triageStatusLabel);
    }
    
    /**
     * NOW Query:<br/>
     *   - Non-recurring with no or null triage status<br/>
     *   - Non-recurring with triage status NOW<br/>
     *   - Modifications with triage status NOW<br/>
     *   - Occurrences whose period overlaps the current point in time 
     */
    private Set<NoteItem> getNow(CollectionItem collection, Date pointInTime) {
        
        // filter for NOW triage notes
        NoteItemFilter nowFilter = getTriageStatusFilter(collection, TriageStatus.CODE_NOW);
        nowFilter.setIsModification(null);
        
        // filter for no (null) triage status
        NoteItemFilter noTriageStatusFilter = getTriageStatusFilter(collection, -1);
        
        // recurring event filter
        NoteItemFilter eventFilter = getRecurringEventFilter(collection);
        
        // store all results here
        Set<NoteItem> results = new HashSet<NoteItem>();
        
        // Add all non-recurring items that are have an explicit NOW triage,
        // modifications with NOW triage, or no triage (null triage)
        for(Item item : contentDao.findItems(new ItemFilter[] { nowFilter, noTriageStatusFilter })) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = EventStamp.getStamp(note);
            
            // Don't add recurring events
            if(eventStamp==null || eventStamp.isRecurring()==false)
                results.add(note);
        }
            
        RecurrenceExpander expander = new RecurrenceExpander();
        DateTime currentDate = new DateTime(pointInTime); 
        
        // Now process recurring events, returning only occurrences that overlap
        // current instant in time
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = EventStamp.getStamp(note);
           
            // Get all occurrences that overlap current instance in time
            InstanceList occurrences = expander.getOcurrences(eventStamp.getCalendar(), currentDate, currentDate);
            
            for(Instance instance: (Collection<Instance>) occurrences.values()) {
                // Not interested in modifications
                if(!instance.isOverridden()) {
                    // add occurrence
                    results.add(new NoteOccurrence(instance.getRid(), note));
                    // add master
                    results.add(note);
                }
            }
        }
        
        // TODO: sorting
        return results; 
    }
    
    /**
     * LATER Query:<br/>
     *   - Non-recurring with triage status LATER<br/>
     *   - For each recurring item, either the next occurring modification 
     *     with triage status LATER or the next occurrence, whichever occurs sooner 
     */
    private Set<NoteItem> getLater(CollectionItem collection, Date pointInTime) {
       
       // filter for LATER triage status 
       NoteItemFilter laterFilter = getTriageStatusFilter(collection, TriageStatus.CODE_LATER);
       
       // recurring event filter
       NoteItemFilter eventFilter = getRecurringEventFilter(collection);
       
       Set<NoteItem> results = new HashSet<NoteItem>();
       
       // Add all items that are have an explicit LATER triage
       for(Item item : contentDao.findItems(laterFilter))
           results.add((NoteItem) item);
       
       Date currentDate = pointInTime;
       Date futureDate = laterDur.getTime(currentDate);
       RecurrenceExpander expander = new RecurrenceExpander();
       
       // Now process recurring events
       for(Item item: contentDao.findItems(eventFilter)) {
           NoteItem note = (NoteItem) item;
           EventStamp eventStamp = EventStamp.getStamp(note);
           
           // calculate the next occurrence or modification of the recurring event
           Instance instance = 
               expander.getFirstInstance(eventStamp.getCalendar(), new DateTime(currentDate), new DateTime(futureDate));
       
           if(instance!=null) {
               // add master
               results.add(note);
               if(instance.isOverridden()==false) {
                   // add occurrence
                   results.add(new NoteOccurrence(instance.getRid(), note));
               }
               else {
                   // add modification
                   ModificationUid modUid = new ModificationUid(note, instance.getRid());
                   results.add((NoteItem) contentDao.findItemByUid(modUid.toString()));
               }
           }
       }
       
       // TODO: sorting
       return results;
    }
    
    /**
     * DONE Query:<br/>
     *   - Non-recurring with triage status DONE<br/>
     *   - For each recurring item, either the most recently occurring 
     *     modification with triage status DONE or the most recent occurrence,
     *     whichever occurred most recently 
     */
    private Set<NoteItem> getDone(CollectionItem collection, Date pointInTime) {
        
        // filter for DONE triage status
        NoteItemFilter doneFilter = getTriageStatusFilter(collection, TriageStatus.CODE_DONE);
        
        // filter for recurring events
        NoteItemFilter eventFilter = getRecurringEventFilter(collection);
        
        Set<NoteItem> results = new HashSet<NoteItem>();
        
        // Add all items that are have an explicit DONE triage
        for(Item item : contentDao.findItems(doneFilter))
            results.add((NoteItem) item);
        
        Date currentDate = pointInTime;
        Date pastDate = doneDur.getTime(currentDate);
        RecurrenceExpander expander = new RecurrenceExpander();
        
        // Now process recurring events
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = EventStamp.getStamp(note);
           
            // calculate the previous occurrence or modification
            Instance instance = 
                expander.getLatestInstance(eventStamp.getCalendar(), new DateTime(pastDate), new DateTime(currentDate));
        
            if(instance!=null) {
                // add master
                results.add(note);
                if(instance.isOverridden()==false) {
                    // add occurrence
                    results.add(new NoteOccurrence(instance.getRid(), note));
                    
                }
                else {
                    // add modification
                    ModificationUid modUid = new ModificationUid(note, instance.getRid());
                    results.add((NoteItem) contentDao.findItemByUid(modUid.toString()));
                }
            }
        }
        
        // TODO: limit to maxDone items and sorting
        return results;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
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
     * Create NoteItemFilter that matches all recurring event NoteItems that belong
     * to a specified parent collection.
     */
    private NoteItemFilter getRecurringEventFilter(CollectionItem collection) {
        NoteItemFilter eventNoteFilter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        eventFilter.setIsRecurring(true);
        eventNoteFilter.setParent(collection);
        eventNoteFilter.getStampFilters().add(eventFilter);
        return eventNoteFilter;
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
