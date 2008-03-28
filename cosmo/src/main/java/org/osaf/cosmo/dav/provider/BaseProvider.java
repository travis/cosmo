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
import org.osaf.cosmo.dav.acl.AclEvaluator;
import org.osaf.cosmo.dav.acl.DavPrivilege;
import org.osaf.cosmo.dav.acl.NeedsPrivilegesException;
import org.osaf.cosmo.dav.acl.TicketAclEvaluator;
import org.osaf.cosmo.dav.acl.UnsupportedPrivilegeException;
import org.osaf.cosmo.dav.acl.UserAclEvaluator;
import org.osaf.cosmo.dav.acl.resource.DavUserPrincipal;
import org.osaf.cosmo.dav.acl.resource.DavUserPrincipalCollection;
import org.osaf.cosmo.dav.caldav.report.FreeBusyReport;
import org.osaf.cosmo.dav.impl.DavItemResource;
import org.osaf.cosmo.dav.impl.DavFile;
import org.osaf.cosmo.dav.io.DavInputContext;
import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.model.EntityFactory;
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
    implements DavProvider, DavConstants, AclConstants, TicketConstants {
    private static final Log log = LogFactory.getLog(BaseProvider.class);

    private DavResourceFactory resourceFactory;
    private EntityFactory entityFactory;

    public BaseProvider(DavResourceFactory resourceFactory,
            EntityFactory entityFactory) {
        this.resourceFactory = resourceFactory;
        this.entityFactory = entityFactory;
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
        checkPropFindAccess(resource, props, type);

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
            checkReportAccess(resource, info);

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
        ticket.setOwner(getSecurityContext().getUser());

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

        dir.removeTicket(ticket);

        response.setStatus(204);
    }

    public void acl(DavRequest request,
                    DavResponse response,
                    DavResource resource)
        throws DavException, IOException {
        if (! resource.exists())
            throw new NotFoundException();

        if (log.isDebugEnabled())
            log.debug("ACL for " + resource.getResourcePath());

        throw new UnsupportedPrivilegeException("No unprotected ACEs are supported on this resource");
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
            new DavFile(locator, resourceFactory, entityFactory);
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

        DavResource toCheck = destination.exists() ?
            destination : destination.getParent();
        Item item = ((DavItemResource)toCheck).getItem();
        DavResourceLocator locator = toCheck.getResourceLocator();
        String href = locator.getHref(toCheck.isCollection());
        DavPrivilege privilege = destination.exists() ?
            DavPrivilege.WRITE : DavPrivilege.BIND;

        User user = getSecurityContext().getUser();
        if (user != null) {
            UserAclEvaluator evaluator = new UserAclEvaluator(user);
            if (evaluator.evaluate(item, privilege))
                return;
            throw new NeedsPrivilegesException(href, privilege);
        }

        Ticket ticket = getSecurityContext().getTicket();
        if (ticket != null) {
            TicketAclEvaluator evaluator = new TicketAclEvaluator(ticket);
            if (evaluator.evaluate(item, privilege))
                return;
            throw new NeedsPrivilegesException(href, privilege);
        }
        
        throw new NeedsPrivilegesException(href, privilege);
    }

    protected AclEvaluator createAclEvaluator() {
        User user = getSecurityContext().getUser();
        if (user != null)
            return new UserAclEvaluator(user);
        Ticket ticket = getSecurityContext().getTicket();
        if (ticket != null)
            return new TicketAclEvaluator(ticket);
        throw new IllegalStateException("Anonymous principal not supported for ACL evaluation");
    }

    protected boolean hasPrivilege(DavResource resource,
                                   AclEvaluator evaluator,
                                   DavPrivilege privilege) {
        boolean hasPrivilege = false;
        if (resource instanceof DavUserPrincipalCollection ||
            resource instanceof DavUserPrincipal) {
            if (evaluator instanceof TicketAclEvaluator)
                throw new IllegalStateException("A ticket may not be used to access a user principal collection or resource");
            UserAclEvaluator uae = (UserAclEvaluator) evaluator;

            if (resource instanceof DavUserPrincipalCollection) {
                hasPrivilege = uae.evaluateUserPrincipalCollection(privilege);
            } else {
                User user = ((DavUserPrincipal)resource).getUser();
                hasPrivilege = uae.evaluateUserPrincipal(user, privilege);
            }
        } else {
            Item item = ((DavItemResource)resource).getItem();
            hasPrivilege = evaluator.evaluate(item, privilege);
        }        

        if (hasPrivilege) {
            if (log.isDebugEnabled())
                log.debug("Principal has privilege " + privilege);
            return true;
        }

        
        if (log.isDebugEnabled())
            log.debug("Principal does not have privilege " + privilege);
        return false;
    }

    protected void checkPropFindAccess(DavResource resource,
                                       DavPropertyNameSet props,
                                       int type)
        throws DavException {
        AclEvaluator evaluator = createAclEvaluator();

        // if the principal has DAV:read, then the propfind can continue
        if (hasPrivilege(resource, evaluator, DavPrivilege.READ)) {
            if (log.isDebugEnabled())
                log.debug("Allowing PROPFIND");
            return;
        }

        // if there is at least one property that can be viewed with
        // DAV:read-current-user-privilege-set, then check for that
        // privilege as well.
        int unprotected = 0;
        if (props.contains(CURRENTUSERPRIVILEGESET))
            unprotected++;
        // ticketdiscovery is only unprotected when the principal is a
        // ticket
        if (props.contains(TICKETDISCOVERY) &&
            evaluator instanceof TicketAclEvaluator)
            unprotected++;

        if (unprotected > 0) {
            if (hasPrivilege(resource, evaluator,
                             DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET)) {

                if (props.getContentSize() > unprotected)
                    // XXX: if they don't have DAV:read, they shouldn't be
                    // able to access any other properties
                    log.warn("Exposing secured properties to ticket without DAV:read");
                             
                if (log.isDebugEnabled())
                    log.debug("Allowing PROPFIND");
                return;
             }
        }

        // don't allow the client to know that this resource actually
        // exists
        if (log.isDebugEnabled())
            log.debug("Denying PROPFIND");
        throw new NotFoundException();
    }

    protected void checkReportAccess(DavResource resource,
                                     ReportInfo info)
        throws DavException {
        AclEvaluator evaluator = createAclEvaluator();

        // if the principal has DAV:read, then the propfind can continue
        if (hasPrivilege(resource, evaluator, DavPrivilege.READ)) {
            if (log.isDebugEnabled())
                log.debug("Allowing REPORT");
            return;
        }

        // if this is a free-busy report, then check CALDAV:read-free-busy
        // also
        if (isFreeBusyReport(info)) {
            Item item = ((DavItemResource)resource).getItem();
            if (hasPrivilege(resource, evaluator,
                             DavPrivilege.READ_FREE_BUSY)) {
                if (log.isDebugEnabled())
                    log.debug("Allowing REPORT");
                return;
            }
        }

        // don't allow the client to know that this resource actually
        // exists
        if (log.isDebugEnabled())
            log.debug("Denying PROPFIND");
        throw new NotFoundException();
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
    
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    private boolean isFreeBusyReport(ReportInfo info) {
        return FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY.
            isRequestedReportType(info);
    }
}
