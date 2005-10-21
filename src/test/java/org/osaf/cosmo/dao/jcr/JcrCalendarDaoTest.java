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
import org.osaf.cosmo.icalendar.RecurrenceException;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Test Case for {@link JcrCalendarDao}.
 */
public class JcrCalendarDaoTest extends BaseJcrDaoTestCase {
    private static final Log log = LogFactory.getLog(JcrCalendarDaoTest.class);

    private JcrCalendarDao dao;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        dao = new JcrCalendarDao();
        dao.setSessionFactory(getSessionFactory());

        try {
            dao.init();
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    /**
     */
    protected void tearDown() throws Exception {
        try {
            dao.destroy();
        } finally {
            dao = null;
            super.tearDown();
        }
    }

    /**
     */
    public void testStoreCalendarObject() throws Exception {
        Calendar c1 = getTestHelper().makeDummyCalendarWithEvent();
        Node n1 = getTestHelper().addNode();

        dao.storeCalendarObject(n1.getPath(), c1);
        Calendar calendar = getTestHelper().findDummyCalendar(n1);
        assertNotNull("Calendar object not stored", calendar);
    }

    /**
     */
    public void testStoreCalendarObjectOnNonExistentNode() throws Exception {
        Calendar c1 = getTestHelper().makeDummyCalendarWithEvent();

        try {
            dao.storeCalendarObject("/dead/beef", c1);
            fail("Calendar stored on nonexistent node");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testStoreCalendarObjectOnProperty() throws Exception {
        Calendar c1 = getTestHelper().makeDummyCalendarWithEvent();
        Node node = getTestHelper().addNode();
        Property property = getTestHelper().addProperty(node);

        try {
            dao.storeCalendarObject(property.getPath(), c1);
            fail("Calendar stored on property");
        } catch (InvalidDataAccessResourceUsageException e) {
            // expected
        }
    }

    /**
     */
    public void testGetCalendarObject() throws Exception {
        Node n1 = getTestHelper().addNode();
        Calendar c1 = getTestHelper().makeAndStoreDummyCalendar(n1);

        Calendar calendar = dao.getCalendarObject(n1.getPath());
        assertNotNull("Calendar null", calendar);
    }


    /**
     */
    public void testGetCalendarObjectOnNonExistentNode() throws Exception {
        try {
            dao.getCalendarObject("/dead/beef");
            fail("Calendar found for on nonexistent node");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testGetCalendarObjectOnProperty() throws Exception {
        Node node = getTestHelper().addNode();
        Property property = getTestHelper().addProperty(node);

        try {
            dao.getCalendarObject(property.getPath());
            fail("Calendar found for property");
        } catch (InvalidDataAccessResourceUsageException e) {
            // expected
         }
    }
}
