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

import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;

/**
 * Extends {@link org.apache.jackrabbit.webdav.property.ResourceType}
 * to define new resource types for dav extensions implemented by
 * Cosmo.
 */
public class CosmoResourceType extends ResourceType {

    /**
     * The calendar home resource type
     */
    public static final int CALENDAR_HOME = COLLECTION + 1;

    /**
     * The calendar collection resource type
     */
    public static final int CALENDAR_COLLECTION = CALENDAR_HOME + 1;

    /**
     * Create a single-valued resource type property
     */
    public CosmoResourceType(int resourceType) {
        super(resourceType);
    }

    /**
     * Create a multi-valued resource type property
     */
    public CosmoResourceType(int[] resourceTypes) {
        super(resourceTypes);
    }

    /**
     * Returns the Xml representation of an individual resource type,
     * or <code>null</code> if the resource type has no Xml
     * representation (e.g. {@link #DEFAULT_RESOURCE}).
     *
     * {@link #getValue()} uses this method to build the full set of
     * Xml elements for the property's resource types. Subclasses
     * should override this method to add support for resource types
     * they define.
     */
    protected Element resourceTypeToXml(int resourceType) {
        if (resourceType == CALENDAR_HOME) {
            return new Element(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_HOME,
                               CosmoDavConstants.NAMESPACE_CALDAV);
        }
        if (resourceType == CALENDAR_COLLECTION) {
            return new Element(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR,
                               CosmoDavConstants.NAMESPACE_CALDAV);
        }
        return super.resourceTypeToXml(resourceType);
    }

    /**
     * Validates the specified resourceType. Subclasses should
     * override this method to add support for resource types they
     * define.
     *
     * @param resourceType
     * @return true if the specified resourceType is valid.
     */
    public boolean isValidResourceType(int resourceType) {
        if (resourceType >= CALENDAR_HOME &&
            resourceType <= CALENDAR_COLLECTION) {
            return true;
        }
        return super.isValidResourceType(resourceType);
    }
}
