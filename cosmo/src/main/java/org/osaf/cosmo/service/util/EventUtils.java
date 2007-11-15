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
package org.osaf.cosmo.service.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStamp;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.lang.StringUtils;
import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.service.ContentService;

/**
 * Contains utility methods for creating and updating
 * event NoteItems using an ical4j Calendar.  This is 
 * useful for protocols like CalDAV which operate on
 * resources that may contain a master and modification
 * components.  Cosmo represents these resources as 
 * multiple items (master and modification items).
 */
public class EventUtils {
    
    private static IdentifierGenerator UID_GENERATOR = new VersionFourGenerator();
    
    /**
     * Create a new event based on an ical4j Calendar.  This will 
     * create the master NoteItem and any modification NoteItem's 
     * for each VEVENT modification.
     * 
     * @parem service ContentService to use to create new event and modification items
     * @param parent parent collection
     * @param masterNote master note item
     * @param calendar Calendar containing master/override VEVENTs
     * @return newly created master note item
     */
    public static NoteItem createEvent(ContentService service, CollectionItem parent, NoteItem note, Calendar calendar) {
        
        EventStamp eventStamp = (EventStamp) note.getStamp(EventStamp.class);
        if(eventStamp==null) {
            eventStamp = new EventStamp(note);
            note.addStamp(eventStamp);
        }
        
        // Need to set uid to be able to set modification uid because
        // modification uids require the master uid to be known
        note.setUid(UID_GENERATOR.nextIdentifier().toString());
        
        updateEventInternal(note, calendar);
        
        LinkedHashSet<ContentItem> itemsToCreate = new LinkedHashSet<ContentItem>();
        itemsToCreate.add(note);
        for(NoteItem mod: note.getModifications())
            itemsToCreate.add(mod);
        
        service.createContentItems(parent, itemsToCreate);
        return note;
    }
    
    /**
     * Update existing event (NoteItem with EventStamp) based on 
     * an ical4j Calendar.  This will update the master NoteItem and 
     * any modification NoteItem's for each VEVENT modification, including
     * removing/adding modification NoteItems.
     * 
     * @parem service ContentService to use to update event and modification items
     * @param note master note item to update
     * @param calendar Calendar containing master/override VEVENTs
     */
    public static void updateEvent(ContentService service, NoteItem note, Calendar calendar) {
        updateEventInternal(note, calendar);
        
        LinkedHashSet<ContentItem> itemsToUpdate = new LinkedHashSet<ContentItem>();
        itemsToUpdate.add(note);
        
        // add modifications to set of items to update
        for(Iterator<NoteItem> it = note.getModifications().iterator(); it.hasNext();) {
            NoteItem mod = it.next();
            itemsToUpdate.add(mod);
        }
      
        service.updateContentItems(note.getParents(), itemsToUpdate);
    }
    
    
    private static void updateEventInternal(NoteItem masterNote, Calendar calendar) {
        HashMap<Date, VEvent> exceptions = new HashMap<Date, VEvent>();
        
        Calendar masterCalendar = calendar;
        
        ComponentList vevents = masterCalendar.getComponents().getComponents(
                Component.VEVENT);
        EventStamp eventStamp = EventStamp.getStamp(masterNote);

        // get list of exceptions (VEVENT with RECURRENCEID)
        for (Iterator<VEvent> i = vevents.iterator(); i.hasNext();) {
            VEvent event = i.next();
            // make sure event has DTSTAMP, otherwise validation will fail
            if(event.getDateStamp()==null)
                event.getProperties().add(new DtStamp(new DateTime()));
            if (event.getRecurrenceId() != null) {
                Date recurrenceIdDate = event.getRecurrenceId().getDate();
                exceptions.put(recurrenceIdDate, event);
            }
        }
        
        // Remove all exceptions from master calendar as these
        // will be stored in each NoteItem modification's EventExceptionStamp
        for (Entry<Date, VEvent> entry : exceptions.entrySet())
            masterCalendar.getComponents().remove(entry.getValue());

        // Master calendar includes everything in the original calendar minus
        // any exception events (VEVENT with RECURRENCEID)
        eventStamp.setEventCalendar(masterCalendar);
        eventStamp.compactTimezones();
        
        VEvent event = eventStamp.getEvent();
        
        masterNote.setIcalUid(eventStamp.getIcalUid());
        
        setCalendarAttributes(masterNote, event);
        
        // synchronize exceptions with master NoteItem modifications
        syncExceptions(exceptions, masterNote);
    }

