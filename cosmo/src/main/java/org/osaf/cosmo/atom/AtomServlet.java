/*
 * Copyright 2005-2007 Open Source Applications Foundation
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.protocol.server.HttpResponse;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.impl.HttpServletRequestContext;
import org.apache.abdera.protocol.server.servlet.HttpResponseServletAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet which handles Atom Syndication Format and Atom Publishing
 * Protocol access to the server.
 */
public class AtomServlet extends HttpServlet implements AtomConstants {
    private static final Log log = LogFactory.getLog(AtomServlet.class);

    private static final String BEAN_SERVICE_CONTEXT =
        "atomServiceContext";

    private WebApplicationContext wac;
    private ServiceContext serviceContext;

    // Servlet methods

    protected void service(HttpServletRequest req,
                           HttpServletResponse res)
        throws ServletException, IOException {
        RequestContext requestContext =
            new HttpServletRequestContext(serviceContext, req);
        HttpResponse response = new HttpResponseServletAdapter(res);
        serviceContext.getRequestHandlerManager().get(requestContext).
            process(serviceContext, requestContext, response);
    }

    // GenericServlet methods

    /**
     * Loads the servlet context's <code>WebApplicationContext</code>
     * and wires up dependencies. If no
     * <code>WebApplicationContext</code> is found, dependencies must
     * be set manually (useful for testing).
     *
     * @throws ServletException if required dependencies are not found
     */
    public void init()
        throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getWebApplicationContext(getServletContext());

        if (wac != null) {
            serviceContext = (ServiceContext)
                getBean(BEAN_SERVICE_CONTEXT, ServiceContext.class);
        }

        if (serviceContext == null)
            throw new ServletException("serviceContext must not be null");
    }

    // our methods

    /** */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    /** */
    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    // private methods

    private Object getBean(String name, Class clazz)
        throws ServletException {
        try {
            return wac.getBean(name, clazz);
        } catch (BeansException e) {
            throw new ServletException("Error retrieving bean " + name +
                                       " of type " + clazz +
                                       " from web application context", e);
        }
    }
}
