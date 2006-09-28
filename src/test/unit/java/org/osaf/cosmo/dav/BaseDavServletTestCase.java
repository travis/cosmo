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
package org.osaf.cosmo.dav;

import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.jackrabbit.webdav.lock.SimpleLockManager;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.osaf.cosmo.BaseMockServletTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.dav.DavServlet;
import org.osaf.cosmo.dav.impl.NoOpDavSessionProvider;
import org.osaf.cosmo.dav.impl.StandardDavResourceFactory;
import org.osaf.cosmo.model.User;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.w3c.dom.Element;

/**
 * Base class for WebDAV+extensions servlet test cases.
 */
public abstract class BaseDavServletTestCase extends BaseMockServletTestCase {
    private static final Log log =
        LogFactory.getLog(BaseDavServletTestCase.class);

    private static final String SERVLET_PATH = "/home";

    protected TestHelper testHelper;
    protected DavServlet servlet;
    protected StandardContentService contentService;
    protected StandardUserService userService;
    protected User user;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

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

        StandardDavResourceFactory resourceFactory =
            new StandardDavResourceFactory();
        resourceFactory.setContentService(contentService);
        resourceFactory.setSecurityManager(getSecurityManager());

        LocatorFactoryImpl locatorFactory =
            new LocatorFactoryImpl(getServletPath());

        NoOpDavSessionProvider sessionProvider = new NoOpDavSessionProvider();

        servlet = new DavServlet();
        servlet.setSecurityManager(getSecurityManager());
        servlet.setLockManager(new SimpleLockManager());
        servlet.setResourceFactory(resourceFactory);
        servlet.setLocatorFactory(locatorFactory);
        servlet.setDavSessionProvider(sessionProvider);
        servlet.init(getServletConfig());

        user = testHelper.makeDummyUser();
        userService.createUser(user);
    }

    /** */
    protected void tearDown() throws Exception {
        userService.removeUser(user);

        servlet.destroy();

        userService.destroy();
        contentService.destroy();

        super.tearDown();
    }

    /** */
    protected MultiStatus
        readMultiStatusResponse(MockHttpServletResponse response)
        throws Exception {
        return MultiStatus.createFromXml(readXmlResponse(response));
    }

    /** */
    protected Element findProp(MultiStatus ms,
                               String href,
                               int code,
                               String name,
                               Namespace ns)
        throws Exception {
        MultiStatus.MultiStatusResponse msr = ms.findResponse(href);
        if (msr == null)
            throw new Exception("no response for href " + href);

        MultiStatus.PropStat ps = msr.findPropStat(code);
        if (ps == null)
            throw new Exception("no " + code + " propstat");

        return ps.findProp(name, ns);
    }

    /** */
    protected Element findProp(MultiStatus ms,
                               String href,
                               String name,
                               Namespace ns)
        throws Exception {
        return findProp(ms, href, MockHttpServletResponse.SC_OK, name, ns);
    }

    /** */
    public String getServletPath() {
        return SERVLET_PATH;
    }

    /** */
    public String toCanonicalPath(String relativePath) {
        StringBuffer buf = new StringBuffer("/");
        buf.append(user.getUsername());
        if (! relativePath.startsWith("/"))
            buf.append("/");
        buf.append(relativePath);
        return buf.toString();
    }
}
