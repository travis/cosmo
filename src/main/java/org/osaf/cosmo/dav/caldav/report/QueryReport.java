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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dav.caldav.CaldavRequestHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the <code>CALDAV:calendar-query</code> report that
 * provides a mechanism for finding calendar resources matching
 * specified criteria. It should be supported by all CalDAV
 * resources. <p/> CalDAV specifies the following required format for
 * the request body:
 *
 * <pre>
 *     &lt;!ELEMENT calendar-query (DAV:allprop | DAV:propname | DAV:prop)?
 *                   filter&gt;
 * </pre>
 */
public class QueryReport extends CaldavMultiStatusReport {
    private static final Log log = LogFactory.getLog(QueryReport.class);

    // Report methods

    private VTimeZone tz;

    /** */
    public ReportType getType() {
        return CALDAV_QUERY;
    }

    // CaldavReport methods

    /**
     * Parse property, timezone and filter information from the given
     * report info. Set query filter if the
     * <code>CALDAV:filter</code> property is included.
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

        tz = findTimeZone(info);
        setQueryFilter(findQueryFilter(info));
    }

    private VTimeZone findTimeZone(ReportInfo info)
        throws DavException {
        Element propdata = DomUtil.getChildElement(info.getReportElement(),
                                                   XML_PROP, NAMESPACE);
        if (propdata == null) {
            return null;
        }
        Element tzdata = DomUtil.
            getChildElement(propdata, ELEMENT_CALDAV_TIMEZONE,
                            NAMESPACE_CALDAV);
        if (tzdata == null) {
            return null;
        }
        String icaltz = DomUtil.getTextTrim(tzdata);

        try {
            return CaldavRequestHelper.extractTimeZone(icaltz);
        } catch (Exception e) {
            log.error("unable to extract CALDAV:timezone", e);
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "unable to extract CALDAV:timezone: " + e.getMessage());
        }
    }

    private CalendarFilter findQueryFilter(ReportInfo info)
        throws DavException {
        // XXX
        return new CalendarFilter();

//         Element filterdata =
//             DomUtil.getChildElement(info.getReportElement(),
//                                     ELEMENT_CALDAV_FILTER, NAMESPACE_CALDAV);
//         if (filterdata == null) {
//             return null;
//         }

//         CaldavQueryFilter qf = new CaldavQueryFilter();
//         if (tz == null) {
//             // if no timezone was specified in the report info, fall
//             // back to one stored with the calendar collection, if any
//             try {
//                 DavProperty prop = getResource().getProperty(CALENDARTIMEZONE);
//                 tz = CaldavRequestHelper.extractTimeZone(prop);
//             } catch (Exception e) {
//                     // this exception should never be raised since the
//                     // timezone was validated when it was added as a
//                     // property of the calendar collection
//                     log.error("invalid CALDAV:timezone property on resource " + getResource().getResourcePath(), e);
//                     throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "invalid CALDAV:timezone property: " + e.getMessage());
//             }
//         }

//         qf.setTimezone(tz);

//         try {
//             qf.createFromXml(filterdata);
//         } catch (ParseException e) {
//             log.error("error parsing CALDAV:calendar-data", e);
//             throw new DavException(DavServletResponse.SC_BAD_REQUEST, "error parsing CALDAV:calendar-data: " + e.getMessage());
//         }

//         return qf;
    }
}
