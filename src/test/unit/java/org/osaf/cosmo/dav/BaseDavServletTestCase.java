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

import org.osaf.cosmo.BaseMockServletTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.dav.DavServlet;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockUserPrincipal;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Base class for WebDAV+extensions servlet test cases.
 */
public abstract class BaseDavServletTestCase extends BaseMockServletTestCase {
    private static final Log log =
        LogFactory.getLog(BaseDavServletTestCase.class);

    private static final String SERVLET_PATH = "/home";

    protected TestHelper testHelper;
    protected DavServlet servlet;
    protected UserService userService;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        userService = createMockUserService();
        userService.init();

        testHelper = new TestHelper();

        servlet = new DavServlet();
        servlet.setSecurityManager(getSecurityManager());
        servlet.init(getServletConfig());
    }

    protected void tearDown() throws Exception {
        servlet.destroy();
        super.tearDown();
    }

    /**
     */
    protected void sendXmlRequest(MockHttpServletRequest request,
                                  byte[] xml)
        throws Exception {
        request.setContentType("text/xml");
        request.setCharacterEncoding("UTF-8");
        request.setContent(xml);
    }

    /**
     */
    protected MultiStatus
        readMultiStatusResponse(MockHttpServletResponse response)
        throws Exception {
        return MultiStatus.createFromXml(readXmlResponse(response));
    }

    /**
     */
    public String getServletPath() {
        return SERVLET_PATH;
    }

    /**
     */
    private UserService createMockUserService() {
        StandardUserService svc = new StandardUserService();
        svc.setUserDao(new MockUserDao());
        svc.setPasswordGenerator(new SessionIdGenerator());
        return svc;
    }
}
