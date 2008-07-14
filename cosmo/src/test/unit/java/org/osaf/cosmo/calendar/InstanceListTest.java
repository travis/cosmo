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

import java.io.InputStream;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Test InstanceList, the meat and potatoes of recurrence
 * expansion.
 */
public class InstanceListTest extends TestCase {
    
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
                TimeZoneRegistryFactory.getInstance().createRegistry();
    
    public void testFloatingRecurring() throws Exception {
        Calendar calendar = getCalendar("floating_recurr_event.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20060101T140000");
        DateTime end = new DateTime("20060108T140000");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(5, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060102T140000", key);
        Assert.assertEquals("20060102T140000", instance.getStart().toString());
        Assert.assertEquals("20060102T150000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060103T140000", key);
        Assert.assertEquals("20060103T140000", instance.getStart().toString());
        Assert.assertEquals("20060103T150000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060104T140000", key);
        Assert.assertEquals("20060104T160000", instance.getStart().toString());
        Assert.assertEquals("20060104T170000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060105T140000", key);
        Assert.assertEquals("20060105T160000", instance.getStart().toString());
        Assert.assertEquals("20060105T170000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060106T140000", key);
        Assert.assertEquals("20060106T140000", instance.getStart().toString());
        Assert.assertEquals("20060106T150000", instance.getEnd().toString());
    }
    
    public void testUTCInstanceList() throws Exception {
        Calendar calendar = getCalendar("floating_recurr_event.ics");
        
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/New_York");
        
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(tz);
        
        DateTime start = new DateTime("20060101T190000Z");
        DateTime end = new DateTime("20060108T190000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(5, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060102T190000Z", key);
        Assert.assertEquals("20060102T190000Z", instance.getStart().toString());
        Assert.assertEquals("20060102T200000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060103T190000Z", key);
        Assert.assertEquals("20060103T190000Z", instance.getStart().toString());
        Assert.assertEquals("20060103T200000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060104T190000Z", key);
        Assert.assertEquals("20060104T210000Z", instance.getStart().toString());
        Assert.assertEquals("20060104T220000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060105T190000Z", key);
        Assert.assertEquals("20060105T210000Z", instance.getStart().toString());
        Assert.assertEquals("20060105T220000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060106T190000Z", key);
        Assert.assertEquals("20060106T190000Z", instance.getStart().toString());
        Assert.assertEquals("20060106T200000Z", instance.getEnd().toString());
    }
    
    public void testUTCInstanceListAllDayEvent() throws Exception {
        
        Calendar calendar = getCalendar("allday_weekly_recurring.ics");
        
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));
        
