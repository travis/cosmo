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
package org.osaf.cosmo.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.icalendar.ICalendarConstants;


/**
 * Represents a calendar event.
 */
@Entity
@DiscriminatorValue("baseevent")
public abstract class BaseEventStamp extends Stamp
    implements java.io.Serializable, ICalendarConstants {

    protected static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    public static final String TIME_INFINITY = "Z-TIME-INFINITY";
    
    protected static final String VALUE_MISSING = "MISSING";
    
    @Transient
    public abstract VEvent getEvent();
    
    public abstract void setCalendar(Calendar calendar);
    
    public abstract void setTimeRangeIndex(EventTimeRangeIndex index);
    
    @Transient
    public abstract EventTimeRangeIndex getTimeRangeIndex();
    
      
    /**
     * Return BaseEventStamp from Item
     * @param item
     * @return BaseEventStamp from Item
     */
    public static BaseEventStamp getStamp(Item item) {
        return (BaseEventStamp) item.getStamp(BaseEventStamp.class);
    }
    
    
    /**
     * Returns a copy of the the iCalendar UID property value of the
     * event .
     */
    @Transient
    public String getIcalUid() {
        return getEvent().getUid().getValue();
    }

    /**
     * Returns a copy of the the iCalendar SUMMARY property value of
     * the event (can be null).
     */
    @Transient
    public String getSummary() {
        Property p = getEvent().getProperties().
            getProperty(Property.SUMMARY);
        if (p == null)
            return null;
        return p.getValue();
    }

    /** 
     * Sets the iCalendar SUMMARY property of the event.
     *
     * @param text a text string
     */
    @Transient
    public void setSummary(String text) {
        setDirty(true);
        setSummary(text, getEvent());
    }
    
    protected void setSummary(String text, VEvent event) {
        Summary summary = (Summary)
        event.getProperties().getProperty(Property.SUMMARY);
        if (text == null) {
            if (summary != null)
                event.getProperties().remove(summary);
            return;
        }                
        if (summary == null) {
            summary = new Summary();
            event.getProperties().add(summary);
        }
        summary.setValue(text);
    }
    
    /**
     * Returns a copy of the the iCalendar DESCRIPTION property value of
     * the event (can be null).
     */
    @Transient
    public String getDescription() {
        Property p = getEvent().getProperties().
            getProperty(Property.DESCRIPTION);
        if (p == null)
            return null;
        return p.getValue();
    }
    
    /** 
     * Sets the iCalendar DESCRIPTION property of the event.
     *
     * @param text a text string
     */
    @Transient
    public void setDescription(String text) {
        setDirty(true);
        
    }
    
    protected void setDescription(String text, VEvent event) {
        Description description = (Description)
        event.getProperties().getProperty(Property.DESCRIPTION);
   
        if (text == null) {
            if (description != null)
                event.getProperties().remove(description);
            return;
        }                
        if (description == null) {
            description = new Description();
            event.getProperties().add(description);
        }
        description.setValue(text);
    }

    /**
     * Returns a copy of the the iCalendar DTSTART property value of
     * the event (never null).
     */
    @Transient
    public Date getStartDate() {
        VEvent event = getEvent();
        if(event==null)
            return null;
        
        DtStart dtStart = event.getStartDate();
        if (dtStart == null)
            return null;
        return dtStart.getDate();
    }

    /** 
     * Sets the iCalendar DTSTART property of the event.
     *
     * @param date a <code>Date</code>
     */
    @Transient
    public void setStartDate(Date date) {
        DtStart dtStart = getEvent().getStartDate();
        if (dtStart != null)
            dtStart.setDate(date);
        else {
            dtStart = new DtStart(date);
            getEvent().getProperties().add(dtStart);
        }
        setDatePropertyValue(dtStart, date);
        setDirty(true);
    }

    /**
     * Returns the end date of the event as calculated from the
     * iCalendar DTEND property value or the the iCalendar DTSTART +
     * DURATION.
     */
    @Transient
    public Date getEndDate() {
        VEvent event = getEvent();
        if(event==null)
            return null;
        DtEnd dtEnd = event.getEndDate(false);
        // if no DTEND, then calculate endDate from DURATION
        if (dtEnd == null) {
            Date startDate = getStartDate();
            Dur duration = getDuration();
            
            // if no DURATION, then there is no end time
            if(duration==null)
                return null;
            
            Date endDate = null;
            if(startDate instanceof DateTime)
                endDate = new DateTime(startDate);
            else
                endDate = new Date(startDate);
            
            endDate.setTime(duration.getTime(startDate).getTime());
            return endDate;
        }
            
        return dtEnd.getDate();
    }

    /** 
     * Sets the iCalendar DTEND property of the event.
     *
     * @param date a <code>Date</code>
     */
    @Transient
    public void setEndDate(Date date) {
        DtEnd dtEnd = getEvent().getEndDate();
        if (dtEnd != null && date != null)
            dtEnd.setDate(date);
        else  if(dtEnd !=null && date == null) {
            // remove DtEnd if there is no end date
            getEvent().getProperties().remove(dtEnd);
        }
        else {
            // remove the duration if there was one
            Duration duration = (Duration) getEvent().getProperties().
                getProperty(Property.DURATION);
            if (duration != null)
                getEvent().getProperties().remove(duration);
            dtEnd = new DtEnd(date);
            getEvent().getProperties().add(dtEnd);
        }
        setDatePropertyValue(dtEnd, date);
        setDirty(true);
    }

    @Transient
    protected void setDatePropertyValue(DateProperty prop,
                                        Date date) {
        if (prop == null)
            return;
        Value value = (Value)
            prop.getParameters().getParameter(Parameter.VALUE);
        if (value != null)
            prop.getParameters().remove(value);
        value = date instanceof DateTime ? Value.DATE_TIME : Value.DATE;
        prop.getParameters().add(value);
        setDirty(true);
    }
    
    @Transient
    protected void setDateListPropertyValue(DateListProperty prop,
                                        Date date) {
        if (prop == null)
            return;
        Value value = (Value)
            prop.getParameters().getParameter(Parameter.VALUE);
        if (value != null)
            prop.getParameters().remove(value);
        
        value = date instanceof DateTime ? Value.DATE_TIME : Value.DATE;
        prop.getParameters().add(value);
        setDirty(true);
    }

    /**
     * Returns the duration of the event as calculated from the
     * iCalendar DURATION property value or the the iCalendar DTEND -
     * DTSTART.
     */
    @Transient
    public Dur getDuration() {
        Duration duration = (Duration)
            getEvent().getProperties().getProperty(Property.DURATION);
        if (duration != null)
            return duration.getDuration();
        DtStart dtstart = getEvent().getStartDate();
        if (dtstart == null)
            return null;
        DtEnd dtend = getEvent().getEndDate();
        if (dtend == null)
            return null;
        return new Duration(dtstart.getDate(), dtend.getDate()).getDuration();
    }

    /** 
     * Sets the iCalendar DURATION property of the event.
     *
     * @param dur a <code>Dur</code>
     */
    @Transient
    public void setDuration(Dur dur) {
        Duration duration = (Duration)
            getEvent().getProperties().getProperty(Property.DURATION);
        
        setDirty(true);
        
        // remove DURATION if dur is null
        if(dur==null) {
            if(duration != null) 
                getEvent().getProperties().remove(duration);
            return;
        }
        
        // update dur on existing DURATION
        if (duration != null)
            duration.setDuration(dur);
        else {
            // remove the dtend if there was one
            DtEnd dtend = getEvent().getEndDate();
            if (dtend != null)
                getEvent().getProperties().remove(dtend);
            duration = new Duration(dur);
            getEvent().getProperties().add(duration);
        }
    }

    /**
     * Returns a copy of the the iCalendar LOCATION property value of
     * the event (can be null).
     */
    @Transient
    public String getLocation() {
        Property p = getEvent().getProperties().
            getProperty(Property.LOCATION);
        if (p == null)
            return null;
        return p.getValue();
    }

    /** 
     * Sets the iCalendar LOCATION property of the event.
     *
     * @param text a text string
     */
    @Transient
    public void setLocation(String text) {
        setDirty(true);
        
        Location location = (Location)
            getEvent().getProperties().getProperty(Property.LOCATION);
        
        if (text == null) {
            if (location != null)
                getEvent().getProperties().remove(location);
            return;
        }                
        if (location == null) {
            location = new Location();
            getEvent().getProperties().add(location);
        }
        location.setValue(text);
    }
    
    /**
     * Returns a list of copies of the iCalendar RRULE property values
     * of the event (can be empty).
     */
    @Transient
    public List<Recur> getRecurrenceRules() {
        ArrayList<Recur> l = new ArrayList<Recur>();
        VEvent event = getEvent();
        if(event!=null) {
            for (RRule rrule : (List<RRule>) getEvent().getProperties().
                     getProperties(Property.RRULE))
                l.add(rrule.getRecur());
        }
        return l;
    }

    /** 
     * Sets the iCalendar RRULE properties of the event,
     * removing any RRULEs that were previously set.
     *
     * @param recurs a <code>List</code> of <code>Recur</code>s
     */
    @Transient
    public void setRecurrenceRules(List<Recur> recurs) {
        if (recurs == null)
            return;
        PropertyList pl = getEvent().getProperties();
        for (RRule rrule : (List<RRule>) pl.getProperties(Property.RRULE))
            pl.remove(rrule);
        for (Recur recur : recurs)
            pl.add(new RRule(recur));
        
        setDirty(true);
    }

    /**
     * Returns a list of copies of the iCalendar EXRULE property values
     * of the event (can be empty).
     */
    @Transient
    public List<Recur> getExceptionRules() {
        ArrayList<Recur> l = new ArrayList<Recur>();
        for (ExRule exrule : (List<ExRule>) getEvent().getProperties().
                 getProperties(Property.EXRULE))
            l.add(exrule.getRecur());
        return l;
    }

    /** 
     * Sets the iCalendar EXRULE properties of the event,
     * removing any EXRULEs that were previously set.
     *
     * @param recurs a <code>List</code> of <code>Recur</code>s
     */
    @Transient
    public void setExceptionRules(List<Recur> recurs) {
        if (recurs == null)
            return;
        PropertyList pl = getEvent().getProperties();
        for (ExRule exrule : (List<ExRule>) pl.getProperties(Property.EXRULE))
            pl.remove(exrule);
        for (Recur recur : recurs)
            pl.add(new ExRule(recur));
        setDirty(true);
    }

    /**
     * Returns a list of copies of the iCalendar RDATE property values
     * of the event (can be empty).
     */
    @Transient
    public DateList getRecurrenceDates() {
        
        DateList l = null;
        
        VEvent event = getEvent();
        if(event==null)
            return null;
        
        for (RDate rdate : (List<RDate>) event.getProperties().
                 getProperties(Property.RDATE)) {
            if(l==null) {
                if(Value.DATE.equals(rdate.getParameter(Parameter.VALUE)))
                    l = new DateList(Value.DATE);
                else
                    l = new DateList(Value.DATE_TIME);
            }
            l.addAll(rdate.getDates());
        }
            
        return l;
    }

    /**
     * Sets a single iCalendar RDATE property of the event,
     * removing any RDATEs that were previously set.
     *
     * @param dates a <code>DateList</code>
     */
    @Transient
    public void setRecurrenceDates(DateList dates) {
        if (dates == null)
            return;
        
        setDirty(true);
        PropertyList pl = getEvent().getProperties();
        for (RDate rdate : (List<RDate>) pl.getProperties(Property.RDATE))
            pl.remove(rdate);
        if (dates.isEmpty())
            return;
        
        RDate rDate = new RDate(dates);
        setDateListPropertyValue(rDate, (Date) dates.get(0));
        pl.add(rDate);   
    }

    /**
     * Returns a list of copies of the values of all iCalendar EXDATE
     * properties of the event (can be empty).
     */
    @Transient
    public DateList getExceptionDates() {
        DateList l = null;
        for (ExDate exdate : (List<ExDate>) getEvent().getProperties().
                 getProperties(Property.EXDATE)) {
            if(l==null) {
                if(Value.DATE.equals(exdate.getParameter(Parameter.VALUE)))
                    l = new DateList(Value.DATE);
                else
                    l = new DateList(Value.DATE_TIME);
            }
            l.addAll(exdate.getDates());
        }
            
        return l;
    }
    
    /**
     * Return the first display alarm on the event
     * @return first display alarm on event
     */
    @Transient
    public VAlarm getDisplayAlarm() {
        VEvent event = getEvent();
       
        if(event==null)
            return null;
        
        return getDisplayAlarm(event);
    }
    
    @Transient
    protected VAlarm getDisplayAlarm(VEvent event) {
        for(Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY))
                return alarm;
        }
        
        return null;
    }
    
    public void removeDisplayAlarm() {
        VEvent event = getEvent();
        
        if(event==null)
            return;
         
        for(Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY)) {
                it.remove();
            }
        }
    }
    
    /**
     * Return the description of the first display alarm on the event.
     * @return alarm description
     */
    @Transient
    public String getDisplayAlarmDescription() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return null;
        
        Description description = (Description) alarm.getProperties()
                .getProperty(Property.DESCRIPTION);
        
        if(description==null)
            return null;
        
        return description.getValue();
    }
    
    /**
     * Set the description of the first display alarm on the event.
     * @param newDescription display alarm description
     */
    public void setDisplayAlarmDescription(String newDescription) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return;
        
        Description description = (Description) alarm.getProperties()
                .getProperty(Property.DESCRIPTION);
        
        if (newDescription == null) {
            if (description != null)
                alarm.getProperties().remove(description);
        }
        if (description == null) {
            description = new Description();
            alarm.getProperties().add(description);
        }
        
        description.setValue(newDescription);
    }
    
    /**
     * Return the Trigger of the first display alarm on the event
     * @return trigger of the first display alarm
     */
    @Transient
    public Trigger getDisplayAlarmTrigger() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return null;
        
        return (Trigger) alarm.getProperties().getProperty(Property.TRIGGER);
    }
    
    
    /**
     * Set the trigger property of the first display alarm on the event.
     * @param newTrigger trigger
     */
    public void setDisplayAlarmTrigger(Trigger newTrigger) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return;
        
        Trigger oldTrigger = (Trigger) alarm.getProperties().getProperty(
                Property.TRIGGER);
        if (oldTrigger != null)
            alarm.getProperties().remove(oldTrigger);

        if(newTrigger!=null)
            alarm.getProperties().add(newTrigger);
    }
    
    /**
     * Set the trigger property of the first display alarm on the event 
     * to be a absolute trigger.
     * @param triggerDate date display alarm triggers
     */
    public void setDisplayAlarmTriggerDate(DateTime triggerDate) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return;

        Trigger oldTrigger = (Trigger) alarm.getProperties().getProperty(
                Property.TRIGGER);
        if (oldTrigger != null)
            alarm.getProperties().remove(oldTrigger);
        
        Trigger newTrigger = new Trigger();
        newTrigger.getParameters().add(Value.DATE_TIME);
        newTrigger.setDateTime(triggerDate);
        
        alarm.getProperties().add(newTrigger);
    }
    
    /**
     * Return the duration of the first display alarm on the event
     * @return duration of the first display alarm
     */
    @Transient
    public Dur getDisplayAlarmDuration() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return null;
        
        Duration dur =  (Duration) alarm.getProperties().getProperty(Property.DURATION);
        if(dur!=null)
            return dur.getDuration();
        else
            return null;
    }
    
    /**
     * Set the durcation of the first display alarm on the event
     * @param dur duration
     */
    public void setDisplayAlarmDuration(Dur dur) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return;
        
        Duration duration = (Duration) alarm.getProperties().getProperty(
                Property.DURATION);
        if (dur == null) {
            if (duration != null)
                alarm.getProperties().remove(duration);
            
            return;
        }
        if (duration == null) {
            duration = new Duration();
            alarm.getProperties().add(duration);
        }
        
        duration.setDuration(dur);
    }
    
    /**
     * Return the repeat count on the first display alarm on the event
     * @return repeat count of the first display alarm on the event
     */
    @Transient
    public Integer getDisplayAlarmRepeat() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return null;
        
        Repeat repeat = (Repeat) alarm.getProperties().getProperty(Property.REPEAT);
        
        if(repeat==null)
            return null;
        
        return repeat.getCount();
    }
    
    /**
     * Set the repeat count on the first display alarm on the event.
     * @param count repeat count of the first display alarm.
     */
    public void setDisplayAlarmRepeat(Integer count) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null)
            return;
        
        Repeat repeat = (Repeat) alarm.getProperties().getProperty(Property.REPEAT);
        if (count == null) {
            if (repeat != null)
                alarm.getProperties().remove(repeat);
            return;
        }
        if (repeat == null) {
            repeat = new Repeat();
            alarm.getProperties().add(repeat);
        }

        repeat.setCount(count.intValue());
    }

    /**
     * Sets a single iCalendar EXDATE property of the event,
     * removing any EXDATEs that were previously set.
     *
     * @param dates a <code>DateList</code>
     */
    @Transient
    public void setExceptionDates(DateList dates) {
        if (dates == null)
            return;
        setDirty(true);
        PropertyList pl = getEvent().getProperties();
        for (ExDate exdate : (List<ExDate>) pl.getProperties(Property.EXDATE))
            pl.remove(exdate);
        if (dates.isEmpty())
            return;
        
        ExDate exDate = new ExDate(dates);
        setDateListPropertyValue(exDate, (Date) dates.get(0));
        pl.add(exDate);
    }

    /**
     * Returns a copy of the the iCalendar RECURRENCE_ID property
     * value of the event (can be null). 
     */
    @Transient
    public Date getRecurrenceId() {
        RecurrenceId rid = getEvent().getRecurrenceId();
        if (rid == null)
            return null;
        return rid.getDate();
    }

    /** 
     * Sets the iCalendar RECURRENCE_ID property of the event.
     *
     * @param date a <code>Date</code>
     */
    @Transient
    public void setRecurrenceId(Date date) {
        setDirty(true);
        RecurrenceId recurrenceId = (RecurrenceId)
            getEvent().getProperties().
            getProperty(Property.RECURRENCE_ID);
        if (date == null) {
            if (recurrenceId != null)
                getEvent().getProperties().remove(recurrenceId);
            return;
        }
        if (recurrenceId == null) {
            recurrenceId = new RecurrenceId();
            getEvent().getProperties().add(recurrenceId);
        }
        
        recurrenceId.setDate(date);
        setDatePropertyValue(recurrenceId, date);
    }

    /**
     * Returns a copy of the the iCalendar STATUS property value of
     * the event (can be null).
     */
    @Transient
    public String getStatus() {
        Property p = getEvent().getProperties().
            getProperty(Property.STATUS);
        if (p == null)
            return null;
        return p.getValue();
    }

    /** 
     * Sets the iCalendar STATUS property of the event.
     *
     * @param text a text string
     */
    @Transient
    public void setStatus(String text) {
        // ical4j Status value is immutable, so if there's any change
        // at all, we have to remove the old status and add a new
        // one.
        setDirty(true);
        Status status = (Status)
            getEvent().getProperties().getProperty(Property.STATUS);
        if (status != null)
            getEvent().getProperties().remove(status);
        if (text == null)
            return;
        getEvent().getProperties().add(new Status(text));
    }
    
    
    /**
     * Is the event marked as anytime.
     * @return true if the event is an anytime event
     */
    @Transient
    public Boolean isAnyTime() {
        DtStart dtStart = getEvent().getStartDate();
        if (dtStart == null)
            return Boolean.FALSE;
        Parameter parameter = dtStart.getParameters()
            .getParameter(PARAM_X_OSAF_ANYTIME);
        if (parameter == null) {
            return Boolean.FALSE;
        }

        return new Boolean(VALUE_TRUE.equals(parameter.getValue()));
    }
    
    @Transient
    public Boolean getAnyTime() {
        return isAnyTime();
    }
    
    /**
     * Toggle the event anytime parameter.
     * @param isAnyTime true if the event occurs anytime
     */
    public void setAnyTime(Boolean isAnyTime) {
        DtStart dtStart = getEvent().getStartDate();
        if (dtStart == null)
            throw new IllegalStateException("event has no start date");
        Parameter parameter = dtStart.getParameters().getParameter(
                PARAM_X_OSAF_ANYTIME);

        setDirty(true);
        
        // add X-OSAF-ANYTIME if it doesn't exist
        if (parameter == null && Boolean.TRUE.equals(isAnyTime)) {
            dtStart.getParameters().add(getAnyTimeXParam());
            return;
        }

        // if it exists, update based on isAnyTime
        if (parameter != null) {
            dtStart.getParameters().remove(parameter);
            if (Boolean.TRUE.equals(isAnyTime))   
                dtStart.getParameters().add(getAnyTimeXParam());
        }
    }
    
    @Transient
    protected Parameter getAnyTimeXParam() {
        return new XParameter(PARAM_X_OSAF_ANYTIME, VALUE_TRUE);
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
        if(note.getIcalUid()!=null)
            uid.setValue(note.getIcalUid());
        else {
            // A modifications UID will be the parent's icaluid
            // or uid
            if(note.getModifies()!=null) {
                if(note.getModifies().getIcalUid()!=null)
                    uid.setValue(note.getModifies().getIcalUid());
                else
                    uid.setValue(note.getModifies().getUid());
            } else {
                uid.setValue(note.getUid());
            }
        }
     
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
     * Determine if an event is recurring
     * @return true if the underlying event is a recurring event
     */
    @Transient
    public boolean isRecurring() {
       if(getRecurrenceRules().size()>0)
           return true;
       
       DateList rdates = getRecurrenceDates();
       
       return (rdates!=null && rdates.size()>0);
    }
    
    /**
     * Create new display alarm on event.
     */
    public void creatDisplayAlarm() {
        VAlarm alarm = new VAlarm();
        alarm.getProperties().add(Action.DISPLAY);
        getEvent().getAlarms().add(alarm);
    }
}
