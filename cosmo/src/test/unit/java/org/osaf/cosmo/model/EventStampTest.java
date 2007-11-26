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

import java.io.FileInputStream;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

import org.osaf.cosmo.calendar.ICalDate;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.mock.MockEventExceptionStamp;
import org.osaf.cosmo.model.mock.MockEventStamp;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test EventStamp
 */
public class EventStampTest extends TestCase {
   
    protected String baseDir = "src/test/unit/resources/testdata/";
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    public void testEventStampGetCalendar() throws Exception {
        TimeZoneRegistry registry =
            TimeZoneRegistryFactory.getInstance().createRegistry();
        NoteItem master = new MockNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        master.setIcalUid("icaluid");
        master.setClientModifiedDate(new DateTime("20070101T100000Z"));
        EventStamp eventStamp = new MockEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        
        
        Calendar cal = eventStamp.getCalendar();
        
        // date has no timezone, so there should be no timezones
        Assert.assertEquals(0, cal.getComponents(Component.VTIMEZONE).size());
      
        eventStamp.setStartDate(new DateTime("20070212T074500",TIMEZONE_REGISTRY.getTimeZone("America/Chicago")));
        
        cal = eventStamp.getCalendar();
        
        // should be a single VEVENT
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(1, comps.size());
        VEvent event = (VEvent) comps.get(0);
        
        // test VALUE=DATE-TIME is not present
        Assert.assertNull(event.getStartDate().getParameter(Parameter.VALUE));
        
        // test item properties got merged into calendar
        Assert.assertEquals("displayName", event.getSummary().getValue());
        Assert.assertEquals("body", event.getDescription().getValue());
        Assert.assertEquals("icaluid", event.getUid().getValue());
        Assert.assertEquals(master.getClientModifiedDate().getTime(), event.getDateStamp().getDate().getTime());
         
        // date has timezone, so there should be a timezone
        Assert.assertEquals(1, cal.getComponents(Component.VTIMEZONE).size());
        
        eventStamp.setEndDate(new DateTime("20070212T074500",TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles")));
        
        cal = eventStamp.getCalendar();
        
        // dates have 2 different timezones, so there should be 2 timezones
        Assert.assertEquals(2, cal.getComponents(Component.VTIMEZONE).size());
        
        // add timezones to master event calendar
        eventStamp.getEventCalendar().getComponents().add(registry.getTimeZone("America/Chicago").getVTimeZone());
        eventStamp.getEventCalendar().getComponents().add(registry.getTimeZone("America/Los_Angeles").getVTimeZone());
        
        cal = eventStamp.getCalendar();
        Assert.assertEquals(2, cal.getComponents(Component.VTIMEZONE).size());
    }
    
    public void testEventModificationGetCalendar() throws Exception {
        NoteItem master = new MockNoteItem();
        master.setIcalUid("icaluid");
        EventStamp eventStamp = new MockEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        eventStamp.setDuration(new Dur("PT1H"));
        DateList dates = new ICalDate(";VALUE=DATE-TIME:20070212T074500,20070213T074500").getDateList();
        eventStamp.setRecurrenceDates(dates);
        
        eventStamp.getEventCalendar().validate(true);
       
        NoteItem mod = new MockNoteItem();
        mod.setDisplayName("modDisplayName");
        mod.setBody("modBody");
        mod.setModifies(master);
        master.addModification(mod);
        EventExceptionStamp exceptionStamp = new MockEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.setStartDate(eventStamp.getStartDate());
        exceptionStamp.setRecurrenceId(eventStamp.getStartDate());

        
        // test modification VEVENT gets added properly
        Calendar cal = eventStamp.getCalendar();
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        VEvent masterEvent = (VEvent) comps.get(0);
        VEvent modEvent = (VEvent) comps.get(1);
        
        // test merged properties
        Assert.assertEquals("modDisplayName", modEvent.getSummary().getValue());
        Assert.assertEquals("modBody", modEvent.getDescription().getValue());
        Assert.assertEquals("icaluid", modEvent.getUid().getValue());
        
        // test duration got added to modfication
        Assert.assertNotNull(modEvent.getDuration());
        Assert.assertEquals("PT1H", modEvent.getDuration().getDuration().toString());
    }
    
    public void testInheritedAlarm() throws Exception {
        NoteItem master = new MockNoteItem();
        master.setIcalUid("icaluid");
        EventStamp eventStamp = new MockEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.creatDisplayAlarm();
        Date date = new ICalDate(";VALUE=DATE-TIME:20070212T074500").getDate();
        eventStamp.setStartDate(date);
        eventStamp.setDisplayAlarmTrigger(EimValueConverter.toIcalTrigger("-PT15M"));
        DateList dates = new ICalDate(";VALUE=DATE-TIME:20070212T074500,20070213T074500").getDateList();
        eventStamp.setRecurrenceDates(dates);
        
        eventStamp.getEventCalendar().validate(true);
        eventStamp.setDisplayAlarmDescription(null);
        eventStamp.getEventCalendar().validate(true);
        
        eventStamp.setDisplayAlarmDescription("alarm");
        
        NoteItem mod = new MockNoteItem();
        mod.setDisplayName("modDisplayName");
        mod.setBody("modBody");
        mod.setModifies(master);
        master.addModification(mod);
        EventExceptionStamp exceptionStamp = new MockEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.creatDisplayAlarm();
        exceptionStamp.setStartDate(date);
        exceptionStamp.setRecurrenceId(date);
        
        // test inherited alarm
        Calendar cal = eventStamp.getCalendar();
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        VEvent masterEvent = (VEvent) comps.get(0);
        VEvent modEvent = (VEvent) comps.get(1);
      
        // test inherited alarm
        VAlarm masterAlarm = (VAlarm) masterEvent.getAlarms().get(0);
        VAlarm modAlarm = (VAlarm) modEvent.getAlarms().get(0);
        
        Assert.assertEquals(masterAlarm, modAlarm);
        
        // next test not inherited
        exceptionStamp.setDisplayAlarmDescription("alarm2");
        exceptionStamp.setDisplayAlarmTrigger(EimValueConverter.toIcalTrigger("-PT30M"));
        cal = eventStamp.getCalendar();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        masterEvent = (VEvent) comps.get(0);
        modEvent = (VEvent) comps.get(1);
        masterAlarm = (VAlarm) masterEvent.getAlarms().get(0);
        modAlarm = (VAlarm) modEvent.getAlarms().get(0);
        
        Assert.assertFalse(masterAlarm.equals(modAlarm));
        
        // finally test no alarm
        exceptionStamp.removeDisplayAlarm();
        
        cal = eventStamp.getCalendar();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        masterEvent = (VEvent) comps.get(0);
        modEvent = (VEvent) comps.get(1);
        
        Assert.assertEquals(1, masterEvent.getAlarms().size());
        Assert.assertEquals(0, modEvent.getAlarms().size());
    }
    
    public void testInheritedAnyTime() throws Exception {
        NoteItem master = new MockNoteItem();
        EventStamp eventStamp = new MockEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        eventStamp.setAnyTime(true);
        DateList dates = new ICalDate(";VALUE=DATE-TIME:20070212T074500,20070213T074500").getDateList();
        eventStamp.setRecurrenceDates(dates);
        
        NoteItem mod = new MockNoteItem();
        mod.setModifies(master);
        master.addModification(mod);
        EventExceptionStamp exceptionStamp = new MockEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.setRecurrenceId(new DateTime("20070212T074500"));
        exceptionStamp.setStartDate(new DateTime("20070212T074500"));
        exceptionStamp.setAnyTime(null);
        
        Calendar cal = eventStamp.getCalendar();
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        VEvent masterEvent = (VEvent) comps.get(0);
        VEvent modEvent = (VEvent) comps.get(1);
        
        Parameter masterAnyTime = masterEvent.getStartDate().getParameter("X-OSAF-ANYTIME");
        Parameter modAnyTime = modEvent.getStartDate().getParameter("X-OSAF-ANYTIME");
        
        Assert.assertNotNull(masterAnyTime);
        Assert.assertEquals("TRUE", masterAnyTime.getValue());
        Assert.assertNotNull(modAnyTime);
        Assert.assertEquals("TRUE", modAnyTime.getValue());
        
        // change master and verify attribute is inherited in modification
        eventStamp.setAnyTime(false);
        
        cal = eventStamp.getCalendar();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        masterEvent = (VEvent) comps.get(0);
        modEvent = (VEvent) comps.get(1);
        
        Assert.assertNull(masterEvent.getStartDate().getParameter("X-OSAF-ANYTIME"));
        Assert.assertNull(modEvent.getStartDate().getParameter("X-OSAF-ANYTIME"));
        
        // change both and verify
        exceptionStamp.setAnyTime(true);
        
        cal = eventStamp.getCalendar();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        masterEvent = (VEvent) comps.get(0);
        modEvent = (VEvent) comps.get(1);
        
        modAnyTime = modEvent.getStartDate().getParameter("X-OSAF-ANYTIME");
        
        Assert.assertNull(masterEvent.getStartDate().getParameter("X-OSAF-ANYTIME"));
        Assert.assertNotNull(modAnyTime);
        Assert.assertEquals("TRUE", modAnyTime.getValue());
    }
    
    public void testExDates() throws Exception {
        NoteItem master = new MockNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        EventStamp eventStamp = new MockEventStamp(master);
        
        eventStamp.setEventCalendar(getCalendar("recurring_with_exdates.ics"));
        
        DateList exdates = eventStamp.getExceptionDates();
        
        Assert.assertNotNull(exdates);
        Assert.assertTrue(2==exdates.size());
        Assert.assertNotNull(exdates.getTimeZone());
    }
    
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
}
