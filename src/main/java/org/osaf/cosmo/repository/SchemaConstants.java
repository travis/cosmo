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
package org.osaf.cosmo.repository;

/**
 * Provides constants for schema items (namespaces, node types, node
 * names, properties, etc) in the Cosmo repository.
 */
public interface SchemaConstants {

    // prefixes
    public static final String PREFIX_DAV = "dav";
    public static final String PREFIX_ICALENDAR = "icalendar";
    public static final String PREFIX_CALENDAR = "calendar";
    public static final String PREFIX_TICKET = "ticket";
    public static final String PREFIX_COSMO = "cosmo";

    // node names
    public static final String NN_JCR_CONTENT = "jcr:content";
    public static final String NN_JCR_DATA = "jcr:data";

    public static final String NN_TICKET = "ticket:ticket";

    // node types

    public static final String NT_BASE = "nt:base";
    public static final String NT_UNSTRUCTURED = "nt:unstructured";
    public static final String NT_FOLDER = "nt:folder";
    public static final String NT_FILE = "nt:file";
    public static final String NT_RESOURCE = "nt:resource";

    public static final String NT_DAV_COLLECTION = "dav:collection";
    public static final String NT_DAV_RESOURCE = "dav:resource";

    public static final String NT_TICKETABLE = "ticket:ticketable";
    public static final String NT_TICKET = "ticket:ticket";

    public static final String NT_CALENDAR_COLLECTION = "calendar:collection";
    public static final String NT_CALENDAR_RESOURCE = "calendar:resource";
    public static final String NT_EVENT_RESOURCE = "calendar:event";

    public static final String NT_USER = "cosmo:user";
    public static final String NT_HOME_COLLECTION = "cosmo:homecollection";

    // node properties

    public static final String NP_JCR_DATA = "jcr:data";
    public static final String NP_JCR_CREATED = "jcr:created";
    public static final String NP_JCR_MIMETYPE = "jcr:mimeType";
    public static final String NP_JCR_ENCODING = "jcr:encoding";
    public static final String NP_JCR_LASTMODIFIED = "jcr:lastModified";

    public static final String NP_DAV_DISPLAYNAME = "dav:displayname";
    public static final String NP_DAV_CONTENTLANGUAGE =
        "dav:contentlanguage";

    public static final String NP_CALENDAR_UID = "calendar:uid";
    public static final String NP_CALENDAR_DESCRIPTION = "calendar:description";
    public static final String NP_CALENDAR_LANGUAGE = "calendar:language";
    public static final String NP_CALENDAR_TIMEZONE = "calendar:timezone";
    public static final String NP_CALENDAR_SUPPORTED_COMPONENT_SET = "calendar:supportedComponentSet";  

    public static final String NP_TICKET_ID = "ticket:id";
    public static final String NP_TICKET_OWNER = "ticket:owner";
    public static final String NP_TICKET_TIMEOUT = "ticket:timeout";
    public static final String NP_TICKET_PRIVILEGES = "ticket:privileges";
    public static final String NP_TICKET_CREATED = "ticket:created";

    public static final String NP_USER_USERNAME = "cosmo:username";
    public static final String NP_USER_PASSWORD = "cosmo:password";
    public static final String NP_USER_FIRSTNAME = "cosmo:firstName";
    public static final String NP_USER_LASTNAME = "cosmo:lastName";
    public static final String NP_USER_EMAIL = "cosmo:email";
    public static final String NP_USER_ADMIN = "cosmo:admin";
    public static final String NP_USER_DATECREATED = "cosmo:dateCreated";
    public static final String NP_USER_DATEMODIFIED = "cosmo:dateModified";
}
