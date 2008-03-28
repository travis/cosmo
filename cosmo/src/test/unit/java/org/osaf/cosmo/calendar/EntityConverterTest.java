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


import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.mock.MockEntityFactory;

/**
 * Test EntityConverter.
 *
 */
public class EntityConverterTest extends TestCase {
    protected String baseDir = "src/test/unit/resources/testdata/entityconverter/";
    private static final Log log = LogFactory.getLog(EntityConverterTest.class);
    
    public void testEntityConverterEvent() throws Exception {
        EntityFactory entityFactory = new MockEntityFactory();
        EntityConverter converter = new EntityConverter(entityFactory);
      
        Calendar calendar = getCalendar("event_with_exception.ics");
        NoteItem master = entityFactory.createNote();
        Set<NoteItem> items = converter.convertEventCalendar(master, calendar);
        
        // should be master and mod
        Assert.assertEquals(2, items.size());
        
        // get master
        Iterator<NoteItem> it = items.iterator();
        master = it.next();
        
        // check ical props
        // DTSTAMP
        Assert.assertEquals(master.getClientModifiedDate().getTime(), new DateTime("20051222T210507Z").getTime());
        // UID
        Assert.assertEquals(master.getIcalUid(), "F5B811E00073B22BA6B87551@ninevah.local");
        // SUMMARY
        Assert.assertEquals(master.getDisplayName(), "event 6");
        
        // get mod
        NoteItem mod = it.next();
        
        ModificationUid uid = new ModificationUid(mod.getUid());
        
        Assert.assertEquals(master.getUid(), uid.getParentUid());
        Assert.assertEquals("20060104T190000Z", uid.getRecurrenceId().toString());
        
        Assert.assertTrue(mod.getModifies()==master);
        EventExceptionStamp ees = StampUtils.getEventExceptionStamp(mod);
        Assert.assertNotNull(ees);
        
        // mod should include VTIMEZONES
        Calendar eventCal = ees.getEventCalendar();
        ComponentList vtimezones = eventCal.getComponents(Component.VTIMEZONE);
        Assert.assertEquals(1, vtimezones.size());
        
        
        // update event (change mod and add mod)
        calendar = getCalendar("event_with_exception2.ics");
        items = converter.convertEventCalendar(master, calendar);
        
        // should be master and 2 mods
        Assert.assertEquals(3, items.size());
        
        mod = findModByRecurrenceId(items, "20060104T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertEquals("event 6 mod 1 changed", mod.getDisplayName());
        
        mod = findModByRecurrenceId(items, "20060105T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertEquals("event 6 mod 2", mod.getDisplayName());
        
        // update event again (remove mod)
        calendar = getCalendar("event_with_exception3.ics");
        items = converter.convertEventCalendar(master, calendar);
        
        // should be master and 1 active mod/ 1 deleted mod
        Assert.assertEquals(3, items.size());
        
        mod = findModByRecurrenceId(items, "20060104T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertFalse(mod.getIsActive().booleanValue());
        
        mod = findModByRecurrenceId(items, "20060105T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertEquals("event 6 mod 2 changed", mod.getDisplayName());
        
    }
    
    public void testEntityConverterMultiComponentCalendar() throws Exception {
        EntityFactory entityFactory = new MockEntityFactory();
        EntityConverter converter = new EntityConverter(entityFactory);
      
        // test converting calendar with many different components
        // into ICalendarItems
        
        Calendar calendar = getCalendar("bigcalendar.ics");
        NoteItem master = entityFactory.createNote();
        Set<ICalendarItem> items = converter.convertCalendar(calendar);
        
        // should be 8
        Assert.assertEquals(8, items.size());
        
        ICalendarItem item = findItemByIcalUid(items, "8qv7nuaq50vk3r98tvj37vjueg@google.com" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "e3i849b29kd3fbp48hmkmgjst0@google.com" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "4csitoh29h1arc46bnchg19oc8@google.com" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "f920n2rdb0qdd6grkjh4m4jrq0@google.com" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "jev0phs8mnfkuvoscrra1fh8j0@google.com" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertNotNull(StampUtils.getEventStamp(item));
        
        item = findModByRecurrenceId(items, "20071129T203000Z" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertNotNull(StampUtils.getEventExceptionStamp(item));
        
        item = findItemByIcalUid(items, "19970901T130000Z-123404@host.com" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertNotNull(StampUtils.getTaskStamp(item));
        
        item = findItemByIcalUid(items, "19970901T130000Z-123405@host.com" );
        Assert.assertNotNull(item);
        Assert.assertTrue(item instanceof NoteItem);
        Assert.assertEquals(0, item.getStamps().size());
        
    }
        
    
    private ICalendarItem findItemByIcalUid(Set<ICalendarItem> items, String icalUid) {
        for(ICalendarItem item: items)
            if(icalUid.equals(item.getIcalUid()))
                return item;
        
        return null;
    }
    
    private ICalendarItem findModByRecurrenceId(Set<ICalendarItem> items, String rid) {
        for(ICalendarItem item: items)
            if(item instanceof NoteItem) {
                NoteItem note = (NoteItem) item;
                if(note.getModifies()!=null && note.getUid().contains(rid))
                    return note;
            }
        
        return null;
    }
    
    private NoteItem findModByRecurrenceId(Set<NoteItem> items, String rid) {
        for(NoteItem note: items)
            if(note.getModifies()!=null && note.getUid().contains(rid))
                return note;
            
        
        return null;
    }
    
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
    
}
