/*
 * Copyright 2007 Open Source Applications Foundation
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.util.PathUtil;
import org.osaf.cosmo.util.UriTemplate;

/**
 * Standard implementation of {@link DavResourceLocator}.
 *
 * @see DavResourceLocator
 * @see ServiceLocator
 */
public class StandardResourceLocator implements DavResourceLocator {
    private static final Log log =
        LogFactory.getLog(StandardResourceLocator.class);

    private ServiceLocator locator;
    private String path;
    private String href;
    private StandardResourceLocatorFactory factory;

    public StandardResourceLocator(ServiceLocator locator,
                                   String path,
                                   StandardResourceLocatorFactory factory) {
        this.locator = locator;
        this.path = path.endsWith("/") && ! path.equals("/") ?
            path.substring(0, path.length()-1) : path;
        this.href = locator.getDavBase() + UriTemplate.escapePath(path);
        this.factory = factory;
    }

    // DavResourceLocator methods

    public String getHref(boolean isCollection) {
        String suffix = isCollection && ! path.equals("/") ? "/" : "";
        return href + suffix;
    }

    public String getBase() {
        return locator.getDavBase();
    }

    public String getPath() {
        return path;
    }

    public DavResourceLocator getParentLocator() {
        return factory.createResourceLocator(locator.getDavBase(),
                                             PathUtil.getParentPath(path));
    }

    public DavResourceLocatorFactory getFactory() {
        return factory;
    }

    public ServiceLocator getServiceLocator() {
        return locator;
    }

    // our methods

    public int hashCode() {
        return href.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (! (o instanceof StandardResourceLocator))
            return false;
        StandardResourceLocator other = (StandardResourceLocator) o;
        return other.hashCode() == hashCode();
    }
}
