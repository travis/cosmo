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
    public static final String NN_JCR_CONTENT = "jcr:content";

    public static final String NN_TICKET = "ticket:ticket";

    public static final String NN_ICAL_CALENDAR = "icalendar:calendar";
    public static final String NN_ICAL_COMPONENT = "icalendar:component";
    public static final String NN_ICAL_REVENT = "icalendar:revent";
    public static final String NN_ICAL_EXEVENT = "icalendar:exevent";
    public static final String NN_ICAL_TIMEZONE = "icalendar:timezone";
    public static final String NN_ICAL_STANDARD = "icalendar:standard";
    public static final String NN_ICAL_DAYLIGHT = "icalendar:daylight";
    public static final String NN_ICAL_ALARM = "icalendar:alarm";
    public static final String NN_ICAL_PRODID = "icalendar:prodid";
    public static final String NN_ICAL_VERSION = "icalendar:version";
    public static final String NN_ICAL_CALSCALE = "icalendar:calscale";
    public static final String NN_ICAL_METHOD = "icalendar:method";
    public static final String NN_ICAL_ACTION = "icalendar:action";
    public static final String NN_ICAL_ATTACH = "icalendar:attach";
    public static final String NN_ICAL_ATTENDEE = "icalendar:attendee";
    public static final String NN_ICAL_CATEGORIES = "icalendar:categories";
    public static final String NN_ICAL_CLASS = "icalendar:class";
    public static final String NN_ICAL_COMMENT = "icalendar:comment";
    public static final String NN_ICAL_CONTACT = "icalendar:contact";
    public static final String NN_ICAL_CREATED = "icalendar:created";
    public static final String NN_ICAL_DESCRIPTION = "icalendar:description";
    public static final String NN_ICAL_DTEND = "icalendar:dtend";
    public static final String NN_ICAL_DTSTAMP = "icalendar:dtstamp";
    public static final String NN_ICAL_DTSTART = "icalendar:dtstart";
    public static final String NN_ICAL_DUR = "icalendar:dur";
    public static final String NN_ICAL_DURATION = "icalendar:duration";
    public static final String NN_ICAL_EXDATE = "icalendar:exdate";
    public static final String NN_ICAL_EXRULE = "icalendar:exrule";
    public static final String NN_ICAL_GEO = "icalendar:geo";
    public static final String NN_ICAL_LASTMODIFIED = "icalendar:lastmodified";
    public static final String NN_ICAL_LOCATION = "icalendar:location";
    public static final String NN_ICAL_ORGANIZER = "icalendar:organizer";
    public static final String NN_ICAL_PERIOD = "icalendar:period";
    public static final String NN_ICAL_PRIORITY = "icalendar:priority";
    public static final String NN_ICAL_RANGE = "icalendar:range";
    public static final String NN_ICAL_RDATE = "icalendar:rdate";
    public static final String NN_ICAL_RECUR = "icalendar:recur";
    public static final String NN_ICAL_RECURRENCEID = "icalendar:recurrenceid";
    public static final String NN_ICAL_RELATEDTO = "icalendar:relatedto";
    public static final String NN_ICAL_REPEAT = "icalendar:repeat";
    public static final String NN_ICAL_REQUESTSTATUS =
        "icalendar:requeststatus";
    public static final String NN_ICAL_RESOURCES = "icalendar:resources";
    public static final String NN_ICAL_RRULE = "icalendar:rrule";
    public static final String NN_ICAL_SEQ = "icalendar:seq";
    public static final String NN_ICAL_STATUS = "icalendar:status";
    public static final String NN_ICAL_SUMMARY = "icalendar:summary";
    public static final String NN_ICAL_TRANSP = "icalendar:transp";
    public static final String NN_ICAL_TRIGGER = "icalendar:trigger";
    public static final String NN_ICAL_TZID = "icalendar:tzid";
    public static final String NN_ICAL_TZNAME = "icalendar:tzname";
    public static final String NN_ICAL_TZOFFSETFROM = "icalendar:tzoffsetfrom";
    public static final String NN_ICAL_TZOFFSETTO = "icalendar:tzoffsetto";
    public static final String NN_ICAL_TZURL = "icalendar:tzurl";
    public static final String NN_ICAL_UID = "icalendar:uid";
    public static final String NN_ICAL_URL = "icalendar:url";

    // node types

    public static final String NT_FOLDER = "nt:folder";

    public static final String NT_DAV_COLLECTION = "dav:collection";
    public static final String NT_DAV_RESOURCE = "dav:resource";

    public static final String NT_TICKETABLE = "mix:ticketable";
    public static final String NT_TICKET = "ticket:ticket";

    public static final String NT_CALDAV_HOME = "caldav:home";
    public static final String NT_CALDAV_COLLECTION = "caldav:collection";
    public static final String NT_CALDAV_RESOURCE = "caldav:resource";
    public static final String NT_CALDAV_EVENT_RESOURCE =
        "caldav:eventresource";

    public static final String NT_ICAL_CALENDAR = "icalendar:calendar";
    public static final String NT_ICAL_EVENT = "icalendar:event";
    public static final String NT_ICAL_TIMEZONE = "icalendar:timezone";
    public static final String NT_ICAL_PRODID = "icalendar:prodid";
    public static final String NT_ICAL_VERSION = "icalendar:version";
    public static final String NT_ICAL_CALSCALE = "icalendar:calscale";
    public static final String NT_ICAL_METHOD = "icalendar:method";
    public static final String NT_ICAL_XPROPERTY = "icalendar:xproperty";

    // node properties

    public static final String NP_JCR_DATA = "jcr:data";
    public static final String NP_JCR_CREATED = "jcr:created";
    public static final String NP_JCR_LASTMODIFIED = "jcr:lastModified";

    public static final String NP_XML_LANG = "xml:lang";

    public static final String NP_DAV_DISPLAYNAME = "dav:displayname";
    public static final String NP_DAV_CONTENTLANGUAGE =
        "dav:contentlanguage";

    public static final String NP_CALDAV_CALENDARDESCRIPTION =
        "caldav:calendar-description";

    public static final String NP_ID = "ticket:id";
    public static final String NP_OWNER = "ticket:owner";
    public static final String NP_TIMEOUT = "ticket:timeout";
    public static final String NP_PRIVILEGES = "ticket:privileges";
    public static final String NP_CREATED = "ticket:created";

    public static final String NP_ICAL_ALTREP = "icalendar:altrep";
    public static final String NP_ICAL_BINARY = "icalendar:binary";
    public static final String NP_ICAL_BYDAY = "icalendar:byday";
    public static final String NP_ICAL_BYDAYOFFSET = "icalendar:bydayoffset";
    public static final String NP_ICAL_BYHOUR = "icalendar:byhour";
    public static final String NP_ICAL_BYMINUTE = "icalendar:byminute";
    public static final String NP_ICAL_BYMONTH = "icalendar:bymonth";
    public static final String NP_ICAL_BYMONTHDAY = "icalendar:bymonthday";
    public static final String NP_ICAL_BYSECOND = "icalendar:bysecond";
    public static final String NP_ICAL_BYSETPOS = "icalendar:bysetpos";
    public static final String NP_ICAL_BYWEEKNO = "icalendar:byweekno";
    public static final String NP_ICAL_BYYEARDAY = "icalendar:byyearday";
    public static final String NP_ICAL_CALADDRESS = "icalendar:caladdress";
    public static final String NP_ICAL_CATEGORY = "icalendar:category";
    public static final String NP_ICAL_CN = "icalendar:cn";
    public static final String NP_ICAL_COUNT = "icalendar:count";
    public static final String NP_ICAL_CUTYPE = "icalendar:cutype";
    public static final String NP_ICAL_DATETIMEVALUE =
        "icalendar:datetimevalue";
    public static final String NP_ICAL_DELFROM = "icalendar:delfrom";
    public static final String NP_ICAL_DELTO = "icalendar:delto";
    public static final String NP_ICAL_DIR = "icalendar:dir";
    public static final String NP_ICAL_DATETIME = "icalendar:datetime";
    public static final String NP_ICAL_DAYS = "icalendar:days";
    public static final String NP_ICAL_DESCRIPTION = "icalendar:description";
    public static final String NP_ICAL_ENCODING = "icalendar:encoding";
    public static final String NP_ICAL_END = "icalendar:end";
    public static final String NP_ICAL_EXDATA = "icalendar:exdata";
    public static final String NP_ICAL_FMTTYPE = "icalendar:fmttype";
    public static final String NP_ICAL_FREQ = "icalendar:freq";
    public static final String NP_ICAL_HOURS = "icalendar:hours";
    public static final String NP_ICAL_INTERVAL = "icalendar:interval";
    public static final String NP_ICAL_LANGUAGE = "icalendar:language";
    public static final String NP_ICAL_LATITUDE = "icalendar:latitude";
    public static final String NP_ICAL_LEVEL = "icalendar:level";
    public static final String NP_ICAL_LONGITUDE = "icalendar:longitude";
    public static final String NP_ICAL_MAXVERSION = "icalendar:maxversion";
    public static final String NP_ICAL_MEMBER = "icalendar:member";
    public static final String NP_ICAL_MINUTES = "icalendar:minutes";
    public static final String NP_ICAL_NEGATIVE = "icalendar:negative";
    public static final String NP_ICAL_OFFSET = "icalendar:offset";
    public static final String NP_ICAL_PARTSTAT = "icalendar:partstat";
    public static final String NP_ICAL_PROPVALUE = "icalendar:propvalue";
    public static final String NP_ICAL_RANGE = "icalendar:range";
    public static final String NP_ICAL_RELATED = "icalendar:related";
    public static final String NP_ICAL_RELTYPE = "icalendar:reltype";
    public static final String NP_ICAL_REQUESTSTATUS =
        "icalendar:requeststatus";
    public static final String NP_ICAL_ROLE = "icalendar:role";
    public static final String NP_ICAL_RSVP = "icalendar:rsvp";
    public static final String NP_ICAL_SECONDS = "icalendar:seconds";
    public static final String NP_ICAL_SENTBY = "icalendar:sentby";
    public static final String NP_ICAL_SEQUENCENO = "icalendar:sequenceno";
    public static final String NP_ICAL_START = "icalendar:start";
    public static final String NP_ICAL_STATCODE = "icalendar:statcode";
    public static final String NP_ICAL_TEXT = "icalendar:text";
    public static final String NP_ICAL_TZID = "icalendar:tzid";
    public static final String NP_ICAL_UID = "icalendar:uid";
    public static final String NP_ICAL_UNTIL = "icalendar:until";
    public static final String NP_ICAL_URI = "icalendar:uri";
    public static final String NP_ICAL_UTC = "icalendar:utc";
    public static final String NP_ICAL_VALUE = "icalendar:value";
    public static final String NP_ICAL_WEEKS = "icalendar:weeks";
    public static final String NP_ICAL_WKST = "icalendar:wkst";
}
