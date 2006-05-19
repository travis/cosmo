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

import org.osaf.cosmo.dav.CosmoDavConstants;

/**
 * Defines a set of constants representing property names for dav
 * extensions implemented by Cosmo.
 */
public class CosmoDavPropertyName {

    /**
     */
    public static final DavPropertyName CALENDARDATA =
        DavPropertyName.create(CosmoDavConstants.
                               PROPERTY_CALDAV_CALENDAR_DATA,
                               CosmoDavConstants.NAMESPACE_CALDAV);

    /**
     */
    public static final DavPropertyName CALENDARDESCRIPTION =
        DavPropertyName.create(CosmoDavConstants.
                               PROPERTY_CALDAV_CALENDAR_DESCRIPTION,
                               CosmoDavConstants.NAMESPACE_CALDAV);

    public static final DavPropertyName CALENDARTIMEZONE =
        DavPropertyName.create(CosmoDavConstants.
                               PROPERTY_CALDAV_CALENDAR_TIMEZONE,
                               CosmoDavConstants.NAMESPACE_CALDAV);

    /**
     */
    public static final DavPropertyName SUPPORTEDCALENDARCOMPONENTSET =
        DavPropertyName.create(CosmoDavConstants.
                               PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET,
                               CosmoDavConstants.NAMESPACE_CALDAV);

    /**
     */
    public static final DavPropertyName SUPPORTEDCALENDARDATA =
        DavPropertyName.create(CosmoDavConstants.
                               PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA,
                               CosmoDavConstants.NAMESPACE_CALDAV);

    /**
     */
    public static final DavPropertyName TICKETDISCOVERY =
        DavPropertyName.create(CosmoDavConstants.
                               PROPERTY_TICKET_TICKETDISCOVERY,
                               CosmoDavConstants.NAMESPACE_TICKET);
}
