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
package org.osaf.cosmo.dav;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.osaf.cosmo.util.UriTemplate;
import org.osaf.cosmo.xml.QName;

/**
 * Provides constants defined by Cosmo proprietary *DAV extensions.
 */
public interface ExtendedDavConstants extends DavConstants {

    public static final String PRE_COSMO = "cosmo";
    public static final String NS_COSMO =
        "http://osafoundation.org/cosmo/DAV";

    /** The Cosmo XML namespace  */
    public static final Namespace NAMESPACE_COSMO =
        Namespace.getNamespace(PRE_COSMO, NS_COSMO);

    /** The Cosmo property name <code>exclude-free-busy-rollup</code> */
    public static final String PROPERTY_EXCLUDE_FREE_BUSY_ROLLUP =
        "exclude-free-busy-rollup";

    /** The Cosmo property <code>cosmo:exclude-free-busy-rollup</code> */
    public static final DavPropertyName EXCLUDEFREEBUSYROLLUP =
        DavPropertyName.create(PROPERTY_EXCLUDE_FREE_BUSY_ROLLUP,
                               NAMESPACE_COSMO);

    /** The Cosmo property name <code>uuid</code> */
    public static final String PROPERTY_UUID = "uuid";

    /** The Cosmo property <code>cosmo:uuid</code> */
    public static final DavPropertyName UUID =
        DavPropertyName.create(PROPERTY_UUID, NAMESPACE_COSMO);

    public static final DavPropertyName OWNER =
        DavPropertyName.create(XML_OWNER, NAMESPACE);
    public static final DavPropertyName SUPPORTEDREPORTSET =
        DavPropertyName.create("supported-report-set", NAMESPACE);

    public static final String QN_PROPFIND =
        DomUtil.getQualifiedName(XML_PROPFIND, NAMESPACE);
    public static final String QN_PROPERTYUPDATE =
        DomUtil.getQualifiedName(XML_PROPERTYUPDATE, NAMESPACE);
    public static final String QN_SET =
        DomUtil.getQualifiedName(XML_SET, NAMESPACE);
    public static final String QN_REMOVE =
        DomUtil.getQualifiedName(XML_REMOVE, NAMESPACE);
    public static final String QN_PROP =
        DomUtil.getQualifiedName(XML_PROP, NAMESPACE);
    public static final String QN_PRIVILEGE =
        DomUtil.getQualifiedName(XML_PRIVILEGE, NAMESPACE);
    public static final String QN_HREF =
        DomUtil.getQualifiedName(XML_HREF, NAMESPACE);
    public static final String QN_OWNER =
        DomUtil.getQualifiedName(XML_OWNER, NAMESPACE);

    public static final QName RESOURCE_TYPE_COLLECTION =
        new QName(NAMESPACE.getURI(), XML_COLLECTION, NAMESPACE.getPrefix());

    public static final UriTemplate TEMPLATE_COLLECTION =
        new UriTemplate("/collection/{uid}/*");
    public static final UriTemplate TEMPLATE_ITEM =
        new UriTemplate("/item/{uid}/*");
    public static final UriTemplate TEMPLATE_USERS =
        new UriTemplate("/users");
    public static final UriTemplate TEMPLATE_USER =
        new UriTemplate("/users/{username}");
    public static final UriTemplate TEMPLATE_HOME =
        new UriTemplate("/{username}/*");
}
