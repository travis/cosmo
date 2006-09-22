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

import static org.osaf.cosmo.util.ICalendarUtils.getFirstEvent;
import static org.osaf.cosmo.util.ICalendarUtils.getTZId;
import static org.osaf.cosmo.util.ICalendarUtils.getUIDValue;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;

public class CosmoToICalendarConverterTest extends TestCase {
    private static final Log log = LogFactory.getLog(CosmoToICalendarConverterTest.class);

    private TestHelper testHelper;
    
    private CosmoToICalendarConverter converter = new CosmoToICalendarConverter();
    
    public void setUp(){
        testHelper = new TestHelper();
    }
    
    public void testCreateVEventAllDay(){
        //we'll make a 2 day all day event
        Event event = new Event();
        event.setAllDay(true);
        
        CosmoDate startDate = new CosmoDate();
        startDate.setYear(2006);
        startDate.setMonth(CosmoDate.MONTH_JANUARY);
        startDate.setDate(30);

        CosmoDate endDate = new CosmoDate();
        endDate.setYear(2006);
        endDate.setMonth(CosmoDate.MONTH_JANUARY);
        endDate.setDate(31);
       
        event.setStart(startDate);
        event.setEnd(endDate);
        
        VEvent vevent = converter.createVEvent(event);
        
        //let's confirm that the start and end dates are right
        DtStart dtstart = (DtStart)vevent.getProperties().getProperty(Property.DTSTART);
        Date  iStartDate = dtstart.getDate();
        String startDateString = "20060130";
        assertEquals(startDateString, iStartDate.toString());
        
        DtEnd dtEnd = (DtEnd)vevent.getProperties().getProperty(Property.DTEND);
        Date  iEndDate = dtEnd.getDate();
        String endDateString = "20060201";
        assertEquals(endDateString, iEndDate.toString());
        
    }
    
    public void testCreateVEventNormalFloating() throws Exception {
        Event event = new Event();

        
        //Jan 30 10:30am 
        CosmoDate startDate = new CosmoDate();
        startDate.setYear(2006);
        startDate.setMonth(CosmoDate.MONTH_JANUARY);
        startDate.setDate(30);
        startDate.setHours(10);
        startDate.setMinutes(30);
        startDate.setSeconds(0);

        //Jan 30 11:30am 
        CosmoDate endDate = new CosmoDate();
        endDate.setYear(2006);
        endDate.setMonth(CosmoDate.MONTH_JANUARY);
        endDate.setDate(30);
        endDate.setHours(11);
        endDate.setMinutes(30);
        endDate.setSeconds(0);
       
        event.setStart(startDate);
        event.setEnd(endDate);
        
        VEvent vevent = converter.createVEvent(event);
        
        //let's confirm that the start and end dates are right
        DtStart dtstart = (DtStart)vevent.getProperties().getProperty(Property.DTSTART);
        Date  iStartDate = dtstart.getDate();
        String startDateString = "20060130T103000";
        assertEquals(startDateString, iStartDate.toString());
        
        DtEnd dtEnd = (DtEnd)vevent.getProperties().getProperty(Property.DTEND);
        Date  iEndDate = dtEnd.getDate();
        String endDateString = "20060130T113000";
        assertEquals(endDateString, iEndDate.toString());
        
    }
    
