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
package org.osaf.cosmo.dav.acl.resource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.ProtectedPropertyModificationException;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.acl.DavAce;
import org.osaf.cosmo.dav.acl.DavAcl;
import org.osaf.cosmo.dav.acl.DavPrivilege;
import org.osaf.cosmo.dav.acl.report.PrincipalMatchReport;
import org.osaf.cosmo.dav.acl.report.PrincipalPropertySearchReport;
import org.osaf.cosmo.dav.acl.report.PrincipalSearchPropertySetReport;
import org.osaf.cosmo.dav.impl.DavResourceBase;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.dav.property.DisplayName;
import org.osaf.cosmo.dav.property.IsCollection;
import org.osaf.cosmo.dav.property.ResourceType;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.xml.DomWriter;

import org.w3c.dom.Element;

/**
 * <p>
 * Models a WebDAV principal collection (as described in RFC 3744) that
 * contains a principal resource for each user account in the server. The
 * principal collection itself is not backed by a persistent entity.
 * </p>
 *
 * @see DavResourceBase
 * @see DavCollection
 */
public class DavUserPrincipalCollection extends DavResourceBase
    implements DavCollection {
    private static final Log log =
        LogFactory.getLog(DavUserPrincipalCollection.class);
    private static final Set<ReportType> REPORT_TYPES =
        new HashSet<ReportType>();

    private ArrayList<DavUserPrincipal> members;
    private DavAcl acl;

    static {
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);

        REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE_PRINCIPAL_MATCH);
        REPORT_TYPES.add(PrincipalPropertySearchReport.
                         REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH);
        REPORT_TYPES.add(PrincipalSearchPropertySetReport.
                         REPORT_TYPE_PRINCIPAL_SEARCH_PROPERTY_SET);
    }

    public DavUserPrincipalCollection(DavResourceLocator locator,
                                      DavResourceFactory factory)
        throws DavException {
        super(locator, factory);
        members = new ArrayList<DavUserPrincipal>();
        acl = makeAcl();
    }

    // Jackrabbit DavResource

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT";
    }

    public boolean isCollection() {
        return true;
    }

    public long getModificationTime() {
        return -1;
    }

    public boolean exists() {
        return true;
    }

    public String getDisplayName() {
        return "User Principals";
    }

    public String getETag() {
        return null;
    }

    public void writeTo(OutputContext outputContext)
        throws DavException, IOException {
        writeHtmlDirectoryIndex(outputContext);
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
                          InputContext inputContext)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        try {
            for (User user : getResourceFactory().getUserService().getUsers()) {
                if (user.isOverlord())
                    continue;
                members.add(memberToResource(user));
            }
            return new DavResourceIteratorImpl(members);
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResource getCollection() {
        throw new UnsupportedOperationException();
    }

    public void move(org.apache.jackrabbit.webdav.DavResource destination)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public void copy(org.apache.jackrabbit.webdav.DavResource destination,
                     boolean shallow)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    // DavResource

    public DavCollection getParent()
        throws DavException {
        return null;
    }

    // DavCollection

    public void addContent(DavContent content,
                           InputContext context)
        throws DavException {
        throw new UnsupportedOperationException();
    }

    public MultiStatusResponse addCollection(DavCollection collection,
                                             DavPropertySet properties)
        throws DavException {
        throw new UnsupportedOperationException();
    }

    public DavUserPrincipal findMember(String uri)
        throws DavException {
        DavResourceLocator locator = getResourceLocator().getFactory().
            createResourceLocatorByUri(getResourceLocator().getContext(),
                                       uri);
        return (DavUserPrincipal) getResourceFactory().resolve(locator);
    }

    // our methods

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(1);
        rt.add(RESOURCE_TYPE_COLLECTION);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }

    /**
     * Returns the resource's access control list. The list contains the
     * following ACEs:
     *
     * <ol>
     * <li> <code>DAV:unauthenticated</code>: deny <code>DAV:all</code> </li>
     * <li> <code>DAV:all</code>: allow
     * <code>DAV:read, DAV:read-current-user-privilege-set</code> </li>
     * <li> <code>DAV:all</code>: deny <code>DAV:all</code> </li>
     * </ol>
     */
    protected DavAcl getAcl() {
        return acl;
    }

    private DavAcl makeAcl() {
        DavAcl acl = new DavAcl();

        DavAce unauthenticated = new DavAce.UnauthenticatedAce();
        unauthenticated.setDenied(true);
        unauthenticated.getPrivileges().add(DavPrivilege.ALL);
        unauthenticated.setProtected(true);
        acl.getAces().add(unauthenticated);

        DavAce allAllow = new DavAce.AllAce();
        allAllow.getPrivileges().add(DavPrivilege.READ);
        allAllow.getPrivileges().add(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
        allAllow.setProtected(true);
        acl.getAces().add(allAllow);

        DavAce allDeny = new DavAce.AllAce();
        allDeny.setDenied(true);
        allDeny.getPrivileges().add(DavPrivilege.ALL);
        allDeny.setProtected(true);
        acl.getAces().add(allDeny);

        return acl;
    }

    /**
     * <p>
     * Extends the superclass method to return {@link DavPrivilege#READ} if
     * the the current principal is a non-admin user.
     * </p>
     */
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
        if (! privileges.isEmpty())
            return privileges;

        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null)
            privileges.add(DavPrivilege.READ);

        return privileges;
    }

    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
    }

    protected void setLiveProperty(DavProperty property)
        throws DavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }

    protected void removeLiveProperty(DavPropertyName name)
        throws DavException {
        throw new ProtectedPropertyModificationException(name);
    }

    protected void loadDeadProperties(DavPropertySet properties) {
    }

    protected void setDeadProperty(DavProperty property)
        throws DavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }

    protected void removeDeadProperty(DavPropertyName name)
        throws DavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }

    private DavUserPrincipal memberToResource(User user)
        throws DavException {
        String path = TEMPLATE_USER.bind(false, user.getUsername());
        DavResourceLocator locator = getResourceLocator().getFactory().
            createResourceLocatorByPath(getResourceLocator().getContext(),
                                        path);
        return new DavUserPrincipal(user, locator, getResourceFactory());
    }

    private void writeHtmlDirectoryIndex(OutputContext context)
        throws DavException, IOException {
        if (log.isDebugEnabled())
            log.debug("writing html directory index for  " +
                      getDisplayName());

        context.setContentType(IOUtil.buildContentType("text/html", "UTF-8"));
        // no modification time or etag

        if (! context.hasStream())
            return;

        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(context.getOutputStream(),
                                                   "utf8"));

        writer.write("<html>\n<head><title>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</title></head>\n");
        writer.write("<body>\n");
        writer.write("<h1>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</h1>\n");

        writer.write("<h2>Members</h2>\n");
        writer.write("<ul>\n");
        for (DavResourceIterator i=getMembers(); i.hasNext();) {
            DavResource child = (DavResource) i.nextResource();
            writer.write("<li><a href=\"");
            writer.write(child.getResourceLocator().getHref(child.isCollection()));
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(child.getDisplayName()));
            writer.write("</a></li>\n");
        }
        writer.write("</ul>\n");

        writer.write("<h2>Properties</h2>\n");
        writer.write("<dl>\n");
        for (DavPropertyIterator i=getProperties().iterator(); i.hasNext();) {
            DavProperty prop = (DavProperty) i.nextProperty();
            Object value = prop.getValue();
            String text = null;
            if (value instanceof Element) {
                try {
                    text = DomWriter.write((Element)value);
                } catch (Exception e) {
                    log.warn("Error serializing value for property " + prop.getName());
                }
            }
            if (text == null)
                text = prop.getValueText();
            writer.write("<dt>");
            writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
            writer.write("</dt><dd>");
            writer.write(StringEscapeUtils.escapeHtml(text));
            writer.write("</dd>\n");
        }
        writer.write("</dl>\n");

        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null) {
            writer.write("<p>\n");
            DavResourceLocator homeLocator =
                getResourceLocator().getFactory().
                createHomeLocator(getResourceLocator().getContext(), user);
            writer.write("<a href=\"");
            writer.write(homeLocator.getHref(true));
            writer.write("\">");
            writer.write("Home collection");
            writer.write("</a><br>\n");

            DavResourceLocator principalLocator = 
                getResourceLocator().getFactory().
                createPrincipalLocator(getResourceLocator().getContext(),
                                       user);
            writer.write("<a href=\"");
            writer.write(principalLocator.getHref(false));
            writer.write("\">");
            writer.write("Principal resource");
            writer.write("</a><br>\n");
        }

        writer.write("</body>");
        writer.write("</html>\n");
        writer.close();
    }
}
