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

import org.apache.jackrabbit.webdav.property.ResourceType;

import org.apache.log4j.Logger;

import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;

/**
 * Extends {@link org.apache.jackrabbit.webdav.property.ResourceType}
 * to define new resource types for CalDAV:
 *
 * <ul>
 * <li>{@link #CALENDAR_COLLECTION CALDAV:calendar-collection},</li>
 * </ul>
 */
public class CosmoResourceType extends ResourceType {
    private static final Logger log = Logger.getLogger(CosmoResourceType.class);

    /**
     * The calendar collection resource type
     */
    public static final int CALENDAR_COLLECTION = BASELINE + 1;

    static {
        registerResourceType(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR,
                             CosmoDavConstants.NAMESPACE_CALDAV);
    }

    /**
     */
    public CosmoResourceType(int resourceType) {
        super(resourceType);
    }

    /**
     */
    public CosmoResourceType(int[] resourceTypes) {
        super(resourceTypes);
    }
}
