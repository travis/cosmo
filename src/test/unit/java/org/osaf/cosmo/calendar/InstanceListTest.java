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
import java.util.Iterator;
import java.util.TreeSet;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Instance;
import net.fortuna.ical4j.model.InstanceList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Test expand InstanceList
 */
public class InstanceListTest extends TestCase {
    protected String baseDir = "src/test/unit/resources/testdata/";
    
    public void testNonUTCInstanceList() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "floating_recurr_event.ics");
        Calendar calendar = cb.build(fis);
        
        InstanceList instances = new InstanceList();
        
        DateTime start = new DateTime("20060101T190000Z");
        DateTime end = new DateTime("20060108T190000Z");
        
        ComponentList comps = calendar.getComponents();
        Iterator<VEvent> it = comps.getComponents("VEVENT").iterator();
        boolean addedMaster = false;
        while(it.hasNext()) {
            VEvent event = it.next();
            if(event.getReccurrenceId()==null) {
                addedMaster = true;
                instances.addComponent(event, start, end);
            }
            else {
                Assert.assertTrue(addedMaster);
                instances.addOverride(event);
            }
        }
        
        TreeSet sortedKeys = new TreeSet(instances.keySet());
        Assert.assertEquals(5, sortedKeys.size() );
        
        Iterator<String> keys = sortedKeys.iterator();
        
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
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "floating_recurr_event.ics");
        Calendar calendar = cb.build(fis);
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(tz);
        
        DateTime start = new DateTime("20060101T190000Z");
        DateTime end = new DateTime("20060108T190000Z");
        
        ComponentList comps = calendar.getComponents();
        Iterator<VEvent> it = comps.getComponents("VEVENT").iterator();
        boolean addedMaster = false;
        while(it.hasNext()) {
            VEvent event = it.next();
            if(event.getReccurrenceId()==null) {
                addedMaster = true;
                instances.addComponent(event, start, end);
            }
            else {
                Assert.assertTrue(addedMaster);
                instances.addOverride(event);
            }
        }
        
        TreeSet sortedKeys = new TreeSet(instances.keySet());
        Assert.assertEquals(5, sortedKeys.size() );
        
        Iterator<String> keys = sortedKeys.iterator();
        
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
    
}
