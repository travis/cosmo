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
import java.util.Iterator;
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
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.hibernate.validator.Event;
import org.osaf.cosmo.icalendar.ICalendarConstants;


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
    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    
    @OneToMany(mappedBy = "eventStamp", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN })
    @OnDelete(action=OnDeleteAction.CASCADE)
    public Set<CalendarTimeRangeIndex> getTimeRangeIndexes() {
        return timeRangeIndexes;
    }

    private void setTimeRangeIndexes(Set<CalendarTimeRangeIndex> indexes) {
        this.timeRangeIndexes = indexes;
    }
    
    @OneToMany(mappedBy = "eventStamp", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN })
    @OnDelete(action=OnDeleteAction.CASCADE)
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
     * Returns a copy of the the iCalendar SUMMARY property value of
     * the master event (can be null).
     */
    @Transient
    public String getSummary() {
        Property p = getMasterEvent().getProperties().
            getProperty(Property.SUMMARY);
        if (p == null)
            return null;
        return p.getValue();
    }

    /** 
     * Sets the iCalendar SUMMARY property of the master event.
     *
     * @param text a text string
     */
    @Transient
    public void setSummary(String text) {
        Summary summary = (Summary)
            getMasterEvent().getProperties().getProperty(Property.SUMMARY);
        if (text == null) {
            if (summary != null)
                getMasterEvent().getProperties().remove(summary);
            return;
        }                
        if (summary == null) {
            summary = new Summary();
            getMasterEvent().getProperties().add(summary);
        }
        summary.setValue(text);
    }
    
    /**
     * Returns a copy of the the iCalendar DESCRIPTION property value of
     * the master event (can be null).
     */
    @Transient
    public String getDescription() {
        Property p = getMasterEvent().getProperties().
            getProperty(Property.DESCRIPTION);
        if (p == null)
            return null;
        return p.getValue();
    }

    /** 
     * Sets the iCalendar DESCRIPTION property of the master event.
     *
     * @param text a text string
     */
    @Transient
    public void setDescription(String text) {
        Description description = (Description)
            getMasterEvent().getProperties().getProperty(Property.DESCRIPTION);
        if (text == null) {
            if (description != null)
                getMasterEvent().getProperties().remove(description);
            return;
        }                
        if (description == null) {
            description = new Description();
            getMasterEvent().getProperties().add(description);
        }
        description.setValue(text);
    }

    /**
     * Returns a copy of the the iCalendar DTSTART property value of
     * the master event (never null).
     */
    @Transient
    public Date getStartDate() {
        DtStart dtStart = getMasterEvent().getStartDate();
        if (dtStart == null)
            return null;
        return dtStart.getDate();
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
        DtEnd dtEnd = getMasterEvent().getEndDate();
        if (dtEnd == null)
            return null;
        return dtEnd.getDate();
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
     * Return the first display alarm on an event (master or modification)
     * @param recurrenceId modification recurrenceId (null for master event)
     * @return first display alarm on event
     */
    public VAlarm getDisplayAlarm(Date recurrenceId) {
        VEvent event = null;
        if(recurrenceId == null)
            event = getMasterEvent();
        else
            event = getModification(recurrenceId);
        
        if(event==null)
            return null;
        
        for(Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY))
                return alarm;
        }
        
        return null;
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
     * Returns override instance.
     * @param date recurrence override date
     */
    @Transient
    public VEvent getModification(Date date) {
        
        // require override date
        if(date==null)
            throw new IllegalArgumentException("override date required");
        
        ComponentList vevents = calendar.getComponents().getComponents(
                Component.VEVENT);
        
        // find instance with RECURRENCE-ID that matches given date
        for(Iterator it = vevents.iterator(); it.hasNext();) {
            VEvent event = (VEvent) it.next();
            RecurrenceId recurrenceId = event.getReccurrenceId();
            if(recurrenceId!=null && recurrenceId.getDate().equals(date))
                return event;
        }
        
        // didn't find it
        return null;
    }
    
    @Transient
    public List<VEvent> getModifications() {
        List<VEvent> overrides = new ArrayList();
        
        ComponentList vevents = calendar.getComponents().getComponents(
                Component.VEVENT);
        
        // find instance with RECURRENCE-ID that matches given date
        for(Iterator it = vevents.iterator(); it.hasNext();) {
            VEvent event = (VEvent) it.next();
            if(event.getReccurrenceId()!=null)
                overrides.add(event);
        }
        
        return overrides;
    }
    
    /**
     * Remove event modification.
     * @param date modification date to remove
     */
    public void removeModification(Date date) {
        if(date==null)
            throw new IllegalArgumentException("instance must have recurrence-id");
        
        ComponentList vevents = calendar.getComponents().getComponents(
                Component.VEVENT);
        
        // find instance with RECURRENCE-ID that matches given date
        for(Iterator it = vevents.iterator(); it.hasNext();) {
            VEvent event = (VEvent) it.next();
            RecurrenceId recurrenceId = event.getReccurrenceId();
            if(recurrenceId!=null && recurrenceId.getDate().equals(date)) {
                it.remove();
                break;
            }
        }
    }
    
    /**
     * Add event modification 
     * @param modification event modification to add
     */
    public void addModification(VEvent modification) {
        
        // modification requires RECURRENCE-ID
        if(modification.getReccurrenceId()==null)
            throw new IllegalArgumentException("date cannot be null");
        
        ComponentList vevents = calendar.getComponents().getComponents(
                Component.VEVENT);
        
        // verify RECURRENCE-ID is unique
        for(Iterator it = vevents.iterator(); it.hasNext();) {
            VEvent event = (VEvent) it.next();
            RecurrenceId recurrId = event.getReccurrenceId();
            if(recurrId!=null && recurrId.getDate().equals(modification.getReccurrenceId().getDate()));
                throw new ModelValidationException("duplicate recurrence id");
        }
        
        calendar.getComponents().add(modification);
    }
    
    /**
     * Is the event marked as anytime.
     * @return true if the event is an anytime event
     */
    @Transient
    public boolean isAnyTime() {
        Parameter parameter = getMasterEvent().getStartDate().getParameters()
                .getParameter(ICalendarConstants.PARAM_X_OSAF_ANYTIME);
        if (parameter == null) {
            return false;
        }

        return ICalendarConstants.VALUE_TRUE.equals(parameter.getValue());
    }
    
    /**
     * Toggle the event anytime parameter.
     * @param isAnyTime true if the event occurs anytime
     */
    public void setAnyTime(boolean isAnyTime) {
        DtStart dtstart = getMasterEvent().getStartDate();
        Parameter parameter = dtstart.getParameters().getParameter(
                ICalendarConstants.PARAM_X_OSAF_ANYTIME);

        // add X-OSAF-ANYTIME if it doesn't exist
        if (parameter == null && isAnyTime) {
            dtstart.getParameters().add(getAnyTimeXParam());
            return;
        }

        // if it exists, update based on isAnyTime
        if (parameter != null) {
            String value = parameter.getValue();
            boolean currIsAnyTime = ICalendarConstants.VALUE_TRUE.equals(value);
            if (currIsAnyTime && !isAnyTime)
                dtstart.getParameters().remove(parameter);
            else if (!currIsAnyTime && isAnyTime) {
                dtstart.getParameters().remove(parameter);
                dtstart.getParameters().add(getAnyTimeXParam());
            }
        }
    }
    
    @Transient
    private Parameter getAnyTimeXParam() {
        return new XParameter(ICalendarConstants.PARAM_X_OSAF_ANYTIME,
                ICalendarConstants.VALUE_TRUE);
    }
    
    /**
     * Initializes the Calendar with a default master event.
     * Initializes the master event using the underlying item's
     * icalUid (if NoteItem) or uid, and if the item is a NoteItem,
     * initializes SUMMARY and DESCRIPTION with the NoteItem's 
     * displayName and body.
     */
    public void createCalendar() {
        
        Calendar cal = new Calendar();
        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        
        VEvent vevent = new VEvent();
        Uid uid = new Uid();
        NoteItem note = null;
        if(getItem()!=null && getItem() instanceof NoteItem)
            note = (NoteItem) getItem();
        
        // VEVENT UID is the NoteItem's icalUid
        // if it exists, or just the Item's uid
        if(note!=null && note.getIcalUid() != null)
            uid.setValue(note.getIcalUid());
        else
            uid.setValue(getItem().getUid());
            
        vevent.getProperties().add(uid);
       
        cal.getComponents().add(vevent);
        setCalendar(cal);
        
        // SUMMARY is NoteItem.displayName and
        // DESCRIPTION is NoteItem.body
        if(note!=null) {
            setSummary(note.getDisplayName());
            setDescription(note.getBody());
        }
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

    @Override
    public void remove() {
        super.remove();
        // clear indexes
        timeRangeIndexes.clear();
        propertyIndexes.clear();
    }
    
    
}
