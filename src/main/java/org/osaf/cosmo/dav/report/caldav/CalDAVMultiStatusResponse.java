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

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.dav.CosmoDavConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author cyrusdaboo
 * 
 * This class extends the jackrabbit MultiStatusResponse by adding the ability
 * to return a calendar-data elements as needed by some CalDAV reports.
 */

public class CalDAVMultiStatusResponse extends MultiStatusResponse {

    /**
     * The calendar data as text.
     */
    private String calendarData;

    public CalDAVMultiStatusResponse(DavResource resource,
                                     DavPropertyNameSet propNameSet,
                                     int propFindType) {
        super(resource, propNameSet, propFindType);
    }

    /**
     * @param calendarData
     *            The calendarData to set.
     */
    public void setCalendarData(String calendarData) {
        this.calendarData = calendarData;
    }

    /*
     */
    public Element toXml(Document doc) {
        // Generate calendar-data element if required
        Element cdata = null;
        if (calendarData != null) {
            cdata =
                DomUtil.createElement(doc,
                                      CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                                      CosmoDavConstants.NAMESPACE_CALDAV);
            DomUtil.setText(cdata, calendarData);
        }

        if (cdata != null) {
            // Create DavProperty for this data
            CalendarDataProperty prop = new CalendarDataProperty(cdata);
            add(prop);
        }

        // Get standard multistatus response from superclass
        Element response = super.toXml(doc);

        return response;
    }

    private class CalendarDataProperty extends AbstractDavProperty {

        private Element calendarData;

        CalendarDataProperty(Element calendarData) {
            super(DavPropertyName.create(
                    CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                    CosmoDavConstants.NAMESPACE_CALDAV), true);
            this.calendarData = calendarData;
        }

        public Object getValue() {
            return null;
        }

        /*
         */
        public Element toXml(Document doc) {
            return calendarData;
        }
    }
}
