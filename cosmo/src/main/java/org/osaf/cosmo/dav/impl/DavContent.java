/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.User;

/**
 * Extends <code>DavResourceBase</code> to adapt the Cosmo
 * <code>ContentItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:getetag</code> (protected)</li>
 * <li><code>DAV:getlastmodified</code> (protected)</li>
 * </ul>
 *
 * @see DavResourceBase
 * @see ContentItem
 */
public abstract class DavContent extends DavResourceBase {
    private static final Log log = LogFactory.getLog(DavContent.class);
    private static final int[] RESOURCE_TYPES;
    private static final Set<String> DEAD_PROPERTY_FILTER =
        new HashSet<String>();

    static {
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);

        RESOURCE_TYPES = new int[] { ResourceType.DEFAULT_RESOURCE };
    }

    /** */
    public DavContent(ContentItem item,
                   DavResourceLocator locator,
                   DavResourceFactory factory,
                   DavSession session) {
        super(item, locator, factory, session);
    }


    // DavResource

    /** */
    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, POST, TRACE, PROPFIND, PROPPATCH, COPY, PUT, DELETE, MOVE, MKTICKET, DELTICKET";
    }

    /** */
    public long getModificationTime() {
        if (getItem() == null)
            return -1;
        if (getItem().getModifiedDate() == null)
            return new Date().getTime();
        return getItem().getModifiedDate().getTime();
    }

    /** */
    public String getETag() {
        if (getItem() == null)
            return null;
        return "\"" + getItem().getEntityTag() + "\"";
    }

    /** */
    public abstract void spool(OutputContext outputContext)
        throws IOException;

    /** */
    public void addMember(DavResource resource,
                          InputContext inputContext)
        throws DavException {
        throw new UnsupportedOperationException();
    }

    /** */
    public MultiStatusResponse addMember(DavResource member,
                                         InputContext inputContext,
                                         DavPropertySet setProperties)
        throws DavException {
        throw new UnsupportedOperationException();
    }

    /** */
    public DavResourceIterator getMembers() {
        return new DavResourceIteratorImpl(new ArrayList());
    }

    /** */
    public DavResource findMember(String href)
        throws DavException {
        throw new DavException(DavServletResponse.SC_BAD_REQUEST);
    }

    /** */
    public void removeMember(DavResource member)
        throws DavException {
        throw new UnsupportedOperationException();
    }

    /** */
    public Report getReport(ReportInfo reportInfo)
        throws DavException {
        throw new UnsupportedOperationException();
    }

    // our methods

    /** */
    protected int[] getResourceTypes() {
        return RESOURCE_TYPES;
    }

    /** */
    protected void populateItem(InputContext inputContext)
        throws DavException {
        super.populateItem(inputContext);

        ContentItem content = (ContentItem) getItem();
        
        User user = getSecurityManager().getSecurityContext().getUser();
        content.setLastModifiedBy(user != null ? user.getEmail() : "");

        if (content.getUid() == null) {
            content.setTriageStatus(TriageStatus.createInitialized());
            content.setLastModification(ContentItem.Action.CREATED);
            content.setSent(Boolean.FALSE);
            content.setNeedsReply(Boolean.FALSE);
        } else {
            content.setLastModification(ContentItem.Action.EDITED);
        }
    }
    
    
    /** */
    protected void loadLiveProperties() {
        super.loadLiveProperties();

        ContentItem content = (ContentItem) getItem();
        if (content == null)
            return;

        DavPropertySet properties = getProperties();

        properties.add(new DefaultDavProperty(DavPropertyName.GETETAG,
                                              getETag()));

        long modTime = getModificationTime();
        properties.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED,
                                              IOUtil.getLastModified(modTime)));
    }

    /** */
    protected void setLiveProperty(DavProperty property) {
        super.setLiveProperty(property);

        ContentItem content = (ContentItem) getItem();
        if (content == null)
            return;

        DavPropertyName name = property.getName();
        String value = property.getValue().toString();

        if (name.equals(DavPropertyName.GETCONTENTLENGTH) ||
            name.equals(DavPropertyName.GETETAG) ||
            name.equals(DavPropertyName.GETLASTMODIFIED)) {
            throw new ModelValidationException("cannot set protected property " + name);
        }

    }

    /** */
    protected void removeLiveProperty(DavPropertyName name) {
        super.removeLiveProperty(name);

        ContentItem content = (ContentItem) getItem();
        if (content == null)
            return;

        if (name.equals(DavPropertyName.GETCONTENTLENGTH) ||
            name.equals(DavPropertyName.GETETAG) ||
            name.equals(DavPropertyName.GETLASTMODIFIED)) {
            throw new ModelValidationException("cannot remove protected property " + name);
        }

        if (name.equals(DavPropertyName.GETCONTENTTYPE))
            throw new ModelValidationException("cannot remove property " + name);
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        return DEAD_PROPERTY_FILTER;
    }
}
