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

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;

import org.osaf.cosmo.jcr.JCREscapist;

/**
 * Wraps an instance of 
 * {@link org.apache.jackrabbit.webdav.DavResourceLocator}
 * to provide customized behavior. Implemented as a wrapper rather
 * than a subclass since the standard jcr-server implementation of
 * DavResourceLocator is a private inner class of
 * {@link org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl}.
 */
public class CosmoDavLocatorImpl implements DavResourceLocator {

    private DavResourceLocator wrapped;
    private String jcrPath;

    /**
     */
    public CosmoDavLocatorImpl(DavResourceLocator locator) {
        this(locator, null);
    }

    /**
     */
    public CosmoDavLocatorImpl(DavResourceLocator locator,
                               String jcrPath) {
        this.wrapped = locator;
        this.jcrPath = jcrPath != null ?
            jcrPath :
            JCREscapist.hexEscapeJCRPath(locator.getResourcePath());
    }

    /**
     */
    public String getPrefix() {
        return wrapped.getPrefix();
    }

    /**
     */
    public String getResourcePath() {
        return wrapped.getResourcePath();
    }

    /**
     */
    public String getWorkspacePath() {
        return wrapped.getWorkspacePath();
    }

    /**
     */
    public String getWorkspaceName() {
        return wrapped.getWorkspaceName();
    }

    /**
     */
    public boolean isSameWorkspace(DavResourceLocator locator) {
        return wrapped.isSameWorkspace(locator);
    }

    /**
     */
    public boolean isSameWorkspace(String workspaceName) {
        return wrapped.isSameWorkspace(workspaceName);
    }

    /**
     */
    public String getHref(boolean isCollection) {
        return wrapped.getHref(isCollection);
    }

    /**
     */
    public boolean isRootLocation() {
        return wrapped.isRootLocation();
    }

    /**
     */
    public DavLocatorFactory getFactory() {
        return wrapped.getFactory();
    }

    /**
     */
    public String getJcrPath() {
        if (jcrPath != null) {
            return jcrPath;
        }

        return getResourcePath();
    }
}
