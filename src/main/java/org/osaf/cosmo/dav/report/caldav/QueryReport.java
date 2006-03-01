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
package org.osaf.cosmo.dav.report.caldav;

import java.util.Iterator;
import java.util.Vector;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.dav.report.ReportType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author cyrusdaboo
 * 
 * <code>QueryReport</code> encapsulates the CALDAV:calendar-query report,
 * that provides a mechanism for finding calendar resources matching specified
 * criteria. It should be supported by all CalDAV resources. <p/> CalDAV
 * specifies the following required format for the request body:
 * 
 * <pre>
 *     &lt;!ELEMENT calendar-query (DAV:allprop | DAV:propname | DAV:prop)?
 *                   filter&gt;
 * </pre>
 * 
 */
public class QueryReport extends AbstractCalendarQueryReport {
    private static final Log log = LogFactory.getLog(QueryReport.class);

    /**
     * Returns {@link ReportType#CALDAV_QUERY}.
     * 
     * @return
     * @see Report#getType()
     */
    public ReportType getType() {
        return ReportType.CALDAV_QUERY;
    }

    /**
     * Set the <code>ReportInfo</code>.
     * 
     * @param info
     * @throws IllegalArgumentException
     *             if the given <code>ReportInfo</code> does not contain a
     *             DAV:expand-property element.
     * @see Report#setInfo(ReportInfo)
     */
    public void setInfo(ReportInfo info)
        throws IllegalArgumentException {
        if (info == null
                || !CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_QUERY.equals(info
                        .getReportElement().getLocalName())) {
            throw new IllegalArgumentException(
                    "CALDAV:calendar-query element expected.");
        }
        this.info = info;

        // Parse the report element.
        // calendar-query is basically a PROPFIND request but with a filter item
        // added in.
        // The code here is pretty much copied from
        // WebdavRequestImpl.parsePropFindRequest.

        propfindProps = new DavPropertyNameSet();
        hasOldStyleCalendarData = false;
        boolean gotPropType = false;

        ElementIterator i = DomUtil.getChildren(info.getReportElement());
        while (i.hasNext()) {
            Element child = i.nextElement();
            String nodeName = child.getLocalName();
            if (XML_PROP.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_BY_PROPERTY;
                propfindProps = new DavPropertyNameSet(child);
                gotPropType = true;

                // Look for CALDAV:calendar-data element as a property
                Iterator iter = propfindProps.iterator();
                while (iter.hasNext()) {
                    DavPropertyName name = (DavPropertyName) iter.next();
                    if (CosmoDavConstants.CALENDARDATA.equals(name)) {
                        // Remove it from the property list that the report will
                        // return as we will handle this one ourselves
                        propfindProps.remove(name);

                        // Now find the calendar-data element inside the prop
                        // element and cache that
                        calendarDataElement =
                            DomUtil.getChildElement(child,
                                CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                                CosmoDavConstants.NAMESPACE_CALDAV);
                    }
                }
            } else if (XML_PROPNAME.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_PROPERTY_NAMES;
                gotPropType = true;
            } else if (XML_ALLPROP.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_ALL_PROP;
                gotPropType = true;
            } else if (CosmoDavConstants.ELEMENT_CALDAV_FILTER.equals(nodeName)) {

                // Can only have one filter
                if (filter != null) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one filter element.");
                }

                // Parse out filter element
                filter = new QueryFilter();
                filter.parseElement(child);

            } else if (CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA
                    .equals(nodeName)) {
                // TODO this is the old-style calendar-data location. Eventually
                // the option to handle this will go away as old-style clients
                // are updated.
                hasOldStyleCalendarData = true;
                calendarDataElement = child;
            }
        }
    }

    /*
     */
    public Element toXml(Document doc) {
        // This is the meaty part of the query report. What wse do here is
        // execute the query and generate the list of hrefs that match. We then
        // hand that off to the base class to complete the actual multi-status
        // output.

        // Initially empty list of matching hrefs
        hrefs = new Vector();

        try {
            // Create a query and run it
            Query q = getQuery();
            QueryResult qR = q.execute();

            // Convert the query results into hrefs for each result
            queryResultToHrefs(qR);

        } catch (RepositoryException e) {
            String msg = "Error while running CALDAV:" +
                info.getReportElement().getLocalName() + " report";
            log.error(msg, e);
            // XXX: refactor so that the query is executed in a
            // different method that throws a checked exception
            throw new RuntimeException(msg, e);
        }

        // Hand off to parent with list of matching hrefs now complete
        return super.toXml(doc);
    }
}
