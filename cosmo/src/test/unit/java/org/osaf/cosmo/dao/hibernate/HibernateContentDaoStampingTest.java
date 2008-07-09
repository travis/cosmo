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
package org.osaf.cosmo.dao.hibernate;

import junit.framework.Assert;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidStateException;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.osaf.cosmo.model.hibernate.HibEventExceptionStamp;
import org.osaf.cosmo.model.hibernate.HibEventStamp;
import org.osaf.cosmo.model.hibernate.HibMessageStamp;
import org.osaf.cosmo.model.hibernate.HibNoteItem;
import org.osaf.cosmo.model.hibernate.HibQName;
import org.osaf.cosmo.model.hibernate.HibStringAttribute;

public class HibernateContentDaoStampingTest extends AbstractHibernateDaoTestCase {

    protected UserDaoImpl userDao = null;
    protected ContentDaoImpl contentDao = null;

    private static final Log log = LogFactory.getLog(HibernateContentDaoStampingTest.class);

    
    public HibernateContentDaoStampingTest() {
        super();
    }

    public void testStampsCreate() throws Exception {
        EntityConverter entityConverter = new EntityConverter(null);
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();
        
        item.setIcalUid("icaluid");
        item.setBody("this is a body");
        
        MessageStamp message = new HibMessageStamp(item);
        message.setBcc("bcc");
        message.setTo("to");
        message.setFrom("from");
        message.setCc("cc");
        
        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("cal1.ics"));
        
        item.addStamp(message);
        item.addStamp(event);
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(2, queryItem.getStamps().size());
        
        Stamp stamp = queryItem.getStamp(EventStamp.class);
        Assert.assertNotNull(stamp.getCreationDate());
        Assert.assertNotNull(stamp.getModifiedDate());
        Assert.assertTrue(stamp.getCreationDate().equals(stamp.getModifiedDate()));
        Assert.assertTrue(stamp instanceof EventStamp);
        Assert.assertEquals("event", stamp.getType());
        EventStamp es = (EventStamp) stamp;
        Assert.assertEquals(es.getEventCalendar().toString(), event.getEventCalendar()
                .toString());
        
        Assert.assertEquals("icaluid", ((NoteItem) queryItem).getIcalUid());
        Assert.assertEquals("this is a body", ((NoteItem) queryItem).getBody());
        