    public void testUpdateNormal() throws Exception{
        Calendar calendar = testHelper.loadIcs("chandler-plain-event.ics");
        
        //create an event that would be an updated version 
        //of the event in the calendar
        Event event = new Event();
        event.setAllDay(false);
        event.setDescription("New Description");
        String uid = "907532a2-6129-11da-a81b-0014516403fe";
        event.setId(uid);
        
        //Original: DTSTART;TZID=America/New_York:20051128T074500
        CosmoDate startDate = new CosmoDate();
        startDate.setYear(2005);
        startDate.setMonth(CosmoDate.MONTH_NOVEMBER);
        startDate.setDate(28);
        startDate.setHours(7);
        startDate.setMinutes(45);
        startDate.setSeconds(0);
        
        
        //Original: DTEND;TZID=America/New_York:20051128T084500
        //  Update: DTEND;TZID=America/New_York:20051128T094500
        CosmoDate endDate = new CosmoDate();
        endDate.setYear(2005);
        endDate.setMonth(CosmoDate.MONTH_NOVEMBER);
        endDate.setDate(28);
        endDate.setHours(9);
        endDate.setMinutes(45);
        endDate.setSeconds(0);

        event.setStart(startDate);
        event.setEnd(endDate);

        // sanity check -- assert that there is only one component
        assertTrue(calendar.getComponents().getComponents(Component.VEVENT)
                .size() == 1);

        converter.updateEvent(event, calendar);

        // first make sure that no "extra" events were created
        assertTrue(calendar.getComponents().getComponents(Component.VEVENT)
                .size() == 1);
        
        VEvent vevent = getFirstEvent(calendar);
        
        //assert it's the right event
        assertEquals(uid, getUIDValue(vevent));
        
        //check the start time
        DateTime dateTimeStart = (DateTime) vevent.getStartDate().getDate();
        assertEquals("20051128T074500",dateTimeStart.toString());
        assertEquals(getTZId(vevent.getStartDate()), "America/New_York");
        
        //check the end time
        DateTime dateTimeEnd = (DateTime) vevent.getEndDate().getDate();
        assertEquals("20051128T094500",dateTimeEnd.toString());
        assertEquals(getTZId(vevent.getEndDate()), "America/New_York");
    }
    
    public void testCreateRecurringDailyNoEndDate(){
        Event event = createBaseEvent();
        RecurrenceRule rr = new RecurrenceRule();
        rr.setFrequency(RecurrenceRule.FREQUENCY_DAILY);
        rr.setEndDate(null);
        event.setRecurrenceRule(rr);
        
        VEvent vevent = converter.createVEvent(event);
        RRule rrule = (RRule) vevent.getProperties().getProperty(Property.RRULE);
        assertNotNull(rrule);
        
        Recur recur = rrule.getRecur();
        assertEquals(Recur.DAILY, recur.getFrequency());
        assertNull(recur.getUntil());
    }
    
    public void testCreateRecurringWeeklyNoEndDate(){
        Event event = createBaseEvent();
        RecurrenceRule rr = new RecurrenceRule();
        rr.setFrequency(RecurrenceRule.FREQUENCY_WEEKLY);
        rr.setEndDate(null);
        event.setRecurrenceRule(rr);
        
        VEvent vevent = converter.createVEvent(event);
        RRule rrule = (RRule) vevent.getProperties().getProperty(Property.RRULE);
        assertNotNull(rrule);
        
        Recur recur = rrule.getRecur();
        assertEquals(Recur.WEEKLY, recur.getFrequency());
        assertNull(recur.getUntil());
    }

    public void testCreateRecurringBiWeeklyNoEndDate(){
        Event event = createBaseEvent();
        RecurrenceRule rr = new RecurrenceRule();
        rr.setFrequency(RecurrenceRule.FREQUENCY_BIWEEKLY);
        rr.setEndDate(null);
        event.setRecurrenceRule(rr);
        
        VEvent vevent = converter.createVEvent(event);
        RRule rrule = (RRule) vevent.getProperties().getProperty(Property.RRULE);
        assertNotNull(rrule);
        
        Recur recur = rrule.getRecur();
        assertEquals(Recur.WEEKLY, recur.getFrequency());
        assertEquals(2, recur.getInterval());
        assertNull(recur.getUntil());
    }

