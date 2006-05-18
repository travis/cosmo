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
package org.osaf.cosmo.dav.caldav;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.report.CosmoReportType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the <code>CALDAV:calendar-multiget</code> report that
 * provides a mechanism for retrieving in one request the properties
 * and filtered calendar data from the resources identifed by the
 * supplied <code>DAV:href</code> elements. It should
 * be supported by all CalDAV resources. <p/> CalDAV specifies the
 * following required format for the request body:
 *
 * <pre>
 *                       &lt;!ELEMENT calendar-multiget (DAV:allprop | DAV:propname | DAV:prop)?
 *                                      DAV:href+&gt;
 * </pre>
 */
public class MultigetReport extends CaldavMultiStatusReport
    implements DavConstants {
    private static final Log log = LogFactory.getLog(MultigetReport.class);

    // Report methods

    /** */
    public ReportType getType() {
        return CosmoReportType.CALDAV_MULTIGET;
    }

    // CaldavReport methods

    /**
     * Parse property and href information from the given report
     * info. Set output filter if the
     * <code>CALDAV:calendar-data</code> property is included.
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_MULTIGET.
            equals(info.getReportName())) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "CALDAV:calendar-multiget element expected.");
        }

        setPropFindProps(info.getPropertyNameSet());
        if (info.containsContentElement(XML_ALLPROP, NAMESPACE)) {
            setPropFindType(PROPFIND_ALL_PROP);
        } else if (info.containsContentElement(XML_PROPNAME, NAMESPACE)) {
            setPropFindType(PROPFIND_PROPERTY_NAMES);
        } else {
            setPropFindType(PROPFIND_BY_PROPERTY);
            setOutputFilter(findOutputFilter(info));
        }

        // find hrefs
        ArrayList hrefs = new ArrayList();
        List hrefElements = info.getContentElements(XML_HREF, NAMESPACE);
        for (Iterator i=hrefElements.iterator(); i.hasNext();) {
            hrefs.add(DomUtil.getTextTrim((Element) i.next()));
        }

        // must have at least one href
        if (hrefs.size() == 0) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "multiget report must contain at least one href element");
        }

        setHrefs(hrefs);
    }

    /**
     * Does nothing, since the hrefs are provided by the report info
     * rather than computed by a query.
     */
    protected void runQuery()
        throws DavException {
    }
}
