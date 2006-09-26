/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl;

import org.apache.log4j.Logger;

/**
 * Extends jcr-server's <code>LocatorFactoryImpl</code> to add factory
 * methods for alternate resource URLs (used by other
 * protocols/interfaces to access the resource).
 *
 * @see org.apache.jackrabbit.webdav.DavLocatorFactory
 * @see org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl
 */
public class StandardLocatorFactory extends LocatorFactoryImpl {
    private static final Logger log =
        Logger.getLogger(StandardLocatorFactory.class);

    private String atomPath;
    private String cmpPath;
    private String webPath;

    /** */
    public StandardLocatorFactory(String davPath,
                                  String atomPath,
                                  String cmpPath,
                                  String webPath) {
        super(davPath);
        this.atomPath = atomPath;
        this.cmpPath = cmpPath;
        this.webPath = webPath;
    }

    /**
     * Returns a locator that provides the Atom URL for the resource
     * at the provided path.
     */
    public DavResourceLocator createAtomLocator(String prefix,
                                                String resourcePath) {
        String authority = prefix.substring(0, prefix.lastIndexOf("/"));
        return createResourceLocator(authority + atomPath, null,
                                     resourcePath, false);
    }

    /**
     * Returns a locator that provides the Cmp URL for the resource
     * at the provided path.
     */
    public DavResourceLocator createCmpLocator(String prefix,
                                                String resourcePath) {
        String authority = prefix.substring(0, prefix.lastIndexOf("/"));
        return createResourceLocator(authority + cmpPath, null,
                                     resourcePath, false);
    }

    /**
     * Returns a locator that provides the Web URL for the resource
     * at the provided path.
     */
    public DavResourceLocator createWebLocator(String prefix,
                                                String resourcePath) {
        String authority = prefix.substring(0, prefix.lastIndexOf("/"));
        return createResourceLocator(authority + webPath, null,
                                     resourcePath, false);
    }
}
