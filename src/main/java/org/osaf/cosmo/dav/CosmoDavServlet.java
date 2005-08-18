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

import org.apache.jackrabbit.j2ee.SimpleWebdavServlet;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl;

import org.apache.log4j.Logger;

import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.impl.CosmoDavRequestImpl;
import org.osaf.cosmo.dav.impl.CosmoDavResourceImpl;
import org.osaf.cosmo.dav.impl.CosmoDavResponseImpl;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

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
    /**
     * The name of the Spring bean identifying the Cosmo security
     * manager
     */
    public static final String BEAN_SECURITY_MANAGER =
        "securityManager";

    private CosmoSecurityManager securityManager;
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
        setDavSessionProvider(sessionProvider);

        DavResourceFactory resourceFactory = (DavResourceFactory)
            getBean(BEAN_DAV_RESOURCE_FACTORY,
                    DavResourceFactory.class);
        setResourceFactory(resourceFactory);

        securityManager = (CosmoSecurityManager)
            getBean(BEAN_SECURITY_MANAGER, CosmoSecurityManager.class);
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
        CosmoDavRequestImpl cosmoRequest = new CosmoDavRequestImpl(request);
        CosmoDavResponseImpl cosmoResponse = new CosmoDavResponseImpl(response);
        CosmoDavResourceImpl cosmoResource = (CosmoDavResourceImpl) resource;
        cosmoResource.setBaseUrl(cosmoRequest.getBaseUrl());
        cosmoResource.setApplicationContext(wac);

        if (method > 0) {
            return super.execute(request, response, method, resource);
        }

        method = CosmoDavMethods.getMethodCode(request.getMethod());
        switch (method) {
        case CosmoDavMethods.DAV_MKCALENDAR:
            doMkCalendar(cosmoRequest, cosmoResponse, cosmoResource);
            break;
        case CosmoDavMethods.DAV_MKTICKET:
            doMkTicket(cosmoRequest, cosmoResponse, cosmoResource);
            break;
        case CosmoDavMethods.DAV_DELTICKET:
            doDelTicket(cosmoRequest, cosmoResponse, cosmoResource);
            break;
        default:
            // any other method
            return false;
        }

        return true;
    }

    /**
     */
    protected void doPut(WebdavRequest request,
                         WebdavResponse response,
                         DavResource resource)
        throws IOException, DavException {
        CosmoDavRequestImpl cosmoRequest = new CosmoDavRequestImpl(request);
        CosmoDavResponseImpl cosmoResponse = new CosmoDavResponseImpl(response);
        CosmoDavResourceImpl cosmoResource = (CosmoDavResourceImpl) resource;

        // caldav (section 4.6.2): honor "If-None-Match"
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifNoneMatch != null) {
            // fail if it's "*" and the resource exists at all
            if ((ifNoneMatch.equals("*") && resource.exists()) ||
                // fail if the resource exists and both etags are
                // strong and they match
                (resource.exists() &&
                 ! ifNoneMatch.startsWith("W/") &&
                 ! cosmoResource.getETag().startsWith("W/") &&
                 cosmoResource.getETag().equals(ifNoneMatch))) {
                response.sendError(DavServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
        }

        // caldav (section 4.6.2): honor "If-Match" header
        String ifMatch = request.getHeader("If-Match");
        if (ifMatch != null) {
            // fail if it's "*" and the resource does not exist
            if ((ifMatch.equals("*") && ! resource.exists()) ||
                // fail if the resource does not exist, one or both of
                // the etags are weak, or if the etags don't match
                (! resource.exists() ||
                 ifMatch.startsWith("W/") ||
                 cosmoResource.getETag().startsWith("W/") ||
                 ! cosmoResource.getETag().equals(ifMatch))) {
                response.sendError(DavServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
        }

        super.doPut(request, response, resource);

        // caldav (section 4.6.2): return ETag header
        if (cosmoResource.isCalendarResource() &&
            ! cosmoResource.getETag().equals("")) {
            response.setHeader("ETag", cosmoResource.getETag());
        }
    }

    /**
     * Executes the MKTICKET method
     *
     * @throws IOException
     * @throws DavException
     */
    protected void doMkCalendar(CosmoDavRequest request,
                                CosmoDavResponse response,
                                CosmoDavResource resource)
        throws IOException, DavException {
        WebdavRequest webdavRequest = request.getWebdavRequest();

        // resource must be null
        if (resource.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("cannot make calendar at " +
                          resource.getResourcePath() + ": resource exists");
            }
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // one or more intermediate collections must be created
        CosmoDavResource parentResource =
            (CosmoDavResource) resource.getCollection();
        if (parentResource == null ||
            ! parentResource.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("cannot make calendar at " +
                          resource.getResourcePath() +
                          ": one or more intermediate collections must be created");
            }
            response.sendError(DavServletResponse.SC_CONFLICT);
            return;
        }

        // parent resource must be a regular collection - calendar
        // collections are not allowed within other calendar
        // collections
        if (! parentResource.isCollection() ||
            parentResource.isCalendarCollection()) {
            if (log.isDebugEnabled()) {
                log.debug("cannot make calendar at " +
                          resource.getResourcePath() +
                          ": parent resource must be a regular collection");
            }
            response.sendError(DavServletResponse.SC_FORBIDDEN);
            return;
        }

        // we do not allow request bodies
        if (webdavRequest.getContentLength() > 0 ||
            webdavRequest.getHeader("Transfer-Encoding") != null) {
            if (log.isDebugEnabled()) {
                log.debug("cannot make calendar at " +
                          resource.getResourcePath() +
                          ": request body not allowed");
            }
            response.sendError(DavServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        // also could return INSUFFICIENT_STORAGE if we do not have
        // enough space for the collection, but how do we determine
        // that?
        
        if (log.isDebugEnabled()) {
            log.debug("adding calendar collection at " +
                      resource.getResourcePath());
        }
        parentResource.addCalendarCollection(resource);
        response.setStatus(DavServletResponse.SC_CREATED);
    }

    /**
     * Executes the MKTICKET method
     *
     * @throws IOException
     * @throws DavException
     */
    protected void doMkTicket(CosmoDavRequest request,
                              CosmoDavResponse response,
                              CosmoDavResource resource)
        throws IOException, DavException {
        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }

        Ticket ticket = null;
        try {
            ticket = request.getTicketInfo();
        } catch (IllegalArgumentException e) {
            response.sendError(DavServletResponse.SC_BAD_REQUEST,
                               e.getMessage());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("saving ticket for resource " +
                      resource.getResourcePath());
        }
        resource.saveTicket(ticket);

        response.sendMkTicketResponse(resource, ticket.getId());
    }

    /**
     * Executes the DELTICKET method
     *
     * @throws IOException
     * @throws DavException
     */
    protected void doDelTicket(CosmoDavRequest request,
                               CosmoDavResponse response,
                               CosmoDavResource resource)
        throws IOException, DavException {
        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }

        String ticketId = request.getTicketId();
        if (ticketId == null) {
            response.sendError(DavServletResponse.SC_BAD_REQUEST,
                               "No ticket was specified.");
            return;
        }
        Ticket ticket = resource.getTicket(ticketId);
        if (ticket == null) {
            response.sendError(DavServletResponse.SC_PRECONDITION_FAILED,
                               "The ticket specified does not exist.");
            return;
        }

        // must either be a root user or the user that created the
        // ticket
        String loggedInUsername =
            securityManager.getSecurityContext().getUser().getUsername();
        if (! (ticket.getOwner().equals(loggedInUsername) ||
               securityManager.getSecurityContext().inRootRole())) {
            response.sendError(DavServletResponse.SC_FORBIDDEN);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("removing ticket " + ticket.getId() + " for resource " +
                      resource.getResourcePath());
        }
        resource.removeTicket(ticket);

        response.sendDelTicketResponse(resource, ticket.getId());
    }

    // our methods

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

    // private methods

    private User getLoggedInUser() {
        return securityManager.getSecurityContext().getUser();
    }
}
