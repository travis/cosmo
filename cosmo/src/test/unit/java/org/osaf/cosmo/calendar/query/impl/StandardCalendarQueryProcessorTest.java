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
package org.osaf.cosmo.calendar.query.impl;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.calendar.query.impl.StandardCalendarQueryProcessor;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.FreeBusyItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockEntityFactory;

/**
 * Test StandardCalendarQueryProcessorTest using mock implementations.
 *
 */
public class StandardCalendarQueryProcessorTest extends TestCase {

    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockEntityFactory factory;
    private MockDaoStorage storage;
    private TestHelper testHelper;
    private StandardCalendarQueryProcessor queryProcessor;

    protected static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    
    protected final String CALENDAR_UID = "calendaruid";

    public StandardCalendarQueryProcessorTest() {
        super();
    }
    
    @Override
    protected void setUp() throws Exception {
        testHelper = new TestHelper();
        factory = new MockEntityFactory();
        storage = new MockDaoStorage();
        contentDao = new MockContentDao(storage);
        queryProcessor = new StandardCalendarQueryProcessor();
        calendarDao = new MockCalendarDao(storage);
        queryProcessor.setCalendarDao(calendarDao);
     
        User user = testHelper.makeDummyUser();
        CollectionItem root = contentDao.createRootItem(user);
        
        CollectionItem calendar = generateCalendar("testcalendar", user);
        calendar.setUid(CALENDAR_UID);
        calendar.setName(calendar.getUid());
          
        contentDao.createCollection(root, calendar);
        
        for (int i = 1; i <= 3; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "eventwithtimezone"
                    + i + ".ics", user);
            event.setUid(CALENDAR_UID + i);
            contentDao.createContent(calendar, event);
        }
        
        FreeBusyItem fb = generateFreeBusy("test4.ics", "vfreebusy.ics", user);
        fb.setUid(CALENDAR_UID + "4");
        contentDao.createContent(calendar, fb);
    }
    
    public void testAddBusyPeriodsRecurringAllDay() throws Exception {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        // the range
        DateTime start = new DateTime("20070103T090000Z");
        DateTime end = new DateTime("20070117T090000Z");
        
        Period fbRange = new Period(start, end);
        
        Calendar calendar = CalendarUtils.parseCalendar(testHelper.getBytes("allday_weekly_recurring.ics"));
        
        // test several timezones
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        
        queryProcessor.addBusyPeriods(calendar, tz, fbRange, busyPeriods, busyTentativePeriods, busyUnavailablePeriods);
        
        Assert.assertEquals("20070108T060000Z/20070109T060000Z,20070115T060000Z/20070116T060000Z", busyPeriods.toString());
        
        busyPeriods.clear();
        
        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        queryProcessor.addBusyPeriods(calendar, tz, fbRange, busyPeriods, busyTentativePeriods, busyUnavailablePeriods);
        
        Assert.assertEquals("20070108T080000Z/20070109T080000Z,20070115T080000Z/20070116T080000Z", busyPeriods.toString());
        
        busyPeriods.clear();
        
        tz = TIMEZONE_REGISTRY.getTimeZone("Australia/Sydney");
        queryProcessor.addBusyPeriods(calendar, tz, fbRange, busyPeriods, busyTentativePeriods, busyUnavailablePeriods);
        
        Assert.assertEquals("20070107T130000Z/20070108T130000Z,20070114T130000Z/20070115T130000Z", busyPeriods.toString());
    }

    public void testFreeBusyQuery() throws Exception {
        DateTime start = new DateTime("20070507T051500Z");
        DateTime end = new DateTime("200705016T051500Z");
        
        Period period = new Period(start, end);
        
        CollectionItem calendar = contentDao.findCollectionByUid(CALENDAR_UID);
        
        // verify we get resuts from VEVENTS in collection
        VFreeBusy vfb = queryProcessor.freeBusyQuery(calendar, period);
        
        verifyPeriods(vfb, null, "20070508T081500Z/20070508T091500Z,20070509T081500Z/20070509T091500Z,20070510T081500Z/20070510T091500Z,20070511T081500Z/20070511T091500Z,20070512T081500Z/20070512T091500Z,20070513T081500Z/20070513T091500Z,20070514T081500Z/20070514T091500Z,20070515T081500Z/20070515T091500Z");
        verifyPeriods(vfb, FbType.BUSY_TENTATIVE, "20070508T101500Z/20070508T111500Z,20070515T101500Z/20070515T111500Z");
       
        // verify we get resuts from VFREEBUSY in collection
        start = new DateTime("20060101T051500Z");
        end = new DateTime("20060105T051500Z");
        
        period = new Period(start, end);
        
        vfb = queryProcessor.freeBusyQuery(calendar, period);
        
        verifyPeriods(vfb, null, "20060103T100000Z/20060103T120000Z,20060104T100000Z/20060104T120000Z");
        verifyPeriods(vfb, FbType.BUSY_TENTATIVE, "20060102T100000Z/20060102T120000Z");
        verifyPeriods(vfb, FbType.BUSY_UNAVAILABLE, "20060105T010000Z/20060105T020000Z");
    }
    
    
    private User getUser(UserDao userDao, String username) {
        return testHelper.makeDummyUser(username, username);
    }

    private CollectionItem generateCalendar(String name, User owner) {
        CollectionItem calendar = factory.createCollection();
        calendar.setOwner(owner);
        
        CalendarCollectionStamp ccs =
            factory.createCalendarCollectionStamp(calendar);
        
        calendar.addStamp(ccs);
        return calendar;
    }

    private NoteItem generateEvent(String name, String file,
            User owner) throws Exception {
        NoteItem event = factory.createNote();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(owner);
       
        EventStamp evs = factory.createEventStamp(event);
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(testHelper.getBytes(file)));
       
        return event;
    }
    
    private FreeBusyItem generateFreeBusy(String name, String file,
            User owner) throws Exception {
        FreeBusyItem fb = factory.createFreeBusy();
        fb.setName(name);
        fb.setDisplayName(name);
        fb.setOwner(owner);
        fb.setFreeBusyCalendar(CalendarUtils.parseCalendar(testHelper.getBytes(file)));
        
        return fb;
    }
    
    private void verifyPeriods(VFreeBusy vfb, FbType fbtype, String periods) {
        PropertyList props = vfb.getProperties(Property.FREEBUSY);
        FreeBusy fb = null;
        
        for(Iterator it = props.iterator();it.hasNext();) {
            FreeBusy next = (FreeBusy) it.next();
            FbType type = (FbType) next.getParameter(Parameter.FBTYPE);
            if(type==null && fbtype==null) {
                fb = next;
            }
            else if(type != null && type.equals(fbtype)) {
                fb = next;
            }
        }
        
        if(fb==null)
            Assert.fail("periods " + periods + " not in " + vfb.toString());
        
        Assert.assertEquals(periods, fb.getPeriods().toString());
    }
}