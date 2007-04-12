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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    private Calendar calendar = null;
    private Collection<CalendarTimeRangeIndex> timeRangeIndexes = 
        new ArrayList<CalendarTimeRangeIndex>(0);

    private Collection<CalendarPropertyIndex> propertyIndexes = 
        new ArrayList<CalendarPropertyIndex>(0);
    
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
    
    @Override
    @Transient
    public VEvent getEvent() {
        return getMasterEvent();
    }

    @Transient
    public Calendar getCalendar() {
        Calendar masterCal = null;
        try {
            masterCal = new Calendar(calendar);
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy calendar", e);
        }
        
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
        
        // add all exception events
        NoteItem note = (NoteItem) getItem();
        TreeMap<String, VEvent> sortedMap = new TreeMap<String, VEvent>();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = EventExceptionStamp.getStamp(exception);
            if(exceptionStamp==null)
                continue;
            sortedMap.put(exceptionStamp.getRecurrenceId().toString(), exceptionStamp.getExceptionEvent());
            
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
        setMasterCalendar(calendar);
    }
    
    @Column(table="event_stamp", name = "icaldata", length=102400000, nullable = false)
    @Type(type="calendar_clob")
    @NotNull
    @Event
    public Calendar getMasterCalendar() {
        return calendar;
    }
    
    public void setMasterCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    
    @OneToMany(mappedBy = "eventStamp", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN })
    @OnDelete(action=OnDeleteAction.CASCADE)
    public Collection<CalendarTimeRangeIndex> getTimeRangeIndexes() {
        return timeRangeIndexes;
    }

    private void setTimeRangeIndexes(Collection<CalendarTimeRangeIndex> indexes) {
        this.timeRangeIndexes = indexes;
    }
    
    @OneToMany(mappedBy = "eventStamp", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN })
    @OnDelete(action=OnDeleteAction.CASCADE)
    public Collection<CalendarPropertyIndex> getPropertyIndexes() {
        return propertyIndexes;
    }

    private void setPropertyIndexes(Collection<CalendarPropertyIndex> propertyIndexes) {
        this.propertyIndexes = propertyIndexes;
    }
    
    public void addTimeRangeIndex(CalendarTimeRangeIndex index) {
        index.setItem(getItem());
        index.setEventStamp(this);
        timeRangeIndexes.add(index);
    }
    
    public void addPropertyIndex(CalendarPropertyIndex index) {
        index.setItem(getItem());
        index.setEventStamp(this);
        propertyIndexes.add(index);
    }

    /**
     * Returns the master event extracted from the underlying
     * icalendar object. Changes to the master event will be persisted
     * when the stamp is saved.
     */
    @Transient
    public VEvent getMasterEvent() {
        return (VEvent) getMasterCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
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
            stamp.calendar = new Calendar(calendar);
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy calendar", e);
        }
        
        for(CalendarTimeRangeIndex index : timeRangeIndexes) {
            stamp.addTimeRangeIndex(index.copy());
        }
        
        for(CalendarPropertyIndex index : propertyIndexes) {
            stamp.addPropertyIndex(index.copy());
        }
        
        return stamp;
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
