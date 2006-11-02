/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.cmp;

import org.apache.commons.id.random.SessionIdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.BaseMockServletTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.cmp.CmpServlet;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardUserService;

/**
 * Base class for CMP servlet test cases.
 */
public abstract class BaseCmpServletTestCase extends BaseMockServletTestCase {
    private static final Log log =
        LogFactory.getLog(BaseCmpServletTestCase.class);

    private static final String SERVLET_PATH = "/cmp";

    protected TestHelper testHelper;
    protected StandardContentService contentService;
    protected StandardUserService userService;
    protected CmpServlet servlet;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        MockDaoStorage storage = new MockDaoStorage();
        MockCalendarDao calendarDao = new MockCalendarDao(storage);
        MockContentDao contentDao = new MockContentDao(storage);
        MockUserDao userDao = new MockUserDao();

        contentService = new StandardContentService();
        contentService.setCalendarDao(calendarDao);
        contentService.setContentDao(contentDao);
        contentService.init();

        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.init();

        testHelper = new TestHelper();

        servlet = new CmpServlet();
        servlet.setContentService(contentService);
        servlet.setUserService(userService);
        servlet.setSecurityManager(getSecurityManager());
        servlet.init(getServletConfig());
    }

    /**
     */
    public String getServletPath() {
        return SERVLET_PATH;
    }
}
