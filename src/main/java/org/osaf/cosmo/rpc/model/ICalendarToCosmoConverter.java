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
import static org.osaf.cosmo.util.ICalendarUtils.VEVENT_START_DATE_COMPARATOR;
import static org.osaf.cosmo.util.ICalendarUtils.getDuration;
import static org.osaf.cosmo.util.ICalendarUtils.getMasterEvent;
import static org.osaf.cosmo.util.ICalendarUtils.getVTimeZone;
import static org.osaf.cosmo.util.ICalendarUtils.hasProperty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.Observance;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;

import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.util.ICalendarUtils;

/**
 * An instance of this class is used to convert ICalendar
 * objects (for now just VEvents) into Scooby objects (Events)
 * @author bobbyrullo
 *
 */
public class ICalendarToCosmoConverter {
    public static final String EVENT_DESCRIPTION = "description";
    public static final String EVENT_START = "start";
    public static final String EVENT_END = "end";
    public static final String EVENT_TITLE = "title";
    public static final String EVENT_STATUS = "status";

    /**
     * Garden variety no-arg contructor
     */
    public ICalendarToCosmoConverter(){

    }

    /**
     * Creates an Event from an iCalendar VEvent
     * @param vevent
     */
    public Event createEvent(String itemId, VEvent vevent, net.fortuna.ical4j.model.Calendar calendar){
        Event event = new Event();
        setSimpleProperties(itemId, vevent, event);
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
            RecurrenceRule recurrenceRule = createRecurrenceRule(rrule, vevent
                    .getStartDate(), vevent, calendar);
            event.setRecurrenceRule(recurrenceRule);
            event.setMasterEvent(true);
        }

