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
import java.util.Set;

import junit.framework.Assert;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.IsNotDefinedFilter;
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
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.osaf.cosmo.model.hibernate.HibCollectionItem;
import org.osaf.cosmo.model.hibernate.HibEventExceptionStamp;
import org.osaf.cosmo.model.hibernate.HibEventStamp;
import org.osaf.cosmo.model.hibernate.HibNoteItem;

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

        // test update
        queryItem.setName("test2");
        ccs.setLanguage("es");
        ccs.setDescription("test description2");

        contentDao.updateCollection(queryItem);
        Assert.assertNotNull(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ccs = (CalendarCollectionStamp) queryItem.getStamp(CalendarCollectionStamp.class);
        
        Assert.assertEquals("test2", queryItem.getName());
        Assert.assertEquals("es", ccs.getLanguage());
        Assert.assertEquals("test description2", ccs.getDescription());

        // test add event
        ContentItem event = generateEvent("test.ics", "cal1.ics",
                "testuser");
        
        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ContentItem newEvent = contentDao.createContent(calendar, event);
        
        clearSession();

        // test query event
        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        EventStamp evs = (EventStamp) queryEvent.getStamp(EventStamp.class);
        
        Assert.assertEquals("test.ics", queryEvent.getName());
        Assert.assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());

        // test update event
        queryEvent.setName("test2.ics");
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes("cal2.ics")));
        
        queryEvent = contentDao.updateContent(queryEvent);

        Calendar cal = evs.getEventCalendar();
        
        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        evs = (EventStamp) queryEvent.getStamp(EventStamp.class);
        
        Assert.assertEquals("test2.ics", queryEvent.getName());
        Assert.assertEquals(evs.getEventCalendar().toString(), cal.toString());
        

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
        
        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());

        // Should match ics.1
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar,
                filter);
        Assert.assertEquals(1, queryEvents.size());
        ContentItem nextItem = queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // Should match all
        eventFilter.setPropFilters(new ArrayList());
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        // should match three
        PropertyFilter propFilter2 = new PropertyFilter("SUMMARY");
        propFilter2.setTextMatchFilter(new TextMatchFilter("Physical"));
        eventFilter.getPropFilters().add(propFilter2);
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(3, queryEvents.size());

        // should match everything except #1...so that means 4
        eventFilter.getPropFilters().remove(propFilter2);
        eventFilter.getPropFilters().add(propFilter);
        propFilter.getTextMatchFilter().setNegateCondition(true);
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(4, queryEvents.size());

        // should match ics.1 again
        propFilter.getTextMatchFilter().setNegateCondition(false);
        propFilter.getTextMatchFilter().setValue("vISiBlE");
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // should match all 5 (none have rrules)
        propFilter.setTextMatchFilter(null);
        propFilter.setName("RRULE");
        propFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        // time range test
        eventFilter.setPropFilters(new ArrayList());
        DateTime start = new DateTime("20050817T115000Z");
        DateTime end = new DateTime("20050818T115000Z");
        
        Period period = new Period(start, end);
        TimeRangeFilter timeRange = new TimeRangeFilter(period);
        eventFilter.setTimeRangeFilter(timeRange);

        // should match ics.1
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // 10 year period
        start.setTime(new GregorianCalendar(1996, 1, 22).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);
        timeRange.setPeriod(period);

        // should match all now
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("Visible"));
        eventFilter.getPropFilters().add(propFilter);

        // should match ics.1
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        start.setTime(new GregorianCalendar(2006, 8, 6).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);

        timeRange.setPeriod(period);

        // should match none
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
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
        event.setUid("test3uid");
        newEvent = (NoteItem) contentDao.createContent(calendar, event);
        

        // modification to infinite daily recurring eventwithtimezone3.ics
        event = generateEventException("mod.ics", "eventmodwithtimezone.ics", "testuser");
        event.setModifies(newEvent);
        newEvent = (NoteItem) contentDao.createContent(calendar, event);
        
        clearSession();

        // Should match all except modification
        Set<ContentItem> queryEvents = calendarDao.findEvents(calendar, new DateTime("20070501T010000Z"), new DateTime("20070601T010000Z"), false);
        Assert.assertEquals(3, queryEvents.size());
        
        verifyItemNameInSet(queryEvents, "test1.ics");
        verifyItemNameInSet(queryEvents, "test2.ics");
        verifyItemNameInSet(queryEvents, "test3.ics");
        
        // Should match two
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20070515T010000Z"), new DateTime("20070518T010000Z"), false);
        Assert.assertEquals(2, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test1.ics");
        verifyItemNameInSet(queryEvents, "test3.ics");
        
        // Should match one (the infinite recurring event)
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20200517T010000Z"), new DateTime("20200518T010000Z"), false);
        Assert.assertEquals(1, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test3.ics");
        
        // Should match the modification, so the results should include the 
        // modification and the master
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20060501T010000Z"), new DateTime("20060601T010000Z"), false);
        Assert.assertEquals(2, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test3.ics");
        verifyItemNameInSet(queryEvents, "mod.ics");
        
        // Should match none
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20060401T010000Z"), new DateTime("20060501T010000Z"), false);
        Assert.assertEquals(0, queryEvents.size());
        
        // Test expand
        // Should match the modification, so the results should include the modification
        // and the master (but no occurences because the only occurrence is the mod)
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20060501T010000Z"), new DateTime("20060601T010000Z"), true);
        Assert.assertEquals(2, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test3.ics");
        verifyItemNameInSet(queryEvents, "mod.ics");
        
        // Should match 10 occurences in the future, so the results should include
        // the master plus 10 occurence items
        queryEvents = calendarDao.findEvents(calendar, new DateTime("20200517T010000Z"), new DateTime("20200527T010000Z"), true);
        Assert.assertEquals(11, queryEvents.size());
        verifyItemNameInSet(queryEvents, "test3.ics");
        verifyUidInSet(queryEvents, "test3uid:20200517T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200518T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200519T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200520T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200521T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200522T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200523T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200524T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200525T081500Z");
        verifyUidInSet(queryEvents, "test3uid:20200526T081500Z");
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private CollectionItem generateCalendar(String name, String owner) {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setName(name);
        calendar.setOwner(getUser(userDao, owner));
        
        CalendarCollectionStamp ccs = new HibCalendarCollectionStamp();
        calendar.addStamp(ccs);
        
        ccs.setDescription("test description");
        ccs.setLanguage("en");
        
        return calendar;
    }

    private NoteItem generateEvent(String name, String file,
            String owner) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setOwner(getUser(userDao, owner));
       
        EventStamp evs = new HibEventStamp();
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes(file)));
        event.setIcalUid(evs.getIcalUid());
        if(evs.getEvent().getDescription()!=null)
            event.setBody(evs.getEvent().getDescription().getValue());
        
        if(evs.getEvent().getSummary()!=null)
            event.setDisplayName(evs.getEvent().getSummary().getValue());
        
        return event;
    }
    
    private NoteItem generateEventException(String name, String file,
            String owner) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(getUser(userDao, owner));
       
        EventExceptionStamp evs = new HibEventExceptionStamp();
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes(file)));
        
        return event;
    }
    
    private Calendar getCalendar(ContentItem item) {
        return new EntityConverter(null).convertContent(item);
    }
    
    private void verifyItemNameInSet(Set<ContentItem> items, String name) {
        for(ContentItem item: items) {
            if(name.equals(item.getName()))
                return;
        }
        
        Assert.fail("item " + name + " not in set");
    }
    
    private void verifyUidInSet(Set<ContentItem> items, String uid) {
        for(ContentItem item: items) {
            if(uid.equals(item.getUid()))
                return;
        }
        
        Assert.fail("uid " + uid + " not in set");
    }

}
