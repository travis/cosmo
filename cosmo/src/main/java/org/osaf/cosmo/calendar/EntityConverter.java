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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.lang.StringUtils;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.FreeBusyItem;
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
     * Converts a single calendar containing many different
     * components and component types into a set of
     * {@link ICalendarItem}.
     * 
     * @param calendar calendar containing any number and type
     *        of calendar components
     * @return set of ICalendarItems
     */
    public Set<ICalendarItem> convertCalendar(Calendar calendar) {
        Set<ICalendarItem> items = new LinkedHashSet<ICalendarItem>();
        for(CalendarContext cc: splitCalendar(calendar)) {
            if(cc.type.equals(Component.VEVENT))
                items.addAll(convertEventCalendar(cc.calendar));
            else if(cc.type.equals(Component.VTODO))
                items.add(convertTaskCalendar(cc.calendar));
            else if(cc.type.equals(Component.VJOURNAL))
                items.add(convertJournalCalendar(cc.calendar));
            else if(cc.type.equals(Component.VFREEBUSY))
                items.add(convertFreeBusyCalendar(cc.calendar));
        }
        
        return items;
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
     * Expands an event calendar and returns a set of notes representing the
     * master and exception items.
     */
    public Set<NoteItem> convertEventCalendar(Calendar calendar) {
        return convertEventCalendar(entityFactory.createNote(), calendar);
    }
    
    /**
     * Convert calendar containing single VJOURNAL into NoteItem
     * @param calendar calendar containing VJOURNAL
     * @return NoteItem representation of VJOURNAL
     */
    public NoteItem convertJournalCalendar(Calendar calendar) {
        NoteItem note = entityFactory.createNote();
        note.setUid(entityFactory.generateUid());
        setBaseContentAttributes(note);
        return convertJournalCalendar(note, calendar);
    }
    
    /**
     * Update existing NoteItem with calendar containing single VJOURNAL
     * @param note note to update
     * @param calendar calendar containing VJOURNAL
     * @return NoteItem representation of VJOURNAL
     */
    public NoteItem convertJournalCalendar(NoteItem  note, Calendar calendar) {
        
        note.setJournalCalendar(calendar);
        
        VJournal vj = (VJournal) getMasterComponent(calendar.getComponents(Component.VJOURNAL));
        
        setCalendarAttributes(note, vj);
        
        return note;
    }
    
    /**
     * Convert calendar containing single VTODO into NoteItem
     * @param calendar calendar containing VTODO
     * @return NoteItem representation of VTODO
     */
    public NoteItem convertTaskCalendar(Calendar calendar) {
        NoteItem note = entityFactory.createNote();
        note.setUid(entityFactory.generateUid());
        setBaseContentAttributes(note);
        return convertTaskCalendar(note, calendar);
    }
    
    /**
     * Convert calendar containing single VTODO into NoteItem
     * @param note note to update
     * @param calendar calendar containing VTODO
     * @return NoteItem representation of VTODO
     */
    public NoteItem convertTaskCalendar(NoteItem  note, Calendar calendar) {
        
        TaskStamp task = StampUtils.getTaskStamp(note);
        if(task==null) {
            task = entityFactory.createTaskStamp();
            note.addStamp(task);
        }
        
        task.setTaskCalendar(calendar);
        VToDo todo = (VToDo) getMasterComponent(calendar.getComponents(Component.VTODO));
        
        setCalendarAttributes(note, todo);
        
        return note;
    }
    
    /**
     * Convert calendar containing single VFREEBUSY into FreeBusyItem
     * @param calendar calendar containing VFREEBUSY
     * @return FreeBusyItem representation of VFREEBUSY
     */
    public FreeBusyItem convertFreeBusyCalendar(Calendar calendar) {
        FreeBusyItem freeBusy = entityFactory.createFreeBusy();
        freeBusy.setUid(entityFactory.generateUid());
        setBaseContentAttributes(freeBusy);
        return convertFreeBusyCalendar(freeBusy, calendar);
    }
    
    /**
     * Convert calendar containing single VFREEBUSY into FreeBusyItem
     * @param freebusy freebusy to update
     * @param calendar calendar containing VFREEBUSY
     * @return FreeBusyItem representation of VFREEBUSY
     */
    public FreeBusyItem convertFreeBusyCalendar(FreeBusyItem freeBusy, Calendar calendar) {
       
        freeBusy.setFreeBusyCalendar(calendar);
        VFreeBusy vfb = (VFreeBusy) getMasterComponent(calendar.getComponents(Component.VFREEBUSY));
        setCalendarAttributes(freeBusy, vfb);
        
        return freeBusy;
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
        
        setBaseContentAttributes(noteMod);
        noteMod.setLastModifiedBy(masterNote.getLastModifiedBy());
        noteMod.setModifies(masterNote);
        masterNote.addModification(noteMod);
        
        setCalendarAttributes(noteMod, event);
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
        
        noteMod.setClientModifiedDate(new Date());
        noteMod.setLastModifiedBy(noteMod.getModifies().getLastModifiedBy());
        noteMod.setLastModification(ContentItem.Action.EDITED);
        
        setCalendarAttributes(noteMod, event);
    }
    
    private void setBaseContentAttributes(ContentItem item) {
        
        TriageStatus ts = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(ts);

        item.setClientCreationDate(new Date());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(ts);
        item.setLastModification(ContentItem.Action.CREATED);
        
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
    }    

    
    private void setCalendarAttributes(NoteItem note,
                                       VEvent event) {
        
        // UID
        if(event.getUid()!=null)
            note.setIcalUid(event.getUid().getValue());
        
        // for now displayName is limited to 1024 chars
        if (event.getSummary() != null)
            note.setDisplayName(StringUtils.substring(event.getSummary()
                    .getValue(), 0, 1024));

        if (event.getDescription() != null)
            note.setBody(event.getDescription().getValue());

        // look for DTSTAMP
        if(event.getDateStamp()!=null)
            note.setClientModifiedDate(event.getDateStamp().getDate());
        
        // look for VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(event);
        if (va != null) {
            Date reminderTime = ICalendarUtils.getTriggerDate(va.getTrigger(),
                    event);
            if (reminderTime != null)
                note.setReminderTime(reminderTime);
        }
    }
    
    private void setCalendarAttributes(NoteItem note, VToDo task) {
        
        // UID
        if(task.getUid()!=null)
            note.setIcalUid(task.getUid().getValue());
        
        // for now displayName is limited to 1024 chars
        if (task.getSummary() != null)
            note.setDisplayName(StringUtils.substring(task.getSummary()
                    .getValue(), 0, 1024));

        if (task.getDescription() != null)
            note.setBody(task.getDescription().getValue());

        // look for DTSTAMP
        if (task.getDateStamp() != null)
            note.setClientModifiedDate(task.getDateStamp().getDate());

        // look for VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(task);
        if (va != null) {
            Date reminderTime = ICalendarUtils.getTriggerDate(va.getTrigger(),
                    task);
            if (reminderTime != null)
                note.setReminderTime(reminderTime);
        }
    }
    
    private void setCalendarAttributes(NoteItem note, VJournal journal) {
        // UID
        if(journal.getUid()!=null)
            note.setIcalUid(journal.getUid().getValue());
        
        // for now displayName is limited to 1024 chars
        if (journal.getSummary() != null)
            note.setDisplayName(StringUtils.substring(journal.getSummary()
                    .getValue(), 0, 1024));

        if (journal.getDescription() != null)
            note.setBody(journal.getDescription().getValue());

        // look for DTSTAMP
        if (journal.getDateStamp() != null)
            note.setClientModifiedDate(journal.getDateStamp().getDate());
    }
    
    private void setCalendarAttributes(FreeBusyItem freeBusy, VFreeBusy vfb) {
        // UID
        if(vfb.getUid()!=null)
            freeBusy.setIcalUid(vfb.getUid().getValue());
        
        // look for DTSTAMP
        if (vfb.getDateStamp() != null)
            freeBusy.setClientModifiedDate(vfb.getDateStamp().getDate());
    }
    
    private Component getMasterComponent(ComponentList components) {
        Iterator<Component> it = components.iterator();
        while(it.hasNext()) {
            Component c = it.next();
            if(c.getProperty(Property.RECURRENCE_ID)==null)
                return c;
        }
        
        throw new IllegalArgumentException("no master found");
    }
    
    /**
     * Given a Calendar with no VTIMZONE components, go through
     * all other components and add all relevent VTIMEZONES.
     */
    private void addTimezones(Calendar calendar) {
        ComponentList comps = calendar.getComponents();
        Set<VTimeZone> timezones = new HashSet<VTimeZone>();
        
        for(Iterator<Component> it = comps.iterator();it.hasNext();) {
            Component comp = it.next();
            PropertyList props = comp.getProperties();
            for(Iterator<Property> it2 = props.iterator();it2.hasNext();) {
                Property prop = it2.next();
                if(prop instanceof DateProperty) {
                    DateProperty dateProp = (DateProperty) prop;
                    if(dateProp.getDate() instanceof DateTime) {
                        DateTime dt = (DateTime) dateProp.getDate();
                        if(dt.getTimeZone()!=null)
                            timezones.add(dt.getTimeZone().getVTimeZone());
                    }
                } else if(prop instanceof DateListProperty) {
                    DateListProperty dateProp = (DateListProperty) prop;
                    if(dateProp.getDates().getTimeZone()!=null)
                        timezones.add(dateProp.getDates().getTimeZone().getVTimeZone());
                }
            }
        }
        
        for(VTimeZone vtz: timezones)
            calendar.getComponents().add(0, vtz);
    }
    
    /**
     * Given a calendar with many different components, split into
     * separate calendars that contain only a single component type
     * and a single UID.
     */
    private CalendarContext[] splitCalendar(Calendar calendar) {
        Vector<CalendarContext> contexts = new Vector<CalendarContext>();
        Set<String> allComponents = new HashSet<String>();
        Map<String, ComponentList> componentMap = new HashMap<String, ComponentList>();
        
        ComponentList comps = calendar.getComponents();
        for(Iterator<Component> it = comps.iterator(); it.hasNext();) {
            Component comp = it.next();
            // ignore vtimezones for now
            if(comp instanceof VTimeZone)
                continue;
            
            Uid uid = (Uid) comp.getProperty(Property.UID);
            RecurrenceId rid = (RecurrenceId) comp.getProperty(Property.RECURRENCE_ID);
            
            String key = uid.getValue();
            if(rid!=null)
                key+=rid.toString();
            
            // ignore duplicates
            if(allComponents.contains(key))
                continue;
            
            allComponents.add(key);
            
            ComponentList cl = componentMap.get(uid.getValue());
            
            if(cl==null) {
                cl = new ComponentList();
                componentMap.put(uid.getValue(), cl);
            }
            
            cl.add(comp);
        }
        
        for(Entry<String, ComponentList> entry : componentMap.entrySet()) {
           
            Component firstComp = (Component) entry.getValue().get(0);
            
            Calendar cal = ICalendarUtils.createBaseCalendar();
            cal.getComponents().addAll(entry.getValue());
            addTimezones(cal);
            
            CalendarContext cc = new CalendarContext();
            cc.calendar = cal;
            cc.type = firstComp.getName();
            
            contexts.add(cc);
        }
        
        return contexts.toArray(new CalendarContext[0]);
    }
    

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }
    
    /**
     * Container for a calendar containing single component type (can
     * be multiple components if the component is recurring and has
     * modifications), and the component type.
     */
    class CalendarContext {
        String type;
        Calendar calendar;
    }
}
