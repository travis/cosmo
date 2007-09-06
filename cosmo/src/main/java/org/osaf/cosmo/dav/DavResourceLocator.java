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

import org.osaf.cosmo.server.ServiceLocator;

/**
 * <p>
 * The interface for classes that encapsulate information about the location
 * of a {@link DavResource}.
 * </p>
 * <p>
 * The URI of a resource is defined as <code>{URI base}{uri-path}</code>.
 * The URI base is defined as the leading portion of a URI that identifies
 * the root of the dav URI space:
 * <code>{scheme}://{authority}{root path}</code>.
 * </p>
 */
public interface DavResourceLocator {

    /**
     * Returns the absolute URI (escaped) of the resource that can be used
     * as the value for a <code>DAV:href</code> property. Appends a trailing
     * slash if <code>isCollection</code>.
     */
    public String getHref(boolean isCollection);

    /**
     * Returns the portion of the resource's URI that identifies the root
     * of the dav URI space.
     */
     public String getBase();

    /**
     * Returns the uri-path (unescaped) of the resource specified relative to
     * the root of the dav URI space.
     */
    public String getPath();

    /**
     * Returns a locator identifying the parent resource.
     */
    public DavResourceLocator getParentLocator();

    /**
     * Returns the factory that instantiated this locator.
     */
    public DavResourceLocatorFactory getFactory();

    /**
     * Returns the <code>ServiceLocator</code> used by this locator to
     * resolve dav URIs.
     */
    public ServiceLocator getServiceLocator();
}
