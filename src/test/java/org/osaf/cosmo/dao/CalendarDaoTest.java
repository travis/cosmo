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

import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
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

    public void testCDCalendarCollection() throws Exception {
        User user = TestHelper.makeDummyUser();

        if (log.isDebugEnabled()) {
            log.debug("creating calendar collection /" + user.getUsername());
        }
        dao.createCalendarCollection("/", user.getUsername());
        assertTrue(dao.existsCalendarCollection("/" + user.getUsername()));

        if (log.isDebugEnabled()) {
            log.debug("deleting calendar collection /" + user.getUsername());
        }
        dao.deleteCalendarCollection("/" + user.getUsername());
        assertTrue(! dao.existsCalendarCollection("/" + user.getUsername()));
    }
}
