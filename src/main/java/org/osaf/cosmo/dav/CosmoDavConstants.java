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
package org.osaf.cosmo.dav;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Provides constants for request and response headers, XML elements
 * and property names defined by the WebDAV extensions implemented by
 * Cosmo.
 */
public class CosmoDavConstants {

    // request headers

    public static final String HEADER_TICKET = "Ticket";

    // request parameters

    public static final String PARAM_TICKET = "ticket";

    // request content types

    public static final String CT_ICALENDAR = "text/calendar";

    // XML namespaces

    public static final Namespace NAMESPACE_XML =
        Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    public static final Namespace NAMESPACE_TICKET =
        Namespace.getNamespace("ticket", "http://www.xythos.com/namespaces/StorageServer");
    public static final Namespace NAMESPACE_CALDAV =
        Namespace.getNamespace("C", "urn:ietf:params:xml:ns:caldav");

    // XML elements

    public static final String ELEMENT_SET = "set";
    public static final String ELEMENT_PROP = "prop";
    public static final String ELEMENT_TICKETINFO = "ticketinfo";
    public static final String ELEMENT_ID = "id";
    public static final String ELEMENT_OWNER = "owner";
    public static final String ELEMENT_HREF = "href";
    public static final String ELEMENT_TIMEOUT = "timeout";
    public static final String ELEMENT_VISITS = "visits";
    public static final String ELEMENT_PRIVILEGE = "privilege";
    public static final String ELEMENT_READ = "read";
    public static final String ELEMENT_WRITE = "write";

    public static final String ELEMENT_CALDAV_MKCALENDAR = "mkcalendar";
    public static final String ELEMENT_CALDAV_CALENDAR = "calendar";
    public static final String ELEMENT_CALDAV_COMP = "comp";
    public static final String ELEMENT_CALDAV_CALENDAR_DATA = "calendar-data";
    public static final String ELEMENT_CALDAV_TIMEZONE = "timezone";
    public static final String ELEMENT_CALDAV_ALLCOMP = "allcomp";
    public static final String ELEMENT_CALDAV_ALLPROP = "allprop";
    public static final String ELEMENT_CALDAV_PROP = "prop";
    public static final String ELEMENT_CALDAV_EXPAND =
        "expand";
    public static final String ELEMENT_CALDAV_LIMIT_RECURRENCE_SET =
        "limit-recurrence-set";
    public static final String ELEMENT_CALDAV_LIMIT_FREEBUSY_SET =
        "limit-freebusy-set";
    public static final String ELEMENT_CALDAV_FILTER = "filter";
    public static final String ELEMENT_CALDAV_COMP_FILTER = "comp-filter";
    public static final String ELEMENT_CALDAV_PROP_FILTER = "prop-filter";
    public static final String ELEMENT_CALDAV_PARAM_FILTER = "param-filter";
    public static final String ELEMENT_CALDAV_TIME_RANGE = "time-range";
    public static final String ELEMENT_CALDAV_TEXT_MATCH = "text-match";

    // XML attributes

    public static final String ATTR_XML_LANG = "lang";

    public static final String ATTR_CALDAV_NAME = "name";
    public static final String ATTR_CALDAV_CONTENT_TYPE = "content-type";
    public static final String ATTR_CALDAV_VERSION = "version";
    public static final String ATTR_CALDAV_NOVALUE = "novalue";
    public static final String ATTR_CALDAV_CASELESS = "caseless";
    public static final String ATTR_CALDAV_START = "start";
    public static final String ATTR_CALDAV_END = "end";

    // XML values

    public static final String VALUE_INFINITE = "Infinite";
    public static final String VALUE_INFINITY = "infinity";
    public static final String VALUE_YES = "yes";
    public static final String VALUE_NO = "no";

    // caldav properties

    public static final String PROPERTY_CALDAV_CALENDAR_DESCRIPTION =
        "calendar-description";
    public static final String PROPERTY_CALDAV_CALENDAR_TIMEZONE =
        "calendar-timezone";
    public static final String PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET =
        "supported-calendar-component-set";
    public static final String PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA =
        "supported-calendar-data";

    // caldav reports

    public static final String ELEMENT_CALDAV_CALENDAR_MULTIGET =
        "calendar-multiget";
    public static final String ELEMENT_CALDAV_CALENDAR_QUERY =
        "calendar-query";
    public static final String ELEMENT_CALDAV_CALENDAR_FREEBUSY =
        "free-busy-query";
    public static final DavPropertyName CALENDARDATA = DavPropertyName.create(
            ELEMENT_CALDAV_CALENDAR_DATA, NAMESPACE_CALDAV);

    public static final DavPropertyName TIMEZONE = DavPropertyName.create(
            ELEMENT_CALDAV_TIMEZONE, NAMESPACE_CALDAV);

    // caldav content types
    public static final String CONTENT_TYPE_CALENDAR_COLLECTION =
        "application/x-calendar-collection";

    // ticket properties

    public static final String PROPERTY_TICKET_TICKETDISCOVERY =
        "ticketdiscovery";
}
