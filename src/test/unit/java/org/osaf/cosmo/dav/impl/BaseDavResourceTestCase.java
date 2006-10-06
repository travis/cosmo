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
package org.osaf.cosmo.dav.impl;

import junit.framework.TestCase;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.dav.impl.StandardDavResourceFactory;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardUserService;

/**
 * Base test case for <code>DavResourceBase</code> and subclasses.
 */
public class BaseDavResourceTestCase extends TestCase {
    private static final Log log =
        LogFactory.getLog(BaseDavResourceTestCase.class);

    protected MockSecurityManager securityManager;
    protected StandardContentService contentService;
    protected StandardUserService userService;
    protected TestHelper testHelper;
    protected StandardDavResourceFactory resourceFactory;
    protected LocatorFactoryImpl locatorFactory;
    protected NoOpDavSession session;
    protected User user;
    protected CollectionItem home;
    protected DavResourceLocator homeLocator;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        securityManager = new MockSecurityManager();

        MockCalendarDao calendarDao = new MockCalendarDao();
        MockContentDao contentDao = new MockContentDao();
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

        resourceFactory = new StandardDavResourceFactory();
        resourceFactory.setContentService(contentService);
        resourceFactory.setSecurityManager(securityManager);

        locatorFactory = new LocatorFactoryImpl("/");

        session = new NoOpDavSession();

        user = testHelper.makeDummyUser();
        userService.createUser(user);

        home = contentService.getRootItem(user);
        homeLocator = locatorFactory.createResourceLocator("", "/");
    }

    /** */
    protected void tearDown() throws Exception {
        userService.removeUser(user);

        userService.destroy();
        contentService.destroy();

        super.tearDown();
    }
}
