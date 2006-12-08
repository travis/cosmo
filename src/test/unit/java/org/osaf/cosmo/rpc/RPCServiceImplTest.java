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
package org.osaf.cosmo.rpc;

import junit.framework.TestCase;

import net.fortuna.ical4j.model.DateTime;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.CosmoDate;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.osaf.cosmo.service.account.AutomaticAccountActivator;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardUserService;

public class RPCServiceImplTest extends TestCase {
    private static final String TEST_CALENDAR_NAME = "RemoteCosmoService";
    
    private static final Log log = LogFactory.getLog(RPCServiceImplTest.class);

    private TestHelper testHelper;

    private User user;
    private String testCalendarUid;

    private MockDaoStorage storage;

    private MockContentDao contentDao;
    private MockCalendarDao calendarDao;
    private MockUserDao userDao;
    private MockSecurityManager securityManager;

    private StandardContentService contentService;
    private StandardUserService userService;

    private RPCServiceImpl rpcService;

    private org.osaf.cosmo.rpc.model.Calendar[] calendars;

    public void setUp(){
        testHelper = new TestHelper();

        user = testHelper.makeDummyUser();
        

        storage = new MockDaoStorage();

        contentDao = new MockContentDao(storage);
        calendarDao = new MockCalendarDao(storage);
        userDao = new MockUserDao();

        securityManager = new MockSecurityManager();
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(user));

        contentService = new StandardContentService();
        contentService.setContentDao(contentDao);
        contentService.setCalendarDao(calendarDao);
        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.setAccountActivator(new AutomaticAccountActivator());
        userService.init();
        userService.createUser(user);

        rpcService = new RPCServiceImpl();
        rpcService.setContentService(contentService);
        rpcService.setCosmoSecurityManager(securityManager);
        rpcService.setUserService(userService);

        try {
            rpcService.createCalendar(TEST_CALENDAR_NAME);
            calendars = rpcService.getCalendars();
            testCalendarUid = calendars[0].getUid();
            
        } catch (RPCException e) {
            log.info(e);
        }
    }
    protected Event createOneHourEvent(String title, String description, int year, int month, int date, int hour) {
        Event evt = new Event();
        evt.setTitle(title);

        CosmoDate start = new CosmoDate();
        start.setYear(year);
        start.setMonth(month);
        start.setDate(date);
        start.setHours(hour);
        start.setMinutes(0);
        start.setSeconds(0);

        evt.setStart(start);

        CosmoDate end = new CosmoDate();
        end.setYear(year);
        end.setMonth(month);
        end.setDate(date);
        end.setHours(hour+1);
        end.setMinutes(0);
        end.setSeconds(0);

        evt.setEnd(end);
        evt.setDescription(description);

        return evt;
    }

    protected Event createTestEvent() {
        return createOneHourEvent("Test Event", "A sample event", 2006, CosmoDate.MONTH_JANUARY, 2, 10);
    }

    public void testGetEvents() throws Exception {
        Event evt0 = createOneHourEvent("Test Event 1", "event before range", 2006, CosmoDate.MONTH_JANUARY,2, 10);
        Event evt1 = createOneHourEvent("Test Event 2", "event2 in range", 2006, CosmoDate.MONTH_MARCH, 4, 11);
        Event evt2 = createOneHourEvent("Test Event 3", "event3 in range", 2006, CosmoDate.MONTH_APRIL, 5, 18);
        Event evt3 = createOneHourEvent("Test Event 4", "event4 after range", 2006, CosmoDate.MONTH_JUNE, 4, 9);

        rpcService.saveEvent(testCalendarUid, evt0);
        rpcService.saveEvent(testCalendarUid, evt1);
        rpcService.saveEvent(testCalendarUid, evt2);
        rpcService.saveEvent(testCalendarUid, evt3);
        long UTC_MAR_ONE = 1141200000000L;    // 3/1/2006 - 1141200000000
        long UTC_MAY_ONE = 1146466800000L;    // 5/1/2006 - 1146466800000
        Event events[] = rpcService.getEvents(testCalendarUid, UTC_MAR_ONE, UTC_MAY_ONE);

        CalendarFilter calendarFilter = calendarDao.getLastCalendarFilter();
        ComponentFilter filter = (ComponentFilter)calendarFilter.getFilter().getComponentFilters().get(0);

        DateTime start = new DateTime(1141200000000L);
        start.setUtc(true);
        DateTime end   = new DateTime(1146466800000L);
        end.setUtc(true);

        TimeRangeFilter timeRangeFilter = filter.getTimeRangeFilter();
        assertEquals(start.toString(),timeRangeFilter.getUTCStart());
        assertEquals(end.toString(), timeRangeFilter.getUTCEnd());
    }

    public void testSaveEvent() throws Exception {
        Event evt = createTestEvent();
        String id = rpcService.saveEvent(testCalendarUid, evt);
        assertNotNull(id);
    }

    public void testRemoveEvent() throws Exception {
        Event evt = createTestEvent();
        String id = rpcService.saveEvent(testCalendarUid, evt);
        Event evt1 = rpcService.getEvent(testCalendarUid, id);
        assertNotNull(evt1);
        rpcService.removeEvent(testCalendarUid, id);
        Event evt2 = rpcService.getEvent(testCalendarUid, id);
        assertNull(evt2);
    }

    public void testGetEvent() throws Exception {
        Event evt = createTestEvent();
        String id = rpcService.saveEvent(testCalendarUid, evt);

        Event evt1 = rpcService.getEvent(testCalendarUid, id);
        evt.setId(evt1.getId()); // to pass equality test
        assertEquals(evt, evt1);
    }

    public void testMoveEvent() throws Exception {
        log.info("testMoveEvent not implemented because MoveEvent is not implemented");
        assertTrue(true);
    }

    public void testGetCalendars() throws Exception {
        rpcService.createCalendar(TEST_CALENDAR_NAME+"1");
        calendars = rpcService.getCalendars();
        assertEquals(calendars.length, 2);
        rpcService.removeCalendar(testCalendarUid);
        calendars = rpcService.getCalendars();
        assertEquals(calendars.length, 1);
    }

    public void testCreateCalendar() throws Exception {
        boolean found = false;
        for (Calendar c : calendars) {
            log.info(c.getUid());
            if (c.getUid().equals(testCalendarUid)) {
                found = true;
            }
        }
        if (found) {
            assertTrue(true);
        } else {
            fail("Calendar not created");
        }
    }

    public void testRemoveCalendar() throws Exception {
        int initialNumberOfCalendars = calendars.length;
        rpcService.removeCalendar(testCalendarUid);
        calendars = rpcService.getCalendars();
        assertEquals(calendars.length, initialNumberOfCalendars - 1);
    }

/* XXX Uncomment once we add preferences to the Cosmo data model
 *     public void testGetPreference() throws Exception {
        rpcService.removePreference("testPreference");
        String result = rpcService.getPreference("testPreference");
        assertNull(result);
        rpcService.setPreference("testPreference", "value");
        result = rpcService.getPreference("testPreference");
        assertEquals(result,"value");
    }

    public void testSetPreference() throws Exception {
        rpcService.setPreference("testPreference", "value");
        String result = rpcService.getPreference("testPreference");
        assertEquals(result,"value");
    }

    public void testRemovePreference() throws Exception {
        rpcService.removePreference("testPreference");
    }*/

}
