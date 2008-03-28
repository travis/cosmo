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
package org.osaf.cosmo.model.hibernate;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.hibernate.EventStampInterceptor;
import org.osaf.cosmo.model.hibernate.HibEventStamp;
import org.osaf.cosmo.model.hibernate.HibEventTimeRangeIndex;
import org.osaf.cosmo.model.hibernate.HibNoteItem;

/**
 * Test EventStampHandler
 */
public class EventStampInterceptorTest extends TestCase {
   
    EventStampInterceptor interceptor = new EventStampInterceptor();
    TimeZoneRegistry registry =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    public void testEventStampHandler() throws Exception {
        
        HibNoteItem master = new HibNoteItem();
        HibEventStamp eventStamp = new HibEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        eventStamp.setEndDate(new DateTime("20070212T094500"));
        master.addStamp(eventStamp);
        
        HibEventTimeRangeIndex index = interceptor.calculateEventStampIndexes(eventStamp);
        
        Assert.assertEquals("20070212T074500", index.getStartDate());
        Assert.assertEquals("20070212T094500", index.getEndDate());
        Assert.assertTrue(index.getIsFloating().booleanValue());
        
        TimeZone ctz = registry.getTimeZone("America/Chicago");
        DateTime start = new DateTime("20070212T074500", ctz);
        eventStamp.setStartDate(start);
        
        DateTime end = new DateTime("20070212T094500", ctz);
        eventStamp.setEndDate(end);
        
        String recur1 = "FREQ=DAILY;";
        
        List recurs = EimValueConverter.toICalRecurs(recur1);
        eventStamp.setRecurrenceRules(recurs);
        
        index = interceptor.calculateEventStampIndexes(eventStamp);
        
        Assert.assertEquals("20070212T134500Z", index.getStartDate());
        Assert.assertEquals(HibEventStamp.TIME_INFINITY, index.getEndDate());
        Assert.assertFalse(index.getIsFloating().booleanValue());
    }
    
    public void testEventStampHandlerAllDay() throws Exception {
        
        HibNoteItem master = new HibNoteItem();
        HibEventStamp eventStamp = new HibEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new Date("20070212"));
        eventStamp.setEndDate(new Date("20070213"));
        master.addStamp(eventStamp);
        
        HibEventTimeRangeIndex index = interceptor.calculateEventStampIndexes(eventStamp);
        
        Assert.assertEquals("20070212", index.getStartDate());
        Assert.assertEquals("20070213", index.getEndDate());
        Assert.assertTrue(index.getIsFloating().booleanValue());
      
        String recur1 = "FREQ=DAILY;";
        
        List recurs = EimValueConverter.toICalRecurs(recur1);
        eventStamp.setRecurrenceRules(recurs);
        
        index = interceptor.calculateEventStampIndexes(eventStamp);
        
        Assert.assertEquals("20070212", index.getStartDate());
        Assert.assertEquals(HibEventStamp.TIME_INFINITY, index.getEndDate());
        Assert.assertTrue(index.getIsFloating().booleanValue());
    }
    
    public void testEventStampHandlerMods() throws Exception {
        
        HibNoteItem master = new HibNoteItem();
        HibEventStamp eventStamp = new HibEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        eventStamp.setEndDate(new DateTime("20070212T094500"));
        eventStamp.setRecurrenceRules(EimValueConverter.toICalRecurs("FREQ=DAILY;"));
        master.addStamp(eventStamp);
        
        HibNoteItem mod = new HibNoteItem();
        mod.setModifies(master);
        HibEventExceptionStamp eventExceptionStamp = new HibEventExceptionStamp(mod);
        eventExceptionStamp.createCalendar();
        eventExceptionStamp.setStartDate(new DateTime("20070213T084500"));
       
        mod.addStamp(eventStamp);
        
        HibEventTimeRangeIndex index = interceptor.calculateEventStampIndexes(eventExceptionStamp);
        
        Assert.assertEquals("20070213T084500", index.getStartDate());
        Assert.assertEquals("20070213T104500", index.getEndDate());
        Assert.assertTrue(index.getIsFloating().booleanValue());
        
        // handle case where master isn't an event anymore
        master.removeStamp(eventStamp);
        index = interceptor.calculateEventStampIndexes(eventExceptionStamp);
        
        Assert.assertEquals("20070213T084500", index.getStartDate());
        Assert.assertEquals("20070213T084500", index.getEndDate());
    }
   
    
    
}
