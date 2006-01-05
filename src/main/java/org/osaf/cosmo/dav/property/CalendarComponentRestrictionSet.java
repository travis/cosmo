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

import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.icalendar.ComponentTypes;

/**
 * Represents the CalDAV calendar-component-restriction-set
 * property. Valid component types are {@link #VEVENT},
 * {@link #VTODO}, {@link #VJOURNAL}, {@link #VFREEBUSY},
 * {@link #VTIMEZONE}.
 */
public class CalendarComponentRestrictionSet extends AbstractDavProperty {

    private int[] componentTypes;

    /**
     */
    public CalendarComponentRestrictionSet() {
        this(ComponentTypes.getAllSupportedComponentTypes());
    }

    /**
     */
    public CalendarComponentRestrictionSet(int[] componentTypes) {
        super(CosmoDavPropertyName.CALENDARCOMPONENTRESTRICTIONSET, true);
        for (int i=0; i<componentTypes.length; i++) {
            if (! ComponentTypes.isValidComponentType(componentTypes[i])) {
                throw new IllegalArgumentException("Invalid component type '" +
                                                   componentTypes[i] + "'.");
            }
        }
        this.componentTypes = componentTypes;
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
     * representing the component types for this property.
     */
    public Object getValue() {
        Set elements = new HashSet();
        for (int i=0; i<componentTypes.length; i++) {
            Element element = new Element(CosmoDavConstants.ELEMENT_CALDAV_COMP,
                                          CosmoDavConstants.NAMESPACE_CALDAV);
            element.setAttribute(CosmoDavConstants.ATTR_CALDAV_NAME,
                                 ComponentTypes.
                                 getComponentTypeName(componentTypes[i]));
            elements.add(element);
        }
        return elements;
    }

    /**
     * Returns the component types for this property.
     */
    public int[] getComponentTypes() {
        return componentTypes;
    }
}
