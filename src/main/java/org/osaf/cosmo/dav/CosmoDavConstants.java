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

import org.apache.jackrabbit.webdav.DavConstants;

import org.jdom.Namespace;

/**
 * Extends {@link org.apache.jackrabbit.webdav.DavConstants} to
 * provide additional constants for request and response headers, XML
 * elements and property names defined by the WebDAV extensions
 * implemented by Cosmo.
 */
public interface CosmoDavConstants extends DavConstants {

    // ticket headers

    public static final String HEADER_TICKET = "Ticket";

    // ticket URL parameters

    public static final String PARAM_TICKET = "ticket";

    // ticket namespace

    // defined by Xythos
    public static final Namespace NAMESPACE_TICKET =
        Namespace.getNamespace("ticket", "http://www.xythos.com/namespaces/StorageServer");

    // ticket elements

    public static final String ELEMENT_PROP = "prop";
    public static final String ELEMENT_TICKETDISCOVERY = "ticketdiscovery";
    public static final String ELEMENT_TICKETINFO = "ticketinfo";
    public static final String ELEMENT_ID = "id";
    public static final String ELEMENT_OWNER = "owner";
    public static final String ELEMENT_HREF = "href";
    public static final String ELEMENT_TIMEOUT = "timeout";
    public static final String ELEMENT_VISITS = "visits";
    public static final String ELEMENT_PRIVILEGE = "privilege";
    public static final String ELEMENT_READ = "read";
    public static final String ELEMENT_WRITE = "write";

    // ticket values

    public static final String VALUE_INFINITY = "infinity";
}
