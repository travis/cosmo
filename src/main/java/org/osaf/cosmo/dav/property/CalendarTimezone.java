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
package org.osaf.cosmo.dav.property;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents the CalDAV calendar-timezone property.
 */
public class CalendarTimezone extends AbstractDavProperty {

    private String icalVTIMEZONE;

    /**
     */
    public CalendarTimezone(String text) {
        super(CosmoDavPropertyName.CALENDARTIMEZONE, true);
        this.icalVTIMEZONE = text;
    }

    /**
     * Returns an <code>Element</code> representing this property.
     */
    public Element toXml(Document document) {
        return DomUtil.createElement(document,
                                     CosmoDavConstants.PROPERTY_CALDAV_CALENDAR_TIMEZONE,
                                     CosmoDavConstants.NAMESPACE_CALDAV, icalVTIMEZONE);
    }

    /**
     * Returns a <code>String</code> representing the property's
     * text.
     */
    public Object getValue() {
        return icalVTIMEZONE;
    }
}