        DateTime start = new DateTime("20070103T090000Z");
        DateTime end = new DateTime("20070117T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(2, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070108T060000Z", key);
        Assert.assertEquals("20070108T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070109T060000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070115T060000Z", key);
        Assert.assertEquals("20070115T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070116T060000Z", instance.getEnd().toString());
    }
    
    public void testUTCInstanceListAllDayWithExDates() throws Exception {
        
        Calendar calendar = getCalendar("allday_recurring_with_exdates.ics");
       
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));
        
        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070106T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(3, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070101T060000Z", key);
        Assert.assertEquals("20070101T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070102T060000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070102T060000Z", key);
        Assert.assertEquals("20070102T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070103T060000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070106T060000Z", key);
        Assert.assertEquals("20070106T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070107T060000Z", instance.getEnd().toString());
    }
    
    public void testUTCInstanceListAllDayEventWithMods() throws Exception {
        
        Calendar calendar = getCalendar("allday_weekly_recurring_with_mods.ics");
        
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));
        
        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070109T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(2, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070101T060000Z", key);
        Assert.assertEquals("20070101T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070102T060000Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070108T060000Z", key);
        Assert.assertEquals("20070109T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070110T060000Z", instance.getEnd().toString());
    }
    
    public void testInstanceListInstanceBeforeStartRange() throws Exception {
        
        Calendar calendar = getCalendar("eventwithtimezone3.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070511T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(3, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070509T081500Z", key);
        Assert.assertEquals("20070509T031500", instance.getStart().toString());
        Assert.assertEquals("20070509T041500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070510T081500Z", key);
        Assert.assertEquals("20070510T031500", instance.getStart().toString());
        Assert.assertEquals("20070510T041500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070511T081500Z", key);
        Assert.assertEquals("20070511T031500", instance.getStart().toString());
        Assert.assertEquals("20070511T041500", instance.getEnd().toString());
    }
    
    public void testFloatingWithSwitchingTimezoneInstanceList() throws Exception {
       
        Calendar calendar = getCalendar("floating_recurr_event.ics");
        
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        InstanceList instances = new InstanceList();
        instances.setTimezone(tz);
        
        DateTime start = new DateTime("20060102T220000Z");
        DateTime end = new DateTime("20060108T190000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(5, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060102T220000Z", key);
        Assert.assertEquals("20060102T140000", instance.getStart().toString());
        Assert.assertEquals("20060102T150000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060103T220000Z", key);
        Assert.assertEquals("20060103T140000", instance.getStart().toString());
        Assert.assertEquals("20060103T150000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060104T220000Z", key);
        Assert.assertEquals("20060104T160000", instance.getStart().toString());
        Assert.assertEquals("20060104T170000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060105T220000Z", key);
        Assert.assertEquals("20060105T160000", instance.getStart().toString());
        Assert.assertEquals("20060105T170000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20060106T220000Z", key);
        Assert.assertEquals("20060106T140000", instance.getStart().toString());
        Assert.assertEquals("20060106T150000", instance.getEnd().toString());
    }
    
    public void testExdateWithTimezone() throws Exception {
        
        Calendar calendar = getCalendar("recurring_with_exdates.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(2, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070605T101500Z", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }
    
    public void testExdateUtc() throws Exception {
       
        Calendar calendar = getCalendar("recurring_with_exdates_utc.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(2, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070605T101500Z", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }
    
    public void testExdateNoTimezone() throws Exception {
        
        Calendar calendar = getCalendar("recurring_with_exdates_floating.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070509T040000");
        DateTime end = new DateTime("20070609T040000");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(2, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070529T051500", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070605T051500", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }
    
    public void testRdateWithTimezone() throws Exception {
        
        Calendar calendar = getCalendar("recurring_with_rdates.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(7, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070515T101500Z", key);
        Assert.assertEquals("20070515T051500", instance.getStart().toString());
        Assert.assertEquals("20070515T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070516T101500Z", key);
        Assert.assertEquals("20070516T051500", instance.getStart().toString());
        Assert.assertEquals("20070516T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070517T101500Z", key);
        Assert.assertEquals("20070517T101500Z", instance.getStart().toString());
        Assert.assertEquals("20070517T131500Z", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070522T101500Z", key);
        Assert.assertEquals("20070522T051500", instance.getStart().toString());
        Assert.assertEquals("20070522T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070523T101500Z", key);
        Assert.assertEquals("20070523T051500", instance.getStart().toString());
        Assert.assertEquals("20070523T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070605T101500Z", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }
    
    public void testExruleWithTimezone() throws Exception {
        
        Calendar calendar = getCalendar("recurring_with_exrule.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(2, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070515T101500Z", key);
        Assert.assertEquals("20070515T051500", instance.getStart().toString());
        Assert.assertEquals("20070515T061500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
    }
    
    public void testAllDayRecurring() throws Exception {
        
        Calendar calendar = getCalendar("allday_recurring.ics");
        
        InstanceList instances = new InstanceList();
        
        // need to normalize to local timezone to get test to pass
        // in mutliple timezones
        DateTime start = new DateTime(new Date("20070101").getTime() + 1000*60);
        DateTime end = new DateTime(new Date("20070103").getTime() + 1000*60);
        start.setUtc(true);
        end.setUtc(true);
        
        //  This fails when run in Australia/Sydney default timezone
        //DateTime start = new DateTime("20070101T090000Z");
        //DateTime end = new DateTime("20070103T090000Z");
       
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(3, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070101", key);
        Assert.assertEquals("20070101", instance.getStart().toString());
        Assert.assertEquals("20070102", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070102", key);
        Assert.assertEquals("20070102", instance.getStart().toString());
        Assert.assertEquals("20070103", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070103", key);
        Assert.assertEquals("20070103", instance.getStart().toString());
        Assert.assertEquals("20070104", instance.getEnd().toString());
    }
    
    public void testAllDayRecurringWithExDates() throws Exception {
        
        Calendar calendar = getCalendar("allday_recurring_with_exdates.ics");
        
        InstanceList instances = new InstanceList();
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        instances.setTimezone(tz);
        
        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070106T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(3, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070101", key);
        Assert.assertEquals("20070101", instance.getStart().toString());
        Assert.assertEquals("20070102", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070102", key);
        Assert.assertEquals("20070102", instance.getStart().toString());
        Assert.assertEquals("20070103", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070106", key);
        Assert.assertEquals("20070106", instance.getStart().toString());
        Assert.assertEquals("20070107", instance.getEnd().toString());
    }
    
    public void testAllDayRecurringWithMods() throws Exception {
        
        Calendar calendar = getCalendar("allday_weekly_recurring_with_mods.ics");
        
        InstanceList instances = new InstanceList();
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        instances.setTimezone(tz);
        
        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070109T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(2, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070101", key);
        Assert.assertEquals("20070101", instance.getStart().toString());
        Assert.assertEquals("20070102", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070108", key);
        Assert.assertEquals("20070109", instance.getStart().toString());
        Assert.assertEquals("20070110", instance.getEnd().toString());
    }
    
    public void testAllDayRecurringWithTimeZone() throws Exception {
        
        Calendar calendar = getCalendar("allday_recurring.ics");
        
        InstanceList instances = new InstanceList();
        
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("Australia/Sydney");
        instances.setTimezone(tz);
        
        // This range in UTC translates to
        // 20070103T010000 to 20070105T010000 in Australia/Sydney local time
        DateTime start = new DateTime("20070102T140000Z");
        DateTime end = new DateTime("20070104T140000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(3, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070103", key);
        Assert.assertEquals("20070103", instance.getStart().toString());
        Assert.assertEquals("20070104", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070104", key);
        Assert.assertEquals("20070104", instance.getStart().toString());
        Assert.assertEquals("20070105", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070105", key);
        Assert.assertEquals("20070105", instance.getStart().toString());
        Assert.assertEquals("20070106", instance.getEnd().toString());
    }
    
    public void testInstanceStartBeforeRange() throws Exception {
        
        Calendar calendar = getCalendar("recurring_with_exdates.ics");
        
        InstanceList instances = new InstanceList();
        
        // make sure startRange is after the startDate of an occurrence,
        // in this case the occurrence is at 20070529T101500Z
        DateTime start = new DateTime("20070529T110000Z");
        DateTime end = new DateTime("20070530T051500Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(1, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
    }
    
    public void testComplicatedRecurringWithTimezone() throws Exception {
        
        Calendar calendar = getCalendar("complicated_recurring.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070201T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(4, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070102T161500Z", key);
        Assert.assertEquals("20070102T101500", instance.getStart().toString());
        Assert.assertEquals("20070102T111500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070104T161500Z", key);
        Assert.assertEquals("20070104T101500", instance.getStart().toString());
        Assert.assertEquals("20070104T111500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070116T161500Z", key);
        Assert.assertEquals("20070116T101500", instance.getStart().toString());
        Assert.assertEquals("20070116T111500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070118T161500Z", key);
        Assert.assertEquals("20070118T101500", instance.getStart().toString());
        Assert.assertEquals("20070118T111500", instance.getEnd().toString());
    }
    
    public void testComplicatedRecurringAllDay() throws Exception {
        
        Calendar calendar = getCalendar("complicated_allday_recurring.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20071201T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(5, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070105", key);
        Assert.assertEquals("20070105", instance.getStart().toString());
        Assert.assertEquals("20070106", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070202", key);
        Assert.assertEquals("20070202", instance.getStart().toString());
        Assert.assertEquals("20070203", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070302", key);
        Assert.assertEquals("20070302", instance.getStart().toString());
        Assert.assertEquals("20070303", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070406", key);
        Assert.assertEquals("20070406", instance.getStart().toString());
        Assert.assertEquals("20070407", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070504", key);
        Assert.assertEquals("20070504", instance.getStart().toString());
        Assert.assertEquals("20070505", instance.getEnd().toString());
    }
    
    public void testRecurringWithUntil() throws Exception {
        
        Calendar calendar = getCalendar("recurring_until.ics");
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070201T090000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(3, instances.size() );
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070102T161500Z", key);
        Assert.assertEquals("20070102T101500", instance.getStart().toString());
        Assert.assertEquals("20070102T111500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070103T161500Z", key);
        Assert.assertEquals("20070103T101500", instance.getStart().toString());
        Assert.assertEquals("20070103T111500", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20070104T161500Z", key);
        Assert.assertEquals("20070104T101500", instance.getStart().toString());
        Assert.assertEquals("20070104T111500", instance.getEnd().toString());
    }
    
    public void testRecurrenceExpanderByDay() throws Exception {
       
        Calendar calendar = getCalendar("recurring_by_day.ics");

        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20080720T170000Z");
        DateTime end = new DateTime("20080726T200000Z");
        
        addToInstanceList(calendar, instances, start, end);
        
        Assert.assertEquals(5, instances.size());
        
        Iterator<String> keys = instances.keySet().iterator();
        
        String key = null;
        Instance instance = null;
            
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20080721T180000Z", key);
        Assert.assertEquals("20080721T110000", instance.getStart().toString());
        Assert.assertEquals("20080721T113000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20080722T180000Z", key);
        Assert.assertEquals("20080722T110000", instance.getStart().toString());
        Assert.assertEquals("20080722T113000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20080723T180000Z", key);
        Assert.assertEquals("20080723T110000", instance.getStart().toString());
        Assert.assertEquals("20080723T113000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20080724T180000Z", key);
        Assert.assertEquals("20080724T110000", instance.getStart().toString());
        Assert.assertEquals("20080724T113000", instance.getEnd().toString());
        
        key = keys.next();
        instance = (Instance) instances.get(key);
        
        Assert.assertEquals("20080725T180000Z", key);
        Assert.assertEquals("20080725T110000", instance.getStart().toString());
        Assert.assertEquals("20080725T113000", instance.getEnd().toString());
    }
    
    private static void addToInstanceList(Calendar calendar,
            InstanceList instances, Date start, Date end) {
        ComponentList comps = calendar.getComponents();
        Iterator<VEvent> it = comps.getComponents("VEVENT").iterator();
        boolean addedMaster = false;
        while (it.hasNext()) {
            VEvent event = it.next();
            if (event.getRecurrenceId() == null) {
                addedMaster = true;
                instances.addComponent(event, start, end);
            } else {
                Assert.assertTrue(addedMaster);
                instances.addOverride(event, start, end);
            }
        }
    }
    
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream("instancelist/" + name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }        
        Calendar calendar = cb.build(in);
        return calendar;
    }
    
}
