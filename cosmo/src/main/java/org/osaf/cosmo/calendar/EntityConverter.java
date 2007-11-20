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
package org.osaf.cosmo.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.DtStamp;

import org.apache.commons.lang.StringUtils;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.TaskStamp;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.TriageStatusUtil;

/**
 * A component that converts iCalendar objects to entities and vice versa.
 * Often this is not a straight one-to-one mapping, because recurring
 * iCalendar events are modeled as multiple events in a single
 * {@link Calendar}, whereas recurring items are modeled as a master
 * {@link NoteItem} with zero or more {@link NoteItem} modifications and
 * potentially also {@link NoteOccurrence}s.
 */
public class EntityConverter { 
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private EntityFactory entityFactory;

    public EntityConverter(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
    /**
     * Expands an event calendar and returns a set of notes representing the
     * master and exception items.
     * <p>
     * The provided note corresponds to the recurrence master or, for
     * non-recurring items, the single event in the calendar. The result set
     * includes both the master note as well as a modification note for
     * exception event in the calendar.
     * </p>
     * <p>
     * If the master note does not already have a UUID or an event stamp, one
     * is assigned to it. A UUID is assigned because any modification items
     * that are created require the master's UUID in order to construct
     * their own.
     * </p>
     * <p>
     * If the given note is already persistent, and the calendar does not
     * contain an exception event matching an existing modification, that
     * modification is set inactive. It is still returned in the result set.
     * </p>
     */
    public Set<NoteItem> convertEventCalendar(NoteItem note,
                                              Calendar calendar) {
        EventStamp eventStamp = (EventStamp) note.getStamp(EventStamp.class);
        if (eventStamp == null) {
            eventStamp = entityFactory.createEventStamp(note);
            note.addStamp(eventStamp);
        }

        if (note.getUid() == null)
            note.setUid(entityFactory.generateUid());

        updateEventInternal(note, calendar);

        LinkedHashSet<NoteItem> items = new LinkedHashSet<NoteItem>();
        items.add(note);

        // add modifications to set of items
        for(Iterator<NoteItem> it = note.getModifications().iterator(); it.hasNext();) {
            NoteItem mod = it.next();
            items.add(mod);
        }

        return items;
    }

    /**
     * Returns a calendar representing the item.
     * <p>
     * If the item is a {@link NoteItem}, delegates to
     * {@link #convertNote(NoteItem)}. If the item is a {@link ICalendarItem},
     * delegates to {@link ICalendarItem#getFullCalendar()}. Otherwise,
     * returns null.
     * </p>
     */
    public static Calendar convertContent(ContentItem item) {

        if(item instanceof NoteItem)
            return convertNote((NoteItem) item);
        else if(item instanceof ICalendarItem)
            return ((ICalendarItem) item).getFullCalendar();

        return null;
    }

    /**
     * Returns a calendar representing the note.
     * <p>
     * If the note is a modification, returns null. If the note has an event
     * stamp, returns a calendar containing the event and any exceptions. If
     * the note has a task stamp, returns a calendar containing the task.
     * Otherwise, returns a calendar containing a journal.
     * </p>
     */
    public static Calendar convertNote(NoteItem note) {

        // must be a master note
        if(note.getModifies()!=null)
            return null;

        EventStamp event = StampUtils.getEventStamp(note);
        if(event!=null)
            return event.getCalendar();

        TaskStamp task = StampUtils.getTaskStamp(note);
        if(task!=null)
            return task.getCalendar();

        return note.getFullCalendar();
    }
    
    private void updateEventInternal(NoteItem masterNote,
                                     Calendar calendar) {
        HashMap<Date, VEvent> exceptions = new HashMap<Date, VEvent>();
        
        Calendar masterCalendar = calendar;
        
        ComponentList vevents = masterCalendar.getComponents().getComponents(
                Component.VEVENT);
        EventStamp eventStamp = StampUtils.getEventStamp(masterNote);

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
        compactTimezones(eventStamp);
        
        VEvent event = eventStamp.getEvent();
        
        masterNote.setIcalUid(eventStamp.getIcalUid());
        
        setCalendarAttributes(masterNote, event);
        
        // synchronize exceptions with master NoteItem modifications
        syncExceptions(exceptions, masterNote);
    }

    private void compactTimezones(BaseEventStamp stamp) {
        Calendar master = stamp.getEventCalendar();
        if(master==null)
            return;

        // Get list of timezones in master calendar and remove all timezone
        // definitions that are in the registry.  The idea is to not store
        // extra data.  Instead, the timezones will be added to the calendar
        // by the getCalendar() api.
        ComponentList timezones = master.getComponents(Component.VTIMEZONE);
        ArrayList toRemove = new ArrayList();
        for(Iterator it = timezones.iterator();it.hasNext();) {
            VTimeZone vtz = (VTimeZone) it.next();
            String tzid = vtz.getTimeZoneId().getValue();
            TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
            //  Remove timezone iff it matches the one in the registry
            if(tz!=null) {
                if(vtz.equals(tz.getVTimeZone()))
                    toRemove.add(vtz);
            }
        }

        // remove known timezones from master calendar
        master.getComponents().removeAll(toRemove);
    }

    private void syncExceptions(Map<Date, VEvent> exceptions,
                                NoteItem masterNote) {
        for (Entry<Date, VEvent> entry : exceptions.entrySet())
            syncException(entry.getValue(), masterNote);

        // remove old exceptions
        for (NoteItem noteItem : masterNote.getModifications()) {
            EventExceptionStamp eventException =
                StampUtils.getEventExceptionStamp(noteItem);
            if (!exceptions.containsKey(eventException.getRecurrenceId()))
                noteItem.setIsActive(false);
        }
    }

    private void syncException(VEvent event,
                               NoteItem masterNote) {
        NoteItem mod =
            getModification(masterNote, event.getRecurrenceId().getDate());

        if (mod == null) {
            // create if not present
            createNoteModification(masterNote, event);
        } else {
            // update existing mod
            updateNoteModification(mod, event);
        }
    }

    private NoteItem getModification(NoteItem masterNote,
                                     Date recurrenceId) {
        for (NoteItem mod : masterNote.getModifications()) {
            EventExceptionStamp exceptionStamp =
                StampUtils.getEventExceptionStamp(mod);
            if (exceptionStamp.getRecurrenceId().equals(recurrenceId))
                return mod;
        }

        return null;
    }

    private void createNoteModification(NoteItem masterNote,
                                        VEvent event) {
        NoteItem noteMod = entityFactory.createNote();
        EventExceptionStamp exceptionStamp =
            entityFactory.createEventExceptionStamp(noteMod);
        exceptionStamp.setExceptionEvent(event);
        noteMod.addStamp(exceptionStamp);

        noteMod.setUid(new ModificationUid(masterNote, event.getRecurrenceId()
                .getDate()).toString());
        noteMod.setOwner(masterNote.getOwner());
        noteMod.setName(noteMod.getUid());
        
        // copy VTIMEZONEs to front if present
        EventStamp es = StampUtils.getEventStamp(masterNote);
        ComponentList vtimezones = es.getEventCalendar().getComponents(Component.VTIMEZONE);
        for(Iterator<Component> it = vtimezones.iterator(); it.hasNext();)
            exceptionStamp.getEventCalendar().getComponents().add(0, it.next());
        
        setCalendarAttributes(noteMod, event);

        TriageStatus ts = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(ts);

        noteMod.setClientCreationDate(new Date());
        noteMod.setClientModifiedDate(noteMod.getClientCreationDate());
        noteMod.setTriageStatus(ts);
        noteMod.setLastModification(ContentItem.Action.CREATED);
        noteMod.setLastModifiedBy(masterNote.getLastModifiedBy());
        noteMod.setSent(Boolean.FALSE);
        noteMod.setNeedsReply(Boolean.FALSE);
        noteMod.setModifies(masterNote);
        masterNote.addModification(noteMod);
    }

    private void updateNoteModification(NoteItem noteMod,
                                        VEvent event) {
        EventExceptionStamp exceptionStamp =
            StampUtils.getEventExceptionStamp(noteMod);
        exceptionStamp.setExceptionEvent(event);
        
        // copy VTIMEZONEs to front if present
        ComponentList vtimezones = exceptionStamp.getMasterStamp()
                .getEventCalendar().getComponents(Component.VTIMEZONE);
        for(Iterator<Component> it = vtimezones.iterator(); it.hasNext();)
            exceptionStamp.getEventCalendar().getComponents().add(0, it.next());
        
        setCalendarAttributes(noteMod, event);
        
        noteMod.setClientModifiedDate(new Date());
        noteMod.setLastModifiedBy(noteMod.getModifies().getLastModifiedBy());
        noteMod.setLastModification(ContentItem.Action.EDITED);
    }
    
    private void setCalendarAttributes(NoteItem note,
                                       VEvent event) {
        // for now displayName is limited to 1024 chars
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

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }
}
