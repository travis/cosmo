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
package org.osaf.cosmo.dav.report;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

/**
 * This class is copied pretty much verbatim from
 * org.apache.jackrabit.webdav.version.Report.
 */

/**
 * The <code>Report</code> interface defines METHODS needed in order to
 * respond to a REPORT request. The REPORT method is a required feature to all
 * resources.
 * 
 * @see DavResource#getReport(ReportInfo)
 */
public interface Report extends XmlSerializable {

    /**
     * Returns the registered type of this report.
     * 
     * @return the type of this report.
     */
    public ReportType getType();

    /**
     * Set the <code>DavResource</code> for which this report was requested.
     * 
     * @param resource
     */
    public void setResource(DavResource resource);

    /**
     * Set the <code>ReportInfo</code> as specified by the REPORT request
     * body, that defines the details for this report.
     * 
     * @param info
     *            providing in detail requirements for this report.
     */
    public void setInfo(ReportInfo info);
}
