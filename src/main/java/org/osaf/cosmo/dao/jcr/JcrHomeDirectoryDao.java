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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.HomeDirectoryDao;
import org.osaf.cosmo.dao.NoSuchResourceException;
import org.osaf.cosmo.model.Resource;
import org.osaf.cosmo.repository.PathTranslator;
import org.osaf.cosmo.repository.ResourceMapper;
import org.osaf.cosmo.repository.SchemaConstants;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

import org.springmodules.jcr.JcrCallback;
import org.springmodules.jcr.support.JcrDaoSupport;

/**
 * Implementation of {@link HomeDirectoryDao} that operates against a
 * JCR repository.
 *
 * This implementation extends
 * {@link org.springmodules.jcr.JcrDaoSupport} to gain access to
 * a {@link org.springmodules.jcr.JcrTemplate}, which it uses to
 * obtain repository sessions. See the Spring Modules documentation
 * for more information on how to configure the template with
 * credentials, a repository reference and a workspace name.
 *
 * It uses {@link ResourceMapper} to convert JCR nodes and
 * properties to and from instances of
 * {@link Resource}.
 */
public class JcrHomeDirectoryDao extends JcrDaoSupport
    implements SchemaConstants, HomeDirectoryDao {
    private static final Log log =
        LogFactory.getLog(JcrHomeDirectoryDao.class);

    // HomeDirectoryDao methods

    /**
     * Returns the resource at the specified path within the
     * repository.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     * @throws InvalidDataAccessResourceUsageException if the item at
     * the specified path is not a node of type
     * <code>dav:resource</code> or <code>dav:collection</code>
     */
    public Resource getResource(final String path) {
        return (Resource) getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    String jcrPath = PathTranslator.toRepositoryPath(path);
                    if (! session.itemExists(jcrPath)) {
                        throw new NoSuchResourceException(path);
                    }

                    Item item = session.getItem(jcrPath);
                    if (! item.isNode()) {
                        throw new InvalidDataAccessResourceUsageException("item at path " + jcrPath + " not a node");
                    }

                    Node node = (Node) item;
                    if (! (node.getPath().equals("/") ||
                           node.isNodeType(NT_DAV_RESOURCE) ||
                           node.isNodeType(NT_DAV_COLLECTION))) {
                        throw new InvalidDataAccessResourceUsageException("item at path " + jcrPath + " not a resource node");
                    }

                    return ResourceMapper.nodeToResource(node);
                }
            });
    }

    /**
     * Removes the resource at the specified path within the
     * repository.
     */
    public void removeResource(final String path) {
        getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    String jcrPath = PathTranslator.toRepositoryPath(path);
                    if (session.itemExists(jcrPath)) {
                        session.getItem(jcrPath).remove();
                        session.save();
                    }
                    return null;
                }
            });
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        // does nothing
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods
}
