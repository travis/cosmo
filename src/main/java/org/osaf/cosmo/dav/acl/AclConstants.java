/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.acl;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Provides constants for media types, XML namespaces, names and
 * values, DAV properties and resource types defined by the WebDAV ACL
 * spec.
 */
public interface AclConstants extends DavConstants {

    /** The ACL XML element name <DAV:principal> */
    public static final String ELEMENT_ACL_PRINCIPAL = "principal";
    /** The ACL XML element name <DAV:alternate-URI-set> */
    public static final String ELEMENT_ACL_ALTERNATE_URI_SET =
        "alternate-URI-set";
    /** The ACL XML element name <DAV:principal-URL> */
    public static final String ELEMENT_ACL_PRINCIPAL_URL =
        "principal-URL";
    /** The ACL XML element name <DAV:group-membership> */
    public static final String ELEMENT_ACL_GROUP_MEMBERSHIP =
        "group-membership";

    /** The ACL property name DAV:alternate-URI-set */
    public static final String PROPERTY_ACL_ALTERNATE_URI_SET =
        "alternate-URI-set";
    /** The ACL property name DAV:principal-URL-set */
    public static final String PROPERTY_ACL_PRINCIPAL_URL =
        "principal-URL";
    /** The ACL property name DAV:group-membership */
    public static final String PROPERTY_ACL_GROUP_MEMBERSHIP =
        "group-membership";

    /** The ACL property DAV:alternate-URI-set */
    public static final DavPropertyName ALTERNATEURISET =
        DavPropertyName.create(PROPERTY_ACL_ALTERNATE_URI_SET, NAMESPACE);
    /** The ACL property DAV:principal-URL */
    public static final DavPropertyName PRINCIPALURL =
        DavPropertyName.create(PROPERTY_ACL_PRINCIPAL_URL, NAMESPACE);
    /** The ACL property DAV:group-membership */
    public static final DavPropertyName GROUPMEMBERSHIP =
        DavPropertyName.create(PROPERTY_ACL_GROUP_MEMBERSHIP, NAMESPACE);
}