    public void testCreateRecurringMonthlyNoEndDate(){
        Event event = createBaseEvent();
        RecurrenceRule rr = new RecurrenceRule();
        rr.setFrequency(RecurrenceRule.FREQUENCY_MONTHLY);
        rr.setEndDate(null);
        event.setRecurrenceRule(rr);
        
        VEvent vevent = converter.createVEvent(event);
        RRule rrule = (RRule) vevent.getProperties().getProperty(Property.RRULE);
        assertNotNull(rrule);
        
        Recur recur = rrule.getRecur();
        assertEquals(Recur.MONTHLY, recur.getFrequency());
        assertNull(recur.getUntil());
    }

    public void testCreateRecurringYearlyNoEndDate(){
        Event event = createBaseEvent();
        RecurrenceRule rr = new RecurrenceRule();
        rr.setFrequency(RecurrenceRule.FREQUENCY_YEARLY);
        rr.setEndDate(null);
        event.setRecurrenceRule(rr);
        
        VEvent vevent = converter.createVEvent(event);
        RRule rrule = (RRule) vevent.getProperties().getProperty(Property.RRULE);
        assertNotNull(rrule);
        
        Recur recur = rrule.getRecur();
        assertEquals(Recur.YEARLY, recur.getFrequency());
        assertNull(recur.getUntil());
    }

    public void testCreateRecurringYearlyEndDate(){
        Event event = createBaseEvent();
        RecurrenceRule rr = new RecurrenceRule();
        rr.setFrequency(RecurrenceRule.FREQUENCY_YEARLY);
        
        CosmoDate endDate = event.getStart().clone();
        endDate.setYear(2010);
        rr.setEndDate(endDate);
        
        event.setRecurrenceRule(rr);
        
        
        VEvent vevent = converter.createVEvent(event);
        RRule rrule = (RRule) vevent.getProperties().getProperty(Property.RRULE);
        assertNotNull(rrule);
        
        Recur recur = rrule.getRecur();
        assertEquals(Recur.YEARLY, recur.getFrequency());
        
        Date untilDate = recur.getUntil();
        assertNotNull(untilDate);
        java.util.Calendar calendar = getGMTCalendar(untilDate);
        assertEquals(2010, calendar.get(java.util.Calendar.YEAR));
    }
    
    /**
     * Creates a very "normal" event, which you can use as a template for new test events.
     *  
     * This event:
     * <ul>
     *     <li>...is floating</li>
     *     <li>...has no recurrence rule</li>
     *     <li>id = "TESTID"</li>
     *     <li>title = "Test Title"</li>
     *     <li>description = "Test Description"</li>
     *     <li>Start Date = 1/1/05 12pm </li>
     *     <li>End Date = 1/1/05 1:30pm </li>
     * </ul>  
     * @return plain vanilla event
     */
    protected Event createBaseEvent(){
        Event event = new Event();

        event.setId("TESTID");
        event.setMasterEvent(true);

        event.setAllDay(false);
        event.setAnyTime(false);
        event.setPointInTime(false);
        event.setRecurrenceRule(null);
        
        //20051128T074500
        CosmoDate startDate = new CosmoDate();
        startDate.setYear(2005);
        startDate.setMonth(CosmoDate.MONTH_JANUARY);
        startDate.setDate(1);
        startDate.setHours(12);
        startDate.setMinutes(0);
        startDate.setSeconds(0);
        startDate.setUtc(false);
        
        //20051128T084500
        CosmoDate endDate = new CosmoDate();
        endDate.setYear(2005);
        endDate.setMonth(CosmoDate.MONTH_JANUARY);
        endDate.setDate(1);
        endDate.setHours(13);
        endDate.setMinutes(30);
        endDate.setSeconds(0);
        endDate.setUtc(false);
       
        event.setStart(startDate);
        event.setEnd(endDate);
        
        event.setDescription("Test Description");
        event.setTitle("Test Title");
        return event;
    }
    
    protected java.util.Calendar getGMTCalendar(Date date){
        java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone
                .getTimeZone("GMT"));
        calendar.setTime(date);
        return calendar;
    }
}
