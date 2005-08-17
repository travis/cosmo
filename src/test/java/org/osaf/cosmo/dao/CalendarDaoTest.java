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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.osaf.commons.spring.jcr.JCRSessionFactory;
import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.UnsupportedFeatureException;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.icalendar.ICalendarUtils;
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
    private static final String SESSIONFACTORY_BEAN = "homedirSessionFactory";

    private CalendarDao dao;
    private JCRSessionFactory sessionFactory;

    /**
     */
    public CalendarDaoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = (CalendarDao) getAppContext().getBean(DAO_BEAN);
        sessionFactory = (JCRSessionFactory)
            getAppContext().getBean(SESSIONFACTORY_BEAN);
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

    public void testStoreCalendarObject() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        // create a calendar object containing an event
        // and a timezone
        Calendar calendar1 = TestHelper.makeDummyCalendar();
        VEvent event1 = TestHelper.makeDummyEvent();
        calendar1.getComponents().add(event1);
        calendar1.getComponents().add(VTimeZone.getDefault());

        // store the calendar object in the repository
        Session session = sessionFactory.getSession();
        String name =
            ICalendarUtils.getSummary(event1).getValue() + ".ics";
        Node resource = session.getRootNode().addNode(name);
        dao.storeCalendarObject(resource, calendar1);
        session.save();
        session.logout();
    }

    public void testStoreEmptyCalendarObject() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        // create an empty calendar object
        Calendar calendar1 = TestHelper.makeDummyCalendar();

        try {
            // try to store the calendar object in the repository
            Session session = sessionFactory.getSession();
            Node resource = session.getRootNode().addNode("empty");
            dao.storeCalendarObject(resource, calendar1);
            session.save();
            session.logout();
            fail("should not have been able to create empty calendar object");
        } catch (UnsupportedFeatureException e) {
            // expected
        }
    }
}
