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
package org.osaf.cosmo.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Extends {@link Item} to represent an item containing binary content
 */
public class ContentItem extends Item {

    /**
     * 
     */
    private static final long serialVersionUID = 4904755977871771389L;
    
    // ContentItem specific attributes
    public static final String ATTR_CONTENT_MIMETYPE = "content:mimeType";
    public static final String ATTR_CONTENT_ENCODING = "content:encoding";
    public static final String ATTR_CONTENT_CONTENTLANGUAGE = "content:contentLanguage";
    public static final String ATTR_CONTENT_DATA = "content:data";
    public static final String ATTR_CONTENT_LENGTH = "content:length";

    public ContentItem() {
    }

    public byte[] getContent() {
        return (byte[]) getAttributeValue(ATTR_CONTENT_DATA);
    }

    public void setContent(byte[] content) {
        setAttribute(ATTR_CONTENT_DATA, content);
    }

    public void setContent(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(is, bos);
        setContent(bos.toByteArray());
    }

    public InputStream getContentInputStream() {
        return new ByteArrayInputStream(getContent());
    }

    public String getContentEncoding() {
        return (String) getAttributeValue(ATTR_CONTENT_ENCODING);
    }

    public void setContentEncoding(String contentEncoding) {
        // allow for nulls
        addStringAttribute(ATTR_CONTENT_ENCODING, contentEncoding);
    }

    public String getContentLanguage() {
        return (String) getAttributeValue(ATTR_CONTENT_CONTENTLANGUAGE);
    }

    public void setContentLanguage(String contentLanguage) {
        // allow for nulls
        addStringAttribute(ATTR_CONTENT_CONTENTLANGUAGE, contentLanguage);
    }

    public String getContentType() {
        return (String) getAttributeValue(ATTR_CONTENT_MIMETYPE);
    }

    public void setContentType(String contentType) {
        setAttribute(ATTR_CONTENT_MIMETYPE, contentType);
    }

    public Long getContentLength() {
       return (Long) getAttributeValue(ATTR_CONTENT_LENGTH);
    }

    public void setContentLength(Long contentLength) {
        setAttribute(ATTR_CONTENT_LENGTH, contentLength);
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append(
                "contentLength", getContentLength()).append("contentType",
                getContentType()).append("contentEncoding",
                getContentEncoding()).append("contentLanguage",
                getContentLanguage()).toString();
    }


}
