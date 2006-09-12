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

import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.WebdavRequest;

import org.apache.log4j.Logger;

/**
 * Default implementation of <code>DavSessionProvider</code> that
 * associates a <code>WebdavRequest</code> with an instance of
 * <code>NoOpDavSession</code>.
 */
public class NoOpDavSessionProvider implements DavSessionProvider {
    private static final Logger log =
        Logger.getLogger(NoOpDavSessionProvider.class);

    // DavSessionProvider

    /**
     * Associates an instance of <code>NoOpDavSession</code> with the
     * request.
     *
     * @param request
     */
    public boolean attachSession(WebdavRequest request) throws DavException {
        if (log.isDebugEnabled()) {
            log.debug("attaching to no op session");
        }
        request.setDavSession(new NoOpDavSession());
        return true;
    }

    /**
     * Releases the reference from the request to the session.
     *
     * @param request
     */
    public void releaseSession(WebdavRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("releasing no op session");
        }
        request.setDavSession(null);
    }
}
