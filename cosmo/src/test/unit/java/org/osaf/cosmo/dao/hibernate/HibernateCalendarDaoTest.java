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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.PropertyFilter;
import org.osaf.cosmo.calendar.query.TextMatchFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;

/**
 * Test CalendarDaoImpl
 */
public class HibernateCalendarDaoTest extends AbstractHibernateDaoTestCase {

    protected CalendarDaoImpl calendarDao = null;

    protected ContentDaoImpl contentDao = null;

    protected UserDaoImpl userDao = null;

    public HibernateCalendarDaoTest() {
        super();
    }

    public void testCalendarDaoBasic() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        CollectionItem newItem = contentDao.createCollection(root, calendar);

        clearSession();

        CollectionItem queryItem = (CollectionItem) contentDao
                .findItemByUid(calendar.getUid());

        CalendarCollectionStamp ccs = (CalendarCollectionStamp) queryItem
                .getStamp(CalendarCollectionStamp.class);
        
        Assert.assertNotNull(queryItem);
        Assert.assertEquals("test", queryItem.getName());
        Assert.assertEquals("en", ccs.getLanguage());
        Assert.assertEquals("test description", ccs.getDescription());
        Assert.assertEquals("VEVENT", (String) ccs
                .getSupportedComponents().iterator().next());

        // test update
        queryItem.setName("test2");
        ccs.setLanguage("es");
        ccs.setDescription("test description2");
        HashSet<String> supportedComponents = new HashSet<String>();
        supportedComponents.add("VTODO");
        ccs.setSupportedComponents(supportedComponents);

