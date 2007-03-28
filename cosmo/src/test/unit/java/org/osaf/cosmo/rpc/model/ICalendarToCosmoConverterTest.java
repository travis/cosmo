/*
 * Copyright 2005 Open Source Applications Foundation
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
import static org.osaf.cosmo.util.ICalendarUtils.getFirstEvent;
import static org.osaf.cosmo.util.ICalendarUtils.setDate;
import static org.osaf.cosmo.util.ICalendarUtils.setDateTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterFactoryImpl;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.util.ICalendarUtils;


public class ICalendarToCosmoConverterTest extends TestCase {
    public static final String NY_TZ = "America/New_York";
    public static final String LA_TZ = "America/Los_Angeles";

    private static final Log log = LogFactory.getLog(ICalendarToCosmoConverterTest.class);

    private TestHelper testHelper = new TestHelper();
    
    private ICalendarToCosmoConverter converter = new ICalendarToCosmoConverter();
    
    public void setUp(){
        testHelper = new TestHelper();
    }
    public void testICalToScoobyAllDay() throws Exception{
        VEvent vevent = new VEvent();
        DtStart dtStart = new DtStart();
        setDate(dtStart, new Date("20060202"));
        vevent.getProperties().add(dtStart);
        Calendar calendar = testHelper.makeDummyCalendar();
        calendar.getComponents().add(vevent);
        log.debug("Generated All Day VEvent:  \n" + vevent.toString());
        org.osaf.cosmo.rpc.model.Event event = converter.createEvent("12345",vevent, calendar);
        assertTrue(event.isAllDay());
        assertEquals(2006, event.getStart().getYear());
        assertEquals(1, event.getStart().getMonth() );
        assertEquals(2, event.getStart().getDate());
    }
    
    public void testICalToScoobyAnyTime() throws Exception{
        VEvent vevent = new VEvent();
        DtStart dtStart = new DtStart();
        Parameter anyTimeParam = ParameterFactoryImpl.getInstance()
            .createParameter(PARAM_X_OSAF_ANYTIME, VALUE_TRUE);
        dtStart.getParameters().add(anyTimeParam);
        setDate(dtStart, new Date());
        vevent.getProperties().add(dtStart);
        log.debug("Generated Any Time VEvent:  \n" + vevent.toString());
        Calendar calendar = testHelper.makeDummyCalendar();
        calendar.getComponents().add(vevent);
        Event event = converter.createEvent("12345", vevent, calendar);
        assertTrue(event.isAnyTime());
    }
    
    public void testICalToScoobyPointInTime() throws Exception{
        VEvent vevent = new VEvent();
        DtStart dtStart = new DtStart();
        setDateTime(dtStart, new DateTime());
        vevent.getProperties().add(dtStart);
        log.debug("Generated Point in Time VEvent:  \n" + vevent.toString());
        Calendar calendar = testHelper.makeDummyCalendar();
        calendar.getComponents().add(vevent);
        Event event = converter.createEvent("12345", vevent, calendar);
        assertTrue(event.isPointInTime());
    }
    
    public void testConvertChandlerPointInTimeEvent() throws Exception{
        Event e = loadEventIcs("chandler-point-in-time-event.ics", "12345");
        assertTrue(e.isPointInTime());
        
        //let's try converting it, and then converting back
        CosmoToICalendarConverter scoobyConverter = new CosmoToICalendarConverter();
        Calendar c = scoobyConverter.createWrappedVEvent(e);
        VEvent ve = getFirstEvent(c);
        e = converter.createEvent("12345",ve, c);
        assertTrue(e.isPointInTime());
    }
    
    public void testConvertChandlerAnyTimeEvent() throws Exception{
        Event e = loadEventIcs("chandler-anytime-event.ics", "12345");
        assertTrue(e.isAnyTime());
        
        //let's try converting it, and then converting back
        CosmoToICalendarConverter scoobyConverter = new CosmoToICalendarConverter();
        Calendar c = scoobyConverter.createWrappedVEvent(e);
        VEvent ve = getFirstEvent(c);
        e = converter.createEvent("111",ve, c);
        assertTrue(e.isAnyTime());
    }
    
    public void testConvertChandlerAllDayEvent() throws Exception{
        Event e = loadEventIcs("chandler-all-day-event.ics", "12345");
        assertTrue(e.isAllDay());
        
        CosmoDate startDate = e.getStart();
        
        //let's try converting it, and then converting back
        CosmoToICalendarConverter scoobyConverter = new CosmoToICalendarConverter();
        Calendar c = scoobyConverter.createWrappedVEvent(e);
        VEvent ve = getFirstEvent(c);
        e = converter.createEvent("1",ve, c);
        CosmoDate convertedStartDate = e.getStart();
        
        assertTrue(convertedStartDate.equals(startDate));
        assertTrue(e.isAllDay());
    }
    
    public void testConvertChandlerPlainEvent() throws Exception{
        Event e = loadEventIcs("chandler-plain-event.ics", "12345");
        assertFalse(e.isAllDay() || e.isAnyTime() || e.isPointInTime());
        
        //let's try converting it, and then converting back
        CosmoToICalendarConverter scoobyConverter = new CosmoToICalendarConverter();
        Calendar c = scoobyConverter.createWrappedVEvent(e);
        VEvent ve = getFirstEvent(c);
        e = converter.createEvent("1",ve, c);
        assertFalse(e.isAllDay() || e.isAnyTime() || e.isPointInTime());
    }
    
    public void testConvertChandlerAllTwoDaysEvent() throws Exception{
        Event e = loadEventIcs("chandler-all-two-days-event.ics", "12345");
        log.debug("Event end date: " + e.getEnd());
        assertTrue(e.isAllDay());
        
        //let's try converting it, and then converting back
        CosmoToICalendarConverter scoobyConverter = new CosmoToICalendarConverter();
        Calendar c = scoobyConverter.createWrappedVEvent(e);
        VEvent ve = getFirstEvent(c);
        log.debug("VEvent end date: " + ve.getEndDate());
        
        e = converter.createEvent("1",ve, c);
        log.debug("Event end date: " + e.getEnd());
        assertTrue(e.isAllDay());
    }
    
    public void testAllDayEveryDay() throws Exception {
        Event e = loadEventIcs("ical_all_day_every_day.ics", "12345");
        RecurrenceRule rrule = e.getRecurrenceRule();
        assertNotNull("Event should not be null", e);
        assertNotNull("No Recurrence Rule.", rrule);
        assertNull("Shouldn't be a custom rule", rrule.getCustomRule());
        assertNull("Shouldn't have an end date", rrule.getEndDate());
        assertTrue("Recurrence Rule should be Daily", rrule.getFrequency()
                .equals(RecurrenceRule.FREQUENCY_DAILY));
    } 
    
    public void testAllDayEveryDayWithEndDate() throws Exception {
        Event e = loadEventIcs("ical_all_day_everyday_enddate.ics", "12345");
        RecurrenceRule rrule = e.getRecurrenceRule();
        CosmoDate endDate = e.getRecurrenceRule().getEndDate(); 
        assertNotNull("Event should not be null", e);
        assertNotNull("No Recurrence Rule.", rrule);
        assertNull("Shouldn't be a custom rule", rrule.getCustomRule());
        assertNotNull("Should have endate", endDate);
        assertEquals(endDate.getMonth(), 0);
        assertEquals(endDate.getYear(), 2006);
        assertEquals(endDate.getDate(), 31);
        assertTrue("Recurrence Rule should be Daily", rrule.getFrequency()
                .equals(RecurrenceRule.FREQUENCY_DAILY));
    }
    
    public void testEveryDay1pm() throws Exception {
        Event e = loadEventIcs("ical_every_day_1pm.ics", "12345");
        RecurrenceRule rrule = e.getRecurrenceRule();
        CosmoDate endDate = e.getRecurrenceRule().getEndDate();
        assertNotNull("Event should not be null", e);
        assertNotNull("No Recurrence Rule.", rrule);
        assertNull("Shouldn't be a custom rule", rrule.getCustomRule());
        assertNotNull("Should have endate", endDate);
        assertEquals("Month not zero", endDate.getMonth(), CosmoDate.MONTH_JANUARY);
        assertEquals("Year not 2006", endDate.getYear(), 2006);
        assertEquals("Date not 31", endDate.getDate(), 31);
        assertTrue("Recurrence Rule should be Daily", rrule.getFrequency()
                .equals(RecurrenceRule.FREQUENCY_DAILY));
    };

    public void testEvery2Days() throws Exception {
        Event e = loadEventIcs("ical_every_2days_1pm.ics", "12345");
        RecurrenceRule rrule = e.getRecurrenceRule();
        CosmoDate endDate = e.getRecurrenceRule().getEndDate();
        assertNotNull("Event should not be null", e);
        assertNotNull("No Recurrence Rule.", rrule);
        assertNotNull("Should be a custom rule", rrule.getCustomRule());
        assertNotNull("Should have endate", endDate);
        assertEquals("Month not zero", endDate.getMonth(), CosmoDate.MONTH_JANUARY);
        assertEquals("Year not 2006", endDate.getYear(), 2006);
        assertEquals("Date not 31", endDate.getDate(), 31);
    }
    
    public void testOneExcetpionDate() throws Exception {
        Calendar c  = testHelper.loadIcs("ical_one_exdate.ics");
        VEvent vevent = getFirstEvent(c);
        Event e = converter.createEvent("1234556", vevent, c);
            
        RecurrenceRule rrule = e.getRecurrenceRule();
        CosmoDate[] exceptionDates = rrule.getExceptionDates();
        assertNotNull(exceptionDates);
        assertEquals(1, exceptionDates.length);
        CosmoDate exceptionDate  = exceptionDates[0];
        assertEquals(CosmoDate.MONTH_SEPTEMBER, exceptionDate.getMonth());
        assertEquals(20, exceptionDate.getDate());
        assertEquals(2006, exceptionDate.getYear());

        //let's make sure the date really gets skipped when we expand.
        VTimeZone vTimeZone = (VTimeZone) c.getComponents().getComponent(Component.VTIMEZONE);
        TimeZone timezone = new TimeZone (vTimeZone);
        DateTime rangeStart = new DateTime("20060917T060000", timezone);
        DateTime rangeEnd   = new DateTime("20060925T070000", timezone);
        List<Event> events = converter.expandEvent(e, vevent, c, rangeStart, rangeEnd);
        assertNotNull(events);
        assertEquals(8, events.size());
    }
    
    public void testCreateVEventWithMinimalProperties(){
        Calendar calendar = new Calendar();
        VEvent vEvent = new VEvent();
        DtStart dtStart = new DtStart(new Date());
        Uid uid = new Uid("12345");
        
        calendar.getComponents().add(vEvent);
        vEvent.getProperties().add(dtStart);
        vEvent.getProperties().add(uid);

        try {
            converter.createEvent("xxx", vEvent, calendar);
        } catch (Exception e){
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testRecurringEventsWithModifiedInstances() throws Exception{
        Calendar c = testHelper.loadIcs("ical_instance_mods.ics");
        VEvent vevent = ICalendarUtils.getMasterEvent(c);
        Event e = converter.createEvent("1234556", vevent, c);
        
        RecurrenceRule rr = e.getRecurrenceRule();
        assertNotNull(rr);
        assertEquals(2, rr.getModifications().length);
        for (Modification modification : rr.getModifications()){
            CosmoDate instanceDate = modification.getInstanceDate();
            String[] modifiedProps  = modification.getModifiedProperties();
            if (instanceDate.getDate() == 20){
                //for this event, only the title was changed, so we should see "1" modified property.
                assertEquals(1, modifiedProps.length);
            } else {
                //for this event the title was modified, and we changed the start time and end time
                assertEquals(21, instanceDate.getDate());
                assertEquals(3, modifiedProps.length);
                Set<String> s = new HashSet<String>(Arrays.asList(modifiedProps));
                assertTrue(s.contains("start"));
                assertTrue(s.contains("end"));
                assertTrue(s.contains("title"));
            }
        }
        
        //let's see if this event expands out properly
        VTimeZone vTimeZone = (VTimeZone) c.getComponents().getComponent(Component.VTIMEZONE);
        TimeZone timezone = new TimeZone (vTimeZone);
        DateTime rangeStart = new DateTime("20060917T000000", timezone);
        DateTime rangeEnd   = new DateTime("20060924T000000", timezone);
        List<Event> events = converter.expandEvent(e, vevent, c, rangeStart, rangeEnd);
        assertEquals(7, events.size());

        Event titleChangeEvent = events.get(3);
        assertEquals("TitleChange",  titleChangeEvent.getTitle());

        Event titleChangeStartChangeEndChangeEvent = events.get(4);
        assertEquals("TitleChangeStartChangeEndChange",  titleChangeStartChangeEndChangeEvent.getTitle());
        assertEquals(30, titleChangeStartChangeEndChangeEvent.getStart().getMinutes());
        assertEquals(30, titleChangeStartChangeEndChangeEvent.getEnd().getMinutes());
    }
    
    public void testBug7182() throws Exception{
        Calendar c = testHelper.loadIcs("bug7182.ics");
        VEvent vevent = ICalendarUtils.getMasterEvent(c);
        Event e = converter.createEvent("1234556", vevent, c);
        
        RecurrenceRule rr = e.getRecurrenceRule();
        assertNotNull(rr);
        assertEquals(1, rr.getModifications().length);
        for (Modification modification : rr.getModifications()){
            CosmoDate instanceDate = modification.getInstanceDate();
            String[] modifiedProps  = modification.getModifiedProperties();
            if (instanceDate.getDate() == 17){
                //for this event the title was modified, 
                //and we changed the start time and end time
                assertEquals(17, instanceDate.getDate());
                assertEquals(3, modifiedProps.length);
                Set<String> s = new HashSet<String>(Arrays.asList(modifiedProps));
                assertTrue(s.contains("start"));
                assertTrue(s.contains("end"));
                assertTrue(s.contains("title"));
            }
        }
        
        //let's see if this event expands out properly
        VTimeZone vTimeZone = (VTimeZone) c.getComponents().getComponent(Component.VTIMEZONE);
        TimeZone timezone = new TimeZone (vTimeZone);
        DateTime rangeStart = new DateTime("20060917T000000", timezone);
        DateTime rangeEnd   = new DateTime("20060924T000000", timezone);
        List<Event> events = converter.expandEvent(e, vevent, c, rangeStart, rangeEnd);

        Event event = events.get(0);
        assertEquals("TitleChangeStartChangeEndChange", event.getTitle());
    }
    
    public void testRecurringEventWithModificationInstanceHavingADifferentTimezone() throws Exception{
        Calendar c = testHelper.loadIcs("mod_with_different_timezone.ics");
        VEvent vevent = ICalendarUtils.getMasterEvent(c);
        Event e = converter.createEvent("1234556", vevent, c);
        RecurrenceRule rr = e.getRecurrenceRule();
        Modification modification = rr.getModifications()[0];
        CosmoDate startDate = modification.getEvent().getStart();
        CosmoDate endDate = modification.getEvent().getEnd();
        assertNotNull(startDate);
        assertNotNull(endDate);
        assertEquals(startDate.getTzId(), LA_TZ);
        assertEquals(endDate.getTzId(), LA_TZ);
        assertEquals(e.getStart().getTzId(), NY_TZ);
        assertEquals(2, modification.getModifiedProperties().length);
        
        //let's see if this event expands out properly
        VTimeZone vTimeZone = ICalendarUtils.getVTimeZone(c, NY_TZ);
        TimeZone timezone = new TimeZone (vTimeZone);
        DateTime rangeStart = new DateTime("20050102T000000", timezone);
        DateTime rangeEnd   = new DateTime("20050104T000000", timezone);
        List<Event> events = converter.expandEvent(e, vevent, c, rangeStart, rangeEnd);

        //The second instance is on the 3rd, where our modification with a different (LA)
        //timezone is
        Event event = events.get(1);
        assertEquals(LA_TZ, event.getStart().getTzId());

    }

    public void testConvertChandlerAnyTimeEventBug8217() throws Exception{
        Event e = loadEventIcs("bug8217-weirdAnytime.ics", "12345");
        assertTrue(e.isAnyTime());
        
        //let's try converting it, and then converting back
        CosmoToICalendarConverter scoobyConverter = new CosmoToICalendarConverter();
        Calendar c = scoobyConverter.createWrappedVEvent(e);
        VEvent ve = getFirstEvent(c);
        e = converter.createEvent("111",ve, c);
        assertTrue(e.isAnyTime());
    }

    public void testBug84161EventsWithDurationInsteadOfEndDate() throws Exception{
        Event e = loadEventIcs("bug8416-noEndDate.ics", "12345");
        assertNotNull(e.getStart());
        assertNotNull(e.getEnd());
    }
    
    protected Event loadEventIcs(String name, String id) throws Exception {
        Calendar c = testHelper.loadIcs(name);
        return converter.createEvent(id, getFirstEvent(c), c);
    }
}
