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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * Extends {@link Item} to represent an item containing binary content.
 */
@Entity
@DiscriminatorValue("file")
public class FileItem extends ContentItem {

    
    /**
     * 
     */
    private static final long serialVersionUID = -3829504638044059875L;

    // max content size is smaller than binary attribute value max
    // size
    public static final long MAX_CONTENT_SIZE = 10 * 1024 * 1024;

    @Column(name = "contentType", length=64)
    private String contentType = null;
    
    @Column(name = "contentLanguage", length=32)
    private String contentLanguage = null;
    
    @Column(name = "contentEncoding", length=32)
    private String contentEncoding = null;
    
    @Column(name = "contentLength")
    private Long contentLength = null;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="contentdataid")
    @Cascade( {CascadeType.ALL }) 
    private ContentData contentData = null;
    
    public FileItem() {
    }

   
    /**
     * Get content data as byte[]
     */
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
    
    public void clearContent() {
        contentData = null;
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

    public InputStream getContentInputStream() {
        if(contentData==null)
            return null;
        else
            return contentData.getContentInputStream();
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public Long getContentLength() {
        return contentLength;
    }


    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }


    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public Item copy() {
        FileItem copy = new FileItem();
        copyToItem(copy);
        return copy;
    }
    
    @Override
    protected void copyToItem(Item item) {
        if(!(item instanceof FileItem))
            return;
        
        super.copyToItem(item);
        
        FileItem contentItem = (FileItem) item;
        
        try {
            InputStream contentStream = getContentInputStream();
            if(contentStream!=null) {
                contentItem.setContent(contentStream);
                contentStream.close();
            }
            contentItem.setContentEncoding(getContentEncoding());
            contentItem.setContentLanguage(getContentLanguage());
            contentItem.setContentType(getContentType());
            contentItem.setContentLength(getContentLength());
        } catch (IOException e) {
            throw new RuntimeException("Error copying content");
        }
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
