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
package org.osaf.cosmo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockTicketPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.w3c.dom.Document;

/**
 * Base class for executing servlet tests in a mock servlet container.
 */
public abstract class BaseMockServletTestCase extends TestCase {
    private static final Log log =
        LogFactory.getLog(BaseMockServletTestCase.class);
    protected static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

    private MockSecurityManager securityManager;
    private MockServletContext servletContext;
    private MockServletConfig servletConfig;

    /**
     */
    protected void setUp() throws Exception {
        securityManager = new MockSecurityManager();
        servletContext = new MockServletContext();
        servletConfig = new MockServletConfig(servletContext);
    }

    /**
     */
    protected MockHttpServletRequest createMockRequest(String method,
                                                       String pathInfo) {
        MockHttpServletRequest request =
            new MockHttpServletRequest(servletContext, method,
                                       getServletPath() + pathInfo);
        request.setServletPath(getServletPath());
        request.setPathInfo(pathInfo);
        request.addHeader("Host", request.getServerName() + ":" +
                          request.getServerPort());
        return request;
    }

    /**
     */
    protected void logInUser(User user) {
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(user));
    }

    /**
     */
    protected void logInTicket(Ticket ticket) {
        securityManager.setUpMockSecurityContext(new MockTicketPrincipal(ticket));
    }

    /**
     */
    protected void sendXmlRequest(MockHttpServletRequest request,
                                  XmlSerializable thing)
        throws Exception {
        Document doc = BUILDER_FACTORY.newDocumentBuilder().newDocument();
        doc.appendChild(thing.toXml(doc));
        sendXmlRequest(request, doc);
    }

    /**
     */
    protected void sendXmlRequest(MockHttpServletRequest request,
                                  Document doc)
        throws Exception {
        OutputFormat format = new OutputFormat("xml", "UTF-8", true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.setNamespaces(true);
        serializer.asDOMSerializer().serialize(doc);
        request.setContentType("text/xml");
        request.setCharacterEncoding("UTF-8");
        // log.debug("content: " + new String(out.toByteArray()));
        request.setContent(out.toByteArray());;
    }

    /**
     */
    protected Document readXmlResponse(MockHttpServletResponse response)
        throws Exception {
        ByteArrayInputStream in =
            new ByteArrayInputStream(response.getContentAsByteArray());
        BUILDER_FACTORY.setNamespaceAware(true);
        return BUILDER_FACTORY.newDocumentBuilder().parse(in);
    }

    /** */
    protected String toAbsoluteUrl(MockHttpServletRequest request,
                                   String path) {
        StringBuffer url = new StringBuffer(request.getScheme());
        url.append("://").append(request.getServerName());
        if ((request.isSecure() && request.getServerPort() != 443) ||
            (request.getServerPort() != 80)) {
            url.append(":").append(request.getServerPort());
        }
        if (! request.getContextPath().equals("/")) {
            url.append(request.getContextPath());
        }
        url.append(path);
        return url.toString();
    }

    /**
     */
    public abstract String getServletPath();

    /**
     */
    public MockSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     */
    public MockServletContext getServletContext() {
        return servletContext;
    }

    /**
     */
    public MockServletConfig getServletConfig() {
        return servletConfig;
    }
}
