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
import java.util.List;
import java.util.Vector;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.dav.report.ReportType;

import org.w3c.dom.Element;

/**
 * @author cyrusdaboo
 * 
 * <code>MultigetReport</code> encapsulates the CALDAV:calendar-multiget
 * report, that provides a mechanism for retrieving in one request the
 * properties and filtered calendar data from the resources identified by
 * supplied DAV:href elements. It should be supported by all CalDAV resources.
 * <p/> CalDAV specifies the following required format for the request body:
 * 
 * <pre>
 *                       &lt;!ELEMENT calendar-multiget (DAV:allprop | DAV:propname | DAV:prop)?
 *                                      DAV:href+&gt;
 * </pre>
 * 
 */
public class MultigetReport extends AbstractCalendarDataReport {

    /**
     * Returns {@link ReportType#CALDAV_MULTIGET}.
     * 
     * @return
     * @see Report#getType()
     */
    public ReportType getType() {
        return ReportType.CALDAV_MULTIGET;
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
                || !CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_MULTIGET
                        .equals(info.getReportElement().getLocalName())) {
            throw new IllegalArgumentException(
                    "CALDAV:calendar-multiget element expected.");
        }
        this.info = info;

        // Parse the report element.
        // calendar-multiget is basically a PROPFIND request but with a list of
        // hrefs also added in.
        // The code here is pretty much copied from
        // WebdavRequestImpl.parsePropFindRequest.

        propfindProps = new DavPropertyNameSet();
        hrefs = new Vector();
        boolean gotPropType = false;

        ElementIterator i = DomUtil.getChildren(info.getReportElement());
        while (i.hasNext()) {
            Element child = i.nextElement();
            String nodeName = child.getLocalName();
            if (XML_PROP.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-multiget must contain only one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_BY_PROPERTY;
                propfindProps = new DavPropertyNameSet(child);
                gotPropType = true;

                // Look for CALDAV:calendar-data element as a property
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
                }
                //If a CALDAV:calendar-data found remove value
                //from the propfindProps since it is managed explicitly.
                if (calendarDataElement != null)
                    propfindProps.remove(CosmoDavConstants.CALENDARDATA);

            } else if (XML_PROPNAME.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-multiget must contain only " +
                            "one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_PROPERTY_NAMES;
                gotPropType = true;
            } else if (XML_ALLPROP.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-multiget must contain only one " +
                            "prop/propname/allprop element.");
                }
                propfindType = PROPFIND_ALL_PROP;
                gotPropType = true;
            } else if (XML_HREF.equals(nodeName)) {
                hrefs.add(DomUtil.getTextTrim(child));
            }
        }

        // Must have at least one href
        if (hrefs.size() == 0) {
            throw new IllegalArgumentException(
                    "CALDAV:calendar-multiget must contain at least one href element.");
        }
    }
}