    private static void syncExceptions(Map<Date, VEvent> exceptions,
            NoteItem masterNote) {
        for (Entry<Date, VEvent> entry : exceptions.entrySet())
            syncException(entry.getValue(), masterNote);

        // remove old exceptions
        for (NoteItem noteItem : masterNote.getModifications()) {
            EventExceptionStamp eventException = EventExceptionStamp
                    .getStamp(noteItem);
            if (!exceptions.containsKey(eventException.getRecurrenceId()))
                noteItem.setIsActive(false);
        }
    }

    private static void syncException(VEvent event, NoteItem masterNote) {
        
        NoteItem mod = getModification(masterNote, event.getRecurrenceId()
                .getDate());

        if (mod == null) {
            // create if not present
            createNoteModification(masterNote, event);
        } else {
            // update existing mod
            updateNoteModification(mod, event);
        }
    }

    private static NoteItem getModification(NoteItem masterNote,
            Date recurrenceId) {
        for (NoteItem mod : masterNote.getModifications()) {
            EventExceptionStamp exceptionStamp = EventExceptionStamp
                    .getStamp(mod);
            if (exceptionStamp.getRecurrenceId().equals(recurrenceId))
                return mod;
        }

        return null;
    }

    private static void createNoteModification(NoteItem masterNote, VEvent event) {
        NoteItem noteMod = new NoteItem();
        EventExceptionStamp exceptionStamp = new EventExceptionStamp(noteMod);
        exceptionStamp.setExceptionEvent(event);
        noteMod.addStamp(exceptionStamp);

        noteMod.setUid(new ModificationUid(masterNote, event.getRecurrenceId()
                .getDate()).toString());
        noteMod.setOwner(masterNote.getOwner());
        
        setCalendarAttributes(noteMod, event);

        noteMod.setClientCreationDate(new Date());
        noteMod.setClientModifiedDate(noteMod.getClientCreationDate());
        noteMod.setTriageStatus(TriageStatus.createInitialized());
        noteMod.setLastModification(ContentItem.Action.CREATED);
        noteMod.setLastModifiedBy(masterNote.getLastModifiedBy());
        noteMod.setSent(Boolean.FALSE);
        noteMod.setNeedsReply(Boolean.FALSE);
        noteMod.setModifies(masterNote);
        masterNote.addModification(noteMod);
    }

    private static void updateNoteModification(NoteItem noteMod, VEvent event) {
        EventExceptionStamp exceptionStamp = EventExceptionStamp
                .getStamp(noteMod);
        exceptionStamp.setExceptionEvent(event);
        
        // for now displayName is limited to 255 chars
        if(event.getSummary()!=null)
            noteMod.setDisplayName(StringUtils.substring(event.getSummary().getValue(),0,1024));
       
        if(event.getDescription()!=null)
            noteMod.setBody(event.getDescription().getValue());
        
        noteMod.setClientModifiedDate(new Date());
        noteMod.setLastModifiedBy(noteMod.getModifies().getLastModifiedBy());
        noteMod.setLastModification(ContentItem.Action.EDITED);
    }
    
    private static void setCalendarAttributes(NoteItem note, VEvent event) {
        // for now displayName is limited to 255 chars
        if (event.getSummary() != null)
            note.setDisplayName(StringUtils.substring(event.getSummary()
                    .getValue(), 0, 1024));

        if (event.getDescription() != null)
            note.setBody(event.getDescription().getValue());

        // look for VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(event);
        if (va != null) {
            Date reminderTime = ICalendarUtils.getTriggerDate(va.getTrigger(),
                    event);
            if (reminderTime != null)
                note.setReminderTime(reminderTime);
        }
    }
}
