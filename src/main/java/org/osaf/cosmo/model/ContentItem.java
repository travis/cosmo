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
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotNull;

/**
 * Extends {@link Item} to represent an item containing binary content
 */
@Entity
@DiscriminatorValue("content")
public class ContentItem extends Item {

    /**
     * 
     */
    private static final long serialVersionUID = 4904755977871771389L;
    
    // max content size is smaller than binary attribute value max
    // size
    public static final long MAX_CONTENT_SIZE = 10 * 1024 * 1024;

    private String contentType = null;
    private String contentLanguage = null;
    private String contentEncoding = null;
    private String lastModifiedBy = null;
    private String triageStatus = null;
    private BigDecimal triageStatusUpdated = null;
    private Long contentLength = null;
    private ContentData contentData = null;
    
    public ContentItem() {
    }

   
    /**
     * Get content data as byte[]
     */
    @Transient
    public byte[] getContent() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream contentStream = contentData.getContentInputStream();
            IOUtils.copy(contentStream, bos);
            contentStream.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error getting content");
        }
    }

    /**
     * Sets content data using byte[]
     * @param content
     */
    public void setContent(byte[] content) {
        if (content.length > MAX_CONTENT_SIZE)
            throw new DataSizeException("Item content too large");
        
        try {
            setContent(new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new RuntimeException("Error setting content");
        }
    }

    /**
     * Set ContentItem's data using InputStream.  The provided InputStream
     * is not closed.
     * @param is data
     * @throws IOException
     */
    public void setContent(InputStream is) throws IOException {
        if(contentData==null) {
            contentData = new ContentData(); 
        }
        
        contentData.setContentInputStream(is);
        
        // Verify size is not greater than MAX.
        // TODO: do this checking in ContentData.setContentInputStream()
        if (contentData.getSize() > MAX_CONTENT_SIZE)
            throw new DataSizeException("Item content too large");
        
        setContentLength(contentData.getSize());
    }

    @Transient
    public InputStream getContentInputStream() {
        if(contentData==null)
            return null;
        else
            return contentData.getContentInputStream();
    }

    @Column(name = "contentEncoding", length=32)
    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    @Column(name = "contentLanguage", length=32)
    public String getContentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    @Column(name = "contentLength")
    @NotNull
    @Min(value=0)
    public Long getContentLength() {
        return contentLength;
    }


    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }


    @Column(name = "contentType", length=64)
    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    @Column(name = "lastmodifiedby", length=255)
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Column(name = "triagestatus", length=64)
    public String getTriageStatus() {
        return triageStatus;
    }

    public void setTriageStatus(String triageStatus) {
        this.triageStatus = triageStatus;
    }

    @Column(name = "triagestatusupdated", precision = 19, scale = 6)
    @Type(type="org.hibernate.type.BigDecimalType")
    public BigDecimal getTriageStatusUpdated() {
        return triageStatusUpdated;
    }

    public void setTriageStatusUpdated(BigDecimal triageStatusUpdated) {
        this.triageStatusUpdated = triageStatusUpdated;
    }

    public void validate() {
        super.validate();
        validateContent();
    }
  
    protected void validateContent() {
        if (getContentLength() == null)
            throw new ModelValidationException("Content Length must be present");

        if (getContentLength().longValue() < 0)
            throw new ModelValidationException("Content Length must be >= 0");

        if (getContentData() == null)
            throw new ModelValidationException("Content must be present");

        if (getContentData().getSize() != getContentLength().longValue())
            throw new ModelValidationException(
                    "Content Length doesn't match Content");
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
    
    // For hibernate use only
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="contentdataid")
    @Cascade( {CascadeType.ALL }) 
    @NotNull
    private ContentData getContentData() {
        return contentData;
    }

    // For hibernate use only
    private void setContentData(ContentData contentFile) {
        this.contentData = contentFile;
    }

}
