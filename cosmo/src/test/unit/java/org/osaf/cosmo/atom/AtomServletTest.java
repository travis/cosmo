/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.atom;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.servlet.DefaultRequestHandlerManager;
import org.apache.abdera.protocol.server.util.RegexTargetResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.BaseMockServletTestCase;
import org.osaf.cosmo.atom.provider.StandardTargetResolver;
import org.osaf.cosmo.atom.mock.MockProvider;
import org.osaf.cosmo.atom.mock.MockProviderManager;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test case for {@link AtomServlet}.
 */
public class AtomServletTest extends BaseMockServletTestCase {
    private static final Log log = LogFactory.getLog(AtomServletTest.class);

    private AtomServlet servlet;

    public void testService() throws Exception {
        String uri = "/collection/deadbeef";

        MockProvider provider = (MockProvider)
            servlet.getServiceContext().getProviderManager().getProvider();
        provider.addCollection("deadbeef");

        MockHttpServletRequest request = createMockRequest("GET", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);
        assertEquals("Wrong response code " + response.getStatus(),
                     MockHttpServletResponse.SC_OK, response.getStatus());
    }

    public void testServiceException() throws Exception {
        String uri = "/collection/cafebebe";

        MockProvider provider = (MockProvider)
            servlet.getServiceContext().getProviderManager().getProvider();
        provider.setFailureMode(true);

        MockHttpServletRequest request = createMockRequest("GET", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);
        assertEquals("Wrong response code " + response.getStatus(),
                     MockHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     response.getStatus());
    }

    protected void setUp() throws Exception {
        super.setUp();

        StandardServiceContext serviceContext = new StandardServiceContext();
        serviceContext.setAbdera(new Abdera());
        serviceContext.setProviderManager(new MockProviderManager());
        serviceContext.
            setRequestHandlerManager(new DefaultRequestHandlerManager());
        serviceContext.setTargetResolver(new StandardTargetResolver());
        serviceContext.init();

        servlet = new AtomServlet();
        servlet.setServiceContext(serviceContext);
        servlet.init(getServletConfig());
    }

    protected void tearDown() throws Exception {
        servlet.destroy();

        super.tearDown();
    }

    public String getServletPath() {
        return "/atom";
    }
}
