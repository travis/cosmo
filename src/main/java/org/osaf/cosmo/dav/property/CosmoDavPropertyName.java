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

import org.apache.jackrabbit.webdav.property.DavPropertyName;

import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;

/**
 * Defines a set of constants representing property names for dav
 * extensions implemented by Cosmo.
 */
public class CosmoDavPropertyName {

    /**
     */
    public static final DavPropertyName CALENDARCOMPONENTRESTRICTIONSET =
        DavPropertyName.create(CosmoDavConstants.
                               PROPERTY_CALDAV_CALENDAR_COMPONENT_RESTRICTION_SET,
                               CosmoDavConstants.NAMESPACE_CALDAV);
}
