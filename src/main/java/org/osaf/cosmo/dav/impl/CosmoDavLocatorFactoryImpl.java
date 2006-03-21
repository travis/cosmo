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
package org.osaf.cosmo.dav.impl;

import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl;

import org.osaf.cosmo.repository.PathTranslator;

/**
 * Extends
 * {@link org.apache.jackrabbit.webdav.simple.DavLocatorFactory}
 * to return instances of {@link CosmoDavLocatorImpl}.
 */
public class CosmoDavLocatorFactoryImpl extends LocatorFactoryImpl {

    /**
     */
    public CosmoDavLocatorFactoryImpl(String repositoryPrefix) {
        super(repositoryPrefix);
    }

    /**
     */
    public DavResourceLocator createResourceLocator(String prefix,
                                                    String href) {
        DavResourceLocator locator = super.createResourceLocator(prefix, href);
        return new CosmoDavLocatorImpl(locator);
    }

    /**
     */
    public DavResourceLocator createResourceLocator(String prefix,
                                                    String workspacePath,
                                                    String resourcePath) {
        return createResourceLocator(prefix, workspacePath, resourcePath,
                                     true);
    }

    /**
     * If <code>isResourcePath</code> is false, it represents a JCR
     * path and must be unescaped as per the following rules:
     *
     * <ol>
     * <li> <code>%27</code> is replaced with <code>'</code>
     * </ol>
     */
    public DavResourceLocator createResourceLocator(String prefix,
                                                    String workspacePath,
                                                    String path,
                                                    boolean isResourcePath) {
        String jcrPath = isResourcePath ? null : path;
        String resourcePath = isResourcePath ? path :
            PathTranslator.hexUnescapeJcrPath(jcrPath);
        DavResourceLocator locator =
            super.createResourceLocator(prefix, workspacePath, resourcePath,
                                        true);
        return new CosmoDavLocatorImpl(locator, jcrPath);
    }
}
