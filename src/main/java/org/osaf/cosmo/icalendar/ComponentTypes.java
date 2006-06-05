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

    private static int[] SUPPORTED_COMPONENT_TYPES = {
        VEVENT
    };

    private static String[] SUPPORTED_COMPONENT_TYPE_NAMES = {
        Component.VEVENT
    };

    /** */
    public static final int VEVENT = 0;

    /**
     * Returns an array of codes representing all supported
     * component types (see specific type constants).
     *
     * @returns array of component type codes
     */
    public static int[] getAllSupportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    /**
     * Returns an array of the names of all supported component types
     * in no particular order.
     *
     * @returns array of component type names
     */
    public static String[] getAllSupportedComponentTypeNames() {
        return SUPPORTED_COMPONENT_TYPE_NAMES;
    }

    /**
     * Returns a string containing the names of all supported
     * component types delimited by the <code>|</code> character.
     *
     * @returns string of component type names
     */
    public static String getAllSupportedComponentTypeNamesAsString() {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<SUPPORTED_COMPONENT_TYPE_NAMES.length; i++) {
            buf.append(SUPPORTED_COMPONENT_TYPE_NAMES[i]);
            if (i<SUPPORTED_COMPONENT_TYPE_NAMES.length-1) {
                buf.append("|");
            }
        }
        return buf.toString();
    }

    /**
     * Returns the name of the component type represented by the given
     * type code.
     *
     * @param code a type code
     * @returns the name of the corresponding component type
     * @throws IllegalArgumentException if the type code does not
     * represent a supported component type
     */
    public static String getComponentTypeName(int code) {
        switch (code) {
        case VEVENT:
            return Component.VEVENT;
        }
        throw new IllegalArgumentException("Invalid component type code '" +
                                           code + "'.");
    }

    /**
     * Returns the code of the component type represented by the given
     * name.
     *
     * @param name a component type name
     * @returns the corresponding type code
     * @throws IllegalArgumentException if the name does not
     * represent a supported component type
     */
    public static int getComponentType(String name) {
        if (name.equalsIgnoreCase(Component.VEVENT)) {
            return VEVENT;
        }
        throw new IllegalArgumentException("Invalid component type name '" +
                                           name + "'.");
    }

    /**
     * Returns whether or not the given component type code represents
     * a supported component type.
     *
     * @param code a type code
     * @returns <code>true</code> if the code represents a supported
     * component type, <code>false</code> otherwise
     */
    public static boolean isValidComponentType(int code) {
        return code == VEVENT;
    }

    /**
     * Returns whether or not the given component type name represents
     * a supported component type.
     *
     * @param name a type name
     * @returns <code>true</code> if the name represents a supported
     * component type, <code>false</code> otherwise
     */
    public static boolean isValidComponentTypeName(String name) {
        return name.equalsIgnoreCase(Component.VEVENT);
    }
}
