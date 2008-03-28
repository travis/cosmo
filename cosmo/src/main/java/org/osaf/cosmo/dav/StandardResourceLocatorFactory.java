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

import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.model.User;

/**
 * Standard implementation of {@link DavResourceLocatorFactory}.
 * Returns instances of {@link StandardResourceLocator}.
 *
 * @see DavResourceLocatorFactory
 */
public class StandardResourceLocatorFactory
    implements DavResourceLocatorFactory, ExtendedDavConstants {
    private static final Log log =
        LogFactory.getLog(StandardResourceLocatorFactory.class);

    // DavResourceLocatorFactory methods

    public DavResourceLocator createResourceLocatorByPath(URL context,
                                                          String path) {
        return new StandardResourceLocator(context, path, this);
    }

    public DavResourceLocator createResourceLocatorByUri(URL context,
                                                         String raw)
        throws DavException {
        try {
            URI uri = new URI(raw);

            URL url = null;
            if (raw.startsWith("/")) {
                // absolute-path relative URL
                url = new URL(context, uri.getPath());
            } else {
                // absolute URL
                url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(),
                              uri.getPath());

                // make sure that absolute URLs use the same scheme and
                // authority
                if (url.getProtocol() != null &&
                    ! url.getProtocol().equals(context.getProtocol()))
                    throw new BadGatewayException(uri + " does not specify same scheme as " + context.toString());
                if (url.getAuthority() != null &&
                    ! url.getAuthority().equals(context.getAuthority()))
                    throw new BadGatewayException(uri + " does not specify same authority as " + context.toString());
            }

            if (! url.getPath().startsWith(context.getPath()))
                throw new BadRequestException(uri + " does not specify correct dav path " + context.getPath());

            // trim base path
            String path = url.getPath().substring(context.getPath().length());

            return new StandardResourceLocator(context, path, this);
        } catch (Exception e) {
            if (e instanceof DavException)
                throw (DavException) e;
            throw new BadRequestException("Invalid URL: " + e.getMessage());
        }
    }

    public DavResourceLocator createHomeLocator(URL context,
                                                User user)
        throws DavException {
        String path = TEMPLATE_HOME.bind(user.getUsername());
        return new StandardResourceLocator(context, path, this);
    }

    public DavResourceLocator createPrincipalLocator(URL context,
                                                     User user)
        throws DavException {
        String path = TEMPLATE_USER.bind(user.getUsername());
        return new StandardResourceLocator(context, path, this);
    }
}