        if (dtEnd == null && duration == null) {
            // must be an All Day, an Any Time, or a Point in Time

            // let's check for the X-OSAF-ANYTIME param first
            if (VALUE_TRUE.equals(getParameterValue(dtStart, PARAM_X_OSAF_ANYTIME))) {
                event.setAnyTime(true);
                event.setStart(createCosmoDate(dtStart, calendar));
            } else if (!startDateHasTime ) {
                // the start is a DATE (not a DATE_TIME) so this must be an All
                // Day event
                event.setAllDay(true);
                event.setStart(createCosmoDate(dtStart, calendar));
            } else {
             // the start is a DATE_TIME (not a DATE) so this must be a
                // "point in time"
                event.setPointInTime(true);
                CosmoDate startAndEnd = createCosmoDate(dtStart, calendar);
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
                event.setStart(createCosmoDate(dtStart, calendar));
                event.setEnd(createCosmoDate((net.fortuna.ical4j.model.Date)endDate, calendar, null));

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
                event.setStart(createCosmoDate(dtStart, calendar));
                event.setEnd(createCosmoDate(dtEnd, calendar));
            }
        }
        return event;
    }


    public Event createModEvent(VEvent vevent,
            VEvent masterEvent, net.fortuna.ical4j.model.Date instanceDate, net.fortuna.ical4j.model.Calendar calendar) {
        Event event = new Event();
        setSimpleProperties(null, vevent, event);

        DtStart masterDtStart = (DtStart) masterEvent.getProperties()
                .getProperty(Property.DTSTART);
        boolean startDateHasTime = masterDtStart.getDate() instanceof DateTime;

        DtStart dtStart = (DtStart) vevent.getProperties().getProperty(
                Property.DTSTART);

        if (dtStart != null){
            event.setStart(createCosmoDate(dtStart, calendar));
        }

        DtEnd dtEnd = (DtEnd) vevent.getProperties()
                .getProperty(Property.DTEND);

        if (dtEnd != null){
            if (!startDateHasTime){
                //must be an ALL DAY event

                // since it's an all-day event let's use a Date as opposed to a
                // DateTime
                event.setAllDay(true);

                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(vevent.getEndDate().getDate());
                endCalendar.add(Calendar.DATE, -1);
                net.fortuna.ical4j.model.Date endDate = new net.fortuna.ical4j.model.Date(endCalendar.getTimeInMillis());
                event.setEnd(createCosmoDate((net.fortuna.ical4j.model.Date)endDate, calendar, null));
            } else {
                event.setEnd(createCosmoDate(dtEnd, calendar));
            }
        }

        return event;

    }
    
    
    /**
     * Returns a single array of Events for every VEVENT in every Calendar. If there
     * are any recurring events, the expanded instances will be returned for the given date
     * range
     * @param calendarEventItems the calendars from which to get the VEVENTS
     * @param startDate the start date to be used when expanding recurring events
     * @param endDate  the end date to be used when expanding recurring events
     */
    public Event[] createEventsFromCalendars(
            Collection<ContentItem> calendarEvents,
            DateTime startDate, DateTime endDate) {
        List<Event> events = new ArrayList<Event>();

        //iterate through all the CalendarEventItem's....
        for (ContentItem event : calendarEvents) {
            net.fortuna.ical4j.model.Calendar calendar = null;
            EventStamp eventStamp = EventStamp.getStamp(event);
            calendar = eventStamp.getCalendar();
            VEvent vevent = getMasterEvent(calendar);

            if (vevent != null) {
                Event e = createEvent(eventStamp.getItem().getUid(), vevent,
                        calendar);
                if (hasProperty(vevent, Property.RRULE)) {
                    List<Event> expandedEvents = expandEvent(e, vevent,
                            calendar, startDate, endDate);
                    events.addAll(expandedEvents);
                } else {
                    events.add(e);
                }
            }
        }
        return (Event[]) events.toArray(new Event[events.size()]);
    }

    public List<Event> expandEvent(Event event, VEvent vevent,
            net.fortuna.ical4j.model.Calendar calendar, DateTime rangeStart,
            DateTime rangeEnd) {
        OverridingInstanceFetcher fetcher = new OverridingInstanceFetcher(calendar);
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

        ExDate exDate =  (ExDate)vevent.getProperties().getProperty(Property.EXDATE);
        DateList exDates = exDate != null ? exDate.getDates() : null;
        DateList startDateList = getExpandedDates(recur, exDates, vevent
                .getStartDate().getDate(), rangeStart, rangeEnd, dateOrDateTime);

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

                Event instance = new Event();
                instance.setId(event.getId());
                instance.setTitle(event.getTitle());
                instance.setDescription(event.getDescription());
                instance.setAllDay(event.isAllDay());
                instance.setAnyTime(event.isAnyTime());
                instance.setStatus(event.getStatus());
                instance.setInstance(true);
                instance.setRecurrenceRule(event.getRecurrenceRule());
                instance.setStart(createCosmoDate(instanceStartDate, calendar,
                        tzid));
                instance.setInstanceDate(instance.getStart());
                if (event.getEnd() != null) {
                    if (event.isAllDay()) {
                        Calendar endCalendar = Calendar.getInstance();
                        DateTime instanceEndDateTime = new DateTime(
                                instanceStartDate.getTime() + duration);
                        endCalendar.setTime(instanceEndDateTime);
                        endCalendar.add(Calendar.DATE, -1);
                        net.fortuna.ical4j.model.Date instanceEndDate = new net.fortuna.ical4j.model.Date(
                                endCalendar.getTimeInMillis());
                        instance.setEnd(createCosmoDate(instanceEndDate,
                                calendar, null));

                    } else {
                        if (dateOrDateTime.equals(Value.DATE_TIME)) {
                            DateTime endTime = new DateTime(instanceStartDate
                                    .getTime()
                                    + duration);

                            if (masterTimezone != null) {
                                endTime.setTimeZone(masterTimezone);
                            }
                            instance.setEnd(createCosmoDate(endTime, calendar,
                                    tzid));
                        }
                    }

                //let's add any individual instance modifications that may exist
                VEvent modificationVEvent = fetcher.getInstance(instanceStartDate);
                if (modificationVEvent != null) {
                    copyModifiedProperties(modificationVEvent, instance,
                            calendar);
                }
                if (instanceStartDate.equals(masterStartDate)) {
                    instance.setMasterEvent(true);
                    events.set(0, instance);
                } else {
                    events.add(instance);
                }
            }
        }
        return events;
    }

    private void copyModifiedProperties(VEvent modificationVEvent,
            Event instance, net.fortuna.ical4j.model.Calendar calendar) {

        //have to do DTSTART first, since others depend on it.
        DtStart dtStart = (DtStart) modificationVEvent.getProperties().getProperty(Property.DTSTART);
        if (dtStart != null){
            instance.setStart(createCosmoDate(dtStart, calendar));
        }
        for (Object o : modificationVEvent.getProperties()) {
            Property property = (Property) o;
            if (property.getName().equals(Property.DTSTART)) {
                continue;
            } else if (property.getName().equals(Property.DTEND)) {
                instance.setEnd(createCosmoDate((DtEnd) property, calendar));
            } else if (property.getName().equals(Property.SUMMARY)) {
                instance.setTitle(getPropertyValue(modificationVEvent,
                        Property.SUMMARY));
            } else if (property.getName().equals(Property.STATUS)){
                instance.setStatus(getPropertyValue(modificationVEvent, Property.STATUS));
            } else if (property.getName().equals(Property.DESCRIPTION)){
                instance.setStatus(getPropertyValue(modificationVEvent, Property.DESCRIPTION));
            } else if (property.getName().equals(Property.DURATION)){

            }
        }
    }

    private static class OverridingInstanceFetcher {
        private VEvent masterEvent = null;

        private Map<Long, VEvent> overrideMap = new HashMap<Long, VEvent>();

        private List<VEvent> thisAndFutures = new ArrayList<VEvent>();

        private List<VEvent> thisAndPriors = new ArrayList<VEvent>();

        public OverridingInstanceFetcher(
                net.fortuna.ical4j.model.Calendar calendar) {
            ComponentList vevents = calendar.getComponents().getComponents(
                    Component.VEVENT);
            for (Object o : vevents) {
                VEvent vEvent = (VEvent) o;
                RecurrenceId recurrenceId = vEvent.getReccurrenceId();
                if (recurrenceId != null) {
                    Range range = (Range) recurrenceId.getParameters()
                            .getParameter(Parameter.RANGE);
                    if (range != null) {
                        if (range.getValue().equals(Range.THISANDFUTURE)) {
                            thisAndFutures.add(vEvent);
                        } else if (range.getValue().equals(Range.THISANDPRIOR)) {
                            thisAndPriors.add(vEvent);
                        }
                    } else {
                        overrideMap.put(recurrenceId.getDate().getTime(),
                                vEvent);
                    }
                } else {
                    masterEvent = vEvent;
                }
            }
            Collections.sort(thisAndFutures,VEVENT_START_DATE_COMPARATOR );
            Collections.sort(thisAndPriors, VEVENT_START_DATE_COMPARATOR);
        }

        public VEvent getInstance(net.fortuna.ical4j.model.Date date){
            VEvent instance = new VEvent();

            for (int x = thisAndPriors.size() - 1; x >=0; x--){
                VEvent curInstance = thisAndPriors.get(x);
                net.fortuna.ical4j.model.Date insDate = curInstance.getReccurrenceId().getDate();

                if (insDate.equals(date)){
                    copyProperties(curInstance, instance, date);
                    break;
                }

                if (insDate.after(date)){
                    copyProperties(curInstance, instance, date);
                    continue;
                }

                break;
            }

            for (int x = 0; x < thisAndFutures.size();x++){
                VEvent curInstance = thisAndPriors.get(x);
                net.fortuna.ical4j.model.Date insDate = curInstance.getReccurrenceId().getDate();

                if (insDate.equals(date)){
                    copyProperties(curInstance, instance, date);
                    break;
                }

                if (insDate.before(date)){
                    copyProperties(curInstance, instance, date);
                    continue;
                }

                break;
            }

            VEvent curInstance = overrideMap.get(date.getTime());

            if (curInstance != null){
                copyProperties(curInstance, instance, date);
            }

            if (instance.getProperties().size() == 0){
                instance = null;
            }

            return instance;
        }

        public void copyProperties(VEvent source, VEvent dest,
                net.fortuna.ical4j.model.Date d) {

            //we must do DT_START, DT_END first, since DTEND relies on value of DTSTART.
            DtStart dtStart = (DtStart) source.getProperties().getProperty(Property.DTSTART);
            if (dtStart != null) {
                if (!dtStart.getDate().equals(
                        source.getReccurrenceId().getDate())) {
                    net.fortuna.ical4j.model.Date newDate = ICalendarUtils.clone(d);
                    long delta = dtStart.getDate().getTime()
                            - source.getReccurrenceId().getDate().getTime();
                    newDate.setTime(newDate.getTime() + delta);
                    DtStart newDtStart = new DtStart(newDate);
                    ICalendarUtils.addOrReplaceProperty(dest, newDtStart);
                }
            }

            DtEnd sourceDtEnd= (DtEnd) source.getProperties().getProperty(Property.DTEND);
            if (sourceDtEnd != null) {
                DtStart sourceDtStart = (DtStart) source.getProperties()
                        .getProperty(Property.DTSTART);

                net.fortuna.ical4j.model.Date sourceStartDate = sourceDtStart != null ?
                        sourceDtStart.getDate() : d;
                net.fortuna.ical4j.model.Date sourceEndDate = sourceDtEnd
                        .getDate();
                long delta = sourceEndDate.getTime()
                        - sourceStartDate.getTime();
                    net.fortuna.ical4j.model.Date newEndDate = ICalendarUtils
                            .clone(dest.getStartDate()  == null ? d : dest.getStartDate().getDate());
                    newEndDate.setTime(newEndDate.getTime() + delta);
                    DtEnd newDtEnd = new DtEnd(newEndDate);
                    ICalendarUtils.addOrReplaceProperty(dest, newDtEnd);
            }

            for (Object o : source.getProperties()) {
                Property p = (Property) o;

                if (p.getName().equals(Property.RECURRENCE_ID)
                        || p.getName().equals(Property.UID)
                        || p.getName().equals(Property.SEQUENCE)
                        || p.getName().equals(Property.DTSTART)
                        || p.getName().equals(Property.DTEND)) {
                    continue;
                }

                ICalendarUtils.addOrReplaceProperty(dest, p);
            }
        }

    }
    private void setSimpleProperties(String itemId, VEvent vevent, Event event) {
        event.setId(itemId);
        event.setDescription(getPropertyValue(vevent, Property.DESCRIPTION));
        event.setTitle(getPropertyValue(vevent, Property.SUMMARY));
        event.setStatus(getPropertyValue(vevent, Property.STATUS));
    }

    private RecurrenceRule createRecurrenceRule(RRule rrule, DtStart dtStart, VEvent vevent,
            net.fortuna.ical4j.model.Calendar calendar) {
        RecurrenceRule recurrenceRule = new RecurrenceRule();
        Recur recur = rrule.getRecur();
        if (isCustom(recur)) {
            // TODO set something more readable?
            recurrenceRule.setCustomRule(rrule.getValue());
        } else {
            if (recur.getFrequency().equals(Recur.WEEKLY)) {
                if (recur.getInterval() == 1 || recur.getInterval() == -1){
                    recurrenceRule.setFrequency(RecurrenceRule.FREQUENCY_WEEKLY);
                } else if (recur.getInterval() == 2){
                    recurrenceRule.setFrequency(RecurrenceRule.FREQUENCY_BIWEEKLY);
                }
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
        String tzIdValue = null;
        // let's make sure that the DtStart actually is a DateTime
        Date startDate = dtStart.getDate();
        if (startDate instanceof DateTime) {
            DateTime dtStartDate = (DateTime) startDate;
            TimeZone timezone = dtStartDate.getTimeZone();
            if (timezone != null) {
                tzId = (TzId) dtStart.getParameters().getParameter(
                        Parameter.TZID);
            }
        }

        // If this is a datetime, we must convert from UTC to the appropriate
        // timezone
        if (until != null) {
            Calendar untilCalendar = Calendar.getInstance();
            untilCalendar.setTime(until);
            untilCalendar.add(Calendar.DATE, -1);
            net.fortuna.ical4j.model.Date date = new net.fortuna.ical4j.model.Date(untilCalendar.getTimeInMillis());
            CosmoDate scoobyDate = createCosmoDate(date , calendar, null);
            recurrenceRule.setEndDate(scoobyDate);
        } else {
            recurrenceRule.setEndDate(null);
        }

        //now deal with EXDATE's
        ExDate exdate = (ExDate) vevent.getProperties().getProperty(
                Property.EXDATE);
        if (exdate != null) {
            DateList dates = exdate.getDates();
            if (dates != null && dates.size() > 0) {
                CosmoDate[] cosmoExceptionDates = new CosmoDate[dates.size()];
                for (int x = 0; x < dates.size(); x++) {
                    net.fortuna.ical4j.model.Date date = (net.fortuna.ical4j.model.Date) dates
                            .get(x);
                    CosmoDate cosmoDate = createCosmoDate(date, calendar,
                            tzIdValue);
                    cosmoExceptionDates[x] = cosmoDate;
                }
                recurrenceRule.setExceptionDates(cosmoExceptionDates);
            }
        }

        //now deal with modifications
        ComponentList vevents = calendar.getComponents().getComponents(
                Component.VEVENT);
        List<Modification> mods = new ArrayList<Modification>();
        if (vevents != null){
            for (int x = 0; x < vevents.size();x++){
                VEvent curVEvent = (VEvent) vevents.get(x);
                RecurrenceId recurrenceId = curVEvent.getReccurrenceId();
                if (recurrenceId != null){
                    Modification modification = new Modification();
                    CosmoDate instanceDate = createCosmoDate(recurrenceId,  calendar);
                    modification.setInstanceDate(instanceDate);
                    Event modEvent = createModEvent(curVEvent, ICalendarUtils
                            .getMasterEvent(calendar), recurrenceId.getDate(),
                            calendar);
                    String[] modifiedProperties = getModifiedProprties(curVEvent, vevent);
                    modification.setModifiedProperties(modifiedProperties);
                    modification.setEvent(modEvent);
                    mods.add(modification);
                }
            }
        }

        recurrenceRule.setModifications(mods.toArray(new Modification[0]));

        return recurrenceRule;
    }

    private String[] getModifiedProprties(VEvent modifiedEvent, VEvent originalEvent) {
        List<String> props = new ArrayList<String>();
        addIfHasProperty(modifiedEvent, props, Property.DESCRIPTION, EVENT_DESCRIPTION);
        addIfHasProperty(modifiedEvent, props, Property.SUMMARY, EVENT_TITLE);
        addIfHasProperty(modifiedEvent, props, Property.STATUS, EVENT_STATUS);

        RecurrenceId recurrenceId = modifiedEvent.getReccurrenceId();
        net.fortuna.ical4j.model.Date origStartDate = recurrenceId.getDate();

        if (modifiedEvent.getStartDate() != null) {
            net.fortuna.ical4j.model.Date modStartDate = modifiedEvent
                    .getStartDate().getDate();
            if (!origStartDate.equals(modStartDate)) {
                props.add(EVENT_START);
            }

        }

        long duration = getDuration(originalEvent);
        net.fortuna.ical4j.model.Date origEndDate = (net.fortuna.ical4j.model.Date) ICalendarUtils
                .clone(origStartDate);
        origEndDate.setTime(origEndDate.getTime() + duration);

        if (modifiedEvent.getProperties().getProperty(Property.DTEND) != null) {
            net.fortuna.ical4j.model.Date modEndDate = modifiedEvent
                    .getEndDate().getDate();
            if (!origEndDate.equals(modEndDate)) {
                props.add(EVENT_END);
            }
        }

        return props.toArray(new String[0]);
    }

    private void addIfHasProperty(VEvent vEvent, List<String> propList, String iCalPropName,
            String cosmoPropName) {
        if (hasProperty(vEvent, iCalPropName)){
            propList.add(cosmoPropName);
        }
    }

    /**
     * Returns true if the recurrence rule is not one that is able to be
     * created/edited in scooby.
     *
     * In other words, this event was created in some other app and has a
     * complex recurrence rule.
     *
     * @param recur
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


    private CosmoDate createCosmoDate(net.fortuna.ical4j.model.Date date,
            net.fortuna.ical4j.model.Calendar calendar, String tzid) {

        CosmoDate scoobyDate = new CosmoDate();
        Calendar jCalendar = null;
        boolean hasTime = false;

        if (tzid != null){
            hasTime = true;
            DateTime dateTime = (DateTime) date;
            scoobyDate.setTzId(tzid);
            VTimeZone vtimeZone = getVTimeZone(tzid, calendar);
            jCalendar = Calendar.getInstance(new TimeZone(vtimeZone));
            jCalendar.setTime(dateTime);
        } else if (isUtc(date)) {
            hasTime = true;
            DateTime dateTime = (DateTime) date;
            scoobyDate.setUtc(true);
            jCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT"));
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

    private CosmoDate createCosmoDate(DateProperty dateProperty,
            net.fortuna.ical4j.model.Calendar calendar){
        String tzid = getParameterValue(dateProperty, Parameter.TZID);
        return createCosmoDate(dateProperty.getDate(), calendar, tzid);
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

    @SuppressWarnings("unchecked")
    private DateList getExpandedDates(Recur recur, DateList exDates,
            net.fortuna.ical4j.model.Date masterEventStartDate,
            net.fortuna.ical4j.model.Date rangeStart,
            net.fortuna.ical4j.model.Date rangeEnd, Value dateOrDateTime) {
        DateList dates = recur.getDates(masterEventStartDate, rangeStart,
                rangeEnd, dateOrDateTime);
        if (exDates != null) {
            dates.removeAll(exDates);
        }
        return dates;
    }
}
