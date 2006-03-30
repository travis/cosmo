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
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.dav.report.ReportType;
import org.osaf.cosmo.util.CosmoUtil;

import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VTimeZone;

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
        VTimeZone tz  = null;
        Element filterEl = null;
        boolean gotPropType = false;

        ElementIterator i = DomUtil.getChildren(info.getReportElement());

        while (i.hasNext()) {
            Element child = i.nextElement();
            String nodeName = child.getLocalName();

            //prop element
            if (XML_PROP.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one " +
                            "prop/propname/allprop element.");
                }
                propfindType = PROPFIND_BY_PROPERTY;
                propfindProps = new DavPropertyNameSet(child);
                gotPropType = true;

                Iterator iter = propfindProps.iterator();

                while (iter.hasNext()) {
                    DavPropertyName name = (DavPropertyName) iter.next();

                    //CALDAV:calendar-data
                    if (CosmoDavConstants.CALENDARDATA.equals(name)) {

                        // Now find the calendar-data element inside the prop
                        // element and cache that
                        calendarDataElement =
                            DomUtil.getChildElement(child,
                                CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                                CosmoDavConstants.NAMESPACE_CALDAV);
                    }
                    //CALDAV:timezone
                    else if (CosmoDavConstants.TIMEZONE.equals(name)) {

                        Element tzEl =
                            DomUtil.getChildElement(child,
                                CosmoDavConstants.ELEMENT_CALDAV_TIMEZONE,
                                CosmoDavConstants.NAMESPACE_CALDAV);

                        String icalTz = DomUtil.getTextTrim(tzEl);

                        //(CALDAV:valid-calendar-data)
                        try {
                            tz = CosmoUtil.validateVtimezone(icalTz);
                        } catch (ValidationException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                    }
                }
                //If a CALDAV:calendar-data and /or a CALDAV:timezone found
                //remove theses values from the propfindProps since they
                //are managed explicitly.
                if (calendarDataElement != null) 
                    propfindProps.remove(CosmoDavConstants.CALENDARDATA);

                if (tz != null) 
                    propfindProps.remove(CosmoDavConstants.TIMEZONE);

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

                //Create the filter. Since the order of xml elements is arbitrary
                //we do not parse the element at this time.
                //Once the request has been fully digested and the timezone
                //retrieved or not then we parse the element.
                filter = new QueryFilter();
                filterEl = child;
            }
        }

        if (filter != null) {
            if (tz == null) {
                //If no CALDAV:timezone was specified with the REPORT query
                //then try and get the timezone of the calendar collection
                DavProperty ptz = resource.getProperty(CosmoDavPropertyName.CALENDARTIMEZONE);

                if (ptz != null) {
                    try {
                        tz = CosmoUtil.validateVtimezone((String) ptz.getValue());
                    } catch (ValidationException e) {
                        //This exception should never be raised since the
                        //timezone was validated when it was added as a property of
                        //the calendar collection
                        throw new IllegalArgumentException(e.getMessage());
                    }
                }
            }

            //The value of tz will either be null or a Vtimezone
            filter.setTimezone(tz);

            filter.parseElement(filterEl);
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
