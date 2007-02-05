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
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;



/**
 * Represents the data of a piece of Content.  For now the
 * data is stored in memory as a byte[].  For the next
 * release, this will be changed to store the data on
 * the disk, to prevent OutOfMemoryExceptions.
 */
@Entity
@Table(name="content_data")
public class ContentData extends BaseModelObject {

    /**
     * 
     */
    private static final long serialVersionUID = -5014854905531456753L;
    private byte[] content = null;
   
    /**
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }


    /**
     * Get an InputStream to the content data.  Repeated
     * calls to this method will return new instances
     * of InputStream.
     */
    @Transient
    public InputStream getContentInputStream() {
        if(content==null)
            return null;
        
        // For now, return byte[] inputstream, later
        // this will be a FileInputStream most likely
        return new ByteArrayInputStream(content);
    }
    
    /**
     * Set the content using an InputSteam.  Does not close the 
     * InputStream.
     * @param is content data
     * @throws IOException
     */
    public void setContentInputStream(InputStream is) throws IOException {
        // For now use byte[], for .6 use temp File
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(is, bos);
        content = bos.toByteArray();
    }
    
    /**
     * @return the size of the data read, or -1 for no data present
     */
    @Transient
    public long getSize() {
        if(content != null)
            return content.length;
        else
            return -1;
    }

    @Column(name = "content", length=102400000)
    @Type(type="bytearray_blob")
    private byte[] getContent() {
        return content;
    }

    private void setContent(byte[] content) {
        this.content = content;
    }
    
}
