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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;

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
@DiscriminatorValue("event")
@SecondaryTable(name="event_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")})
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
    }
    
    @Transient
    public String getType() {
        return "event";
    }
    
    @Column(table="event_stamp", name = "icaldata", length=102400000, nullable = false)
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
     * Sets the iCalendar DTSTART property of the master event.
     *
     * @param date a <code>Date</code>
     */
    @Transient
    public void setStartDate(Date date) {
        getMasterEvent().getStartDate().setDate(date);
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
     * Sets the iCalendar DTEND property of the master event.
     *
     * @param date a <code>Date</code>
     */
    @Transient
    public void setEndDate(Date date) {
        getMasterEvent().getEndDate().setDate(date);
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
     * Sets the iCalendar LOCATION property of the master event.
     *
     * @param text a text string
     */
    @Transient
    public void setLocation(String text) {
        Location location = (Location)
            getMasterEvent().getProperties().getProperty(Property.LOCATION);
        if (text == null) {
            if (location != null)
                getMasterEvent().getProperties().remove(location);
            return;
        }                
        if (location == null) {
            location = new Location();
            getMasterEvent().getProperties().add(location);
        }
        location.setValue(text);
    }

    /**
     * Returns a list of copies of the iCalendar RRULE property values
     * of the master event (can be empty).
     */
    @Transient
    public List<Recur> getRecurrenceRules() {
        ArrayList<Recur> l = new ArrayList<Recur>();
        for (RRule rrule : (List<RRule>) getMasterEvent().getProperties().
                 getProperties(Property.RRULE))
            l.add(rrule.getRecur());
        return l;
    }

    /** 
     * Sets the iCalendar RRULE properties of the master event,
     * removing any RRULEs that were previously set.
     *
     * @param recurs a <code>List</code> of <code>Recur</code>s
     */
    @Transient
    public void setRecurrenceRules(List<Recur> recurs) {
        PropertyList pl = getMasterEvent().getProperties();
        for (RRule rrule : (List<RRule>) pl.getProperties(Property.RRULE))
            pl.remove(rrule);
        for (Recur recur : recurs)
            pl.add(new RRule(recur));
    }

    /**
     * Returns a list of copies of the iCalendar EXRULE property values
     * of the master event (can be empty).
     */
    @Transient
    public List<Recur> getExceptionRules() {
        ArrayList<Recur> l = new ArrayList<Recur>();
        for (ExRule exrule : (List<ExRule>) getMasterEvent().getProperties().
                 getProperties(Property.EXRULE))
            l.add(exrule.getRecur());
        return l;
    }

    /** 
     * Sets the iCalendar EXRULE properties of the master event,
     * removing any EXRULEs that were previously set.
     *
     * @param recurs a <code>List</code> of <code>Recur</code>s
     */
    @Transient
    public void setExceptionRules(List<Recur> recurs) {
        PropertyList pl = getMasterEvent().getProperties();
        for (ExRule exrule : (List<ExRule>) pl.getProperties(Property.EXRULE))
            pl.remove(exrule);
        for (Recur recur : recurs)
            pl.add(new ExRule(recur));
    }

    /**
     * Returns a list of copies of the iCalendar RDATE property values
     * of the master event (can be empty).
     */
    @Transient
    public DateList getRecurrenceDates() {
        DateList l = new DateList(Value.DATE_TIME);
        for (RDate rdate : (List<RDate>) getMasterEvent().getProperties().
                 getProperties(Property.RDATE))
            l.addAll(rdate.getDates());
        return l;
    }

    /**
     * Sets a single iCalendar RDATE property of the master event,
     * removing any RDATEs that were previously set.
     *
     * @param dates a <code>DateList</code>
     */
    @Transient
    public void setRecurrenceDates(DateList dates) {
        PropertyList pl = getMasterEvent().getProperties();
        for (RDate rdate : (List<RDate>) pl.getProperties(Property.RDATE))
            pl.remove(rdate);
        if (dates.isEmpty())
            return;
        pl.add(new RDate(dates));
    }

    /**
     * Returns a list of copies of the values of all iCalendar EXDATE
     * properties of the master event (can be empty).
     */
    @Transient
    public DateList getExceptionDates() {
        DateList l = new DateList(Value.DATE_TIME);
        for (ExDate exdate : (List<ExDate>) getMasterEvent().getProperties().
                 getProperties(Property.EXDATE))
            l.addAll(exdate.getDates());
        return l;
    }

    /**
     * Sets a single iCalendar EXDATE property of the master event,
     * removing any EXDATEs that were previously set.
     *
     * @param dates a <code>DateList</code>
     */
    @Transient
    public void setExceptionDates(DateList dates) {
        PropertyList pl = getMasterEvent().getProperties();
        for (ExDate exdate : (List<ExDate>) pl.getProperties(Property.EXDATE))
            pl.remove(exdate);
        if (dates.isEmpty())
            return;
        pl.add(new ExDate(dates));
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
     * Sets the iCalendar RECURRENCE_ID property of the master event.
     *
     * @param date a <code>Date</code>
     */
    @Transient
    public void setRecurrenceId(Date date) {
        RecurrenceId recurrenceId = (RecurrenceId)
            getMasterEvent().getProperties().
            getProperty(Property.RECURRENCE_ID);
        if (date == null) {
            if (recurrenceId != null)
                getMasterEvent().getProperties().remove(recurrenceId);
            return;
        }
        if (recurrenceId == null) {
            recurrenceId = new RecurrenceId();
            getMasterEvent().getProperties().add(recurrenceId);
        }
        recurrenceId.setDate(date);
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
     * Sets the iCalendar STATUS property of the master event.
     *
     * @param text a text string
     */
    @Transient
    public void setStatus(String text) {
        Status status = (Status)
            getMasterEvent().getProperties().getProperty(Property.STATUS);
        if (text == null) {
            if (status != null)
                getMasterEvent().getProperties().remove(status);
            return;
        }                
        if (status == null) {
            status = new Status();
            getMasterEvent().getProperties().add(status);
        }
        status.setValue(text);
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
