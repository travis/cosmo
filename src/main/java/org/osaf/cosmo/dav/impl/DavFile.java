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
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
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

import org.apache.log4j.Logger;

import org.osaf.cosmo.model.DataSizeException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ModelValidationException;

/**
 * Extends <code>DavResourceBase</code> to adapt the Cosmo
 * <code>ContentItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:getcontentlanguage</code></li>
 * <li><code>DAV:getcontentlength</code> (protected)</li>
 * <li><code>DAV:getcontenttype</code></li>
 * <li><code>DAV:getetag</code> (protected)</li>
 * <li><code>DAV:getlastmodified</code> (protected)</li>
 * </ul>
 *
 * @see DavResourceBase
 * @see ContentItem
 */
public class DavFile extends DavResourceBase {
    private static final Logger log = Logger.getLogger(DavFile.class);
    private static final int[] RESOURCE_TYPES;
    private static final Set<String> DEAD_PROPERTY_FILTER =
        new HashSet<String>();

    private String etag;

    static {
        registerLiveProperty(DavPropertyName.GETCONTENTLANGUAGE);
        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);

        RESOURCE_TYPES = new int[] { ResourceType.DEFAULT_RESOURCE };

        DEAD_PROPERTY_FILTER.add(ContentItem.ATTR_CONTENT_MIMETYPE);
        DEAD_PROPERTY_FILTER.add(ContentItem.ATTR_CONTENT_ENCODING);
        DEAD_PROPERTY_FILTER.add(ContentItem.ATTR_CONTENT_CONTENTLANGUAGE);
        DEAD_PROPERTY_FILTER.add(ContentItem.ATTR_CONTENT_DATA);
        DEAD_PROPERTY_FILTER.add(ContentItem.ATTR_CONTENT_LENGTH);
    }

    /** */
    public DavFile(ContentItem item,
                   DavResourceLocator locator,
                   DavResourceFactory factory,
                   DavSession session) {
        super(item, locator, factory, session);

        // pre-emptively calculate etag, which will not change until
        // the item content changes
        if (exists())
            this.etag = calculateEtag();
    }

    /** */
    public DavFile(DavResourceLocator locator,
                   DavResourceFactory factory,
                   DavSession session) {
        this(new ContentItem(), locator, factory, session);
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
        if (etag == null) {
            etag = calculateEtag();
        }
        return etag;
    }

    /** */
    public void spool(OutputContext outputContext)
        throws IOException {
        if (! exists())
            throw new IllegalStateException("cannot spool a nonexistent resource");

        if (log.isDebugEnabled())
            log.debug("spooling file " + getResourcePath());

        ContentItem content = (ContentItem) getItem();

        String contentType =
            IOUtil.buildContentType(content.getContentType(),
                                    content.getContentEncoding());
        outputContext.setContentType(contentType);

        if (content.getContentLanguage() != null)
            outputContext.setContentLanguage(content.getContentLanguage());

        outputContext.setContentLength(content.getContentLength().longValue());
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());

        if (! outputContext.hasStream())
            return;

        IOUtil.spool(content.getContentInputStream(),
                     outputContext.getOutputStream());
    }

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

        if (inputContext.hasStream()) {
            try {
                // XXX: read to tmp file, checking bytes read against
                // content length
                content.setContent(inputContext.getInputStream());
            } catch (IOException e) {
                log.error("Cannot read resource content", e);
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot read resource content: " + e.getMessage());
            } catch (DataSizeException e) {
                throw new DavException(DavServletResponse.SC_FORBIDDEN, "Cannot store resource content: " + e.getMessage());
            }

        }

        try {
            if (inputContext.getContentLanguage() != null)
                content.setContentLanguage(inputContext.getContentLanguage());

            String contentType = inputContext.getContentType();
            if (contentType != null)
                content.setContentType(IOUtil.getMimeType(contentType));
            else
                content.setContentType(IOUtil.MIME_RESOLVER.
                                       getMimeType(content.getName()));

            String contentEncoding = IOUtil.getEncoding(contentType);
            if (contentEncoding != null)
                content.setContentEncoding(contentEncoding);
        } catch (DataSizeException e) {
            throw new DavException(DavServletResponse.SC_FORBIDDEN, "Cannot store resource attribute: " + e.getMessage());
        }
    }

    /** */
    protected void loadLiveProperties() {
        super.loadLiveProperties();

        ContentItem content = (ContentItem) getItem();
        if (content == null)
            return;

        DavPropertySet properties = getProperties();

        if (content.getContentLanguage() != null) {
            properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLANGUAGE,
                                                  content.getContentLanguage()));
        }

        properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLENGTH,
                                              content.getContentLength()));

        properties.add(new DefaultDavProperty(DavPropertyName.GETETAG,
                                              getETag()));

        String contentType =
            IOUtil.buildContentType(content.getContentType(),
                                    content.getContentEncoding());
        properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE,
                                              contentType));

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

        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
            content.setContentLanguage(value);
            return;
        }

        if (name.equals(DavPropertyName.GETCONTENTTYPE)) {
            String type = IOUtil.getMimeType(value);
            if (type == null)
                throw new ModelValidationException("null mime type for property " + name);
            content.setContentType(type);
            content.setContentEncoding(IOUtil.getEncoding(value));
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

        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
            content.setContentLanguage(null);
            return;
        }

        if (name.equals(DavPropertyName.GETCONTENTTYPE))
            throw new ModelValidationException("cannot remove property " + name);
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        return DEAD_PROPERTY_FILTER;
    }

    private String calculateEtag() {
        ContentItem content = (ContentItem) getItem();
        String length = content.getContentLength() != null ?
            content.getContentLength().toString() : "-";
        String modTime = content.getModifiedDate() != null ?
            new Long(content.getModifiedDate().getTime()).toString() : "-";
        return "\"" + length + "-" + modTime + "\"";
   }
}
