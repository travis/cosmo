/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.caldav.property;

import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.property.StandardDavProperty;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CalDAV supported-calendar-component-set
 * property. Valid component types are defined by
 * {@link ComponentTypes}.
 */
public class SupportedCalendarComponentSet extends StandardDavProperty
    implements CaldavConstants, ICalendarConstants {

    public SupportedCalendarComponentSet() {
        this(SUPPORTED_COMPONENT_TYPES);
    }

    public SupportedCalendarComponentSet(Set<String> componentTypes) {
        this((String[]) componentTypes.toArray(new String[0]));
    }

    public SupportedCalendarComponentSet(String[] componentTypes) {
        super(SUPPORTEDCALENDARCOMPONENTSET, componentTypes(componentTypes),
                true);
        for (String type : componentTypes) {
            if (!CalendarUtils.isSupportedComponent(type)) {
                throw new IllegalArgumentException("Invalid component type '"
                        + type + "'.");
            }
        }
    }
    
    private static HashSet<String> componentTypes(String[] types) {
        HashSet<String> typesSet = new HashSet<String>();
        
        for (String t: types)
            typesSet.add(t);
        return typesSet;
    }

    public Set<String> getComponentTypes() {
        return (Set<String>) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        for (String type : getComponentTypes()) {
            Element e = DomUtil.createElement(document,
                    ELEMENT_CALDAV_COMP, NAMESPACE_CALDAV);
            DomUtil.setAttribute(e, ATTR_CALDAV_NAME,  NAMESPACE_CALDAV,
                    type);
            name.appendChild(e);
        }

        return name;
    }
}
