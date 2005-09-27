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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.osaf.commons.spring.jcr.JCRSessionFactory;
import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.UnsupportedFeatureException;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.icalendar.CosmoICalendarConstants;
import org.osaf.cosmo.icalendar.ICalendarUtils;
import org.osaf.cosmo.icalendar.DuplicateUidException;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
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

    private CalendarDao dao;
    private JCRSessionFactory sessionFactory;

    public void testCreateCalendarCollection() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        // create the calendar collection in the repository
        Session session = sessionFactory.getSession();
        Node root = session.getRootNode();
        dao.createCalendarCollection(root, "calendarcollection");
        session.save();
        session.logout();

    }

    public void testStoreAndGetCalendarObject() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }
        Session session = sessionFactory.getSession();

        // create a calendar object containing an event
        // and a timezone
        Calendar calendar1 = TestHelper.makeDummyCalendar();
        VEvent event1 = TestHelper.makeDummyEvent();
        calendar1.getComponents().add(event1);
        calendar1.getComponents().add(VTimeZone.getDefault());

        // store the calendar object in the repository
        String name =
            ICalendarUtils.getSummary(event1).getValue() + ".ics";
        Node resource = session.getRootNode().addNode(name);
        dao.storeCalendarObject(resource, calendar1);
        session.save();

        // get the calendar object from the repository
        Calendar calendar2 = dao.getCalendarObject(resource);
        assertTrue(calendar2.getComponents().size() == 2);

        session.logout();
    }

    public void testStoreEmptyCalendarObject() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }
        Session session = sessionFactory.getSession();

        // create an empty calendar object
        Calendar calendar1 = TestHelper.makeDummyCalendar();

        try {
            // try to store the calendar object in the repository
            Node resource = session.getRootNode().addNode("empty");
            dao.storeCalendarObject(resource, calendar1);
            fail("should not have been able to create empty calendar object");
        } catch (UnsupportedFeatureException e) {
            // expected
        } finally {
            session.logout();
        }
    }

    public void testStoreCalendarObjectInCalendarCollectionWithDuplicateUid()
        throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }
        Session session = sessionFactory.getSession();

        // create a calendar object containing an event
        // and a timezone
        Calendar calendar = TestHelper.makeDummyCalendar();
        VEvent event = TestHelper.makeDummyEvent();
        calendar.getComponents().add(event);
        calendar.getComponents().add(VTimeZone.getDefault());

        // make a calendar collection in the repository
        // XXX createCalendarCollection ought to return the created node
        String collectionName = "dupcal";
        dao.createCalendarCollection(session.getRootNode(), collectionName);
        Node collection = session.getRootNode().getNode(collectionName);

        // make a dav resource in the repository
        String summary = ICalendarUtils.getSummary(event).getValue();
        String name = summary +  ".ics";
        // XXX createICalendarResource should be in the dao even if it
        // isn't used by the dav server
        Node resource = createICalendarResource(collection, name, calendar);

        // store the calendar object with the dav resource
        dao.storeCalendarObject(resource, calendar);
        session.save();

        // make another dav resource in the repository
        name = summary + "-dup.ics";
        Node dupResource = createICalendarResource(collection, name, calendar);

        // now store the calendar with the second dav resource
        try {
            dao.storeCalendarObject(dupResource, calendar);
            fail("should not have been able to store calendar object with duplicate uid");
        } catch (DuplicateUidException e) {
            // expected
        } finally {
            session.logout();
        }
    }

    public void testStoreCalendarObjectWithDuplicateUid() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }
        Session session = sessionFactory.getSession();

        // create a calendar object containing an event
        // and a timezone
        Calendar calendar1 = TestHelper.makeDummyCalendar();
        VEvent event1 = TestHelper.makeDummyEvent();
        calendar1.getComponents().add(event1);
        calendar1.getComponents().add(VTimeZone.getDefault());

        // store the calendar object in the repository
        String name =
            ICalendarUtils.getSummary(event1).getValue() + ".ics";
        Node resource = session.getRootNode().addNode(name);
        dao.storeCalendarObject(resource, calendar1);
        session.save();

        // now store it again
        try {
            name = ICalendarUtils.getSummary(event1).getValue() + "-dup.ics";
            resource = session.getRootNode().addNode(name);
            dao.storeCalendarObject(resource, calendar1);
        } catch (DuplicateUidException e) {
            fail("should have been able to store calendar object with duplicate uid");
        } finally {
            session.logout();
        }
    }

    public void testStoreAndGetSimpleEvent()
        throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }
        Session session = sessionFactory.getSession();

        // load event1.ics
        String name = "event1.ics";
        InputStream in =
            getClass().getClassLoader().getResourceAsStream(name);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar1 = builder.build(in);

        // store the calendar object in the repository
        Node resource = session.getRootNode().addNode(name);
        dao.storeCalendarObject(resource, calendar1);
        session.save();

        // get the calendar object from the repository
        Calendar calendar2 = dao.getCalendarObject(resource);

        session.logout();
    }

    public void testStoreAndGetEventWithAlarm()
        throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }
        Session session = sessionFactory.getSession();

        // load event1.ics
        String name = "event2.ics";
        InputStream in =
            getClass().getClassLoader().getResourceAsStream(name);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar1 = builder.build(in);

        // store the calendar object in the repository
        Node resource = session.getRootNode().addNode(name);
        dao.storeCalendarObject(resource, calendar1);
        session.save();

        // get the calendar object from the repository
        Calendar calendar2 = dao.getCalendarObject(resource);

        session.logout();
    }

    public void testStoreAndGetRecurringEventWithExceptions()
        throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }
        Session session = sessionFactory.getSession();

        // load event1.ics
        String name = "event3.ics";
        InputStream in =
            getClass().getClassLoader().getResourceAsStream(name);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar1 = builder.build(in);

        // store the calendar object in the repository
        Node resource = session.getRootNode().addNode(name);
        dao.storeCalendarObject(resource, calendar1);
        session.save();

        // get the calendar object from the repository
        Calendar calendar2 = dao.getCalendarObject(resource);

        session.logout();
    }

    // helper methods

    /**
     */
    protected Node createICalendarResource(Node collection, String name,
                                           Calendar calendar)
        throws Exception {
        InputStream content =
            new ByteArrayInputStream(calendar.toString().getBytes("utf8"));
        return createDavResource(collection, name, content,
                                 CosmoICalendarConstants.CONTENT_TYPE, "utf8");
    }

    /**
     */
    protected Node createDavResource(Node collection, String name,
                                     InputStream content, String contentType,
                                     String contentCharset)
        throws Exception {
        Node resourceNode = collection.
            addNode(name, CosmoJcrConstants.NT_DAV_RESOURCE);
        resourceNode.setProperty(CosmoJcrConstants.NP_DAV_DISPLAYNAME, name);
        resourceNode.addMixin(CosmoJcrConstants.NT_TICKETABLE);
        Node contentNode =
            resourceNode.addNode(CosmoJcrConstants.NN_JCR_CONTENT,
                                 CosmoJcrConstants.NT_RESOURCE);
        contentNode.setProperty(CosmoJcrConstants.NN_JCR_DATA, content);
        contentNode.setProperty(CosmoJcrConstants.NN_JCR_MIMETYPE,
                                contentType);
        contentNode.setProperty(CosmoJcrConstants.NN_JCR_ENCODING,
                                contentCharset);
        contentNode.setProperty(CosmoJcrConstants.NN_JCR_LASTMODIFIED,
                                java.util.Calendar.getInstance());
        return resourceNode;
    }

    /**
     */
    public void setCalendarDao(CalendarDao calendarDao) {
        this.dao = calendarDao;
    }

    /**
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
