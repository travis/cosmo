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

import java.util.Set;
import java.util.HashSet;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.icalendar.ComponentTypes;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * Represents the CalDAV supported-calendar-component-set
 * property. Valid component types are defined by
 * {@link ComponentTypes}.
 */
public class SupportedCalendarComponentSet extends AbstractDavProperty {

    private int[] componentTypes;

    /**
     */
    public SupportedCalendarComponentSet() {
        this(ComponentTypes.getAllSupportedComponentTypes());
    }

    /**
     */
    public SupportedCalendarComponentSet(int[] componentTypes) {
        super(CosmoDavPropertyName.SUPPORTEDCALENDARCOMPONENTSET, true);
        for (int i=0; i<componentTypes.length; i++) {
            if (! ComponentTypes.isValidComponentType(componentTypes[i])) {
                throw new IllegalArgumentException("Invalid component type '" +
                                                   componentTypes[i] + "'.");
            }
        }
        this.componentTypes = componentTypes;
    }

    /**
     * (Returns a <code>Set</code> of
     * <code>SupportedCalendarComponentSet.CalendarComponentInfo</code>s
     * for this property.
     */
    public Object getValue() {
        Set infos = new HashSet();
        for (int i=0; i<componentTypes.length; i++) {
            String type =
                ComponentTypes.getComponentTypeName(componentTypes[i]);
            infos.add(new CalendarComponentInfo(type));
        }
        return infos;
    }

    /**
     * Returns the component types for this property.
     */
    public int[] getComponentTypes() {
        return componentTypes;
    }

    /**
     */
    public class CalendarComponentInfo implements XmlSerializable {
        private String type;

        /**
         */
        public CalendarComponentInfo(String type) {
            this.type = type;
        }

        /**
         */
        public Element toXml(Document document) {
            Element elem =
                DomUtil.createElement(document,
                                      CosmoDavConstants.ELEMENT_CALDAV_COMP,
                                      CosmoDavConstants.NAMESPACE_CALDAV);
            DomUtil.setAttribute(elem, CosmoDavConstants.ATTR_CALDAV_NAME,
                                 CosmoDavConstants.NAMESPACE_CALDAV, type);
            return elem;
        }
    }
}
