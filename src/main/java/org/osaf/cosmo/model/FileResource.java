/*
 * Copyright 2005 Open Source Applications Foundation
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

import java.io.InputStream;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Extends {@link Resource} to represent a file-based resource.
 */
public class FileResource extends Resource {

    private Long contentLength;
    private String contentType;
    private String contentEncoding;
    private String contentLanguage;
    private InputStream content;
    private Date dateModified;

    /**
     */
    public FileResource() {
        super();
    }

    /**
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     */
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     */
    public String getContentType() {
        return contentType;
    }

    /**
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     */
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     */
    public InputStream getContent() {
        return content;
    }

    /**
     */
    public void setContent(InputStream content) {
        this.content = content;
    }

    /**
     */
    public Date getDateModified() {
        return dateModified;
    }

    /**
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof FileResource)) {
            return false;
        }
        FileResource it = (FileResource) o;
        return new EqualsBuilder().
            appendSuper(super.equals(o)).
            append(contentLength, it.contentLength).
            append(contentType, it.contentType).
            append(contentEncoding, it.contentEncoding).
            append(contentLanguage, it.contentLanguage).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(11, 13).
            appendSuper(super.hashCode()).
            append(contentLength).
            append(contentType).
            append(contentEncoding).
            append(contentLanguage).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            appendSuper(super.toString()).
            append("contentLength", contentLength).
            append("contentType", contentType).
            append("contentEncoding", contentEncoding).
            append("contentLanguage", contentLanguage).
            toString();
    }
}
