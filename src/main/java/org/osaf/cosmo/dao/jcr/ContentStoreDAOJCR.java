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
package org.osaf.cosmo.dao.jcr;

import org.osaf.commons.spring.jcr.JCRCallback;
import org.osaf.commons.spring.jcr.support.JCRDaoSupport;
import org.osaf.cosmo.dao.ShareDAO;
import org.osaf.cosmo.jcr.CosmoJcrConstants;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JCR implementation of ShareDAO.
 *
 * @author Brian Moseley
 */
public class ContentStoreDAOJCR extends JCRDaoSupport implements ShareDAO {
    private static final Log log = LogFactory.getLog(ContentStoreDAOJCR.class);

    /**
     */
    public void createHomedir(final String username) {
        if (log.isDebugEnabled()) {
            log.debug("creating homedir for " + username);
        }
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Node rootNode = session.getRootNode();
                    Node homedirNode =
                        rootNode.addNode(username,
                                         CosmoJcrConstants.NT_DAV_COLLECTION);
                    homedirNode.addMixin(CosmoJcrConstants.NT_CALDAV_HOME);
                    homedirNode.addMixin(CosmoJcrConstants.NT_TICKETABLE);
                    homedirNode.
                        setProperty(CosmoJcrConstants.NP_DAV_DISPLAYNAME,
                                    username);
                    // XXX: set calendar home properties
                    rootNode.save();
                    return null;
                }
            });
    }

    /**
     */
    public boolean existsHomedir(final String username) {
        if (log.isDebugEnabled()) {
            log.debug("checking existence of homedir for " + username);
        }
        Boolean rv = (Boolean) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    return new Boolean(session.itemExists("/" + username));
                }
            });
        return rv.booleanValue();
    }

    /**
     */
    public void renameHomedir(final String oldUsername,
                              final String newUsername) {
        if (log.isDebugEnabled()) {
            log.debug("renaming homedir from " + oldUsername + " to " +
                      newUsername);
        }
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    session.move("/" + oldUsername,
                                 "/" + newUsername);
                    session.save();
                    return null;
                }
            });
    }

    /**
     */
    public void deleteHomedir(final String username) {
        if (log.isDebugEnabled()) {
            log.debug("deleting homedir for " + username);
        }
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Item homedir = session.getItem("/" + username);
                    homedir.remove();
                    session.save();
                    return null;
                }
            });
    }
}
