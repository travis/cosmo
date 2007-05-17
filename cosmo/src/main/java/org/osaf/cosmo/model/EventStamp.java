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
package org.osaf.cosmo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.DtStart;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.osaf.cosmo.hibernate.validator.Event;


/**
 * Represents a calendar event.
 */
@Entity
@DiscriminatorValue("event")
@SecondaryTable(name="event_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventStamp extends BaseEventStamp implements
        java.io.Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;

    private Calendar eventCalendar = null;
    private EventTimeRangeIndex timeRangeIndex = null;
    
    /** default constructor */
    public EventStamp() {
    }
    
    public EventStamp(Item item) {
        setItem(item);
    }
    
    @Transient
    public String getType() {
        return "event";
    }
    
    @Column(table="event_stamp", name = "icaldata", length=102400000, nullable = false)
    @Type(type="calendar_clob")
    @NotNull
    @Event
    public Calendar getEventCalendar() {
        return eventCalendar;
    }
    
    public void setEventCalendar(Calendar calendar) {
        this.eventCalendar = calendar;
    }
    
    @Embedded
    public EventTimeRangeIndex getTimeRangeIndex() {
        return timeRangeIndex;
    }

    public void setTimeRangeIndex(EventTimeRangeIndex timeRangeIndex) {
        this.timeRangeIndex = timeRangeIndex;
    }

    @Override
    @Transient
    public VEvent getEvent() {
        return getMasterEvent();
    }

    @Transient
    public Calendar getCalendar() {
        Calendar masterCal = null;
        
        try {
            masterCal = new Calendar(getEventCalendar());
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy calendar", e);
        }
        
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
        
        // If event is not recurring, skip all the event modification
        // processing
        if(!isRecurring())
            return masterCal;
        
        // add all exception events
        NoteItem note = (NoteItem) getItem();
        TreeMap<String, VEvent> sortedMap = new TreeMap<String, VEvent>();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = EventExceptionStamp.getStamp(exception);
            if(exceptionStamp==null)
                continue;
            
            // Get exception event copy
            VEvent exceptionEvent = null;
            try {
                exceptionEvent = (VEvent) exceptionStamp.getExceptionEvent().copy();
            } catch (Exception e) {
                throw new RuntimeException("Cannot copy calendar", e);
            }
            
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
                
            // check for inherited displayAlarm
            VAlarm displayAlarm = getDisplayAlarm(exceptionEvent);
            if(displayAlarm !=null && displayAlarm.getProperty(Property.TRIGGER)==null) {
                if(masterAlarm!=null) {
                    exceptionEvent.getAlarms().remove(displayAlarm);
                    exceptionEvent.getAlarms().add(masterAlarm);
                }
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

    public void setCalendar(Calendar calendar) {
        setEventCalendar(calendar);
    }
    
    
    /**
     * Returns the master event extracted from the underlying
     * icalendar object. Changes to the master event will be persisted
     * when the stamp is saved.
     */
    @Transient
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
    public Stamp copy(Item item) {
        EventStamp stamp = new EventStamp(item);
        
        // Need to copy Calendar, and indexes
        try {
            stamp.setEventCalendar(new Calendar(getEventCalendar()));
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy calendar", e);
        }
        
        return stamp;
    }
    
    
    /**
     * Remove any timezones in the master calendar that are
     * found in the timezone registry.
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
            if(tz!=null && tz.getID().equals(tzid))
                toRemove.add(vtz);
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
}
