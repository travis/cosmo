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

import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.util.PathUtil;

/**
 * Standard implementation of {@link DavResourceLocator}.
 *
 * @see DavResourceLocator
 */
public class StandardResourceLocator implements DavResourceLocator {
    private static final Log log =
        LogFactory.getLog(StandardResourceLocator.class);

    private URL context;
    private String path;
    private StandardResourceLocatorFactory factory;

    /**
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param path the unescaped dav-relative path of the resource
     * @param factory the locator factory
     */
    public StandardResourceLocator(URL context,
                                   String path,
                                   StandardResourceLocatorFactory factory) {
        this.context = context;
        this.path = path.endsWith("/") && ! path.equals("/") ?
            path.substring(0, path.length()-1) : path;
        this.factory = factory;
    }

    // DavResourceLocator methods

    public String getHref(boolean isCollection) {
        return getHref(false, isCollection);
    }

    public String getHref(boolean absolute,
                          boolean isCollection) {
        String path = isCollection && ! this.path.equals("/") ?
            this.path  + "/" : this.path;
        try {
            path = context.getPath() + path;
            String scheme = absolute ? context.getProtocol() : null;
            String host = absolute ? context.getHost() : null;
            int port = absolute ? context.getPort() : -1;
            URI uri = new URI(scheme, null, host, port, path, null, null);
            return uri.toASCIIString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public URL getUrl(boolean isCollection) {
        try {
            return new URL(getHref(isCollection));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public URL getUrl(boolean absolute,
                      boolean isCollection) {
    try {
        return new URL(getHref(absolute, isCollection));
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }

    public String getPrefix() {
        return context.getProtocol() + "://" + context.getAuthority();
    }

    public String getBasePath() {
        return context.getPath();
    }

    public String getBaseHref() {
        return getBaseHref(false);
    }

    public String getBaseHref(boolean absolute) {
        try {
            if (absolute)
                return context.toURI().toASCIIString();
            return new URI(null, null, context.getPath(), null).
                toASCIIString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPath() {
        return path;
    }

    public URL getContext() {
        return context;
    }

    public DavResourceLocator getParentLocator() {
        return factory.createResourceLocatorByPath(context,
                                             PathUtil.getParentPath(path));
    }

    public DavResourceLocatorFactory getFactory() {
        return factory;
    }

    // our methods

    public int hashCode() {
        return context.hashCode() + path.hashCode();
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
