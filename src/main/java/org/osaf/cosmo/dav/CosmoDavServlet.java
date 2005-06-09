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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.j2ee.SimpleWebdavServlet;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavResponse;

import org.apache.log4j.Logger;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * An extension of Jackrabbit's 
 * {@link org.apache.jackrabbit.server.simple.WebdavServlet} which
 * integrates the Spring Framework for configuring support objects.
 */
public class CosmoDavServlet extends SimpleWebdavServlet {
    private static final Logger log =
        Logger.getLogger(CosmoDavServlet.class);

    /** The name of the Spring bean identifying the servlet's
     * {@link org.apache.jackrabbit.webdav.DavResourceFactory}
     */
    public static final String BEAN_DAV_RESOURCE_FACTORY =
        "resourceFactory";
    /** The name of the Spring bean identifying the servlet's
     * {@link org.apache.jackrabbit.webdav.DavSessionProvider}
     */
    public static final String BEAN_DAV_SESSION_PROVIDER =
        "sessionProvider";

    private WebApplicationContext wac;

    /**
     * Load the servlet context's
     * {@link org.springframework.web.context.WebApplicationContext}
     * and look up support objects.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getRequiredWebApplicationContext(getServletContext());

        DavSessionProvider sessionProvider = (DavSessionProvider)
            getBean(BEAN_DAV_SESSION_PROVIDER,
                    DavSessionProvider.class);
        setSessionProvider(sessionProvider);

        DavResourceFactory resourceFactory = (DavResourceFactory)
            getBean(BEAN_DAV_RESOURCE_FACTORY,
                    DavResourceFactory.class);
        setResourceFactory(resourceFactory);
    }


    /**
     * Dispatch dav methods that jcr-server does not know about.
     *
     * @throws ServletException
     * @throws IOException
     * @throws DavException
     */
    protected boolean execute(WebdavRequest request,
                              WebdavResponse response,
                              int method,
                              DavResource resource)
            throws ServletException, IOException, DavException {
        if (method > 0) {
            return super.execute(request, response, method, resource);
        }

        method = CosmoDavMethods.getMethodCode(request.getMethod());
        switch (method) {
        case CosmoDavMethods.DAV_MKTICKET:
            doMkTicket(request, response, resource);
            break;
        case CosmoDavMethods.DAV_DELTICKET:
            doDelTicket(request, response, resource);
            break;
        default:
            // any other method
            return false;
        }

        return true;
    }

    /**
     * Executes the MKTICKET method
     *
     * @throws IOException
     * @throws DavException
     */
    protected void doMkTicket(WebdavRequest request,
                              WebdavResponse response,
                              DavResource resource)
        throws IOException, DavException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setIntHeader("Content-Length", 0);
    }

    /**
     * Executes the DELTICKET method
     *
     * @throws IOException
     * @throws DavException
     */
    protected void doDelTicket(WebdavRequest request,
                               WebdavResponse response,
                               DavResource resource)
        throws IOException, DavException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setIntHeader("Content-Length", 0);
    }

    /**
     * Looks up the bean with given name and class in the web
     * application context.
     *
     * @param name the bean's name
     * @param clazz the bean's class
     */
    protected Object getBean(String name, Class clazz)
        throws ServletException {
        try {
            return wac.getBean(name, clazz);
        } catch (BeansException e) {
            throw new ServletException("Error retrieving bean " + name +
                                       " of type " + clazz +
                                       " from web application context", e);
        }
    }

    /**
     */
    public WebApplicationContext getWebApplicationContext() {
        return wac;
    }
}
