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
package org.osaf.cosmo.cmp;

import org.apache.commons.id.random.SessionIdGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.BaseMockServletTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.cmp.CmpServlet;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.impl.StandardUserService;

/**
 * Base class for CMP servlet test cases.
 */
public abstract class BaseCmpServletTestCase extends BaseMockServletTestCase {
    private static final Log log =
        LogFactory.getLog(BaseCmpServletTestCase.class);

    private static final String SERVLET_PATH = "/cmp";

    protected TestHelper testHelper;
    protected UserService userService;
    protected CmpServlet servlet;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        userService = createMockUserService();
        userService.init();

        testHelper = new TestHelper();

        servlet = new CmpServlet();
        servlet.setUserService(userService);
        servlet.setSecurityManager(getSecurityManager());
        servlet.init(getServletConfig());
   }

    private UserService createMockUserService() {
        StandardUserService svc = new StandardUserService();
        svc.setUserDao(new MockUserDao());
        svc.setPasswordGenerator(new SessionIdGenerator());
        return svc;
    }

    /**
     */
    public String getServletPath() {
        return SERVLET_PATH;
    }
}
