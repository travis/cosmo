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

import java.util.List;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;


public class ICalendarToCosmoConverterTest extends TestCase {
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
        assertEquals("Month not zero", endDate.getMonth(), 0);
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
        assertEquals("Month not zero", endDate.getMonth(), 0);
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
    
    protected Event loadEventIcs(String name, String id) throws Exception {
        Calendar c = testHelper.loadIcs(name);
        return converter.createEvent(id, getFirstEvent(c), c);
    }
}
