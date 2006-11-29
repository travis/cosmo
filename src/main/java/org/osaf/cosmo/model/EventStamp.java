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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RecurrenceId;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.hibernate.validator.Event;


/**
 * Represents an Event Stamp.
 */
@Entity
@Table(name="event_stamp")
@PrimaryKeyJoinColumn(name="stampid")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventStamp extends Stamp implements
        java.io.Serializable {

    
    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;

    private Calendar calendar = null;
    private Set<CalendarTimeRangeIndex> timeRangeIndexes = 
        new HashSet<CalendarTimeRangeIndex>(0);

    private Set<CalendarPropertyIndex> propertyIndexes = 
        new HashSet<CalendarPropertyIndex>(0);
    
    /** default constructor */
    public EventStamp() {
        setType("event");
    }

    @Column(name = "icaldata", length=102400000)
    @Type(type="calendar_clob")
    @NotNull
    @Event
    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    
    @OneToMany(mappedBy = "eventStamp", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<CalendarTimeRangeIndex> getTimeRangeIndexes() {
        return timeRangeIndexes;
    }

    private void setTimeRangeIndexes(Set<CalendarTimeRangeIndex> indexes) {
        this.timeRangeIndexes = indexes;
    }
    
    @OneToMany(mappedBy = "eventStamp", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<CalendarPropertyIndex> getPropertyIndexes() {
        return propertyIndexes;
    }

    private void setPropertyIndexes(Set<CalendarPropertyIndex> propertyIndexes) {
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
        return (VEvent) getCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }

    /**
     * Returns a copy of the the iCalendar UID property value of the
     * master event .
     */
    @Transient
    public String getIcalUid() {
        return getMasterEvent().getUid().getValue();
    }

    /**
     * Returns a copy of the the iCalendar DTSTART property value of
     * the master event (never null).
     */
    @Transient
    public Date getStartDate() {
        return getMasterEvent().getStartDate().getDate();
    }

    /**
     * Returns the end date of the master event as calculated from the
     * iCalendar DTEND property value or the the iCalendar DTSTART +
     * DURATION (never null).
     */
    @Transient
    public Date getEndDate() {
        return getMasterEvent().getEndDate().getDate();
    }

    /**
     * Returns a copy of the the iCalendar LOCATION property value of
     * the master event (can be null).
     */
    @Transient
    public String getLocation() {
        Property p = getMasterEvent().getProperties().
            getProperty(Property.LOCATION);
        if (p == null)
            return null;
        return p.getValue();
    }

    /**
     * Returns a list of copies of the iCalendar RRULE property values
     * of the master event (can be empty).
     */
    @Transient
    public List<String> getRecurrenceRules() {
        ArrayList l = new ArrayList();
        for (Property p : (List<Property>) getMasterEvent().getProperties().
                 getProperties(Property.RRULE))
            l.add(p.getValue());
        return l;
    }

    /**
     * Returns a list of copies of the iCalendar EXRULE property values
     * of the master event (can be empty).
     */
    @Transient
    public List<String> getExceptionRules() {
        ArrayList l = new ArrayList();
        for (Property p : (List<Property>) getMasterEvent().getProperties().
                 getProperties(Property.EXRULE))
            l.add(p.getValue());
        return l;
    }

    /**
     * Returns a list of copies of the iCalendar RDATE property values
     * of the master event (can be empty).
     */
    @Transient
    public List<String> getRecurrenceDates() {
        ArrayList l = new ArrayList();
        for (Property p : (List<Property>) getMasterEvent().getProperties().
                 getProperties(Property.RDATE))
            l.add(p.getValue());
        return l;
    }

    /**
     * Returns a list of copies of the iCalendar EXDATE property values
     * of the master event (can be empty).
     */
    @Transient
    public List<String> getExceptionDates() {
        ArrayList l = new ArrayList();
        for (Property p : (List<Property>) getMasterEvent().getProperties().
                 getProperties(Property.EXDATE))
            l.add(p.getValue());
        return l;
    }

    /**
     * Returns a copy of the the iCalendar RECURRENCE_ID property
     * value of the master event (can be null). 
     */
    @Transient
    public Date getRecurrenceId() {
        RecurrenceId rid = getMasterEvent().getReccurrenceId();
        if (rid == null)
            return null;
        return rid.getDate();
    }

    /**
     * Returns a copy of the the iCalendar STATUS property value of
     * the master event (can be null).
     */
    @Transient
    public String getStatus() {
        Property p = getMasterEvent().getProperties().
            getProperty(Property.STATUS);
        if (p == null)
            return null;
        return p.getValue();
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
        EventStamp stamp = new EventStamp();
        
        // Need to copy Calendar, and indexes
        stamp.calendar = CalendarUtils.copyCalendar(calendar);
        
        for(CalendarTimeRangeIndex index : timeRangeIndexes)
            stamp.addTimeRangeIndex(index.copy());
        
        for(CalendarPropertyIndex index : propertyIndexes)
            stamp.addPropertyIndex(index.copy());
        
        return stamp;
    }
}
