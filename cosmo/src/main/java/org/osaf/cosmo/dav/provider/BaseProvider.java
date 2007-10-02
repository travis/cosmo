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
package org.osaf.cosmo.dav.provider;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.io.OutputContextImpl;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.ConflictException;
import org.osaf.cosmo.dav.ContentLengthRequiredException;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavRequest;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.DavResponse;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.MethodNotAllowedException;
import org.osaf.cosmo.dav.NotFoundException;
import org.osaf.cosmo.dav.PreconditionFailedException;
import org.osaf.cosmo.dav.UnsupportedMediaTypeException;
import org.osaf.cosmo.dav.acl.AclConstants;
import org.osaf.cosmo.dav.caldav.report.FreeBusyReport;
import org.osaf.cosmo.dav.impl.DavItemResource;
import org.osaf.cosmo.dav.impl.DavFile;
import org.osaf.cosmo.dav.io.DavInputContext;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;

/**
 * <p>
 * A base class for implementations of <code>DavProvider</code>.
 * </p>
 *
 * @see DavProvider
 */
public abstract class BaseProvider
    implements DavProvider, DavConstants, AclConstants {
    private static final Log log = LogFactory.getLog(BaseProvider.class);

    private DavResourceFactory resourceFactory;

    public BaseProvider(DavResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    // DavProvider methods

    public void get(DavRequest request,
                    DavResponse response,
                    DavResource resource)
        throws DavException, IOException {
        spool(request, response, resource, true);
    }

    public void head(DavRequest request,
                     DavResponse response,
                     DavResource resource)
        throws DavException, IOException {
        spool(request, response, resource, false);
    }

    public void propfind(DavRequest request,
                         DavResponse response,
                         DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();

        int depth = getDepth(request);
        if (depth != DEPTH_0 && ! resource.isCollection())
            throw new BadRequestException("Depth must be 0 for non-collection resources");

        DavPropertyNameSet props = request.getPropFindProperties();
        int type = request.getPropFindType();

        // Since the propfind properties could not be determined in the
        // security filter in order to check specific property privileges, the
        // check must be done manually here.
        checkPropFindAccess(props, type);

        MultiStatus ms = new MultiStatus();
        ms.addResourceProperties(resource, props, type, depth);

        response.sendMultiStatus(ms);
    }

    public void proppatch(DavRequest request,
                          DavResponse response,
                          DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();

        DavPropertySet set = request.getProppatchSetProperties();
        DavPropertyNameSet remove = request.getProppatchRemoveProperties();

        MultiStatus ms = new MultiStatus();
        MultiStatusResponse msr = resource.updateProperties(set, remove);
        ms.addResponse(msr);

        response.sendMultiStatus(ms);
    }

    public void delete(DavRequest request,
                       DavResponse response,
                       DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();
        checkNoRequestBody(request);

        int depth = getDepth(request);
        if (depth != DEPTH_INFINITY)
            throw new BadRequestException("Depth for DELETE must be infinity");

        try {
            resource.getParent().removeMember(resource);
            response.setStatus(204);
        } catch (org.apache.jackrabbit.webdav.DavException e) {
            throw new DavException(e);
        }
    }

    public void copy(DavRequest request,
                     DavResponse response,
                     DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();
        checkNoRequestBody(request);

        int depth = getDepth(request);
        if (! (depth == DEPTH_0 || depth == DEPTH_INFINITY))
            throw new BadRequestException("Depth for COPY must be 0 or infinity");

        DavResource destination =
            resolveDestination(request.getDestinationResourceLocator(),
                               resource);
        validateDestination(request, destination);

        checkCopyMoveAccess(resource, destination);

        try {
            if (destination.exists() && request.isOverwrite())
                destination.getCollection().removeMember(destination);
            resource.copy(destination, depth == DEPTH_0);
            response.setStatus(destination.exists() ? 204 : 201);
        } catch (org.apache.jackrabbit.webdav.DavException e) {
            if (e instanceof DavException)
                throw (DavException)e;
            throw new DavException(e);
        }
    }

    public void move(DavRequest request,
                     DavResponse response,
                     DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();
        checkNoRequestBody(request);

        DavResource destination =
            resolveDestination(request.getDestinationResourceLocator(),
                               resource);
        validateDestination(request, destination);

        checkCopyMoveAccess(resource, destination);

        try {
            if (destination.exists() && request.isOverwrite())
                destination.getCollection().removeMember(destination);
            resource.move(destination);
            response.setStatus(destination.exists() ? 204 : 201);
        } catch (org.apache.jackrabbit.webdav.DavException e) {
            if (e instanceof DavException)
                throw (DavException)e;
            throw new DavException(e);
        }
    }

    public void report(DavRequest request,
                       DavResponse response,
                       DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();

        try {
            ReportInfo info = request.getReportInfo();

            // Since the report type could not be determined in the security
            // filter in order to check ticket permissions on REPORT, the
            // check must be done manually here.
            checkReportAccess(info);

            resource.getReport(info).run(response);
        } catch (org.apache.jackrabbit.webdav.DavException e) {
            if (e instanceof DavException)
                throw (DavException) e;
            throw new DavException(e);
        }
    }

    public void mkticket(DavRequest request,
                         DavResponse response,
                         DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();
        if (! (resource instanceof DavItemResource))
            throw new MethodNotAllowedException("MKTICKET requires a content collection or content resource");
        DavItemResource dir = (DavItemResource) resource;

        if (log.isDebugEnabled())
            log.debug("MKTICKET for " + resource.getResourcePath());

        Ticket ticket = request.getTicketInfo();
        User user = getSecurityContext().getUser();
        if (user == null)
            throw new ForbiddenException("MKTICKET requires an authenticated user");
        ticket.setOwner(user);

        dir.saveTicket(ticket);

        response.sendMkTicketResponse(dir, ticket.getKey());
    }

    public void delticket(DavRequest request,
                          DavResponse response,
                          DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();
        checkNoRequestBody(request);

        if (! (resource instanceof DavItemResource))
            throw new MethodNotAllowedException("DELTICKET requires a content collection or content resource");
        DavItemResource dir = (DavItemResource) resource;

        if (log.isDebugEnabled())
            log.debug("DELTICKET for " + resource.getResourcePath());

        String key = request.getTicketKey();
        Ticket ticket = dir.getTicket(key);
        if (ticket == null)
            throw new PreconditionFailedException("Ticket " + key + " does not exist");

        checkDelTicketAccess(ticket);

        dir.removeTicket(ticket);

        response.setStatus(204);
    }

    // our methods

    protected void spool(DavRequest request,
                         DavResponse response,
                         DavResource resource,
                         boolean withEntity)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();
        checkNoRequestBody(request);

        if (log.isDebugEnabled())
            log.debug("spooling resource " + resource.getResourcePath());

        resource.writeTo(createOutputContext(response, withEntity));
        response.flushBuffer();
    }

    protected InputContext createInputContext(DavRequest request)
        throws DavException, IOException {
        String xfer = request.getHeader("Transfer-Encoding");
        boolean chunked = xfer != null && xfer.equals("chunked");
        if (xfer != null && ! chunked)
            throw new BadRequestException("Unknown Transfer-Encoding " + xfer);
        if (chunked && request.getContentLength() <= 0)
            throw new ContentLengthRequiredException();

        InputStream in = (request.getContentLength() > 0 || chunked) ?
            request.getInputStream() : null;
        return new DavInputContext(request, in);
    }

    protected OutputContext createOutputContext(DavResponse response,
                                                boolean withEntity)
        throws IOException {
        OutputStream out = withEntity ? response.getOutputStream() : null;
        return new OutputContextImpl(response, out);
    }

    protected DavResource resolveDestination(DavResourceLocator locator,
                                             DavResource original)
        throws DavException {
        if (locator == null)
            return null;
        DavResource destination = resourceFactory.resolve(locator);
        return destination != null ? destination :
            new DavFile(locator, resourceFactory);
    }

    protected void validateDestination(DavRequest request,
                                       DavResource destination)
        throws DavException {
        if (destination == null)
            throw new BadRequestException("Destination required");
        if (destination.getResourceLocator().equals(request.getResourceLocator()))
            throw new ForbiddenException("Destination URI is the same as the original resource URI");
        if (destination.exists() && ! request.isOverwrite())
            throw new PreconditionFailedException("Overwrite header false was not specified for existing destination");
        if (! destination.getParent().exists())
            throw new ConflictException("One or more intermediate collections must be created");
    }

    protected void checkCopyMoveAccess(DavResource source,
                                       DavResource destination)
        throws DavException {
        // XXX refactor a BaseItemProvider so we don't have to do this check
        if (! (source instanceof DavItemResource))
            // we're operating on a principal resource which can't be moved
            // anyway
            return;

        // because the security filter let us get this far, we know the
        // security context has access to the source resource. we have to
        // check that it also has access to the destination resource.
        
        if (getSecurityContext().isAdmin())
            return;

        Item destinationItem = destination.exists() ?
                ((DavItemResource)destination).getItem() :
                ((DavItemResource)destination.getParent()).getItem();

        User user = getSecurityContext().getUser();
        if (user != null) {
            // requires DAV:bind on parent of destination tiem
            if (user.equals(destinationItem.getOwner()))
                return;
            throw new ForbiddenException("User privileges deny access");
        }

        Ticket ticket = getSecurityContext().getTicket();
        if (ticket != null) {
            // requires DAV:bind on parent of destination item
            if (ticket.isGranted(destinationItem) &&
                ticket.getPrivileges().contains(Ticket.PRIVILEGE_WRITE))
                return;
            throw new ForbiddenException("Ticket privileges deny access");
        }

        throw new ForbiddenException("Anonymous privileges deny access");
    }

    protected void checkPropFindAccess(DavPropertyNameSet props,
                                       int type)
        throws DavException {
        Ticket ticket = getSecurityContext().getTicket();
        if (ticket == null)
            return;

        if (props.contains(CURRENTUSERPRIVILEGESET)) {
            // requires DAV:read-current-user-privilege-set, which all tickets
            // have, even those without DAV:read
            if (! ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ) &&
                props.getContentSize() > 1)
                log.warn("Exposing secured properties to ticket without DAV:read");
            return;
        }

        // requires DAV:read
        if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ))
            return;

        throw new ForbiddenException("Ticket privileges deny access");
    }

    protected void checkReportAccess(ReportInfo info)
        throws DavException {
        Ticket ticket = getSecurityContext().getTicket();
        if (ticket == null)
            return;

        if (isFreeBusyReport(info)) {
            // requires DAV:read-free-busy
            if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_FREEBUSY))
                return;
            // Do not allow the client to know that this resource actually
            // exists, as per CalDAV report definition
            throw new NotFoundException();
        }

        // requires DAV:read
        if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ))
            return;

       throw new ForbiddenException("Ticket privileges deny access");
    }

    private void checkDelTicketAccess(Ticket ticket)
        throws DavException {
        User user = getSecurityContext().getUser();
        if (user != null) {
            // user must either own the ticket or be an administrator
            if (! (ticket.getOwner().equals(user) || getSecurityContext().isAdmin()))
                throw new ForbiddenException("Authenticated user not ticket owner");
            return;
        }
        Ticket authTicket = getSecurityContext().getTicket();
        if (authTicket != null)
            // security layer has already validated this ticket
            return;

        throw new ForbiddenException("Anonymous user not ticket owner");
    }

    protected void checkNoRequestBody(DavRequest request)
        throws DavException {
        boolean hasBody = false;
        try {
            hasBody = request.getRequestDocument() != null;
        } catch (IllegalArgumentException e) {
            // parse error indicates that there was a body to parse
            hasBody = true;
        }
        if (hasBody)
            throw new UnsupportedMediaTypeException("Body not expected for method " + request.getMethod());
    }

    protected int getDepth(DavRequest request)
        throws DavException {
        try {
            return request.getDepth();
        } catch (IllegalArgumentException e) {    
            throw new BadRequestException(e.getMessage());
        }
    }

    protected CosmoSecurityContext getSecurityContext() {
        return getResourceFactory().getSecurityManager().getSecurityContext();
    }

    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    private boolean isFreeBusyReport(ReportInfo info) {
        return FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY.
            isRequestedReportType(info);
    }
}
