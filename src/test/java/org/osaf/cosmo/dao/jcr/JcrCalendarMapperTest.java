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
package org.osaf.cosmo.dao.jcr;

import java.util.Date;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import net.fortuna.ical4j.data.*;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.parameter.*;
import net.fortuna.ical4j.model.property.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.UnsupportedCalendarObjectException;
import org.osaf.cosmo.icalendar.DuplicateUidException;
import org.osaf.cosmo.icalendar.RecurrenceException;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Test Case for {@link JcrCalendarMapper}.
 */
public class JcrCalendarMapperTest extends BaseJcrDaoTestCase {
    private static final Log log =
        LogFactory.getLog(JcrCalendarMapperTest.class);

    /**
     */
    public void testCalendarToNode() throws Exception {
        Calendar c1 = getTestHelper().makeDummyCalendarWithEvent();
        Node n1 = getTestHelper().addNode();

        JcrCalendarMapper.calendarToNode(c1, n1);
        Calendar calendar = getTestHelper().findDummyCalendar(n1);
        assertNotNull("Calendar object not stored", calendar);
        assertTrue("Node does not have event resource type",
                   n1.isNodeType(NT_CALDAV_EVENT_RESOURCE));
    }

    /**
     */
    public void testEmptyCalendarToNode() throws Exception {
        Calendar c1 = getTestHelper().makeDummyCalendar();
        Node n1 = getTestHelper().addNode();

        try {
            JcrCalendarMapper.calendarToNode(c1, n1);
            fail("Empty calendar stored");
        } catch (UnsupportedCalendarObjectException e) {
            // expected
        }
    }

    /**
     */
    public void testCalendarWithDuplicateUidToNode() throws Exception {
        Node n1 = getTestHelper().addNode();
        Calendar c1 = getTestHelper().makeAndStoreDummyCalendar(n1);
        Node n2 = getTestHelper().addNode();

        try {
            JcrCalendarMapper.calendarToNode(c1, n2);
            // expected
        } catch (DuplicateUidException e) {
            fail("Calendar with duplicate uid stored");
        }
    }

    /**
     */
    public void testSimpleEventToNode() throws Exception {
        Calendar c1 = getTestHelper().loadCalendar("event1.ics");
        Node n1 = getTestHelper().addNode();

        JcrCalendarMapper.calendarToNode(c1, n1);
        Calendar calendar = getTestHelper().findDummyCalendar(n1);
        assertNotNull("Calendar object not stored", calendar);
    }

    /**
     */
    public void testEventWithAlarmToNode() throws Exception {
        Calendar c1 = getTestHelper().loadCalendar("event2.ics");
        Node n1 = getTestHelper().addNode();

        JcrCalendarMapper.calendarToNode(c1, n1);
        Calendar calendar = getTestHelper().findDummyCalendar(n1);
        assertNotNull("Calendar object not stored", calendar);
    }

    /**
     */
    public void testRecurringEventToNode() throws Exception {
        Calendar c1 = getTestHelper().loadCalendar("event3.ics");
        Node n1 = getTestHelper().addNode();

        JcrCalendarMapper.calendarToNode(c1, n1);
        Calendar calendar = getTestHelper().findDummyCalendar(n1);
        assertNotNull("Calendar object not stored", calendar);
    }

    /**
     */
    public void testCaldavCalendarToNode() throws Exception {
        Calendar c1 = getTestHelper().makeDummyCalendarWithEvent();
        Node n1 = getTestHelper().addCalendarCollectionNode();
        Node r1 = getTestHelper().addCalendarResourceNode(n1, c1);

        JcrCalendarMapper.calendarToNode(c1, r1);
        Calendar calendar = getTestHelper().findDummyCalendar(r1);
        assertNotNull("Calendar object not stored", calendar);
    }

    /**
     */
    public void testCaldavCalendarWithDuplicateUidToNode() throws Exception {
        Node n1 = getTestHelper().addCalendarCollectionNode();
        Calendar c1 = getTestHelper().makeAndStoreDummyCaldavCalendar(n1);
        // have to save the first node so that it's visible to the
        // query manager
        getTestHelper().getSession().save();

        Node r2 = getTestHelper().addCalendarResourceNode(n1, c1);

        try {
            JcrCalendarMapper.calendarToNode(c1, r2);
            fail("Calendar with duplicate uid stored");
        } catch (DuplicateUidException e) {
            // expected
        } finally {
            n1.remove();
            getTestHelper().getSession().save();
        }
    }

    /**
     */
    public void testNodeToCalendar() throws Exception {
        Node n1 = getTestHelper().addNode();
        Calendar c1 = getTestHelper().makeAndStoreDummyCalendar(n1);

        Calendar calendar = JcrCalendarMapper.nodeToCalendar(n1);
        assertNotNull("Calendar null", calendar);
    }

    /**
     */
    public void testNonEventResourceNodeToCalendar() throws Exception {
        Node n1 = getTestHelper().addNode();

        try {
            JcrCalendarMapper.nodeToCalendar(n1);
            fail("Non event resource node allowed");
        } catch (UnsupportedCalendarObjectException e) {
            // expected
        }
    }

    /**
     */
    public void testNodeToCaldavCalendar() throws Exception {
        Node n1 = getTestHelper().addCalendarCollectionNode();
        Calendar c1 = getTestHelper().makeAndStoreDummyCaldavCalendar(n1);

        Calendar calendar = JcrCalendarMapper.nodeToCalendar(n1);
        assertNotNull("Calendar null", calendar);
    }
}