        contentDao.updateCollection(queryItem);
        Assert.assertNotNull(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ccs = (CalendarCollectionStamp) queryItem.getStamp(CalendarCollectionStamp.class);
        
        Assert.assertEquals("test2", queryItem.getName());
        Assert.assertEquals("es", ccs.getLanguage());
        Assert.assertEquals("test description2", ccs.getDescription());
        Assert.assertEquals("VTODO", (String) ccs
                .getSupportedComponents().iterator().next());

        // test add event
        ContentItem event = generateEvent("test.ics", "cal1.ics",
                "testuser");
        
        calendar = contentDao.findCollectionByUid(calendar.getUid());
        ContentItem newEvent = contentDao.createContent(calendar, event);
        
        clearSession();

        // test query event
        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        EventStamp evs = (EventStamp) queryEvent.getStamp(EventStamp.class);
        
        Assert.assertEquals("test.ics", queryEvent.getName());
        Assert.assertEquals(getCalendar(event).toString(), evs.getCalendar().toString());

        // test update event
        queryEvent.setName("test2.ics");
        evs.setCalendar(CalendarUtils.parseCalendar(helper.getBytes(baseDir + "/cal2.ics")));
        
        queryEvent = contentDao.updateContent(queryEvent);

        Calendar cal = evs.getCalendar();
        
        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        evs = (EventStamp) queryEvent.getStamp(EventStamp.class);
        
        Assert.assertEquals("test2.ics", queryEvent.getName());
        Assert.assertEquals(evs.getCalendar().toString(), cal.toString());
        

        // test delete
        contentDao.removeContent(queryEvent);

        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        Assert.assertNull(queryEvent);

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        contentDao.removeCollection(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        Assert.assertNull(queryItem);
    }

    public void testLongPropertyValue() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        CollectionItem newItem = contentDao.createCollection(root, calendar);

        ContentItem event = generateEvent("big.ics", "big.ics",
                "testuser");

        event = contentDao.createContent(calendar, event);

        clearSession();

        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(event.getUid());
        Assert.assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());
    }

    /*public void testDuplicateEventUid() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        CollectionItem newItem = contentDao.createCollection(root, calendar);

        ContentItem event = generateEvent("test.ics", "cal1.ics",
                "testuser");

        event = contentDao.createContent(calendar, event);
        calendarDao.indexEvent(EventStamp.getStamp(event));

        ContentItem event2 = generateEvent("testduplicate.ics",
                "cal1.ics", "testuser");
        calendarDao.indexEvent(EventStamp.getStamp(event));

        clearSession();

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());

        try {
            event2 = contentDao.createContent(calendar, event2);
            Assert.fail("able to create event with duplicate uid");
        } catch (DuplicateEventUidException e) {
        }
    }*/

    public void testFindByEventIcalUid() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        contentDao.createCollection(root, calendar);

        NoteItem event = generateEvent("test.ics", "cal1.ics",
                "testuser");

        event = (NoteItem) contentDao.createContent(calendar, event);
        
        clearSession();

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        String uid = "68ADA955-67FF-4D49-BBAC-AF182C620CF6";
        ContentItem queryEvent = calendarDao.findEventByIcalUid(uid,
                calendar);
        Assert.assertNotNull(queryEvent);
        Assert.assertEquals(event.getUid(), queryEvent.getUid());
    }

   

    public void testCalendarQuerying() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        CollectionItem newItem = contentDao.createCollection(root, calendar);

        for (int i = 1; i <= 5; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "cal"
                    + i + ".ics", "testuser");
            ContentItem newEvent = contentDao.createContent(calendar, event);
        }

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);
        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("Visible"));
        eventFilter.getPropFilters().add(propFilter);

        clearSession();

        // Should match ics.1
        Set<ContentItem> queryEvents = calendarDao.findEvents(calendar,
                filter);
        Assert.assertEquals(1, queryEvents.size());
        ContentItem nextItem = queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // Should match all
        eventFilter.setPropFilters(new ArrayList());
        queryEvents = calendarDao.findEvents(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        // should match three
        eventFilter.getPropFilters().add(propFilter);
        PropertyFilter propFilter2 = new PropertyFilter("SUMMARY");
        propFilter2.setTextMatchFilter(new TextMatchFilter("Physical"));
        eventFilter.getPropFilters().add(propFilter2);
        queryEvents = calendarDao.findEvents(calendar, filter);
        Assert.assertEquals(3, queryEvents.size());

        // should match everything except #1...so that means 4
//        eventFilter.getPropFilters().remove(propFilter2);
//        propFilter.getTextMatchFilter().setNegateCondition(true);
//        queryEvents = calendarDao.findEvents(calendar, filter);
//        Assert.assertEquals(4, queryEvents.size());

        // should match ics.1 again
//        propFilter.getTextMatchFilter().setNegateCondition(false);
//        propFilter.getTextMatchFilter().setCaseless(true);
//        propFilter.getTextMatchFilter().setValue("vISiBlE");
//        queryEvents = calendarDao.findEvents(calendar, filter);
//        Assert.assertEquals(1, queryEvents.size());
//        nextItem = (ContentItem) queryEvents.iterator().next();
//        Assert.assertEquals("test1.ics", nextItem.getName());

        // should match all 5 (none have rrules)
//        propFilter.setTextMatchFilter(null);
//        propFilter.setName("RRULE");
//        propFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
//        queryEvents = calendarDao.findEvents(calendar, filter);
//        Assert.assertEquals(5, queryEvents.size());

        // time range test
        eventFilter.setPropFilters(new ArrayList());
        DateTime start = new DateTime("20050817T115000");
        DateTime end = new DateTime("20050818T115000");
        start.setUtc(true);
        end.setUtc(true);

        Period period = new Period(start, end);
        TimeRangeFilter timeRange = new TimeRangeFilter(period);
        eventFilter.setTimeRangeFilter(timeRange);

        // should match ics.1
        queryEvents = calendarDao.findEvents(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // 10 year period
        start.setTime(new GregorianCalendar(1996, 1, 22).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);
        timeRange.setPeriod(period);

        // should match all now
        queryEvents = calendarDao.findEvents(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("Visible"));
        eventFilter.getPropFilters().add(propFilter);

        // should match ics.1
        queryEvents = calendarDao.findEvents(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        start.setTime(new GregorianCalendar(2006, 8, 6).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);

        timeRange.setPeriod(period);

        // should match none
        queryEvents = calendarDao.findEvents(calendar, filter);
        Assert.assertEquals(0, queryEvents.size());
    }
    
    public void testCalendarTimeRangeQuerying() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        calendar = contentDao.createCollection(root, calendar);

        // create 3 events, 2 of them recurring, one of those infinite
        NoteItem event = generateEvent("test1.ics", "eventwithtimezone1.ics", "testuser");
        NoteItem newEvent = (NoteItem) contentDao.createContent(calendar, event);
        event = generateEvent("test2.ics", "eventwithtimezone2.ics", "testuser");
        newEvent = (NoteItem) contentDao.createContent(calendar, event);
        event = generateEvent("test3.ics", "eventwithtimezone3.ics", "testuser");
        newEvent = (NoteItem) contentDao.createContent(calendar, event);

        // modification to infinite daily recurring eventwithtimezone3.ics
        event = generateEventException("mod.ics", "eventmodwithtimezone.ics", "testuser");
        event.setModifies(newEvent);
        newEvent = (NoteItem) contentDao.createContent(calendar, event);
        
        clearSession();

        // Should match all
        Set<ContentItem> queryEvents = calendarDao.findEvents(calendar, new DateTime("20070501T010000Z"), new DateTime("20070601T010000Z"));
        Assert.assertEquals(3, queryEvents.size());
        
        verifyItemNameInSet(queryEvents, "test1.ics");
        verifyItemNameInSet(queryEvents, "test2.ics");
        verifyItemNameInSet(queryEvents, "test3.ics");
        
        // Should match two
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20070515T010000Z"), new DateTime("20070518T010000Z"));
        Assert.assertEquals(2, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test1.ics");
        verifyItemNameInSet(queryEvents, "test3.ics");
        
        // Should match one
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20200517T010000Z"), new DateTime("20200518T010000Z"));
        Assert.assertEquals(1, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test3.ics");
        
        // Should match one
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20060501T010000Z"), new DateTime("20060601T010000Z"));
        Assert.assertEquals(1, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test3.ics");
        
        // Should match none
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20060401T010000Z"), new DateTime("20060501T010000Z"));
        Assert.assertEquals(0, queryEvents.size());
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private CollectionItem generateCalendar(String name, String owner) {
        CollectionItem calendar = new CollectionItem();
        calendar.setName(name);
        calendar.setOwner(getUser(userDao, owner));
        
        CalendarCollectionStamp ccs = new CalendarCollectionStamp();
        calendar.addStamp(ccs);
        
        ccs.setDescription("test description");
        ccs.setLanguage("en");

        HashSet<String> supportedComponents = new HashSet<String>();
        supportedComponents.add("VEVENT");
        ccs.setSupportedComponents(supportedComponents);
        
        return calendar;
    }

    private NoteItem generateEvent(String name, String file,
            String owner) throws Exception {
        NoteItem event = new NoteItem();
        event.setName(name);
        event.setOwner(getUser(userDao, owner));
       
        EventStamp evs = new EventStamp();
        event.addStamp(evs);
        evs.setCalendar(CalendarUtils.parseCalendar(helper.getBytes(baseDir + "/" + file)));
        event.setIcalUid(evs.getIcalUid());
        event.setBody(evs.getDescription());
        event.setDisplayName(evs.getSummary());
        
        return event;
    }
    
    private NoteItem generateEventException(String name, String file,
            String owner) throws Exception {
        NoteItem event = new NoteItem();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(getUser(userDao, owner));
       
        EventExceptionStamp evs = new EventExceptionStamp();
        event.addStamp(evs);
        evs.setCalendar(CalendarUtils.parseCalendar(helper.getBytes(baseDir + "/" + file)));
        event.setIcalUid(evs.getIcalUid());
        
        return event;
    }
    
    private Calendar getCalendar(ContentItem item) {
        EventStamp evs = (EventStamp) item.getStamp(EventStamp.class);
        return evs.getCalendar();
    }
    
    private void verifyItemNameInSet(Set<ContentItem> items, String name) {
        for(ContentItem item: items) {
            if(item.getName().equals(name))
                return;
        }
        
        Assert.fail("item " + name + " not in set");
    }

}
