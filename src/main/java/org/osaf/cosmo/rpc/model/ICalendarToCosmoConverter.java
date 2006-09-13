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
package org.osaf.cosmo.rpc.model;

import static org.osaf.cosmo.icalendar.ICalendarConstants.PARAM_X_OSAF_ANYTIME;
import static org.osaf.cosmo.icalendar.ICalendarConstants.VALUE_TRUE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.Observance;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.RRule;

import org.osaf.cosmo.model.CalendarEventItem;

/**
 * An instance of this class is used to convert ICalendar 
 * objects (for now just VEvents) into Scooby objects (Events)
 * @author bobbyrullo
 *
 */
public class ICalendarToCosmoConverter {

    /**
     * Garden variety no-arg contructor
     */
    public ICalendarToCosmoConverter(){
        
    }
    
    /**
     * Creates an Event from an iCalendar VEvent
     * @param vevent
     * @return
     */
    public Event createEvent(String itemId, VEvent vevent, net.fortuna.ical4j.model.Calendar calendar){
        Event event = new Event();
        event.setId(itemId);
        event.setDescription(getPropertyValue(vevent, Property.DESCRIPTION));
        event.setTitle(getPropertyValue(vevent, Property.SUMMARY));
        event.setStatus(getPropertyValue(vevent, Property.STATUS));
        DtEnd dtEnd = (DtEnd) vevent.getProperties()
                .getProperty(Property.DTEND);
        DtStart dtStart = (DtStart) vevent.getProperties().getProperty(
                Property.DTSTART);
        Duration duration = (Duration) vevent.getProperties().getProperty(
                Property.DURATION);
        boolean startDateHasTime = dtStart.getDate() instanceof DateTime;
        
        RRule rrule = (RRule) vevent.getProperties()
            .getProperty(Property.RRULE);
        if (rrule != null){
            RecurrenceRule recurrenceRule = createRecurrenceRule(rrule, vevent.getStartDate(), calendar);
            event.setRecurrenceRule(recurrenceRule);
            event.setMasterEvent(true);
        }
        
        if (dtEnd == null && duration == null) {
            // must be an All Day, an Any Time, or a Point in Time
            
            // let's check for the X-OSAF-ANYTIME param first
            if (VALUE_TRUE.equals(getParameterValue(dtStart, PARAM_X_OSAF_ANYTIME))) {
                event.setAnyTime(true);
                event.setStart(createScoobyDate(dtStart, calendar));
            } else if (!startDateHasTime ) {
                // the start is a DATE (not a DATE_TIME) so this must be an All
                // Day event
                event.setAllDay(true);
                event.setStart(createScoobyDate(dtStart, calendar));   
            } else {
             // the start is a DATE_TIME (not a DATE) so this must be a
                // "point in time"
                event.setPointInTime(true);
                CosmoDate startAndEnd = createScoobyDate(dtStart, calendar); 
                event.setStart(startAndEnd);
                event.setEnd(startAndEnd);
            }
        } else {
            Date startDate = null;
            Date endDate = null;
            // we have a start and end (or a start and a duration)
            if (!startDateHasTime){
                //must be an ALL DAY event

                //since it's an all-day event let's use a Date as opposed to a DateTime
                startDate = new net.fortuna.ical4j.model.Date(vevent
                        .getStartDate().getDate().getTime());
                event.setAllDay(true);
                
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(vevent.getEndDate().getDate());
                endCalendar.add(Calendar.DATE, -1);
                endDate = new net.fortuna.ical4j.model.Date(endCalendar.getTimeInMillis());
                event.setStart(createScoobyDate(dtStart, calendar));
                event.setEnd(createScoobyDate((net.fortuna.ical4j.model.Date)endDate, calendar, null));
                
            } else {
                startDate = new DateTime(dtStart.getDate());
                if (dtEnd == null) {
                    dtEnd = vevent.getEndDate();
                    DateTime dateTime = (DateTime) dtStart.getDate();
                    TimeZone tz = dateTime.getTimeZone();
                    TzId tzid= (TzId) dtStart.getParameters().getParameter(Parameter.TZID);
                    if (tz != null){
                        dtEnd.setTimeZone(tz);
                        dtEnd.getParameters().add(tzid);
                    }
                }
                endDate = new DateTime(dtEnd.getDate());
                event.setStart(createScoobyDate(dtStart, calendar));
                event.setEnd(createScoobyDate(dtEnd, calendar));
            }
        }
        return event;
    }
    
