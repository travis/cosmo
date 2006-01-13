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

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.cmp.CmpServlet;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.impl.StandardUserService;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Base class for CMP servlet test cases.
 */
public abstract class BaseCmpServletTestCase extends TestCase {
    private static final Log log =
        LogFactory.getLog(BaseCmpServletTestCase.class);

    private static final String SERVLET_PATH = "/cmp";

    protected TestHelper testHelper;
    protected UserService userService;
    protected CosmoSecurityManager securityManager;
    protected MockServletContext servletContext;
    protected CmpServlet servlet;

    /**
     */
    protected void setUp() throws Exception {
        userService = createMockUserService();
        userService.init();

        securityManager = new MockSecurityManager();

        testHelper = new TestHelper();

        servletContext = new MockServletContext();

        servlet = new CmpServlet();
        servlet.setUserService(userService);
        servlet.setSecurityManager(securityManager);
        servlet.init(new MockServletConfig(servletContext));
   }

    private UserService createMockUserService() {
        StandardUserService svc = new StandardUserService();
        svc.setUserDao(new MockUserDao());
        svc.setPasswordGenerator(new SessionIdGenerator());
        return svc;
    }

    /**
     */
    protected MockHttpServletRequest createMockRequest(String method,
                                                       String cmpPath) {
        MockHttpServletRequest request =
            new MockHttpServletRequest(servletContext, method,
                                       SERVLET_PATH + cmpPath);
        request.setServletPath(SERVLET_PATH);
        request.setPathInfo(cmpPath);
        return request;
    }

    /**
     */
    protected Document readXmlResponse(MockHttpServletResponse response)
        throws Exception {
        ByteArrayInputStream in =
            new ByteArrayInputStream(response.getContentAsByteArray());
        SAXBuilder builder = new SAXBuilder(false);
        return builder.build(in);
    }
}
