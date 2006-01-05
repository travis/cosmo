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
import java.util.Set;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;

import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.icalendar.ICalendarConstants;

/**
 * Represents the CalDAV supported-calendar-data property.
 */
public class SupportedCalendarData extends AbstractDavProperty
    implements ICalendarConstants {

    /**
     */
    public SupportedCalendarData() {
        super(CosmoDavPropertyName.SUPPORTEDCALENDARDATA, true);
    }

    /**
     * Returns an <code>Element</code> representing this property.
     */
    public Element toXml() {
        Element element = getName().toXml();
        if (getValue() != null) {
            element.addContent((Set) getValue());
        }
        return element;
    }

    /**
     * (Returns a <code>Set</code> of <code>Element</code>s
     * representing the data of this property.
     */
    public Object getValue() {
        Set elements = new HashSet();
        Element element =
            new Element(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                        CosmoDavConstants.NAMESPACE_CALDAV);
        element.setAttribute(CosmoDavConstants.ATTR_CALDAV_CONTENT_TYPE,
                             CONTENT_TYPE);
        element.setAttribute(CosmoDavConstants.ATTR_CALDAV_VERSION, VERSION);
        elements.add(element);
        return elements;
    }
}
