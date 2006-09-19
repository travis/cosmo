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
package org.osaf.cosmo.dav.caldav.report;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.impl.DavCalendarResource;

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
public class MultigetReport extends CaldavMultiStatusReport {
    private static final Log log = LogFactory.getLog(MultigetReport.class);

    /** */
    public static final ReportType REPORT_TYPE_CALDAV_MULTIGET =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_MULTIGET,
                            NAMESPACE_CALDAV, MultigetReport.class);

    private Set<String> hrefs;

    // Report methods

    /** */
    public ReportType getType() {
        return REPORT_TYPE_CALDAV_MULTIGET;
    }

    // CaldavReport methods

    /**
     * Parse property and href information from the given report
     * info. Set output filter if the
     * <code>CALDAV:calendar-data</code> property is included.
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! getType().isRequestedReportType(info)) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "report not of type " + getType());
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

        List<Element> hrefElements =
            info.getContentElements(XML_HREF, NAMESPACE);
        if (hrefElements.size() == 0) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "multiget report must contain at least one href element");
        }
        if (! getResource().isCollection() && hrefElements.size() > 1)
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "multiget report against a calendar object resource may only contain one href");

        String parentHref = getResource().getHref();
        String host = parentHref.substring(0, parentHref.indexOf("/", 8));

        // validate submitted hrefs
        hrefs = new HashSet<String>();
        for (Element element : hrefElements) {
            String href = DomUtil.getTextTrim(element);
            // Note that the href sent by the client may be relative,
            // so we need to convert to absolute for subsequent
            // comparisons
            if (! href.startsWith("http"))
                href = host + href;

            // Check if the href represents the requested resource or
            // a child of it
            if (getResource().isCollection()) {
                if (! isDescendentOrEqual(parentHref, href)) {
                    throw new DavException(DavServletResponse.SC_BAD_REQUEST, "href " + href + " does no refer to a child of the requested collection or the collection itself");
                } 
            } else {
                if (! href.equals(parentHref)) {
                    throw new DavException(DavServletResponse.SC_BAD_REQUEST, "href " + href + " does not refer to the requested calendar object resource");
                }
            }

            hrefs.add(href);
        }
    }

    /**
     * Resolves the hrefs provided in the report info to resources.
     */
    protected void runQuery()
        throws DavException {
        for (String href : hrefs) {
            DavCalendarResource target = (DavCalendarResource)
                getResource().findMember(href);
            if (target != null)
                getMultiStatus().addResponse(buildMultiStatusResponse(target));
            else
                getMultiStatus().addResponse(new MultiStatusResponse(href, DavServletResponse.SC_NOT_FOUND));
        }
    }


    private boolean isDescendentOrEqual(String path, String descendant) {
        if (path.equals(descendant)) {
            return true;
        } else {
            String pattern = path.endsWith("/") ? path : path + "/";
            return descendant.startsWith(pattern);
        }
    }
}
