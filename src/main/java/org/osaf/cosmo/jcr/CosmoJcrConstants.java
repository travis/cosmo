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
package org.osaf.cosmo.jcr;

/**
 * Provides constants for JCR items, node types,  etc. implemented by
 * Cosmo.
 */
public class CosmoJcrConstants {

    // node names

    /** <code>dav:ticket</code> */
    public static final String NN_TICKET = "dav:ticket";

    /** <code>icalendar:calendar</code> */
    public static final String NN_ICAL_CALENDAR = "icalendar:calendar";
    /** <code>icalendar:calendar</code> */
    public static final String NN_ICAL_COMPONENT = "icalendar:component";
    /** <code>icalendar:revent</code> */
    public static final String NN_ICAL_REVENT = "icalendar:revent";
    /** <code>icalendar:exevent</code> */
    public static final String NN_ICAL_EXEVENT = "icalendar:exevent";
    /** <code>icalendar:prodid</code> */
    public static final String NN_ICAL_PRODID = "icalendar:prodid";
    /** <code>icalendar:version</code> */
    public static final String NN_ICAL_VERSION = "icalendar:version";
    /** <code>icalendar:calscale</code> */
    public static final String NN_ICAL_CALSCALE = "icalendar:calscale";
    /** <code>icalendar:method</code> */
    public static final String NN_ICAL_METHOD = "icalendar:method";
    /** <code>icalendar:class</code> */
    public static final String NN_ICAL_CLASS = "icalendar:class";
    /** <code>icalendar:uid</code> */
    public static final String NN_ICAL_UID = "icalendar:uid";

    // node types

    /** <code>nt:folder</code> */
    public static final String NT_FOLDER = "nt:folder";

    /** <code>dav:collection</code> **/
    public static final String NT_DAV_COLLECTION = "dav:collection";
    /** <code>dav:resource</code> **/
    public static final String NT_DAV_RESOURCE = "dav:resource";

    /** <code>mix:ticketable</code> */
    public static final String NT_TICKETABLE = "mix:ticketable";
    /** <code>ticket:ticket</code> */
    public static final String NT_TICKET = "ticket:ticket";

    /** <code>caldav:collection</code> */
    public static final String NT_CALDAV_COLLECTION = "caldav:collection";
    /** <code>caldav:resource</code> */
    public static final String NT_CALDAV_RESOURCE = "caldav:resource";

    /** <code>icalendar:calendar</code> */
    public static final String NT_ICAL_CALENDAR = "icalendar:calendar";
    /** <code>icalendar:event</code> */
    public static final String NT_ICAL_EVENT = "icalendar:event";
    /** <code>icalendar:timezone</code> */
    public static final String NT_ICAL_TIMEZONE = "icalendar:timezone";
    /** <code>icalendar:prodid</code> */
    public static final String NT_ICAL_PRODID = "icalendar:prodid";
    /** <code>icalendar:version</code> */
    public static final String NT_ICAL_VERSION = "icalendar:version";
    /** <code>icalendar:calscale</code> */
    public static final String NT_ICAL_CALSCALE = "icalendar:calscale";
    /** <code>icalendar:method</code> */
    public static final String NT_ICAL_METHOD = "icalendar:method";

    // node properties

    /** <code>ticket:id</code> */
    public static final String NP_ID = "ticket:id";
    /** <code>ticket:owner</code> */
    public static final String NP_OWNER = "ticket:owner";
    /** <code>ticket:timeout</code> */
    public static final String NP_TIMEOUT = "ticket:timeout";
    /** <code>ticket:privileges</code> */
    public static final String NP_PRIVILEGES = "ticket:privileges";
    /** <code>ticket:created</code> */
    public static final String NP_CREATED = "ticket:created";

    /** <code>icalendar:value</code> */
    public static final String NP_ICAL_VALUE = "icalendar:value";
    /** <code>icalendar:text</code> */
    public static final String NP_ICAL_TEXT = "icalendar:text";
    /** <code>icalendar:maxVersion</code> */
    public static final String NP_ICAL_MAX_VERSION = "icalendar:maxVersion";
}
