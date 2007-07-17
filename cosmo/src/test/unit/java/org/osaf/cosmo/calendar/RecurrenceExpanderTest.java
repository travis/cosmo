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
package org.osaf.cosmo.calendar;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test RecurrenceExpander.
 *
 */
public class RecurrenceExpanderTest extends TestCase {
    private static final Log log = LogFactory.getLog(RecurrenceExpanderTest.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    public void testRecurrenceExpanderAllDay() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        es.setStartDate(new Date("20070101"));
        es.setEndDate(new Date("20070102"));
        
        String recur1 = "FREQ=DAILY;COUNT=10;INTERVAL=2";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Date[] range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20070101", range[0].toString());
        Assert.assertEquals("20070120", range[1].toString());
        
        recur1 = "FREQ=DAILY;";
        
        recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20070101", range[0].toString());
        Assert.assertNull(range[1]);
    }
    
    public void testRecurrenceExpanderFloating() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        es.setStartDate(new DateTime("20070101T100000"));
        es.setEndDate(new DateTime("20070101T120000"));
        
        String recur1 = "FREQ=DAILY;COUNT=10;INTERVAL=2";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Date[] range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertEquals("20070119T120000", range[1].toString());
        
        recur1 = "FREQ=DAILY;";
        
        recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertNull(range[1]);
    }
    
    public void testRecurrenceExpanderTimezone() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        TimeZone ctz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        DateTime start = new DateTime("20070101T100000", ctz);
        DateTime end = new DateTime("20070101T120000", ctz);
      
        es.setStartDate(start);
        es.setEndDate(end);
        
        String recur1 = "FREQ=DAILY;COUNT=10;INTERVAL=2";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Date[] range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertEquals("20070119T120000", range[1].toString());
        
        Assert.assertEquals(((DateTime) range[0]).getTimeZone().getID(), "America/Chicago");
        Assert.assertEquals(((DateTime) range[1]).getTimeZone().getID(), "America/Chicago");
        
        recur1 = "FREQ=DAILY;";
        
        recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertNull(range[1]);
        
        Assert.assertEquals(((DateTime) range[0]).getTimeZone().getID(), "America/Chicago");
    }
    
    public void testRecurrenceExpanderLongEvent() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        TimeZone ctz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        es.setStartDate(new DateTime("20070101T100000", ctz));
        es.setEndDate(new DateTime("20070101T120000", ctz));
        
        String recur1 = "FREQ=DAILY;UNTIL=20100101T120000Z";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Date[] range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20070101T100000", range[0].toString());
        Assert.assertEquals("20091231T120000", range[1].toString());
    }
    
    public void testRecurrenceExpanderRDates() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        es.setStartDate(new DateTime("20070101T100000"));
        es.setEndDate(new DateTime("20070101T120000"));
        
        String recur1 = "FREQ=DAILY;UNTIL=20080101T120000Z";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        String rdates = ";VALUE=DATE-TIME:20061212T100000,20101212T100000";

        DateList dl = new ICalDate(rdates).getDateList();
        es.setRecurrenceDates(dl);
        
        Date[] range = expander.calculateRecurrenceRange(es.getCalendar());
        
        Assert.assertEquals("20061212T100000", range[0].toString());
        Assert.assertEquals("20101212T120000", range[1].toString());
    }
    
    public void testRecurrenceExpanderSingleOccurrence() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        es.setStartDate(new DateTime("20070101T100000"));
        es.setEndDate(new DateTime("20070101T120000"));
        
        String recur1 = "FREQ=DAILY;UNTIL=20100101T120000Z";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        InstanceList instances = expander.getOcurrences(es.getCalendar(), new DateTime("20080101T100000"), new DateTime("20080101T100001"), null);
        
        Assert.assertEquals(1, instances.size());
    }
    
    public void testRecurrenceExpanderFirstOccurrence() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        es.setStartDate(new DateTime("20070101T100000"));
        es.setEndDate(new DateTime("20070101T120000"));
        
        String recur1 = "FREQ=DAILY;UNTIL=20100101T120000Z";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Instance instance = expander.getFirstInstance(es.getCalendar(), new DateTime("20080101T100000"), new DateTime("20080105T100000"), null);
        
        Assert.assertEquals("20080101T100000", instance.getRid().toString());
    }
    
    public void testRecurrenceExpanderLatestOccurrence() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        es.setStartDate(new DateTime("20070101T100000"));
        es.setEndDate(new DateTime("20070101T120000"));
        
        String recur1 = "FREQ=DAILY;UNTIL=20100101T120000Z";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Instance instance = expander.getLatestInstance(es.getCalendar(), new DateTime("20080101T100000"), new DateTime("20080105T110000"), null);
        
        Assert.assertEquals("20080105T100000", instance.getRid().toString());
    }
    
    public void testIsOccurrence() throws Exception {
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp es = new EventStamp(new NoteItem());
        es.createCalendar();
        
        // test floating DATETIME
        es.setStartDate(new DateTime("20070101T100000"));
        es.setEndDate(new DateTime("20070101T120000"));
        
        String recur1 = "FREQ=DAILY;UNTIL=20100101T120000Z";
       
        List recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Assert.assertTrue(expander.isOccurrence(es.getCalendar(), new DateTime("20070102T100000")));
        Assert.assertFalse(expander.isOccurrence(es.getCalendar(), new DateTime("20070102T110000")));
        Assert.assertFalse(expander.isOccurrence(es.getCalendar(), new DateTime("20070102T100001")));
    
        // test DATE
        es.setStartDate(new Date("20070101"));
        es.setEndDate(new Date("20070102"));
        recur1 = "FREQ=WEEKLY;UNTIL=20100101";
        
        recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Assert.assertTrue(expander.isOccurrence(es.getCalendar(), new Date("20070101")));
        Assert.assertFalse(expander.isOccurrence(es.getCalendar(), new Date("20070102")));
        Assert.assertTrue(expander.isOccurrence(es.getCalendar(), new Date("20070108")));
        
        // test DATETIME with timezone
        TimeZone ctz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        es.setStartDate(new DateTime("20070101T100000", ctz));
        es.setEndDate(new DateTime("20070101T120000", ctz));
        
        recur1 = "FREQ=DAILY;UNTIL=20100101T120000Z";
       
        recurs = EimValueConverter.toICalRecurs(recur1);
        es.setRecurrenceRules(recurs);
        
        Assert.assertTrue(expander.isOccurrence(es.getCalendar(), new DateTime("20070102T100000", ctz)));
        Assert.assertFalse(expander.isOccurrence(es.getCalendar(), new DateTime("20070102T110000", ctz)));
        Assert.assertFalse(expander.isOccurrence(es.getCalendar(), new DateTime("20070102T100001", ctz)));
    }
    
}
