/*
 * Copyright 007 Open Source Applications Foundation
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

import org.osaf.cosmo.server.ServiceLocator;

/**
 * <p>
 * The interface for factory classes that create instances of
 * {@link DavResourceLocator}.
 * </p>
 */
public interface DavResourceLocatorFactory {

    /**
     * <p>
     * Returns a locator for the resource at the given URI or uri-path as
     * resolved against the given URI base.
     * </p>
     * <p>
     * If the URI starts with the URI base, it is interpreted as an
     * absolute, unescaped URI. Otherwise, it is interpreted as an escaped
     * uri-path relative to the URI base.
     * </p>
     * <p>
     * See {@link DavResourceLocator} for the definition of the URI base.
     * </p>
     */
    public DavResourceLocator createResourceLocator(String base,
                                                    String uri);

    /**
     * <p>
     * Returns a locator for the resource at the given URI or uri-path as
     * resolved against the given <code>ServiceLocator</code>.
     * </p>
     * <p>
     * Follows the same rules as
     * {@link #createDavResourceLocator(String, String)}.
     * </p>
     */
    public DavResourceLocator createResourceLocator(ServiceLocator sl,
                                                    String uri);

    /**
     * <p>
     * Returns a locator for the resource at the given URI or uri-path as
     * resolved against the given <code>DavResourceLocator</code>.
     * </p>
     * <p>
     * Follows the same rules as
     * {@link #createDavResourceLocator(String, String)}.
     * </p>
     */
    public DavResourceLocator createResourceLocator(DavResourceLocator rl,
                                                    String uri);
}
