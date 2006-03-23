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
 * Defines the iCalendar component types supported by Cosmo.
 */
public class ComponentTypes {

    /**
     */
    public static final int VEVENT = 0;

    private static int[] SUPPORTED_COMPONENT_TYPES = {
        VEVENT
    };

    private static String[] SUPPORTED_COMPONENT_TYPE_NAMES = {
        Component.VEVENT
    };

    /**
     */
    public static int[] getAllSupportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    public static String[] getAllSupportedComponentTypeNames() {
        return SUPPORTED_COMPONENT_TYPE_NAMES;
    }

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
     */
    public static String getComponentTypeName(int componentType) {
        switch (componentType) {
        case VEVENT:
            return Component.VEVENT;
        }
        throw new IllegalArgumentException("Invalid component type '" +
                                           componentType + "'.");
    }

    /**
     */
    public static int getComponentType(String componentTypeName) {
        if (componentTypeName.equalsIgnoreCase(Component.VEVENT))
            return VEVENT;

        throw new IllegalArgumentException("Invalid component type name '" +
                                           componentTypeName + "'.");
    }

    /**
     */
    public static boolean isValidComponentType(int componentType) {
        return componentType == VEVENT;
    }

    /**
     */
    public static boolean isValidComponentTypeName(String componentTypeName) {
        return componentTypeName.equalsIgnoreCase(Component.VEVENT);
    }
}
