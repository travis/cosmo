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

import javax.jcr.Session;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.simple.DavSessionImpl;

import org.apache.log4j.Logger;

import org.springmodules.jcr.JcrSessionFactory;

/**
 * Implementation of the jcr-server {@link DavSessionProvider}
 * interface that uses a {@link JcrSessionFactory} to log into the
 * repository and provide a
 * {@link org.apache.jackrabbit.webdav.DavSession} to the request.
 */
public class CosmoDavSessionProviderImpl implements DavSessionProvider {
    private static final Logger log =
        Logger.getLogger(CosmoDavSessionProviderImpl.class);

    private JcrSessionFactory sessionFactory;

    // DavSessionProvider methods

    /**
     * Acquires a DavSession. Upon success, the WebdavRequest will
     * reference that session.
     *
     * A session will not be available if an exception is thrown.
     *
     * @param request
     * @return <code>true</code> if the session was attached to the request;
     *         <code>false</code> otherwise.
     * @throws DavException if a problem occurred while obtaining the session
     */
    public boolean attachSession(WebdavRequest request) throws DavException {
        try {
            Session session = sessionFactory.getSession();
            DavSession davSession = new DavSessionImpl(session);
            request.setDavSession(davSession);
            return true;
        } catch (Exception e) {
            log.error("error logging into repository", e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Releases the reference from the request to the session.
     *
     * @param request
     */
    public void releaseSession(WebdavRequest request) {
        request.getDavSession().getRepositorySession().logout();
        request.setDavSession(null);
    }

    // our methods

    /**
     */
    public JcrSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     */
    public void setSessionFactory(JcrSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
