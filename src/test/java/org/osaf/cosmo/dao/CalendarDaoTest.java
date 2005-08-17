/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.dao;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Summary;

import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.UnsupportedFeatureException;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.model.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO Test Case for {@link CalendarDao}.
 *
 * @author Brian Moseley
 */
public class CalendarDaoTest extends BaseCoreTestCase {
    private static final Log log = LogFactory.getLog(CalendarDaoTest.class);

    private static final String DAO_BEAN = "calendarDao";
    private CalendarDao dao = null;

    /**
     */
    public CalendarDaoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = (CalendarDao) getAppContext().getBean(DAO_BEAN);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dao = null;
    }

    public void testCDCalendar() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        User user = TestHelper.makeDummyUser();
        String path = "/";

        dao.createCalendar(path, user.getUsername());
        assertTrue(dao.existsCalendar(path + user.getUsername()));

        dao.deleteCalendar(path + user.getUsername());
        assertTrue(! dao.existsCalendar(path + user.getUsername()));
    }

    public void testCRDResource() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        String path = "/";

        // create a calendar
        Calendar calendar1 = TestHelper.makeDummyCalendar();

        // create an event
        VEvent event1 = TestHelper.makeDummyEvent();
        Summary summary = 
            (Summary) event1.getProperties().getProperty(Property.SUMMARY);
        String name = summary.getValue() + ".ics";
        calendar1.getComponents().add(event1);

        // add its timezone
        calendar1.getComponents().add(VTimeZone.getDefault());

        // create the resource in the repository
        dao.setCalendarResource(path, name, calendar1);

        // get the resource out of the repository
        Calendar calendar2 = dao.getCalendarResource(path + name);
        log.debug("calendar2:\n" + calendar2);

        // XXX: delete the event
    }

    public void testEmptyResource() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        String path = "/";

        // create a calendar
        Calendar calendar1 = TestHelper.makeDummyCalendar();

        // try to create the empty resource in the repository
        try {
            dao.setCalendarResource(path, "blah", calendar1);
            fail("should not have been able to create empty calendar resource");
        } catch (UnsupportedFeatureException e) {
            // expected
        }
    }
}
