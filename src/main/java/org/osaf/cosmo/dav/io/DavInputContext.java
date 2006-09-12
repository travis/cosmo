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
package org.osaf.cosmo.dav.io;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.io.InputContextImpl;

import org.osaf.cosmo.dav.CosmoDavMethods;
import org.osaf.cosmo.dav.caldav.CaldavConstants;

/**
 * An <code>InputContext</code> that supports the semantics of DAV
 * extensions like CalDAV.
 *
 * @see org.apache.jackrabbit.webdav.io.InputContext
 */
public class DavInputContext extends InputContextImpl
    implements CaldavConstants {
    private static final Log log = LogFactory.getLog(DavInputContext.class);

    private String contentType;

    /**
     * If the HTTP request method is MKCALENDAR, sets the context's
     * content type to indicate calendar collection.
     */
    public DavInputContext(HttpServletRequest request,
                           InputStream in) {
        super(request, in);
        if (request.getMethod().equals(CosmoDavMethods.METHOD_MKCALENDAR)) {
            contentType = CONTENT_TYPE_CALENDAR_COLLECTION;
        }
    }

    // InputContext methods

    /**
     * If the content type has been explicitly set, return
     * that. Otherwise, defer to the superclass method.
     */
    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        return super.getContentType();
    }
}
