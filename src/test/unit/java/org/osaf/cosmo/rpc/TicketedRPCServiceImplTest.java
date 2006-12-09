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

import java.util.Date;
import java.util.HashSet;

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
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.CosmoDate;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockTicketPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.osaf.cosmo.service.account.AutomaticAccountActivator;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardUserService;

public class TicketedRPCServiceImplTest extends TestCase {
    private static final String TEST_CALENDAR_NAME = "RemoteCosmoService";
    private static final String TEST_CALENDAR_PATH = "RemoteCosmoServiceTest";
    private static final Log log = LogFactory.getLog(TicketedRPCServiceImplTest.class);

    private TestHelper testHelper;

    private Ticket readTicket;
    private Ticket writeTicket;
    private Ticket readWriteTicket;
    
    private User user;
    CollectionItem testCalendar;

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

        readTicket = testHelper.makeDummyTicket();
        
        user = testHelper.makeDummyUser();
        
        storage = new MockDaoStorage();

        contentDao = new MockContentDao(storage);
        calendarDao = new MockCalendarDao(storage);
        userDao = new MockUserDao();
        
        securityManager = new MockSecurityManager();
        securityManager.setUpMockSecurityContext(
                testHelper.makeDummyAnonymousPrincipal());
        

        contentService = new StandardContentService();
        contentService.setContentDao(contentDao);
        contentService.setCalendarDao(calendarDao);
        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.setAccountActivator(new AutomaticAccountActivator());
        userService.init();

        rpcService = new RPCServiceImpl();
        rpcService.setContentService(contentService);
        rpcService.setCosmoSecurityManager(securityManager);
        rpcService.setUserService(userService);

        testCalendar = testHelper.makeDummyCollection(user);
        
        contentDao.createRootItem(user);
        contentService.createCollection(
                contentService.getRootItem(user), testCalendar);
        
        HashSet<String> readPermissions = new HashSet<String>();
        readPermissions.add(Ticket.PRIVILEGE_READ);
        
        readTicket = testHelper.makeDummyTicket(60000);
        readTicket.setCreated(new Date());
        readTicket.setKey("readticket");
        readTicket.setPrivileges(readPermissions);
        
        HashSet<String> writePermissions = new HashSet<String>();
        writePermissions.add(Ticket.PRIVILEGE_WRITE);
        writeTicket = testHelper.makeDummyTicket(60000);
        writeTicket.setCreated(new Date());
        writeTicket.setKey("writeticket");
        writeTicket.setPrivileges(writePermissions);
        
        HashSet<String> readWritePermissions = new HashSet<String>();
        readWritePermissions.add(Ticket.PRIVILEGE_WRITE);
        readWritePermissions.add(Ticket.PRIVILEGE_WRITE);
        readWriteTicket = testHelper.makeDummyTicket(60000);
        readWriteTicket.setCreated(new Date());
        readWriteTicket.setKey("readwriteticket");
        readWriteTicket.setPrivileges(readWritePermissions);
        
        

        
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

    public void testTicketsOnCollections() throws Exception {
        Event evt0 = createOneHourEvent("Test Event 1", "event before range", 2006, CosmoDate.MONTH_JANUARY,2, 10);
        try{
            String id = rpcService.saveEvent(testCalendar.getUid(), evt0, writeTicket.getKey());
            fail("saveEvent succeeded without ticket on calendar.");
        } catch (RPCException e){
            // This should happen
        }
        testCalendar.addTicket(writeTicket);
        String id = rpcService.saveEvent(testCalendar.getUid(), evt0, writeTicket.getKey());
        try {
            Event evt1 = rpcService.getEvent(testCalendar.getUid(), id, readTicket.getKey());
            fail("getEvent succeeded without ticket on calendar.");
        } catch (RPCException e){
            // This should happen
        }
        
    }
    
