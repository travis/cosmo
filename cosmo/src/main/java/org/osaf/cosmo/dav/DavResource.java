/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav;

import java.io.IOException;

import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

/**
 * An interface providing resource functionality required by WebDAV
 * extensions implemented by Cosmo.
 */
public interface DavResource
    extends org.apache.jackrabbit.webdav.DavResource {

    /**
     * String constant representing the WebDAV 1 compliance
     * class as well as the Cosmo extended classes.
     */
    // see bug 5137 for why we don't include class 2
    public String COMPLIANCE_CLASS =
        "1, 3, access-control, calendar-access, ticket";

    /**
     * Returns the parent collection for this resource.
     */
    public DavCollection getParent() throws DavException;

    public MultiStatusResponse
        updateProperties(DavPropertySet setProperties,
                         DavPropertyNameSet removePropertyNames)
        throws DavException;

    public void writeTo(OutputContext out)
        throws DavException, IOException;

    /**
     * Return the report that matches the given report info if it is
     * supported by this resource.
     */
    public Report getReport(ReportInfo info)
        throws DavException;

    public DavResourceFactory getResourceFactory();

    public DavResourceLocator getResourceLocator();
}
