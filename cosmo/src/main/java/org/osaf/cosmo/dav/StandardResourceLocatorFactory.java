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
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.util.UriTemplate;

/**
 * Standard implementation of {@link DavResourceLocatorFactory}.
 * Returns instances of {@link StandardResourceLocator}.
 *
 * @see DavResourceLocatorFactory
 * @see ServiceLocatorFactory
 */
public class StandardResourceLocatorFactory
    implements DavResourceLocatorFactory {
    private static final Log log =
        LogFactory.getLog(StandardResourceLocatorFactory.class);

    private ServiceLocatorFactory factory;

    public StandardResourceLocatorFactory(ServiceLocatorFactory factory) {
        this.factory = factory;
    }

    // DavResourceLocatorFactory methods

    public DavResourceLocator createResourceLocator(String base,
                                                    String uri) {
        return createResourceLocator(factory.createServiceLocator(base), uri);
    }

    public DavResourceLocator createResourceLocator(ServiceLocator sl,
                                                    String uri) {
        String path = uri;
        if (path != null) {
            if (path.startsWith(sl.getDavBase())) {
                path = UriTemplate.
                    unescape(path.substring(sl.getDavBase().length()));
                if (! path.startsWith("/"))
                    path = "/" + path;
            } else if (path.startsWith(sl.getFactory().getDavPrefix())) {
                path = UriTemplate.unescape(path.substring(4));
            }
        } else if (path == null || path.equals(""))
            path = "/";
        return new StandardResourceLocator(sl, path, this);
    }

    public DavResourceLocator createResourceLocator(DavResourceLocator rl,
                                                    String uri) {
        return createResourceLocator(rl.getServiceLocator(), uri);
    }
}
