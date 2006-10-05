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
package org.osaf.cosmo.calendar;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.osaf.cosmo.dav.caldav.report.CaldavOutputFilter;

/**
 * Test expand output filter
 */
public class CalendarExpandRecurringEventsTest extends TestCase {
    protected String baseDir = "src/test/unit/resources/testdata/";
    
    public void testExpandEvent() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "expand_recurr_test1.ics");
        Calendar calendar = cb.build(fis);
        
        Assert.assertEquals(1, calendar.getComponents().getComponents("VEVENT").size());
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        CaldavOutputFilter filter = new CaldavOutputFilter("test");
        DateTime start = new DateTime("20060102T140000", tz);
        DateTime end = new DateTime("20060105T140000", tz);
        start.setUtc(true);
        end.setUtc(true);
        
        Period period = new Period(start, end);
        filter.setExpand(period);
        filter.setAllSubComponents();
        filter.setAllProperties();
        
        StringReader sr = new StringReader(calendar.toString(filter));
        
        Calendar filterCal = cb.build(sr);
        
        // Should expand to 3 event components
        Assert.assertEquals(3, filterCal.getComponents().getComponents("VEVENT").size());
        
        verifyExpandedCalendar(filterCal);
    }
    
    public void testExpandEventWithOverrides() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "expand_recurr_test2.ics");
        Calendar calendar = cb.build(fis);
        
        Assert.assertEquals(5, calendar.getComponents().getComponents("VEVENT").size());
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        CaldavOutputFilter filter = new CaldavOutputFilter("test");
        DateTime start = new DateTime("20060102T140000", tz);
        DateTime end = new DateTime("20060105T140000", tz);
        start.setUtc(true);
        end.setUtc(true);
        
        Period period = new Period(start, end);
        filter.setExpand(period);
        filter.setAllSubComponents();
        filter.setAllProperties();
        
        StringReader sr = new StringReader(calendar.toString(filter));
        
        Calendar filterCal = cb.build(sr);
        
        // Should expand to 3 event components
        Assert.assertEquals(3, filterCal.getComponents().getComponents("VEVENT").size());
        
        verifyExpandedCalendar(filterCal);
    }
    
    public void testExpandNonRecurringEvent() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "expand_nonrecurr_test3.ics");
        Calendar calendar = cb.build(fis);
        
        Assert.assertEquals(1, calendar.getComponents().getComponents("VEVENT").size());
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        CaldavOutputFilter filter = new CaldavOutputFilter("test");
        DateTime start = new DateTime("20060102T140000", tz);
        DateTime end = new DateTime("20060105T140000", tz);
        start.setUtc(true);
        end.setUtc(true);
        
        Period period = new Period(start, end);
        filter.setExpand(period);
        filter.setAllSubComponents();
        filter.setAllProperties();
        
        StringReader sr = new StringReader(calendar.toString(filter));
        
        Calendar filterCal = cb.build(sr);
        
        // Should be the same component
        Assert.assertEquals(1, filterCal.getComponents().getComponents("VEVENT").size());
        
        verifyExpandedCalendar(filterCal);
    }
    
    private void verifyExpandedCalendar(Calendar calendar) {
        // timezone should be stripped
        Assert.assertNull(calendar.getComponents().getComponent("VTIMEZONE"));
        
        ComponentList comps = calendar.getComponents().getComponents("VEVENT");
        
        for(Iterator<VEvent> it = comps.iterator();it.hasNext();) {
            VEvent event = it.next();
            DateTime dt = (DateTime) event.getStartDate().getDate();
            
            // verify start dates are UTC
            Assert.assertNull(event.getStartDate().getParameters().getParameter(Parameter.TZID));
            Assert.assertTrue(dt.isUtc());
            
            // verify no recurrence rules
            Assert.assertNull(event.getProperties().getProperty(Property.RRULE));
        }
    }
    
}
