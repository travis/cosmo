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

    /**
     * Indicates whether to use the old-style calendar-data element placement
     * 
     * TODO Remove once old-style clients are updated
     */
    private boolean oldStyle;

    public CalDAVMultiStatusResponse(DavResource resource,
                                     DavPropertyNameSet propNameSet,
                                     int propFindType) {
        super(resource, propNameSet, propFindType);
    }

    /**
     * @param calendarData
     *            The calendarData to set.
     */
    public void setCalendarData(String calendarData, boolean oldStyle) {
        this.calendarData = calendarData;
        this.oldStyle = oldStyle;
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

        // TODO We currently support the old-style and new-style of
        // calendar-data placement. Eventually we should remove the old-style
        // when clients are updated.

        if ((cdata != null) && !oldStyle) {

            // Create DavProperty for this data
            CalendarDataProperty prop = new CalendarDataProperty(cdata);
            add(prop);
        }

        // Get standard multistatus response from superclass
        Element response = super.toXml(doc);

        // Now add the calendar-data element if required
        if ((cdata != null) && oldStyle) {
            response.appendChild(cdata);
        }

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
