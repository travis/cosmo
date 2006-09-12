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
package org.osaf.cosmo.dav.caldav.report;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for CalDAV reports that return single (200 OK)
 * responses.
 */
public abstract class CaldavSingleResourceReport extends CaldavReport {
    private static final Log log =
        LogFactory.getLog(CaldavSingleResourceReport.class);

    private String contentType;
    private String encoding;
    private InputStream stream;

    // Report methods

    /**
     * Returns false.
     */
    public boolean isMultiStatusReport() {
        return false;
    }

    /**
     * Write output to the response.
     */
    protected void output(DavServletResponse response)
        throws IOException {
        response.setStatus(DavServletResponse.SC_OK);
        response.setContentType(contentType);
        response.setCharacterEncoding(encoding);
        IOUtil.spool(stream, response.getOutputStream());
        response.flushBuffer();
    }

    // our methods

    /** */
    public String getContentType() {
        return contentType;
    }

    /** */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /** */
    public String getEncoding() {
        return encoding;
    }

    /** */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /** */
    public InputStream getStream() {
        return stream;
    }

    /**
     */
    public void setStream(InputStream stream) {
        this.stream = stream;
    }
}
