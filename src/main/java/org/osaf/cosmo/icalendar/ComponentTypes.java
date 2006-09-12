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
package org.osaf.cosmo.icalendar;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.model.Component;

/**
 * Defines the iCalendar component types understood by Cosmo.
 *
 * Calendar objects stored in Cosmo of the supported types defined by
 * this class are queryable through such mechanisms as CalDAV reports,
 * and the Web Console is able to treat them specially, such as
 * offering type-specific displays or controls.
 */
public class ComponentTypes {

    private static Set<String> SUPPORTED_COMPONENT_TYPES = new HashSet();

    static {
        SUPPORTED_COMPONENT_TYPES.add(Component.VEVENT);
    };

    /**
     * Returns an array of all supported component types
     *
     * @return array of component types
     */
    public static Set<String> getAllSupportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    /**
     * Returns a string containing the names of all supported
     * component types delimited by the <code>|</code> character.
     *
     * @return string of component types
     */
    public static String getAllSupportedComponentTypesAsString() {
        StringBuffer buf = new StringBuffer();
        for (Iterator<String> i=SUPPORTED_COMPONENT_TYPES.iterator();
             i.hasNext();) {
            buf.append(i.next());
            if (i.hasNext())
                buf.append("|");
        }
        return buf.toString();
    }

    /**
     * Returns whether or not the given component type code represents
     * a supported component type.
     *
     * @param code a type string
     * @return <code>true</code> if the string represents a supported
     * component type, <code>false</code> otherwise
     */
    public static boolean isValidComponentType(String type) {
        for (Iterator<String> i=SUPPORTED_COMPONENT_TYPES.iterator();
             i.hasNext();) {
            if (type.equalsIgnoreCase(i.next()))
                return true;
        }
        return false;
    }
}