    public void testGetEvents() throws Exception {

        testCalendar.addTicket(readTicket);
        testCalendar.addTicket(writeTicket);
                      
        Event evt0 = createOneHourEvent("Test Event 1", "event before range", 2006, CosmoDate.MONTH_JANUARY,2, 10);
        Event evt1 = createOneHourEvent("Test Event 2", "event2 in range", 2006, CosmoDate.MONTH_MARCH, 4, 11);
        Event evt2 = createOneHourEvent("Test Event 3", "event3 in range", 2006, CosmoDate.MONTH_APRIL, 5, 18);
        Event evt3 = createOneHourEvent("Test Event 4", "event4 after range", 2006, CosmoDate.MONTH_JUNE, 4, 9);
        
        rpcService.saveEvent(testCalendar.getUid(), evt0, writeTicket.getKey());
        rpcService.saveEvent(testCalendar.getUid(), evt1, writeTicket.getKey());
        rpcService.saveEvent(testCalendar.getUid(), evt2, writeTicket.getKey());
        rpcService.saveEvent(testCalendar.getUid(), evt3, writeTicket.getKey());
        long UTC_MAR_ONE = 1141200000000L;    // 3/1/2006 - 1141200000000
        long UTC_MAY_ONE = 1146466800000L;    // 5/1/2006 - 1146466800000
        Event events[] = rpcService.getEvents(testCalendar.getUid(), UTC_MAR_ONE, UTC_MAY_ONE, readTicket.getKey());
        try{
            events = rpcService.getEvents(testCalendar.getUid(), UTC_MAR_ONE, UTC_MAY_ONE, writeTicket.getKey());
            fail("getEvents succeeded without write permissions");
        } catch (RPCException e){
            //This should happen
        }
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
        
        testCalendar.addTicket(writeTicket);
        testCalendar.addTicket(readTicket);

        Event evt = createTestEvent();
        String id = rpcService.saveEvent(testCalendar.getUid(), evt, writeTicket.getKey());
        try {
            id = rpcService.saveEvent(testCalendar.getUid(), evt, readTicket.getKey());
            fail("saveEvent succeeded without write permissions.");
        } catch (RPCException e){
            // This should happen
        }
        assertNotNull(id);
    }

    public void testRemoveEvent() throws Exception {
        
        testCalendar.addTicket(writeTicket);
        testCalendar.addTicket(readTicket);
        
        Event evt = createTestEvent();
        String id = rpcService.saveEvent(testCalendar.getUid(), evt, writeTicket.getKey());

        Event evt1 = rpcService.getEvent(testCalendar.getUid(), id, readTicket.getKey());
        assertNotNull(evt1);
        try {
            rpcService.removeEvent(testCalendar.getUid(), id, readTicket.getKey());
            fail("removeEvent succeeded without write permissions.");
        } catch (RPCException e){
            // This is what should happen
        }
        
        Event evt2 = rpcService.getEvent(testCalendar.getUid(), id, readTicket.getKey());
        assertNotNull(evt2);
        
        evt1 = rpcService.getEvent(testCalendar.getUid(), id, readTicket.getKey());
        assertNotNull(evt1);
        rpcService.removeEvent(testCalendar.getUid(), id, writeTicket.getKey());
        evt2 = rpcService.getEvent(testCalendar.getUid(), id, readTicket.getKey());
        assertNull(evt2);

}

    public void testGetEvent() throws Exception {
        
        testCalendar.addTicket(readTicket);
        testCalendar.addTicket(writeTicket);
        
        Event evt = createTestEvent();
        String id = rpcService.saveEvent(testCalendar.getUid(), evt, writeTicket.getKey());

        Event evt1 = rpcService.getEvent(testCalendar.getUid(), id, readTicket.getKey());
        try{
            evt1 = rpcService.getEvent(testCalendar.getUid(), id, writeTicket.getKey());
            fail("getEvent succeeded without read permissions.");
        } catch (RPCException e){
            // This is what shoud happen.
        }
        evt.setId(evt1.getId()); // to pass equality test
        assertEquals(evt, evt1);
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
