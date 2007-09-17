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
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.version.report.SupportedReportSetProperty;

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.ProtectedPropertyModificationException;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.acl.report.PrincipalMatchReport;
import org.osaf.cosmo.dav.acl.report.PrincipalPropertySearchReport;
import org.osaf.cosmo.dav.acl.report.PrincipalSearchPropertySetReport;
import org.osaf.cosmo.dav.impl.DavResourceBase;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.dav.property.DisplayName;
import org.osaf.cosmo.dav.property.IsCollection;
import org.osaf.cosmo.dav.property.ResourceType;
import org.osaf.cosmo.model.User;

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
        context.setContentType(IOUtil.buildContentType("text/html", "UTF-8"));

        if (! context.hasStream())
            return;

        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(context.getOutputStream(),
                                                   "utf8"));

        writer.write("<html><head><title>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</title></head>");
        writer.write("<body>");
        writer.write("<h1>");
        writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
        writer.write("</h1>");
        writer.write("<p>");
        writer.write("The following reports are supported on this collection:");
        writer.write("<ul>");
        for (ReportType rt : getReportTypes())
            writer.write(StringEscapeUtils.escapeHtml(rt.getReportName()));
        writer.write("</ul>");
        writer.write("</body>");
        writer.write("</html>");
        writer.write("\n");
        writer.close();
    }
}
