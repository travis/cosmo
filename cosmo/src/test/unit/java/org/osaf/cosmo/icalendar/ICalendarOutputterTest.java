/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.icalendar;

import java.io.FileInputStream;

import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.mock.MockCalendarCollectionStamp;
import org.osaf.cosmo.model.mock.MockCollectionItem;
import org.osaf.cosmo.model.mock.MockEntityFactory;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

/**
 * Test ICalendarOutputter
 */
public class ICalendarOutputterTest extends TestCase {
   
    protected String baseDir = "src/test/unit/resources/testdata/";
    
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    public void testGetCalendarFromCollection() throws Exception {
        EntityConverter ec = new EntityConverter(new MockEntityFactory());
        Calendar c1 = getCalendar("eventwithtimezone1.ics");
        Calendar c2 = getCalendar("vtodo.ics");
        NoteItem note1 = ec.convertEventCalendar(c1).iterator().next();
        NoteItem note2 = ec.convertTaskCalendar(c2);
       
        MockCollectionItem collection = new MockCollectionItem();
        collection.addStamp(new MockCalendarCollectionStamp(collection));
        collection.addChild(note1);
        collection.addChild(note2);
        
        Calendar fullCal = ICalendarOutputter.getCalendarFromCollection(collection);
        Assert.assertNotNull(fullCal);
        
        // VTIMEZONE, VTODO, VEVENT
        Assert.assertEquals(3,fullCal.getComponents().size());
        Assert.assertEquals(1, fullCal.getComponents(Component.VTIMEZONE).size());
        Assert.assertEquals(1, fullCal.getComponents(Component.VEVENT).size());
        Assert.assertEquals(1, fullCal.getComponents(Component.VTODO).size());
    }
    
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
}
