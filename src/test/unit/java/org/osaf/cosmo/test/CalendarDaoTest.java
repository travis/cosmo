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
package org.osaf.cosmo.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.IsNotDefinedFilter;
import org.osaf.cosmo.calendar.query.PropertyFilter;
import org.osaf.cosmo.calendar.query.TextMatchFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DuplicateEventUidException;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.User;

public class CalendarDaoTest extends HibernateDaoTestCase {

	CalendarDao calendarDao = null;
    ContentDao contentDao = null;
	UserDao userDao = null;
	
	public CalendarDaoTest()
	{
		calendarDao = (CalendarDao) ctx.getBean("calendarDao");
		userDao = (UserDao) ctx.getBean("userDao");
        contentDao = (ContentDao) ctx.getBean("contentDao");
	}
    
    protected void setUp() throws Exception
    {
        super.setUp();
        // clean DB
        removeAllUsers(userDao);
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
	public void testCalendarDaoBasic() throws Exception
	{
		CalendarCollectionItem calendar = new CalendarCollectionItem();
		calendar.setName("test");
		calendar.setOwner(getUser(userDao, "testuser"));
		calendar.setDescription("test description");
		calendar.setLanguage("en");
		
		HashSet supportedComponents = new HashSet();
		supportedComponents.add("VEVENT");
		calendar.setSupportedComponents(supportedComponents);
		

		CalendarCollectionItem newItem = calendarDao.createCalendar(calendar);
		
        clearSession();
        
        CalendarCollectionItem queryItem = calendarDao.findCalendarByUid(calendar.getUid());
		
        Assert.assertNotNull(queryItem);
		Assert.assertEquals("test", queryItem.getName());
		Assert.assertEquals("en", queryItem.getLanguage());
		Assert.assertEquals("test description", queryItem.getDescription());
		Assert.assertEquals("VEVENT", (String) queryItem.getSupportedComponents().iterator().next());
		
		// test update
		queryItem.setName("test2");
		queryItem.setLanguage("es");
		queryItem.setDescription("test description2");
		supportedComponents = new HashSet();
		supportedComponents.add("VTODO");
		queryItem.setSupportedComponents(supportedComponents);
		
		calendarDao.updateCalendar(queryItem);
		Assert.assertNotNull(queryItem);
		
        clearSession();
        
        queryItem = calendarDao.findCalendarByUid(calendar.getUid());
		Assert.assertEquals("test2", queryItem.getName());
		Assert.assertEquals("es", queryItem.getLanguage());
		Assert.assertEquals("test description2", queryItem.getDescription());
		Assert.assertEquals("VTODO", (String) queryItem.getSupportedComponents().iterator().next());
		
		// test add event
		CalendarEventItem event = new CalendarEventItem();
		event.setName("test.ics");
		event.setOwner(getUser(userDao, "testuser"));
		event.setContent(getBytes(baseDir + "/cal1.ics"));
		event.setContentEncoding("UTF8");
		event.setContentType("text/calendar");
		event.setContentLanguage("en");
		
		CalendarEventItem newEvent = calendarDao.addEvent(calendar, event);
		
        clearSession();
        
		// test query event
		CalendarEventItem queryEvent = calendarDao.findEventByUid(newEvent.getUid());
		Assert.assertEquals("test.ics", queryEvent.getName());
		Assert.assertEquals("text/calendar", queryEvent.getContentType());
		Assert.assertEquals("UTF8", queryEvent.getContentEncoding());
		Assert.assertEquals("en", queryEvent.getContentLanguage());
		
		verifyInputStream(new FileInputStream(baseDir + "/cal1.ics"), queryEvent.getContent());
		
		// test update event
		queryEvent.setName("test2.ics");
		queryEvent.setContentEncoding("UTF16");
		queryEvent.setContentLanguage("es");
		queryEvent.setContentType("text/ical");
		queryEvent.setContent(getBytes(baseDir + "/cal2.ics"));
		
		queryEvent = calendarDao.updateEvent(queryEvent);
		
        clearSession();
        
        queryEvent = calendarDao.findEventByUid(queryEvent.getUid());
		
		Assert.assertEquals("test2.ics", queryEvent.getName());
		Assert.assertEquals("text/ical", queryEvent.getContentType());
		Assert.assertEquals("UTF16", queryEvent.getContentEncoding());
		Assert.assertEquals("es", queryEvent.getContentLanguage());
		
		verifyInputStream(new FileInputStream(baseDir + "/cal2.ics"), queryEvent.getContent());
	
		// test delete
		calendarDao.removeEvent(queryEvent);
		
        clearSession();
        
		queryEvent = calendarDao.findEventByUid(queryEvent.getUid());
		Assert.assertNull(queryEvent);
		
        queryItem = calendarDao.findCalendarByUid(queryItem.getUid());
		calendarDao.removeCalendar(queryItem);
        
        clearSession();
        
		queryItem = calendarDao.findCalendarByUid(queryItem.getUid());
		Assert.assertNull(queryItem);
	}
	
    public void testDuplicateEventUid() throws Exception
    {
        CalendarCollectionItem calendar = new CalendarCollectionItem();
        calendar.setName("test");
        calendar.setOwner(getUser(userDao, "testuser"));
        calendar.setDescription("test description");
        calendar.setLanguage("en");
        calendar = calendarDao.createCalendar(calendar);
        
        CalendarEventItem event = new CalendarEventItem();
        event.setName("test.ics");
        event.setOwner(getUser(userDao, "testuser"));
        event.setContent(getBytes(baseDir + "/cal1.ics"));
        event.setContentEncoding("UTF8");
        event.setContentType("text/calendar");
        event.setContentLanguage("en");
        
        event = calendarDao.addEvent(calendar, event);
        
        CalendarEventItem event2 = new CalendarEventItem();
        event2.setName("testduplicate.ics");
        event2.setOwner(getUser(userDao, "testuser"));
        event2.setContent(getBytes(baseDir + "/cal1.ics"));
        event2.setContentEncoding("UTF8");
        event2.setContentType("text/calendar");
        event2.setContentLanguage("en");
        
        try {
            event2 = calendarDao.addEvent(calendar, event2);
            Assert.fail("able to create event with duplicat uid");
        } catch(DuplicateEventUidException e) {}
    }
    
    public void testFindByEventIcalUid() throws Exception
    {
        CalendarCollectionItem calendar = new CalendarCollectionItem();
        calendar.setName("test");
        calendar.setOwner(getUser(userDao, "testuser"));
        calendar.setDescription("test description");
        calendar.setLanguage("en");
        calendar = calendarDao.createCalendar(calendar);
        
        CalendarEventItem event = new CalendarEventItem();
        event.setName("test.ics");
        event.setOwner(getUser(userDao, "testuser"));
        event.setContent(getBytes(baseDir + "/cal1.ics"));
        event.setContentEncoding("UTF8");
        event.setContentType("text/calendar");
        event.setContentLanguage("en");
        
        event = calendarDao.addEvent(calendar, event);
        
        clearSession();
        
        calendar = calendarDao.findCalendarByUid(calendar.getUid());
        String uid = "68ADA955-67FF-4D49-BBAC-AF182C620CF6";
        CalendarEventItem queryEvent = calendarDao.findEventByIcalUid(uid, calendar);
        Assert.assertNotNull(queryEvent);
        Assert.assertEquals(event.getUid(), queryEvent.getUid());
    }
    
	public void testCalendarDaoAdvanced() throws Exception
	{
		CalendarCollectionItem calendar = new CalendarCollectionItem();
		calendar.setName("test");
		calendar.setOwner(getUser(userDao, "testuser"));
		calendar.setDescription("test description");
		calendar.setLanguage("en");
		
		HashSet supportedComponents = new HashSet();
		supportedComponents.add("VEVENT");
		
		calendar.setSupportedComponents(supportedComponents);
		
		
		calendar = calendarDao.createCalendar(calendar);
		
		CalendarCollectionItem calendar2 = new CalendarCollectionItem();
		calendar2.setName("test");
		calendar2.setOwner(getUser(userDao,"testuser"));
		calendar2.setDescription("test description");
		calendar2.setLanguage("en");
		calendar2.setSupportedComponents(supportedComponents);
		
		try
		{
			calendarDao.createCalendar(calendar2);
			Assert.fail("shouldn't be able to create duplicate calendar");
		}
		catch(DuplicateItemNameException dine)
		{
		}
		
		calendar2.setOwner(getUser(userDao,"testuser2"));
		calendar2 = calendarDao.createCalendar(calendar2);
		
		// test add events
		CalendarEventItem event = new CalendarEventItem();
		event.setName("test.ics");
		event.setOwner(getUser(userDao, "testuser"));
		event.setContent(getBytes(baseDir + "/cal1.ics"));
		event.setContentEncoding("UTF8");
		event.setContentType("text/calendar");
		event.setContentLanguage("en");
		
		CalendarEventItem newEvent = calendarDao.addEvent(calendar, event);
		
		CalendarEventItem event2 = new CalendarEventItem();
		event2.setName("test2.ics");
		event2.setOwner(getUser(userDao, "testuser"));
		event2.setContent(getBytes(baseDir + "/cal2.ics"));
		event2.setContentEncoding("UTF8");
		event2.setContentType("text/calendar");
		event2.setContentLanguage("en");
		
		CalendarEventItem newEvent2 = calendarDao.addEvent(calendar, event2);
		
        clearSession();
        
		// test query by path
		CalendarCollectionItem queryCollection = calendarDao.findCalendarByPath("/testuser2/test");
		Assert.assertNotNull(queryCollection);
		Assert.assertEquals(calendar2.getUid(), queryCollection.getUid());
		
		CalendarEventItem queryEvent = calendarDao.findEventByPath("/testuser/test/test.ics");
		Assert.assertNotNull(queryEvent);
		Assert.assertEquals(event.getUid(), queryEvent.getUid());
	
		// test get by criteria
//		HashMap criteria = new HashMap();
//		criteria.put("icalendar:vcalendar-vevent_description", "12:10 w/ Chris (113.138.4504)");
//		criteria.put("icalendar:vcalendar-vevent_class", "PRIVATE");
//		criteria.put("icalendar:vcalendar-vevent_summary", "Visible Changes @ Memorial City");
//		
//		Set<CalendarEventItem> queryEvents = calendarDao.findEvents(calendar, criteria);
//		Assert.assertEquals(1, queryEvents.size());
//		CalendarEventItem nextItem = queryEvents.iterator().next();
//		Assert.assertEquals(newEvent.getUid(), nextItem.getUid());
//		verifyInputStream(new FileInputStream(baseDir + "/cal1.ics"), nextItem.getContent());
		
		
        calendar = calendarDao.findCalendarByUid(calendar.getUid());
        calendar2 = calendarDao.findCalendarByUid(calendar2.getUid());
        
		// test delete
		calendarDao.removeCalendar(calendar);
		calendarDao.removeCalendar(calendar2);
		
        clearSession();
        
		queryEvent = calendarDao.findEventByPath("/testuser/test/test.ics");
		Assert.assertNull(queryEvent);
		
		queryEvent = calendarDao.findEventByUid(event.getUid());
		Assert.assertNull(queryEvent);
	}
    
    public void testCalendarEventStamping() throws Exception
    {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        CalendarCollectionItem calendar = new CalendarCollectionItem();
        calendar.setName("test");
        calendar.setOwner(user);
        calendar.setDescription("test description");
        calendar.setLanguage("en");
        
        HashSet supportedComponents = new HashSet();
        supportedComponents.add("VEVENT");
        calendar.setSupportedComponents(supportedComponents);
       
        CalendarCollectionItem newCalendar = calendarDao.createCalendar(calendar);
    
        ContentItem item1 = new ContentItem();
        item1.setName("test.ics");
        item1.setOwner(getUser(userDao, "testuser"));
        item1.setContent(getBytes(baseDir + "/cal1.ics"));
        item1.setContentEncoding("UTF8");
        item1.setContentType("text/calendar");
        item1.setContentLanguage("en");
        
        ContentItem item2 = new ContentItem();
        item2.setName("test2.ics");
        item2.setOwner(getUser(userDao, "testuser"));
        item2.setContent(getBytes(baseDir + "/cal1.ics"));
        item2.setContentEncoding("UTF8");
        item2.setContentType("text/calendar");
        item2.setContentLanguage("en");
        
        ContentItem newItem1 = contentDao.createContent(root, item1);
        ContentItem newItem2 = contentDao.createContent(calendar, item2);
        
        try {
            calendarDao.addEvent(item1);
            Assert.fail("able to convert content that is not part of calendar");
        } catch(IllegalArgumentException e) {}
        
        CalendarEventItem newEvent = calendarDao.addEvent(newItem2);
        Assert.assertNotNull(newEvent);
        
        clearSession();
        
        CalendarEventItem queryEvent = calendarDao.findEventByUid(newEvent.getUid());
        Assert.assertNotNull(queryEvent);
    }
	
	public void testCalendarQuerying() throws Exception
	{
		CalendarCollectionItem calendar = new CalendarCollectionItem();
		calendar.setName("test");
		calendar.setOwner(getUser(userDao, "testuser"));
		calendar.setDescription("test description");
		calendar.setLanguage("en");
		
		HashSet supportedComponents = new HashSet();
		supportedComponents.add("VEVENT");
		
		calendar.setSupportedComponents(supportedComponents);
		
		calendar = calendarDao.createCalendar(calendar);
	
		for(int i=1;i<=5;i++)
		{
			CalendarEventItem event = new CalendarEventItem();
			event.setName("test" + i +".ics");
			event.setOwner(getUser(userDao, "testuser"));
			event.setContent(getBytes(baseDir + "/cal" + i + ".ics"));
			event.setContentEncoding("UTF8");
			event.setContentType("text/calendar");
			event.setContentLanguage("en");
			CalendarEventItem newEvent = calendarDao.addEvent(calendar, event);
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
		Set<CalendarEventItem> queryEvents =
                    calendarDao.findEvents(calendar, filter);
		Assert.assertEquals(1, queryEvents.size());
		CalendarEventItem nextItem = queryEvents.iterator().next();
		Assert.assertEquals("test1.ics", nextItem.getName());
		
		// Should match all
		eventFilter.setPropFilters(new ArrayList());
		queryEvents = calendarDao.findEvents(calendar, filter);
		Assert.assertEquals(5, queryEvents.size());
		
		// should match four
		eventFilter.getPropFilters().add(propFilter);
		PropertyFilter propFilter2 = new PropertyFilter("SUMMARY");
		propFilter2.setTextMatchFilter(new TextMatchFilter("Physical"));
		eventFilter.getPropFilters().add(propFilter2);
		queryEvents = calendarDao.findEvents(calendar, filter);
		Assert.assertEquals(4, queryEvents.size());
		
		// should match everything except #1...so that means 4
		eventFilter.getPropFilters().remove(propFilter2);
		propFilter.getTextMatchFilter().setNegateCondition(true);
		queryEvents = calendarDao.findEvents(calendar, filter);
		Assert.assertEquals(4, queryEvents.size());
		
		// should match ics.1 again
		propFilter.getTextMatchFilter().setNegateCondition(false);
		propFilter.getTextMatchFilter().setCaseless(true);
		propFilter.getTextMatchFilter().setValue("vISiBlE");
		queryEvents = calendarDao.findEvents(calendar, filter);
		Assert.assertEquals(1, queryEvents.size());
		nextItem = (CalendarEventItem) queryEvents.iterator().next();
		Assert.assertEquals("test1.ics", nextItem.getName());
		
		// should match all 5 (none have rrules)
		propFilter.setTextMatchFilter(null);
		propFilter.setName("RRULE");
		propFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
		queryEvents = calendarDao.findEvents(calendar, filter);
		Assert.assertEquals(5, queryEvents.size());
		
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
		nextItem = (CalendarEventItem) queryEvents.iterator().next();
		Assert.assertEquals("test1.ics", nextItem.getName());
		
		// 10 year period
		start.setTime(new GregorianCalendar(1996,1,22).getTimeInMillis());
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
		nextItem = (CalendarEventItem) queryEvents.iterator().next();
		Assert.assertEquals("test1.ics", nextItem.getName());
		
		start.setTime(new GregorianCalendar(2006,8,6).getTimeInMillis());
		end.setTime(System.currentTimeMillis());
		period = new Period(start, end);
		
		timeRange.setPeriod(period);
		
		// should match none
		queryEvents = calendarDao.findEvents(calendar, filter);
		Assert.assertEquals(0, queryEvents.size());
		
	}
	
    private User getUser(UserDao userDao, String username)
    {
        return getUser(userDao,contentDao,username);
    }
	
}
