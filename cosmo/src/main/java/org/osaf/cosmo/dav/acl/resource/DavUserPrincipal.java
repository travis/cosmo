/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.ProtectedPropertyModificationException;
import org.osaf.cosmo.dav.acl.AclConstants;
import org.osaf.cosmo.dav.acl.property.AlternateUriSet;
import org.osaf.cosmo.dav.acl.property.GroupMembership;
import org.osaf.cosmo.dav.acl.property.PrincipalUrl;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.property.CalendarHomeSet;
import org.osaf.cosmo.dav.impl.DavResourceBase;
import org.osaf.cosmo.dav.property.CreationDate;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.dav.property.DisplayName;
import org.osaf.cosmo.dav.property.Etag;
import org.osaf.cosmo.dav.property.IsCollection;
import org.osaf.cosmo.dav.property.LastModified;
import org.osaf.cosmo.dav.property.ResourceType;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PathUtil;

/**
* <p>
* Models a WebDAV principal resource (as described in RFC 3744) that
* represents a user account.
* </p>
 *
 * @see DavContent
 * @see DavResourceBase
 * @see User
 */
public class DavUserPrincipal extends DavResourceBase
    implements AclConstants, CaldavConstants, DavContent {
    private static final Log log = LogFactory.getLog(DavUserPrincipal.class);

    static {
        registerLiveProperty(DavPropertyName.CREATIONDATE);
        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(CALENDARHOMESET);
        registerLiveProperty(ALTERNATEURISET);
        registerLiveProperty(PRINCIPALURL);
        registerLiveProperty(GROUPMEMBERSHIP);
    }

    private User user;
    private DavUserPrincipalCollection parent;

    public DavUserPrincipal(User user,
                            DavResourceLocator locator,
                            DavResourceFactory factory)
        throws DavException {
        super(locator, factory);
        this.user = user;
    }


    // Jackrabbit DavResource

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH";
    }

    public boolean isCollection() {
        return true;
    }

    public long getModificationTime() {
        return user.getModifiedDate().getTime();
    }

    public boolean exists() {
        return true;
    }

    public String getDisplayName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    public String getETag() {
        return "\"" + user.getEntityTag() + "\"";
    }

    public void writeTo(OutputContext context)
        throws DavException, IOException {
        context.setContentType(IOUtil.buildContentType("text/plain",
                                                       "UTF-8"));

        if (! context.hasStream())
            return;

        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(context.getOutputStream(),
                                                   "utf8"));
        writer.write(user.getUsername());
        writer.write("\n");
        writer.close();
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
                          InputContext inputContext)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        // while it would be ideal to throw an UnsupportedOperationException,
        // MultiStatus tries to add a MultiStatusResponse for every member
        // of a DavResource regardless of whether or not it's a collection,
        // so we need to return an empty iterator.
        return new DavResourceIteratorImpl(new ArrayList());
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResource getCollection() {
        try {
            return getParent();
        } catch (DavException e) {
            throw new RuntimeException(e);
        }
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

    // DavResource methods

    public DavCollection getParent()
        throws DavException {
        if (parent == null) {
            DavResourceLocator parentLocator =
                getResourceLocator().getParentLocator();
            parent = (DavUserPrincipalCollection)
                getResourceFactory().resolve(parentLocator);
        }

        return parent;
    }

    public Report getReport(ReportInfo reportInfo)
        throws DavException {
        throw new UnsupportedOperationException();
    }

    // our methods

    public User getUser() {
        return user;
    }

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(1);
        rt.add(RESOURCE_TYPE_PRINCIPAL);
        return rt;
    }
    
    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new CreationDate(user.getCreationDate()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        properties.add(new Etag(user.getEntityTag()));
        properties.add(new LastModified(user.getModifiedDate()));
        properties.add(new CalendarHomeSet(getResourceLocator(), user));
        properties.add(new AlternateUriSet(getResourceLocator(), user));
        properties.add(new PrincipalUrl(getResourceLocator(), user));
        properties.add(new GroupMembership(getResourceLocator(), user));
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
        throw new ForbiddenException("Dead properties are not supported on this resource");
    }

    protected void removeDeadProperty(DavPropertyName name)
        throws DavException {
        throw new ForbiddenException("Dead properties are not supported on this resource");
    }
}
