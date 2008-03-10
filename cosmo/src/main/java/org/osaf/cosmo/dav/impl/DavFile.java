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
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.property.ContentLanguage;
import org.osaf.cosmo.dav.property.ContentLength;
import org.osaf.cosmo.dav.property.ContentType;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.model.DataSizeException;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.FileItem;

/**
 * Extends <code>DavResourceBase</code> to adapt the Cosmo
 * <code>FileItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:getcontentlanguage</code></li>
 * <li><code>DAV:getcontentlength</code> (protected)</li>
 * <li><code>DAV:getcontenttype</code></li>
 * </ul>
 *
 * @see DavContent
 * @see FileItem
 */
public class DavFile extends DavContentBase {
    private static final Log log = LogFactory.getLog(DavFile.class);

    static {
        registerLiveProperty(DavPropertyName.GETCONTENTLANGUAGE);
        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);
    }

    /** */
    public DavFile(FileItem item,
                   DavResourceLocator locator,
                   DavResourceFactory factory,
                   EntityFactory entityFactory)
        throws DavException {
        super(item, locator, factory, entityFactory);
    }

    /** */
    public DavFile(DavResourceLocator locator,
                   DavResourceFactory factory,
                   EntityFactory entityFactory)
        throws DavException {
        this(entityFactory.createFileItem(), locator, factory, entityFactory);
    }

    // DavResource

    public void writeTo(OutputContext outputContext)
        throws DavException, IOException {
        if (! exists())
            throw new IllegalStateException("cannot spool a nonexistent resource");

        if (log.isDebugEnabled())
            log.debug("spooling file " + getResourcePath());

        FileItem content = (FileItem) getItem();

        String contentType =
            IOUtil.buildContentType(content.getContentType(),
                                    content.getContentEncoding());
        outputContext.setContentType(contentType);

        if (content.getContentLanguage() != null)
            outputContext.setContentLanguage(content.getContentLanguage());

        long len = content.getContentLength() != null ?
            content.getContentLength().longValue() : 0;
        outputContext.setContentLength(len);
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());

        if (! outputContext.hasStream())
            return;
        if (content.getContentInputStream() == null)
            return;

        IOUtil.spool(content.getContentInputStream(),
                     outputContext.getOutputStream());
    }

    
    /** */
    protected void populateItem(InputContext inputContext)
        throws DavException {
        super.populateItem(inputContext);

        FileItem file = (FileItem) getItem();

        try {
            InputStream content = inputContext.getInputStream();
            if (content != null)
                file.setContent(content);

            if (inputContext.getContentLanguage() != null)
                file.setContentLanguage(inputContext.getContentLanguage());

            String contentType = inputContext.getContentType();
            if (contentType != null)
                file.setContentType(IOUtil.getMimeType(contentType));
            else
                file.setContentType(IOUtil.MIME_RESOLVER.
                                    getMimeType(file.getName()));

            String contentEncoding = IOUtil.getEncoding(contentType);
            if (contentEncoding != null)
                file.setContentEncoding(contentEncoding);
        } catch (IOException e) {
            throw new DavException(e);
        } catch (DataSizeException e) {
            throw new ForbiddenException(e.getMessage());
        }
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        FileItem content = (FileItem) getItem();
        if (content == null)
            return;

        if (content.getContentLanguage() != null)
            properties.add(new ContentLanguage(content.getContentLanguage()));
        properties.add(new ContentLength(content.getContentLength()));
        properties.add(new ContentType(content.getContentType(),
                                       content.getContentEncoding()));
    }

    /** */
    protected void setLiveProperty(DavProperty property)
        throws DavException {
        super.setLiveProperty(property);

        FileItem content = (FileItem) getItem();
        if (content == null)
            return;

        DavPropertyName name = property.getName();
        String text = property.getValueText();

        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
            content.setContentLanguage(text);
            return;
        }

        if (name.equals(DavPropertyName.GETCONTENTTYPE)) {
            String type = IOUtil.getMimeType(text);
            if (StringUtils.isBlank(type))
                throw new BadRequestException("Property " + name + " requires a valid media type");
            content.setContentType(type);
            content.setContentEncoding(IOUtil.getEncoding(text));
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
        throws DavException {
        super.removeLiveProperty(name);

        FileItem content = (FileItem) getItem();
        if (content == null)
            return;

        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
            content.setContentLanguage(null);
            return;
        }
    }
}