        stamp = queryItem.getStamp(MessageStamp.class);
        Assert.assertTrue(stamp instanceof MessageStamp);
        Assert.assertEquals("message", stamp.getType());
        MessageStamp ms = (MessageStamp) stamp;
        Assert.assertEquals(ms.getBcc(), message.getBcc());
        Assert.assertEquals(ms.getCc(), message.getCc());
        Assert.assertEquals(ms.getTo(), message.getTo());
        Assert.assertEquals(ms.getFrom(), message.getFrom());
    }
    
    public void testStampHandlers() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();
        
        item.setIcalUid("icaluid");
        item.setBody("this is a body");
        
        HibEventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("cal1.ics"));
        
        item.addStamp(event);
        
        Assert.assertNull(event.getTimeRangeIndex());
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        
        event = (HibEventStamp) queryItem.getStamp(EventStamp.class);
        Assert.assertEquals("20050817T115000Z", event.getTimeRangeIndex().getStartDate());
        Assert.assertEquals("20050817T131500Z",event.getTimeRangeIndex().getEndDate());
        Assert.assertFalse(event.getTimeRangeIndex().getIsFloating().booleanValue());
        
        event.setStartDate(new Date("20070101"));
        event.setEndDate(null);
        
        contentDao.updateContent(queryItem);
        clearSession();
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        
        event = (HibEventStamp) queryItem.getStamp(EventStamp.class);
        Assert.assertEquals("20070101", event.getTimeRangeIndex().getStartDate());
        Assert.assertEquals("20070101",event.getTimeRangeIndex().getEndDate());
        Assert.assertTrue(event.getTimeRangeIndex().getIsFloating().booleanValue());
    }
    
    public void testStampsUpdate() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        
        ((NoteItem) item).setBody("this is a body");
        ((NoteItem) item).setIcalUid("icaluid");
        
        MessageStamp message = new HibMessageStamp(item);
        message.setBcc("bcc");
        message.setTo("to");
        message.setFrom("from");
        message.setCc("cc");
        
        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("cal1.ics"));
        
        item.addStamp(message);
        item.addStamp(event);
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(2, queryItem.getStamps().size());
        
        Stamp stamp = queryItem.getStamp(MessageStamp.class);
        queryItem.removeStamp(stamp);
        
        stamp = queryItem.getStamp(EventStamp.class);
        EventStamp es = (EventStamp) stamp;
        queryItem.setClientModifiedDate(new Date());
        es.setEventCalendar(helper.getCalendar("cal2.ics"));
        Calendar newCal = es.getEventCalendar();
        Thread.sleep(10);
        
        contentDao.updateContent(queryItem);
        
        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
        Assert.assertNull(queryItem.getStamp(MessageStamp.class));
        stamp = queryItem.getStamp(EventStamp.class);
        es = (EventStamp) stamp;
       
        Assert.assertTrue(stamp.getModifiedDate().after(stamp.getCreationDate()));
        
        if(!es.getEventCalendar().toString().equals(newCal.toString())) {
            log.error(es.getEventCalendar().toString());
            log.error(newCal.toString());
        }
        Assert.assertEquals(es.getEventCalendar().toString(), newCal.toString());
    }
    
    public void testEventStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        
        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("noevent.ics"));
        item.addStamp(event);
       
        try {
            contentDao.createContent(root, item);
            clearSession();
            Assert.fail("able to create invalid event!");
        } catch (InvalidStateException e) {}
        catch (IllegalStateException e) {}
    }
    
    public void testRemoveStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();
        
        item.setIcalUid("icaluid");
        item.setBody("this is a body");
        
        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("cal1.ics"));
        
        item.addStamp(event);
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
       
        Stamp stamp = queryItem.getStamp(EventStamp.class);
        queryItem.removeStamp(stamp);
        contentDao.updateContent(queryItem);
        clearSession();
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertEquals(queryItem.getStamps().size(),0);
        Assert.assertEquals(1, queryItem.getTombstones().size());
        
        event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("cal1.ics"));
        queryItem.addStamp(event);
        
        contentDao.updateContent(queryItem);
        clearSession();
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
    }
    
    public void testCalendarCollectionStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        
        Calendar testCal = helper.getCalendar("timezone.ics");
        
        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setDescription("description");
        calendarStamp.setTimezoneCalendar(testCal);
        calendarStamp.setLanguage("en");
        
        root.addStamp(calendarStamp);
        
        contentDao.updateCollection(root);
        clearSession();
        
        root = (CollectionItem) contentDao.findItemByUid(root.getUid());
        
        ContentItem item = generateTestContent();
        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("cal1.ics"));
        item.addStamp(event);
        
        contentDao.createContent(root, item);
        
        clearSession();
        
        CollectionItem queryCol = (CollectionItem) contentDao.findItemByUid(root.getUid());
        Assert.assertEquals(1, queryCol.getStamps().size());
        Stamp stamp = queryCol.getStamp(CalendarCollectionStamp.class);
        Assert.assertTrue(stamp instanceof CalendarCollectionStamp);
        Assert.assertEquals("calendar", stamp.getType());
        CalendarCollectionStamp ccs = (CalendarCollectionStamp) stamp;
        Assert.assertEquals("description", ccs.getDescription());
        Assert.assertEquals(testCal.toString(), ccs.getTimezoneCalendar().toString());
        Assert.assertEquals("en", ccs.getLanguage());
        
        Calendar cal = new EntityConverter(null).convertCollection(queryCol);
        Assert.assertEquals(1, cal.getComponents().getComponents(Component.VEVENT).size());
    }
    
    public void testCalendarCollectionStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        
        Calendar testCal = helper.getCalendar("cal1.ics");
        
        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setTimezoneCalendar(testCal);
        
        root.addStamp(calendarStamp);
        
        try {
            contentDao.updateCollection(root);
            clearSession();
            Assert.fail("able to save invalid timezone");
        } catch (InvalidStateException ise) {
            
        } 
    }
    
    public void testEventExceptionStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();
        
        item.setIcalUid("icaluid");
        item.setBody("this is a body");
        
        EventExceptionStamp eventex = new HibEventExceptionStamp();
        eventex.setEventCalendar(helper.getCalendar("exception.ics"));
        
        item.addStamp(eventex);
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
       
        Stamp stamp = queryItem.getStamp(EventExceptionStamp.class);
        Assert.assertNotNull(stamp.getCreationDate());
        Assert.assertNotNull(stamp.getModifiedDate());
        Assert.assertTrue(stamp.getCreationDate().equals(stamp.getModifiedDate()));
        Assert.assertTrue(stamp instanceof EventExceptionStamp);
        Assert.assertEquals("eventexception", stamp.getType());
        EventExceptionStamp ees = (EventExceptionStamp) stamp;
        Assert.assertEquals(ees.getEventCalendar().toString(), eventex.getEventCalendar()
                .toString());
    }
    
    public void testEventExceptionStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();
        
        item.setIcalUid("icaluid");
        item.setBody("this is a body");
        
        EventExceptionStamp eventex = new HibEventExceptionStamp();
        eventex.setEventCalendar(helper.getCalendar("cal1.ics"));
        
        item.addStamp(eventex);
        
        try {
            ContentItem newItem = contentDao.createContent(root, item);
            clearSession();
            Assert.fail("able to save invalid exception event");
        } catch (InvalidStateException e) {
        }
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private NoteItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    private NoteItem generateTestContent(String name, String owner)
            throws Exception {
        NoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new HibStringAttribute(new HibQName("customattribute"),
                "customattributevalue"));
        return content;
    }

}
