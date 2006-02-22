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
package org.osaf.cosmo.dav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.jcr.Node;
import javax.servlet.ServletContextEvent;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.osaf.cosmo.dao.jcr.JcrTestHelper;
import org.osaf.cosmo.dao.mock.MockTicketDao;
import org.osaf.cosmo.dav.CosmoDavServlet;
import org.osaf.cosmo.jackrabbit.JackrabbitTestSessionManager;
import org.osaf.cosmo.jackrabbit.query.TextFilterListener;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockUserPrincipal;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Base class for WebDAV+extensions servlet test cases.
 */
public abstract class BaseDavServletTestCase extends TestCase {
    private static final Log log =
        LogFactory.getLog(BaseDavServletTestCase.class);

    private static final String SERVLET_PATH = "/home";

    private static final String[] REPORT_EVENTS = {
        "reports/put/1.ics",
        "reports/put/2.ics",
        "reports/put/3.ics",
        "reports/put/4.ics",
        "reports/put/5.ics",
        "reports/put/6.ics",
        "reports/put/7.ics"
    };

    private JackrabbitTestSessionManager sessionManager;
    protected MockSecurityManager securityManager;
    protected JcrTestHelper testHelper;
    protected MockServletContext servletContext;
    protected CosmoDavServlet servlet;
    protected String testUri;

    /**
     */
    protected void setUp() throws Exception {
        // XXX: refactor CosmoDavServlet.init so that we can provide
        // our own mock session provider, locator factory, and
        // resource factory; then we never need to actually hit the
        // jcr repository, and we test this class in isolation rather
        // than depending on all those other classes
        sessionManager = new JackrabbitTestSessionManager();
        sessionManager.setUp();

        securityManager = new MockSecurityManager();

        testHelper = new JcrTestHelper(sessionManager.getSession());

        servletContext = new MockServletContext();

        // load special query language extension
        TextFilterListener listener = new TextFilterListener();
        listener.contextInitialized(new ServletContextEvent(servletContext));

        MockServletConfig servletConfig = new MockServletConfig(servletContext);
        servletConfig.
            addInitParameter(CosmoDavServlet.INIT_PARAM_RESOURCE_PATH_PREFIX,
                             "/home");
        servletConfig.
            addInitParameter(CosmoDavServlet.INIT_PARAM_RESOURCE_CONFIG,
                             "/resource-config.xml");

        servlet = new CosmoDavServlet();
        servlet.setSecurityManager(securityManager);
        servlet.setSessionFactory(sessionManager.getSessionFactory());
        servlet.setTicketDao(new MockTicketDao());
        servlet.init(servletConfig);

        // make a calendar collection to put events into
        Node calendarCollectionNode =
            testHelper.addCalendarCollectionNode(getName());

        // put all test events in the calendar collection
        for (int i=0; i<REPORT_EVENTS.length; i++) {
            Node resourceNode =
                testHelper.addCalendarResourceNode(calendarCollectionNode,
                                                   REPORT_EVENTS[i]);
        }

        // XXX: not sure why we have to save, since theoretically
        // testHelper and the servlet are using the same jcr session
        calendarCollectionNode.getParent().save();

        // XXX: URL-escape
        testUri = calendarCollectionNode.getPath();
    }

    protected void tearDown() throws Exception {
        servlet.destroy();

        sessionManager.tearDown();
    }

    /**
     */
    protected MockHttpServletRequest createMockRequest(String method,
                                                       String davPath) {
        MockHttpServletRequest request =
            new MockHttpServletRequest(servletContext, method,
                                       SERVLET_PATH + davPath);
        request.setServletPath(SERVLET_PATH);
        request.setPathInfo(davPath);
        return request;
    }

    /**
     */
    protected void logInUser(User user) {
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(user));
    }

    /**
     */
    protected void sendXmlRequest(MockHttpServletRequest request,
                                  Document doc)
        throws Exception {
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        outputter.output(doc, buf);
        request.setContentType("text/xml");
        request.setCharacterEncoding("UTF-8");
        request.setContent(buf.toByteArray());;
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
    protected Document readXmlResponse(MockHttpServletResponse response)
        throws Exception {
        ByteArrayInputStream in =
            new ByteArrayInputStream(response.getContentAsByteArray());
        SAXBuilder builder = new SAXBuilder(false);
        return builder.build(in);
    }

    /**
     */
    protected MultiStatus
        readMultiStatusResponse(MockHttpServletResponse response)
        throws Exception {
        return MultiStatus.createFromXml(readXmlResponse(response));
    }
}
