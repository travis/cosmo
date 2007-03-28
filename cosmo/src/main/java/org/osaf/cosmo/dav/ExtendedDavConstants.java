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

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Provides constants defined by Cosmo proprietary *DAV extensions.
 */
public interface ExtendedDavConstants {

    /** The Cosmo XML namespace  */
    public static final Namespace NAMESPACE_COSMO =
        Namespace.getNamespace("cosmo", "http://osafoundation.org/cosmo/DAV");

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
}
