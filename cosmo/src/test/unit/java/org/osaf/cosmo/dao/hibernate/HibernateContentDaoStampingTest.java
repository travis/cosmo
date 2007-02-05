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

import org.hibernate.validator.InvalidStateException;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.User;

public class HibernateContentDaoStampingTest extends AbstractHibernateDaoTestCase {

    protected UserDaoImpl userDao = null;
    protected ContentDaoImpl contentDao = null;

    public HibernateContentDaoStampingTest() {
        super();
    }

    public void testStampsCreate() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();
        
        item.setIcalUid("icaluid");
        item.setBody("this is a body");
        
        MessageStamp message = new MessageStamp();
        message.setBcc("bcc");
        message.setTo("to");
        message.setSubject("subject");
        message.setCc("cc");
        
        EventStamp event = new EventStamp();
        event.setCalendar(helper.getCalendar(baseDir + "/cal1.ics"));
        
        item.addStamp(message);
        item.addStamp(event);
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());
        Assert.assertEquals(2, queryItem.getStamps().size());
        Assert.assertEquals(2, queryItem.getActiveStamps().size());
        
        Stamp stamp = queryItem.getStamp(EventStamp.class);
        Assert.assertNotNull(stamp.getCreationDate());
        Assert.assertNotNull(stamp.getModifiedDate());
        Assert.assertTrue(stamp.getCreationDate().equals(stamp.getModifiedDate()));
        Assert.assertTrue(stamp instanceof EventStamp);
        Assert.assertEquals("event", stamp.getType());
        EventStamp es = (EventStamp) stamp;
        Assert.assertEquals(es.getCalendar().toString(), event.getCalendar()
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
        Assert.assertEquals(ms.getSubject(), message.getSubject());
    }
    
    public void testStampsUpdate() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        
        ((NoteItem) item).setBody("this is a body");
        ((NoteItem) item).setIcalUid("icaluid");
        
        MessageStamp message = new MessageStamp();
        message.setBcc("bcc");
        message.setTo("to");
        message.setSubject("subject");
        message.setCc("cc");
        
        EventStamp event = new EventStamp();
        event.setCalendar(helper.getCalendar(baseDir + "/cal1.ics"));
        
        item.addStamp(message);
        item.addStamp(event);
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());
        Assert.assertEquals(2, queryItem.getStamps().size());
        Assert.assertEquals(2, queryItem.getActiveStamps().size());
        
        Stamp stamp = queryItem.getStamp(MessageStamp.class);
        queryItem.removeStamp(stamp);
        
        stamp = queryItem.getStamp(EventStamp.class);
        EventStamp es = (EventStamp) stamp;
        es.setCalendar(helper.getCalendar(baseDir + "/cal2.ics"));
        Calendar newCal = es.getCalendar();
        
        contentDao.updateContent(queryItem);
        
        clearSession();
        queryItem = contentDao.findContentByUid(newItem.getUid());
        Assert.assertEquals(2, queryItem.getStamps().size());
        Assert.assertEquals(1, queryItem.getActiveStamps().size());
        Assert.assertNull(queryItem.getStamp(MessageStamp.class));
        stamp = queryItem.getStamp(EventStamp.class);
        es = (EventStamp) stamp;
       
        Assert.assertTrue(stamp.getModifiedDate().after(stamp.getCreationDate()));
        Assert.assertEquals(es.getCalendar().toString(), newCal.toString());
    }
    
    public void testEventStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        
        EventStamp event = new EventStamp();
        event.setCalendar(helper.getCalendar(baseDir + "/noevent.ics"));
        item.addStamp(event);
       
        try {
            contentDao.createContent(root, item);
            clearSession();
            Assert.fail("able to create invalid event!");
        } catch (ModelValidationException e) {}
    }
    
    public void testRemoveStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();
        
        item.setIcalUid("icaluid");
        item.setBody("this is a body");
        
        EventStamp event = new EventStamp();
        event.setCalendar(helper.getCalendar(baseDir + "/cal1.ics"));
        
        item.addStamp(event);
        
        ContentItem newItem = contentDao.createContent(root, item);
        clearSession();

        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
        Assert.assertEquals(1, queryItem.getActiveStamps().size());
        
        Stamp stamp = queryItem.getStamp(EventStamp.class);
        queryItem.removeStamp(stamp);
        contentDao.updateContent(queryItem);
        clearSession();
        
        queryItem = contentDao.findContentByUid(newItem.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertEquals(queryItem.getStamps().size(),1);
        Assert.assertEquals(queryItem.getActiveStamps().size(),0);
        Assert.assertTrue(
                queryItem.getStamps().iterator().next() instanceof EventStamp);
        
    }
    
    public void testCalendarCollectionStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        
        Calendar testCal = helper.getCalendar(baseDir + "/timezone.ics");
        
        CalendarCollectionStamp calendarStamp = new CalendarCollectionStamp(root);
        calendarStamp.setDescription("description");
        calendarStamp.setTimezone(testCal);
        calendarStamp.setLanguage("en");
        
        root.addStamp(calendarStamp);
        
        contentDao.updateCollection(root);
        clearSession();
        
        root = contentDao.findCollectionByUid(root.getUid());
        
        ContentItem item = generateTestContent();
        EventStamp event = new EventStamp();
        event.setCalendar(helper.getCalendar(baseDir + "/cal1.ics"));
        item.addStamp(event);
        
        contentDao.createContent(root, item);
        
        clearSession();
        
        CollectionItem queryCol = contentDao.findCollectionByUid(root.getUid());
        Assert.assertEquals(1, queryCol.getStamps().size());
        Assert.assertEquals(1, queryCol.getActiveStamps().size());
        Stamp stamp = queryCol.getStamp(CalendarCollectionStamp.class);
        Assert.assertTrue(stamp instanceof CalendarCollectionStamp);
        Assert.assertEquals("calendar", stamp.getType());
        CalendarCollectionStamp ccs = (CalendarCollectionStamp) stamp;
        Assert.assertEquals("description", ccs.getDescription());
        Assert.assertEquals(testCal.toString(), ccs.getTimezone().toString());
        Assert.assertEquals("en", ccs.getLanguage());
        Assert.assertEquals(1, ccs.getSupportedComponents().size());
        
        Calendar cal = ccs.getCalendar();
        Assert.assertEquals(1, cal.getComponents().getComponents(Component.VEVENT).size());
    }
    
    public void testCalendarCollectionStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        
        Calendar testCal = helper.getCalendar(baseDir + "/cal1.ics");
        
        CalendarCollectionStamp calendarStamp = new CalendarCollectionStamp(root);
        calendarStamp.setTimezone(testCal);
        
        root.addStamp(calendarStamp);
        
        try {
            contentDao.updateCollection(root);
            clearSession();
            Assert.fail("able to save invalid timezone");
        } catch (InvalidStateException ise) {
            
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
        NoteItem content = new NoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setContent(helper.getBytes(baseDir + "/testdata1.txt"));
        content.setContentLanguage("en");
        content.setContentEncoding("UTF8");
        content.setContentType("text/text");
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new StringAttribute(new QName("customattribute"),
                "customattributevalue"));
        return content;
    }

}
