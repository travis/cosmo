/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.model.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtStart;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.hibernate.validator.Event;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Stamp;


/**
 * Hibernate persistent EventStamp.
 */
@Entity
@DiscriminatorValue("event")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibEventStamp extends HibBaseEventStamp implements
        java.io.Serializable, EventStamp {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;
    
    /** default constructor */
    public HibEventStamp() {
    }
    
    public HibEventStamp(Item item) {
        setItem(item);
    }
    
    public String getType() {
        return "event";
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#getEvent()
     */
    @Override
    public VEvent getEvent() {
        return getMasterEvent();
    }

    /** Used by the hibernate validator **/
    @Event
    private Calendar getValidationCalendar() {
        return getEventCalendar();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#getCalendar()
     */
    public Calendar getCalendar() {
        Calendar masterCal = CalendarUtils.copyCalendar(getEventCalendar());
        if (masterCal == null)
            return null;
       
        // the master calendar might not have any events; for
        // instance, a client might be trying to save a VTODO
        if (masterCal.getComponents(Component.VEVENT).isEmpty())
            return masterCal;

        VEvent masterEvent = (VEvent) masterCal.getComponents(Component.VEVENT).get(0);
        VAlarm masterAlarm = getDisplayAlarm(masterEvent);
        
        // build timezone map that includes all timezones in master calendar
        ComponentList timezones = masterCal.getComponents(Component.VTIMEZONE);
        HashMap<String, VTimeZone> tzMap = new HashMap<String, VTimeZone>();
        for(Iterator it = timezones.iterator(); it.hasNext();) {
            VTimeZone vtz = (VTimeZone) it.next();
            tzMap.put(vtz.getTimeZoneId().getValue(), vtz);
        }
        
        // check start/end date tz is included, and add if it isn't
        String tzid = getTzId(getStartDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
            masterCal.getComponents().add(vtz);
            tzMap.put(tzid, vtz);
        }
        
        tzid = getTzId(getEndDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
            masterCal.getComponents().add(vtz);
            tzMap.put(tzid, vtz);
        }
        
        // merge item properties to icalendar props
        mergeCalendarProperties(masterEvent, (NoteItem) getItem());
        
        // bug 10558: remove redundant VALUE=DATE-TIME params because
        // some clients don't like them
        fixDateTimeProperties(masterEvent);
        
        // bug 9606: handle displayAlarm with no trigger by not including
        // in exported icalendar
        if(masterAlarm!=null) {
            if(getDisplayAlarmTrigger()==null) {
                masterEvent.getAlarms().remove(masterAlarm);
                masterAlarm = null;
            }
        }
        
        // If event is not recurring, skip all the event modification
        // processing
        if(!isRecurring())
            return masterCal;
        
        // add all exception events
        NoteItem note = (NoteItem) getItem();
        TreeMap<String, VEvent> sortedMap = new TreeMap<String, VEvent>();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = HibEventExceptionStamp.getStamp(exception);
            if(exceptionStamp==null)
                continue;
            
            // Get exception event copy
            VEvent exceptionEvent = (VEvent) CalendarUtils
                    .copyComponent(exceptionStamp.getExceptionEvent());

            // ensure DURATION or DTEND exists on modfication
            if (ICalendarUtils.getDuration(exceptionEvent) == null) {
                ICalendarUtils.setDuration(exceptionEvent, ICalendarUtils
                        .getDuration(masterEvent));
            }
            
            // merge item properties to icalendar props
            mergeCalendarProperties(exceptionEvent, exception);
            
            // bug 10558: remove redundant VALUE=DATE-TIME params because
            // some clients don't like them
            fixDateTimeProperties(masterEvent);
            
            // check for inherited anyTime
            if(exceptionStamp.isAnyTime()==null) {
                DtStart modDtStart = exceptionEvent.getStartDate();
                // remove "missing" value
                modDtStart.getParameters().remove(modDtStart.getParameter(PARAM_X_OSAF_ANYTIME));
                // add inherited value
                if(isAnyTime()) {
                    modDtStart.getParameters().add(getAnyTimeXParam());
                }
            }
                
            // Check for inherited displayAlarm, which is represented
            // by a valarm with no TRIGGER
            VAlarm displayAlarm = getDisplayAlarm(exceptionEvent);
            if(displayAlarm !=null && exceptionStamp.getDisplayAlarmTrigger()==null) {
                exceptionEvent.getAlarms().remove(displayAlarm);
                if(masterAlarm!=null)
                    exceptionEvent.getAlarms().add(masterAlarm);
            }
            
            sortedMap.put(exceptionStamp.getRecurrenceId().toString(), exceptionEvent);
            
            // verify that timezones are present for exceptions, and add if not
            tzid = getTzId(exceptionStamp.getStartDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
                masterCal.getComponents().add(vtz);
                tzMap.put(tzid, vtz);
            }
            
            tzid = getTzId(exceptionStamp.getEndDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
                masterCal.getComponents().add(vtz);
                tzMap.put(tzid, vtz);
            }
        }
        
        masterCal.getComponents().addAll(sortedMap.values());
        
        return masterCal;
    }

   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#getExceptions()
     */
    public List<Component> getExceptions() {
        ArrayList<Component> exceptions = new ArrayList<Component>();
        
        // add all exception events
        NoteItem note = (NoteItem) getItem();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = HibEventExceptionStamp.getStamp(exception);
            if(exceptionStamp!=null)
                exceptions.add(exceptionStamp.getEvent());
        }
        
        return exceptions;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#getMasterEvent()
     */
    public VEvent getMasterEvent() {
        if(getEventCalendar()==null)
            return null;
        
        ComponentList events = getEventCalendar().getComponents().getComponents(
                Component.VEVENT);
        
        if(events.size()==0)
            return null;
        
        return (VEvent) events.get(0);
    }

    /**
     * Return EventStamp from Item
     * @param item
     * @return EventStamp from Item
     */
    public static EventStamp getStamp(Item item) {
        return (EventStamp) item.getStamp(EventStamp.class);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Stamp#copy()
     */
    public Stamp copy() {
        EventStamp stamp = new HibEventStamp();
        
        // Need to copy Calendar, and indexes
        try {
            stamp.setEventCalendar(new Calendar(getEventCalendar()));
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy calendar", e);
        }
        
        return stamp;
    }
    
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#compactTimezones()
     */
    public void compactTimezones() {
        Calendar master = getEventCalendar();
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

    private String getTzId(Date date) {
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            if(dt.getTimeZone()!=null)
                return dt.getTimeZone().getID();
        }
        
        return null;
    }
    
    private void mergeCalendarProperties(VEvent event, NoteItem note) {
        //summary = displayName
        //description = body
        //uid = icalUid
        if(note.getModifies()!=null)
            ICalendarUtils.setUid(note.getModifies().getIcalUid(), event);
        else
            ICalendarUtils.setUid(note.getIcalUid(), event);
        
        ICalendarUtils.setSummary(note.getDisplayName(), event);
        ICalendarUtils.setDescription(note.getBody(), event);
    }
    
    // Remove VALUE=DATE-TIME because it is redundant and some clients
    // don't like when its present.
    private void fixDateTimeProperties(VEvent event) {
        PropertyList props = event.getProperties();
        for(Iterator<Property> it = props.iterator(); it.hasNext();) {
            Property prop = it.next();
            if(prop instanceof DateProperty || prop instanceof DateListProperty) {
                Value v = (Value) prop.getParameter(Parameter.VALUE);
                if(v!=null && Value.DATE_TIME.equals(v))
                    prop.getParameters().remove(v);
            }
        }
    }
}
