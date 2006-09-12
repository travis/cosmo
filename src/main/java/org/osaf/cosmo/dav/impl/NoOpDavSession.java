/*
 * Copyright 2006 Open Source Applications Foundation
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

import org.apache.jackrabbit.webdav.DavSession;

import org.apache.log4j.Logger;

/**
 * Default implementation of <code>DavSession</code> that does not
 * actually retain any information across requests. All methods throw
 * <code>UnsupportedOperationException</code>.
 */
public class NoOpDavSession implements DavSession {
    private static final Logger log =
        Logger.getLogger(NoOpDavSession.class);

    // DavSession

    /** */
    public void addReference(Object reference) {
        throw new UnsupportedOperationException();
    }

    /** */
    public void removeReference(Object reference) {
        throw new UnsupportedOperationException();
    }

    /** */
    public void addLockToken(String token) {
        throw new UnsupportedOperationException();
    }

    /** */
    public String[] getLockTokens() {
        throw new UnsupportedOperationException();
    }

    /** */
    public void removeLockToken(String token) {
        throw new UnsupportedOperationException();
    }
}