    /**
     * Returns a single array of Events for every VEVENT in every Calendar. If there
     * are any recurring events, the expanded instances will be returned for the given date
     * range
     * @param calendarCollectionItems the calendars from which to get the VEVENTS
     * @param startDate the start date to be used when expanding recurring events
     * @param endDate  the end date to be used when expanding recurring events
     * @return
     */
    public Event[] createEventsFromCalendars(
            Collection<CalendarEventItem> calendarEventItems,
            DateTime startDate, DateTime endDate) {
        List<Event> events = new ArrayList<Event>();
        for (CalendarEventItem calendarEventItem : calendarEventItems) {
            net.fortuna.ical4j.model.Calendar calendar = null;
            try {
            calendar = calendarEventItem.getCalendar();
            } catch (Exception pe){
                throw new RuntimeException(pe);
            }
            ComponentList vevents = calendar.getComponents().getComponents(
                    Component.VEVENT);
            for (Object o : vevents) {
                VEvent vevent = (VEvent) o;
                Event e = createEvent(calendarEventItem.getUid(), vevent, calendar);
                if (hasProperty(vevent, Property.RRULE) ){
                    List<Event> expandedEvents = expandEvent(e, vevent, calendar,
                            startDate, endDate);
                    events.addAll(expandedEvents);
                } else {
                    events.add(e);
                }
            }
        }
        return (Event[]) events.toArray(new Event[events.size()]);
    }
    
    private List<Event> expandEvent(Event event, VEvent vevent,
            net.fortuna.ical4j.model.Calendar calendar, DateTime rangeStart,
            DateTime rangeEnd) {
        Date masterStartDate = vevent.getStartDate().getDate();

        List<Event> events = new ArrayList<Event>();

        // first let's see if the master event should go in the list.
        if (isEventInRange(vevent, rangeStart, rangeEnd)) {
            events.add(event);
        }

        // let's recur!
        RRule rrule = (RRule) vevent.getProperties()
                .getProperty(Property.RRULE);
        Recur recur = rrule.getRecur();

        Value dateOrDateTime = null;
        if (event.isAllDay() || event.isAnyTime()) {
            dateOrDateTime = Value.DATE;
        } else {
            dateOrDateTime = Value.DATE_TIME;
        }

        DateList startDateList = recur.getDates(
                vevent.getStartDate().getDate(), rangeStart, rangeEnd,
                dateOrDateTime);

        long duration = -1;
        if (event.isPointInTime()){
            duration = 0;
        } else if (event.getEnd() != null) {
            long startTime = vevent.getStartDate().getDate().getTime();
            long endTime = vevent.getEndDate().getDate().getTime();
            duration = endTime - startTime;
        }

        String tzid = getParameterValue(vevent.getStartDate(),
                Parameter.TZID);
        TimeZone masterTimezone = null;
        if (masterStartDate instanceof DateTime) {
            DateTime masterStartDateTime = (DateTime) masterStartDate;
            masterTimezone = masterStartDateTime.getTimeZone();
        }
        for (int x = 0; x < startDateList.size(); x++) {
            net.fortuna.ical4j.model.Date instanceStartDate = (net.fortuna.ical4j.model.Date) 
            startDateList.get(x);

            if (instanceStartDate instanceof DateTime && masterTimezone != null) {
                DateTime instanceStartDateTime = (DateTime) instanceStartDate;
                instanceStartDateTime.setTimeZone(masterTimezone);
            }

            if (!instanceStartDate.equals(masterStartDate)) {
                Event instance = new Event();
                instance.setId(event.getId());
                instance.setTitle(event.getTitle());
                instance.setDescription(event.getDescription());
                instance.setAllDay(event.isAllDay());
                instance.setAnyTime(event.isAnyTime());
                instance.setInstance(true);
                instance.setRecurrenceRule(event.getRecurrenceRule());
                instance.setStart(createScoobyDate(instanceStartDate, calendar,
                        tzid));
                if (event.getEnd() != null) {
                    if (event.isAllDay()) {
                        Calendar endCalendar = Calendar.getInstance();
                        DateTime instanceEndDateTime = new DateTime(
                                instanceStartDate.getTime() + duration);
                        endCalendar.setTime(instanceEndDateTime);
                        endCalendar.add(Calendar.DATE, -1);
                        net.fortuna.ical4j.model.Date instanceEndDate = new net.fortuna.ical4j.model.Date(
                                endCalendar.getTimeInMillis());
                        event.setEnd(createScoobyDate(instanceEndDate,
                                calendar, null));

                    } else {
                        if (dateOrDateTime.equals(Value.DATE_TIME)) {
                            DateTime endTime = new DateTime(instanceStartDate
                                    .getTime()
                                    + duration);

                            if (masterTimezone != null) {
                                endTime.setTimeZone(masterTimezone);
                            }
                            instance.setEnd(createScoobyDate(endTime, calendar,
                                    tzid));
                        }
                    }
                }

                events.add(instance);
            }
        }
        return events;
    }

