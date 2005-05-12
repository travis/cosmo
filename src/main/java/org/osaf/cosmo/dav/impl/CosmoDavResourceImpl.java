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

import java. io.IOException;
import java. io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.server.simple.dav.DavResourceImpl;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 *
 * For now, this class is a placeholder.
 */
public class CosmoDavResourceImpl extends DavResourceImpl {

    /**
     * Create a new {@link DavResource}.
     *
     * @param locator
     * @param factory
     * @param session
     */
    public CosmoDavResourceImpl(DavResourceLocator locator,
                                DavResourceFactory factory,
                                DavSession session)
        throws RepositoryException {
        super(locator, factory, session);
    }
}
