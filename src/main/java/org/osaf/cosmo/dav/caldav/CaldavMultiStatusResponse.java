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
 * Subclass of
 * {@link org.apache.jackrabbit.webdav.MultiStatusResponse} that adds
 * the ability to return the <code>&lt;C:calendar-data&gt;</code> XML
 * element needed by some CalDAV reports.
 *
 * Based on code originally written by Cyrus Daboo.
 */
public class CaldavMultiStatusResponse extends MultiStatusResponse {

    private String calendarData;

    /** */
    public CaldavMultiStatusResponse(DavResource resource,
                                     DavPropertyNameSet propNameSet,
                                     int propFindType) {
        super(resource, propNameSet, propFindType);
    }

    // our methods

    /** */
    public void setCalendarData(String calendarData) {
        this.calendarData = calendarData;
    }

    /** */
    public Element toXml(Document doc) {
        if (calendarData != null) {
            Element cdata = DomUtil.
                createElement(doc,
                              CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                              CosmoDavConstants.NAMESPACE_CALDAV);
            DomUtil.setText(cdata, calendarData);

            CalendarDataProperty prop = new CalendarDataProperty(cdata);
            add(prop);
        }

        return super.toXml(doc);
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

        public Element toXml(Document doc) {
            return calendarData;
        }
    }
}
