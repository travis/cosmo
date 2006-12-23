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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.AbstractWebdavServlet;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.CaldavRequest;
import org.osaf.cosmo.dav.caldav.report.FreeBusyReport;
import org.osaf.cosmo.dav.impl.StandardDavRequest;
import org.osaf.cosmo.dav.impl.StandardDavResponse;
import org.osaf.cosmo.dav.io.DavInputContext;
import org.osaf.cosmo.dav.ticket.TicketDavRequest;
import org.osaf.cosmo.dav.ticket.TicketDavResponse;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Extends jcr-server's <code>AbstractWebdavServlet</code> to
 * implement the methods for WebDAV and its extensions.
 */
public class DavServlet extends AbstractWebdavServlet
    implements CaldavConstants {
    private static final Log log = LogFactory.getLog(DavServlet.class);

    /**
     * The name of the Spring bean identifying the security manager.
     */
    public static final String BEAN_SECURITY_MANAGER = "securityManager";

    /**
     * The name of the Spring bean identifying the dav lock manager.
     */
    public static final String BEAN_LOCK_MANAGER = "davLockManager";

    /**
     * The name of the Spring bean identifying the dav resource factory.
     */
    public static final String BEAN_RESOURCE_FACTORY = "davResourceFactory";

    /**
     * The name of the Spring bean identifying the dav locator factory.
     */
    public static final String BEAN_LOCATOR_FACTORY = "davLocatorFactory";

    /**
     * The name of the Spring bean identifying the dav session provider.
     */
    public static final String BEAN_SESSION_PROVIDER = "davSessionProvider";

    private CosmoSecurityManager securityManager;
    private LockManager lockManager;
    private DavResourceFactory resourceFactory;
    private DavLocatorFactory locatorFactory;
    private DavSessionProvider sessionProvider;

    /**
     * Ensures that the servlet's dependent objects are ready for
     * service.
     *
     * Looks for a Spring web application context in the servlet
     * context. If one is found, then dependencies will be wired in
     * from it. For each dependency, if one has already been set by
     * outside code using accessors, then the corresponding Spring
     * bean will be ignored.
     *
     * @throws ServletException if a dependent object is not provided
     */
    public void init() throws ServletException {
        super.init();

        WebApplicationContext wac = WebApplicationContextUtils.
            getWebApplicationContext(getServletContext());

        if (wac != null) {
            if (securityManager == null) {
                securityManager = (CosmoSecurityManager)
                    getBean(wac, BEAN_SECURITY_MANAGER,
                            CosmoSecurityManager.class);
            }
            if (lockManager == null) {
                lockManager = (LockManager)
                    getBean(wac, BEAN_LOCK_MANAGER, LockManager.class);
            }
            if (resourceFactory == null) {
                resourceFactory = (DavResourceFactory)
                    getBean(wac, BEAN_RESOURCE_FACTORY,
                            DavResourceFactory.class);
            }
            if (locatorFactory == null) {
                locatorFactory = (DavLocatorFactory)
                    getBean(wac, BEAN_LOCATOR_FACTORY,
                            DavLocatorFactory.class);
            }
            if (sessionProvider == null) {
                sessionProvider = (DavSessionProvider)
                    getBean(wac, BEAN_SESSION_PROVIDER,
                            DavSessionProvider.class);
            }
        }

        if (securityManager == null) {
            throw new ServletException("no security manager provided");
        }
        if (lockManager == null) {
            throw new ServletException("no lock manager provided");
        }
        if (resourceFactory == null) {
            throw new ServletException("no resource factory provided");
        }
        if (locatorFactory == null) {
            throw new ServletException("no locator factory provided");
        }
        if (sessionProvider == null) {
            throw new ServletException("no session provider provided");
        }
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
        // method is 0 if jcr-server does not understand the method
        // (either it's an extension method or one that is not
        // supported

        if (method > 0) {
            return super.execute(request, response, method, resource);
        }

        method = CosmoDavMethods.getMethodCode(request.getMethod());
        switch (method) {
        case CosmoDavMethods.DAV_MKCALENDAR:
            doMkCalendar(request, response, resource);
            break;
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
     * Deletes a resource. Disallows home collections from being
     * deleted.
     */
    protected void doDelete(WebdavRequest request,
                            WebdavResponse response,
                            DavResource resource)
        throws IOException, DavException {
        if (((ExtendedDavResource) resource).isHomeCollection()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN,
                               "Home collection may not be deleted");
            return;
        }

        if (! resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }

        super.doDelete(request, response, resource);
    }

    /**
     * Copies a resource. Disallows home collections from being
     * copied.
     */
    protected void doCopy(WebdavRequest request,
                          WebdavResponse response,
                          DavResource resource)
        throws IOException, DavException {
        if (((ExtendedDavResource) resource).isHomeCollection()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN,
                               "Home collection may not be copied");
            return;
        }

        super.doCopy(request, response, resource);
    }

    /**
     * Moves a resource. Disallows home collections from being moved.
     */
    protected void doMove(WebdavRequest request,
                          WebdavResponse response,
                          DavResource resource)
        throws IOException, DavException {
        if (((ExtendedDavResource) resource).isHomeCollection()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN,
                               "Home collection may not be moved");
            return;
        }

        super.doMove(request, response, resource);
    }

    /**
     * Posts to a resource. Collections may not be posted to.
     */
    protected void doPost(WebdavRequest request,
                          WebdavResponse response,
                          DavResource resource)
        throws IOException, DavException {
        if (resource.isCollection()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN,
                               "Collections may not be posted to.");
            return;
        }

        super.doPost(request, response, resource);
    }

    /**
     * Puts a resource. Requires a <code>Transfer-Encoding</code>
     * header for non-chunked requests. Collections may not be put
     * to.
     */
    protected void doPut(WebdavRequest request,
                         WebdavResponse response,
                         DavResource resource)
        throws IOException, DavException {
        if (resource.isCollection()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN,
                               "Collections may not be put to.");
            return;
        }

        // if the request is not chunked, require a content length
        String transferEncoding = request.getHeader("Transfer-Encoding");
        if ((transferEncoding == null || transferEncoding.equals("identity")) &&
            request.getContentLength() <= 0) {
            response.sendError(DavServletResponse.SC_LENGTH_REQUIRED);
            return;
        }

        // Catch DataAccessException (concurrency failure or data integrity
        // failure) and retry a maximum of 5 attempts and return 500 
        // if all attempts fail.
        boolean success = false;
        int tries = 0;
        while (!success && tries<5) {
            tries++;
            try {
                super.doPut(request, response, resource);
                success = true;
            } catch (org.springframework.dao.DataAccessException e) {
                log.warn("Concurrency failure during PUT", e);
                
                // Need to re-initialze resource because it has changed.
                // check matching if=header for lock-token relevant operations
                resource = getResourceFactory().createResource(request.getRequestLocator(), request, response);
                if (!isPreconditionValid(request, resource)) {
                    response.sendError(DavServletResponse.SC_PRECONDITION_FAILED);
                    return;
                }

                // check If-None-Match header
                if (ifNoneMatch(request, resource)) {
                    response.sendError(DavServletResponse.SC_PRECONDITION_FAILED);
                    return;
                }

                // check If-Match header
                if (ifMatch(request, resource)) {
                    response.sendError(DavServletResponse.SC_PRECONDITION_FAILED);
                    return;
                }
            }
        }

        if (!success) {
            response.sendError(DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Concurrency failure.");
            return;
        }
        
        response.setHeader("ETag", resource.getETag());
    }

    /**
     * Executes a report on a resource. Requires read or free-busy
     * permission for free-busy reports, read permission for all other
     * reports.
     */
    protected void doReport(WebdavRequest request,
                            WebdavResponse response,
                            DavResource resource)
        throws DavException, IOException {
        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }

        ExtendedDavResource eresource = (ExtendedDavResource) resource;
        ReportInfo info = request.getReportInfo();

        Ticket authTicket = securityManager.getSecurityContext().getTicket();

        // XXX move these checks into DavCollection or even into the
        // report run method

        if (FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY.
            isRequestedReportType(info)) {
            // check resource type
            if (! (resource.isCollection() ||
                   eresource.isCalendarCollection())) {
                response.sendError(DavServletResponse.SC_FORBIDDEN,
                                   "CALDAV:free-busy-query REPORT can only be run against a collection");
                return;
            }

            // check permissions
            if (authTicket != null) {
                if (! (authTicket.getPrivileges().
                       contains(Ticket.PRIVILEGE_READ) ||
                       authTicket.getPrivileges().
                       contains(Ticket.PRIVILEGE_FREEBUSY))) {
                    // see "Marshalling" section of
                    // "CALDAV:free-busy-query Report" for why we
                    // return NOT_FOUND
                    response.sendError(DavServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }
        else {
            // check permissions
            if (authTicket != null) {
                if (! authTicket.getPrivileges().
                    contains(Ticket.PRIVILEGE_READ)) {
                    response.sendError(DavServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }

        super.doReport(request, response, resource);
    }

    /**
     * Creates a calendar collection resource.
     */
    protected void doMkCalendar(WebdavRequest request,
                                WebdavResponse response,
                                DavResource resource)
        throws IOException, DavException {
        // {DAV:resource-must-be-null}
        if (resource.exists()) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED,
                               "Resource exists");
            return;
        }

        ExtendedDavResource eresource = (ExtendedDavResource) resource;

        // {DAV:calendar-collection-location-ok}
        ExtendedDavResource parentResource =
            (ExtendedDavResource) resource.getCollection();

        if (parentResource == null ||
            ! parentResource.exists()) {
            response.sendError(DavServletResponse.SC_CONFLICT,
                               "One or more intermediate collections must be created");
            return;
        }

        // {DAV:calendar-collection-location-ok}
        if (! parentResource.isCollection() ||
            parentResource.isCalendarCollection()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN,
                               "Parent resource must be a regular collection");
            return;
        }

        //XXX: {DAV:needs-privilege}
        //The DAV:bind privilege MUST be granted to
        //     the current user on the parent collection of the Request-URI.

        // also could return INSUFFICIENT_STORAGE if we do not have
        // enough space for the collection, but how do we determine
        // that?

        if (log.isDebugEnabled()) {
            log.debug("adding calendar collection at " +
                      resource.getResourcePath());
        }

        DavPropertySet properties = null;
        MultiStatusResponse msr = null;
        try {
            // we don't allow the input context to take the input
            // stream because we are managing properties ourselves
            DavInputContext ctx =
                (DavInputContext) getInputContext(request, null);
            properties = ((CaldavRequest)request).getMkCalendarSetProperties();
            msr = parentResource.addMember(resource, ctx, properties);
        } catch (IllegalArgumentException e) {
            log.warn("error parsing MKCALENDAR properties", e);
            response.sendError(DavServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        if (properties.isEmpty() || ! msr.hasNonOk()) {
            response.setStatus(DavServletResponse.SC_CREATED);
            return;
        }

        MultiStatus m = new MultiStatus();
        m.addResponse(msr);
        response.sendMultiStatus(m);
    }

    /**
     * Grants a ticket on a resource.
     */
    protected void doMkTicket(WebdavRequest request,
                              WebdavResponse response,
                              DavResource resource)
        throws IOException, DavException {
        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }

        ExtendedDavResource eresource = (ExtendedDavResource) resource;

        Ticket ticket = null;
        try {
            ticket = ((TicketDavRequest)request).getTicketInfo();
            User user = securityManager.getSecurityContext().getUser();
            ticket.setOwner(user);
        } catch (IllegalArgumentException e) {
            log.warn("error parsing request content", e);
            response.sendError(DavServletResponse.SC_BAD_REQUEST,
                               e.getMessage());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("saving ticket for resource " +
                      resource.getResourcePath());
        }
        eresource.saveTicket(ticket);

        ((TicketDavResponse)response).
            sendMkTicketResponse(eresource, ticket.getKey());
    }

    /**
     * Revokes a ticket from a resource.
     */
    protected void doDelTicket(WebdavRequest request,
                               WebdavResponse response,
                               DavResource resource)
        throws IOException, DavException {
        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }

        ExtendedDavResource eresource = (ExtendedDavResource) resource;

        String ticketId = ((TicketDavRequest)request).getTicketId();
        if (ticketId == null) {
            response.sendError(DavServletResponse.SC_BAD_REQUEST,
                               "No ticket was specified.");
            return;
        }
        Ticket ticket = eresource.getTicket(ticketId);
        if (ticket == null) {
            response.sendError(DavServletResponse.SC_PRECONDITION_FAILED,
                               "The ticket specified does not exist.");
            return;
        }

        // in order to delete a ticket, the authenticated principal
        // must meet one of these conditions:
        // 1) admin user
        // 2) owner of the ticket
        // 3) a write ticket on this resource (security layer
        //    will have already validated the ticket)

        User user = securityManager.getSecurityContext().getUser();
        if (user != null) {
            if (! (ticket.getOwner().equals(user) ||
                   securityManager.getSecurityContext().isAdmin())) {
                response.sendError(DavServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        else {
            Ticket authTicket =
                securityManager.getSecurityContext().getTicket();
            if (authTicket == null) {
                response.sendError(DavServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("removing ticket " + ticket.getKey() + " for resource " +
                      resource.getResourcePath());
        }
        eresource.removeTicket(ticket);

        ((TicketDavResponse)response).
            sendDelTicketResponse(eresource, ticket.getKey());
    }

    /**
     * Checks if the request method (or any other dimension of the
     * request) allows the response to be cached by clients and
     * proxies.
     */
    protected boolean isResponseCacheable(WebdavRequest request) {
        int methodCode = CosmoDavMethods.getMethodCode(request.getMethod());

        // CalDAV requires MKCALENDAR to not be cached
        if (methodCode == CosmoDavMethods.DAV_MKCALENDAR)
            return false;

        return super.isResponseCacheable(request);
    }

    /**
     * Creates an instance of <code>WebdavRequest</code> based on the
     * provided <code>HttpServletRequest</code>.
     */
    protected WebdavRequest createWebdavRequest(HttpServletRequest request) {
        // Create buffered request if method is PUT so we can retry
        // on concurrency exceptions
        if (request.getMethod().equals(DavMethods.METHOD_PUT))
            return new StandardDavRequest(request, getLocatorFactory(), true);
        else
            return new StandardDavRequest(request, getLocatorFactory());   
    }

    /**
     * Creates an instance of <code>WebdavResponse</code> based on the
     * provided <code>HttpServletResponse</code>.
     */
    protected WebdavResponse createWebdavResponse(HttpServletResponse response,
                                                  boolean noCache) {
        return new StandardDavResponse(response, noCache);
    }

    /**
     * Creates an instance of <code>InputContext</code> based on the
     * provided servlet request and input stream.
     */
    public InputContext getInputContext(DavServletRequest request,
                                        InputStream in) {
        // If the request is a PUT, then we have buffered the input and
        // know the exact length read
        if(request.getMethod().equals(DavMethods.METHOD_PUT)) {
            long bufferedLength = ((StandardDavRequest) request).getBufferedContentLength();
            return new DavInputContext(request, in, bufferedLength);
        } else {
            return new DavInputContext(request, in);
        }
    }

    /** {@inheritDoc} */
    protected boolean isPreconditionValid(WebdavRequest request,
                                          DavResource resource) {
        return !resource.exists() || request.matchesIfHeader(resource);
    }

    /**
     * Returns <code>null</code> since the servlet does not handle
     * authentication itself.
     */
    public String getAuthenticateHeaderValue() {
        return null;
    }

    /** */
    public LockManager getLockManager() {
        return lockManager;
    }

    /** */
    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    /** {@inheritDoc} */
    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    /** {@inheritDoc} */
    public void setResourceFactory(DavResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    /** {@inheritDoc} */
    public DavLocatorFactory getLocatorFactory() {
        return locatorFactory;
    }

    /** {@inheritDoc} */
    public void setLocatorFactory(DavLocatorFactory locatorFactory) {
        this.locatorFactory = locatorFactory;
    }

    /** {@inheritDoc} */
    public synchronized DavSessionProvider getDavSessionProvider() {
        return sessionProvider;
    }

    /** {@inheritDoc} */
    public synchronized void setDavSessionProvider(DavSessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    /**
     * Returns the <code>CosmoSecurityManager</code>.
     *
     * @return the security manager
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Sets the <code>CosmoSecurityManager</code>
     *
     * @param securityManager
     */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Looks up the bean with given name and class in the web
     * application context.
     *
     * @param wac the web application context
     * @param name the bean's name
     * @param clazz the bean's class
     */
    protected Object getBean(WebApplicationContext wac,
                             String name,
                             Class clazz)
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

