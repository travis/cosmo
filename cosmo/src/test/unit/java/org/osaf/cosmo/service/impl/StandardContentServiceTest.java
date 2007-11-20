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
package org.osaf.cosmo.service.impl;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockCollectionItem;
import org.osaf.cosmo.model.mock.MockEventStamp;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.service.lock.SingleVMLockManager;

/**
 * Test Case for <code>StandardContentService</code> which uses mock
 * data access objects.
 *
 * @see StandardContentService
 * @see MockContentDao
 */
public class StandardContentServiceTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(StandardContentServiceTest.class);

    private StandardContentService service;
    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockDaoStorage storage;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    
    protected String baseDir = "src/test/unit/resources/testdata/";

    /** */
    protected void setUp() throws Exception {
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        calendarDao = new MockCalendarDao(storage);
        contentDao = new MockContentDao(storage);
        service = new StandardContentService();
        lockManager = new SingleVMLockManager();
        service.setCalendarDao(calendarDao);
        service.setContentDao(contentDao);
        service.setLockManager(lockManager);
        service.setTriageStatusQueryProcessor(new StandardTriageStatusQueryProcessor());
        service.init();
    }

    /** */
    public void testFindItemByPath() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNotNull(item);
        assertEquals(dummyContent, item);

        contentDao.removeContent(dummyContent);
    }
    
    /** */
    public void testInvalidModUid() throws Exception {
        
        Item item = service.findItemByUid("uid" + ModificationUid.RECURRENCEID_DELIMITER + "bogus");
        
        // bogus mod uid should result in no item found, not a ModelValidationException
        assertNull(item);
    }

    /** */
    public void testFindNonExistentItemByPath() throws Exception {
        String path = "/foo/bar/baz";
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }

    /** */
    public void testRemoveItem() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        contentDao.removeItem(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }

    /** */
    public void testCreateContent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        ContentItem content = new MockNoteItem();
        content.setName("foo");
        content.setOwner(user);
        content = contentDao.createContent(rootCollection, content);

        assertNotNull(content);
        assertEquals("foo", content.getName());
        assertEquals(user, content.getOwner());

        contentDao.removeContent(content);
    }

    /** */
    public void testRemoveContent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        contentDao.removeContent(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }

    public void testCreateCollectionWithChildren() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        NoteItem dummyContent = new MockNoteItem();
        dummyContent.setName("bar");
        dummyContent.setOwner(user);
        
        HashSet<Item> children = new HashSet<Item>();
        children.add(dummyContent);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection, children);
        
        assertNotNull(dummyCollection);
        assertEquals(1, dummyCollection.getChildren().size());
        assertEquals("bar", 
                dummyCollection.getChildren().iterator().next().getName());
    }
    
    public void testUpdateCollectionWithChildren() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        ContentItem dummyContent1 = new MockNoteItem();
        dummyContent1.setName("bar1");
        dummyContent1.setOwner(user);
        
        ContentItem dummyContent2 = new MockNoteItem();
        dummyContent2.setName("bar2");
        dummyContent2.setOwner(user);
        
        HashSet<Item> children = new HashSet<Item>();
        children.add(dummyContent1);
        children.add(dummyContent2);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection, children);
        
        assertEquals(2, dummyCollection.getChildren().size());
        
        ContentItem bar1 = 
            getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        ContentItem bar2 = 
            getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        assertNotNull(bar1);
        assertNotNull(bar2);
        
        bar1.setIsActive(false);
        bar2.addStringAttribute("foo", "bar");
        
        ContentItem bar3 = new MockNoteItem();
        bar3.setName("bar3");
        bar3.setOwner(user);
        
        children.clear();
        children.add(bar1);
        children.add(bar2);
        children.add(bar3);
        
        dummyCollection = service.updateCollection(dummyCollection, children);
          
        assertEquals(2, dummyCollection.getChildren().size());
        
        bar1 = getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        bar2 = getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        bar3 = getContentItemFromSet(dummyCollection.getChildren(), "bar3");
        
        assertNull(bar1);
        assertNotNull(bar2);
        assertEquals(1,bar2.getAttributes().size());
        assertEquals("bar", bar2.getAttributeValue("foo"));
        assertNotNull(bar3);
    }
    
    public void testCollectionHashGetsUpdated() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("bar1");
        dummyContent.setOwner(user);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection);
        
        dummyContent = 
            service.createContent(dummyCollection, dummyContent);
        
        assertEquals(1, dummyCollection.generateHash());
        
   
        dummyContent.addStringAttribute("foo", "bar");
        dummyContent = service.updateContent(dummyContent);
           
        assertEquals(2, dummyCollection.generateHash());
        
        dummyContent.addStringAttribute("foo2", "bar2");
        dummyContent = service.updateContent(dummyContent);
        assertEquals(3, dummyCollection.generateHash());
    }
    
    /** */
    public void testUpdateEvent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        NoteItem masterNote = new MockNoteItem();
        masterNote.setName("foo");
        masterNote.setOwner(user);
        
        Calendar calendar = getCalendar("event_with_exceptions1.ics"); 
        
        EventStamp eventStamp = new MockEventStamp(masterNote);
        masterNote.addStamp(eventStamp);
        contentDao.createContent(rootCollection, masterNote);
        
        EntityConverter converter = new EntityConverter(testHelper.getEntityFactory());
        Set<ContentItem> toUpdate = new HashSet<ContentItem>();
        toUpdate.addAll(converter.convertEventCalendar(masterNote, calendar));
        service.updateContentItems(masterNote.getParents(), toUpdate);
        
        Calendar masterCal = eventStamp.getEventCalendar();
        VEvent masterEvent = eventStamp.getMasterEvent();
        
        Assert.assertEquals(1, masterCal.getComponents().getComponents(Component.VEVENT).size());
        Assert.assertNull(eventStamp.getMasterEvent().getRecurrenceId());
        
        Assert.assertEquals(masterNote.getModifications().size(), 4);
        for(NoteItem mod : masterNote.getModifications()) {
            EventExceptionStamp eventException = StampUtils.getEventExceptionStamp(mod);
            VEvent exceptionEvent = eventException.getExceptionEvent();
            Assert.assertEquals(mod.getModifies(), masterNote);
            Assert.assertEquals(masterEvent.getUid().getValue(), exceptionEvent.getUid().getValue());
        }
        
        Assert.assertNotNull(getEvent("20060104T140000", eventStamp.getCalendar()));
        Assert.assertNotNull(getEvent("20060105T140000", eventStamp.getCalendar()));
        Assert.assertNotNull(getEvent("20060106T140000", eventStamp.getCalendar()));
        Assert.assertNotNull(getEvent("20060107T140000", eventStamp.getCalendar()));
        
        Assert.assertNotNull(getEventException("20060104T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060105T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060106T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060107T140000", masterNote.getModifications()));
        
        
        Calendar fullCal = eventStamp.getCalendar();
        Assert.assertEquals(fullCal.getComponents().getComponents(Component.VEVENT).size(), 5);
        
        // now update
        calendar = getCalendar("event_with_exceptions2.ics"); 
        toUpdate.addAll(converter.convertEventCalendar(masterNote, calendar));
        service.updateContentItems(masterNote.getParents(), toUpdate);
        
        // should have removed 1, added 2 so that makes 4-1+2=5
        Assert.assertEquals(masterNote.getModifications().size(), 5);
        Assert.assertNotNull(getEventException("20060104T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060105T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060106T140000", masterNote.getModifications()));
        Assert.assertNull(getEventException("20060107T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060108T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060109T140000", masterNote.getModifications()));
        
        Assert.assertNotNull(getEvent("20060104T140000", eventStamp.getCalendar()));
        Assert.assertNotNull(getEvent("20060105T140000", eventStamp.getCalendar()));
        Assert.assertNotNull(getEvent("20060106T140000", eventStamp.getCalendar()));
        Assert.assertNull(getEvent("20060107T140000", eventStamp.getCalendar()));
        Assert.assertNotNull(getEvent("20060108T140000", eventStamp.getCalendar()));
        Assert.assertNotNull(getEvent("20060109T140000", eventStamp.getCalendar()));
    }
    
    
    /** */
    public void testNullContentDao() throws Exception {
        service.setContentDao(null);
        try {
            service.init();
            fail("Should not be able to initialize service without contentDao");
        } catch (IllegalStateException e) {
            // expected
        }
    }
    
    private ContentItem getContentItemFromSet(Set<Item> items, String name) {
        for(Item item : items)
            if(item.getName().equals(name))
                return (ContentItem) item;
        return null;
    }
    
    private EventExceptionStamp getEventException(String recurrenceId, Set<NoteItem> items) {
        for(NoteItem mod : items) {
            EventExceptionStamp ees = StampUtils.getEventExceptionStamp(mod);
            if(ees.getRecurrenceId().toString().equals(recurrenceId))
                return ees;
        }
        return null;
    }
    
    private Calendar getCalendar(String filename) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + filename);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
    
    private VEvent getEvent(String recurrenceId, Calendar calendar) {
        ComponentList events = calendar.getComponents().getComponents(Component.VEVENT);
        for(Iterator<VEvent> it = events.iterator(); it.hasNext();) {
            VEvent event = it.next();
            if(event.getRecurrenceId()!=null && event.getRecurrenceId().getDate().toString().equals(recurrenceId))
                return event;
        }
        return null;
    }
}