    private RecurrenceRule createRecurrenceRule(RRule rrule, DtStart dtStart,
            net.fortuna.ical4j.model.Calendar calendar) {
        RecurrenceRule recurrenceRule = new RecurrenceRule();
        Recur recur = rrule.getRecur();
        if (isCustom(recur)) {
            // TODO set something more readable?
            recurrenceRule.setCustomRule(rrule.getValue());
        } else {
            if (recur.getFrequency().equals(Recur.WEEKLY)) {
                recurrenceRule.setFrequency(RecurrenceRule.FREQUENCY_WEEKLY);
            } else if (recur.getFrequency().equals(Recur.MONTHLY)) {
                recurrenceRule.setFrequency(RecurrenceRule.FREQUENCY_MONTHLY);
            } else if (recur.getFrequency().equals(Recur.DAILY)) {
                recurrenceRule.setFrequency(RecurrenceRule.FREQUENCY_DAILY);
            } else if (recur.getFrequency().equals(Recur.YEARLY)) {
                recurrenceRule.setFrequency(RecurrenceRule.FREQUENCY_YEARLY);
            }
        }

        net.fortuna.ical4j.model.Date until = recur.getUntil();
        TzId tzId = null;

        // If this is a datetime, we must convert from UTC to the appropriate
        // timezone
        if (until != null) {
            if (until instanceof DateTime) {
                DateTime dtUntil = (DateTime) until;

                // let's make sure that the DtStart actually is a DateTime
                Date startDate = dtStart.getDate();
                if (startDate instanceof DateTime) {
                    DateTime dtStartDate = (DateTime) startDate;
                    TimeZone timezone = dtStartDate.getTimeZone();
                    if (timezone != null) {
                        tzId = (TzId) dtStart.getParameters().getParameter(
                                Parameter.TZID);
                        // this changes the timezone from utc
                        dtUntil.setTimeZone(timezone);
                        until = dtUntil;
                    }
                }
            }
            String tzIdValue = null;
            if (tzId != null) {
                tzIdValue = tzId.getValue();
            }

            CosmoDate scoobyDate = createScoobyDate(until, calendar, tzIdValue);
            recurrenceRule.setEndDate(scoobyDate);

        } else {
            recurrenceRule.setEndDate(null);
        }
        return recurrenceRule;
    }   

    /**
     * Returns true if the recurrence rule is not one that is able to be
     * created/edited in scooby.
     * 
     * In other words, this event was created in some other app and has a
     * complex recurrence rule.
     * 
     * @param recur
     * @return
     */
    private boolean isCustom(Recur recur) {
        if (recur.getFrequency().equals(Recur.SECONDLY)
                || recur.getFrequency().equals(Recur.MINUTELY)) {
            return true;
        }
        
        //If they specified a count, it's custom
        if (recur.getCount() != -1){
            return true;
        }
        
        if (!isEmpty(recur.getYearDayList())){
            return true;
        }
        
        if (!isEmpty(recur.getMonthDayList())){
            return true;
        }

        if (!isEmpty(recur.getMonthList())){
            return true;
        }
        
        if (!isEmpty(recur.getWeekNoList())){
            return true;
        }
        
        if (!isEmpty(recur.getDayList())){
            return true;
        }
        
        if (!isEmpty(recur.getHourList())){
            return true;
        }

        if (!isEmpty(recur.getMinuteList())){
            return true;
        }
        
        if (!isEmpty(recur.getSecondList())){
            return true;
        }
        
        int interval = recur.getInterval();
        
        //We don't support any interval except for "1" or none (-1) 
        //with the exception of "2" for weekly events, in other words bi-weekly.
        if (interval != -1 && interval != 1 ){
            //if this is not a weekly event, it's custom.
            if (!recur.getFrequency().equals(Recur.WEEKLY)){
               return true; 
            }
            
            //so it IS A weekly event, but the value is not "2", so it's custom
            if (interval != 2){
                return true;
            }
        }

        return false;
    }

    private boolean hasProperty(Component c, String propName){
        PropertyList l = c.getProperties().getProperties(propName);
        return l != null && l.size() > 0;
    }
    
    private String getPropertyValue(Component component, String propertyName){
        Property property = component.getProperties().getProperty(propertyName);
        if (property == null){
            return null;
        } 
        
        return property.getValue();
    }
    
