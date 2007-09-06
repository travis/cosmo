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

import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.MultiStatusResponse;

/**
 * An interface for DAV collection resources.
 */
public interface DavCollection extends DavResource {

    /**
     * Adds a new content item to this resource.
     */
    public void addContent(DavContent content,
                           InputContext input)
        throws DavException;

    /**
     * Adds a new collection to this resource.
     */
    public MultiStatusResponse addCollection(DavCollection collection,
                                             DavPropertySet properties)
        throws DavException;

    /**
     * Returns the member resource at the given absolute href.
     */
    public DavResource findMember(String href)
        throws DavException;
}