    private String getParameterValue(Property property, String paramName){
        Parameter parameter = property.getParameters().getParameter(paramName);
        if (parameter == null){
            return null;
        } 
        
        return parameter.getValue();
    }
    
    
    private CosmoDate createScoobyDate(net.fortuna.ical4j.model.Date date,
            net.fortuna.ical4j.model.Calendar calendar, String tzid) {

        CosmoDate scoobyDate = new CosmoDate();
        Calendar jCalendar = null;
        boolean hasTime = false;
        
        if (tzid != null){
            hasTime = true;
            DateTime dateTime = (DateTime) date;
            CosmoTimeZone scoobyTimeZone = new CosmoTimeZone();
            scoobyTimeZone.setId(tzid);
            VTimeZone vtimeZone = getVTimeZone(tzid, calendar);
            jCalendar = Calendar.getInstance(new TimeZone(vtimeZone));
            jCalendar.setTime(dateTime);
            int minutesOffset = (jCalendar.get(Calendar.ZONE_OFFSET) + jCalendar.get(Calendar.DST_OFFSET))  / (60 * 1000);
            scoobyTimeZone.setMinutesOffset(minutesOffset);
            Observance ob = vtimeZone.getApplicableObservance(dateTime);
            Property timezoneName = ob.getProperties().getProperty(Property.TZNAME);
            scoobyTimeZone.setName(timezoneName != null ? timezoneName.getValue() : ob.getName());
            scoobyDate.setTimezone(scoobyTimeZone);
        } else if (isUtc(date)) {
            hasTime = true;
            DateTime dateTime = (DateTime) date;
            jCalendar = Calendar.getInstance(dateTime.getTimeZone());
            jCalendar.setTime(dateTime);
        } else {
            hasTime = date instanceof DateTime;
            jCalendar = Calendar.getInstance();
            jCalendar.setTime(date);
        }

        scoobyDate.setYear(jCalendar.get(Calendar.YEAR));
        scoobyDate.setMonth(jCalendar.get(Calendar.MONTH));
        scoobyDate.setDate(jCalendar.get(Calendar.DATE));
        if (hasTime) {
            scoobyDate.setHours(jCalendar.get(Calendar.HOUR_OF_DAY));
            scoobyDate.setMinutes(jCalendar.get(Calendar.MINUTE));
            scoobyDate.setSeconds(jCalendar.get(Calendar.SECOND));
        }
        return scoobyDate;
    }
    
    private CosmoDate createScoobyDate(DateProperty dateProperty, 
            net.fortuna.ical4j.model.Calendar calendar){
        String tzid = getParameterValue(dateProperty, Parameter.TZID);
        return createScoobyDate(dateProperty.getDate(), calendar, tzid);
    }
    
    private VTimeZone getVTimeZone(String tzid, net.fortuna.ical4j.model.Calendar calendar){
        ComponentList list = calendar.getComponents().getComponents(Component.VTIMEZONE);
        for (Object component : list){
            VTimeZone vtimezone = (VTimeZone) component;
            String curTzid = getPropertyValue(vtimezone, Property.TZID);
            if (tzid.equals(curTzid)){
                return vtimezone;
            }
        }
        return null;
    }
    
    private boolean isUtc(net.fortuna.ical4j.model.Date date){
        if (date instanceof DateTime){
            DateTime dateTime = (DateTime) date;
            return dateTime.isUtc();
        }
        return false;
    }
    
    private boolean isDateInRange (Date d,  Date rangeBegin, Date rangeEnd){
        return ((d.after(rangeBegin) || d.equals(rangeBegin)) && (d
                .before(rangeEnd) || d.equals(rangeEnd)));
    }
    
    private boolean isEventInRange(Date eventStartDate, Date eventEndDate,
            Date rangeStart, Date rangeEnd) {
        return (isDateInRange(eventStartDate, rangeStart, rangeEnd) || (eventEndDate != null && isDateInRange(
                eventEndDate, rangeStart, rangeEnd)));
    }
    
    private boolean isEventInRange(VEvent vevent, Date rangeStart, Date rangeEnd) {
        Date startDate = vevent.getStartDate().getDate();
        Date endDate = null;
        DtEnd dtEnd = (DtEnd) vevent.getProperties()
                .getProperty(Property.DTEND);
        Duration duration = (Duration) vevent.getProperties().getProperty(
                Property.DURATION);
        if (dtEnd != null || duration != null){
            endDate = vevent.getEndDate().getDate();
        }
        return isEventInRange(startDate, endDate, rangeStart, rangeEnd);
    }
    
    private boolean isEmpty(Collection coll){
        return coll == null || coll.isEmpty();
    }
}
