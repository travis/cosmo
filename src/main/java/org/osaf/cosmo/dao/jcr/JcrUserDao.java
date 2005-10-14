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

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.commons.spring.jcr.JCRCallback;
import org.osaf.commons.spring.jcr.support.JCRDaoSupport;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * JCR implementation of <code>UserDao</code>.
 *
 * @author Brian Moseley
 */
public class JcrUserDao extends JCRDaoSupport implements UserDao {
    private static final Log log = LogFactory.getLog(JcrUserDao.class);

    // UserDao methods

    /**
     * Returns all of the <code>User</code>s in the system.
     */
    public Set getUsers() {
        return (Set) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Set users = new HashSet();
                    for (NodeIterator i=session.getRootNode().getNodes();
                         i.hasNext();) {
                        Node node = i.nextNode();
                        if (node.isNodeType(CosmoJcrConstants.NT_COSMO_USER)) {
                            users.add(nodeToUser(node));
                        }
                    }

                    return users;
                }
            });
    }

    /**
     */
    public User getUser(final String username) {
        return (User) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    String path = calculateUserNodePath(username);
                    if (! session.itemExists(path)) {
                        throw new ObjectRetrievalFailureException(User.class,
                                                                  username);
                    }

                    return nodeToUser((Node) session.getItem(path));
                }
            });
    }

    /**
     */
    public User getUserByEmail(final String email) {
        return (User) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    QueryResult qr = queryForUserByEmail(session, email);
                    NodeIterator i = qr.getNodes();
                    if (! i.hasNext()) {
                        throw new ObjectRetrievalFailureException(User.class,
                                                                  email);
                    }

                    return nodeToUser(i.nextNode());
                }
            });
    }

    /**
     */
    public void createUser(final User user) {
        user.validate();
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Node parent = getUserNodeParentNode(session);
                    String path = calculateUserNodePath(user.getUsername());

                    // validate username uniquess
                    if (session.itemExists(path)) {
                        throw new DuplicateUsernameException();
                    }
                    // validate email uniqueness
                    QueryResult qr = queryForUserByEmail(session,
                                                         user.getEmail());
                    NodeIterator i = qr.getNodes();
                    if (i.hasNext()) {
                        throw new DuplicateEmailException();
                    }

                    Node node = parent.addNode(user.getUsername(),
                                               CosmoJcrConstants.NT_FOLDER);
                    node.addMixin(CosmoJcrConstants.NT_COSMO_USER);
                    user.setDateModified(new Date());
                    user.setDateCreated(user.getDateModified());
                    userToNode(user, node);

                    node.addMixin(CosmoJcrConstants.NT_TICKETABLE);
                    node.addMixin(CosmoJcrConstants.NT_DAV_COLLECTION);
                    node.addMixin(CosmoJcrConstants.NT_CALDAV_HOME);
                    node.setProperty(CosmoJcrConstants.NP_DAV_DISPLAYNAME,
                                     user.getUsername());
                    node.setProperty(CosmoJcrConstants.
                                     NP_CALDAV_CALENDARDESCRIPTION,
                                     user.getUsername());
                    node.setProperty(CosmoJcrConstants.NP_XML_LANG,
                                     Locale.getDefault().toString());

                    session.save();
                    return null;
                }
            });
    }

    /**
     */
    public void updateUser(final User user) {
        user.validate();
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    String path = calculateUserNodePath(user.getUsername());

                    if (user.isUsernameChanged()) {
                        // validate uniqueness of new username
                        if (session.itemExists(path)) {
                            throw new DuplicateUsernameException();
                        }
                    }
                    else if (! session.itemExists(path)) {
                        throw new
                            ObjectRetrievalFailureException(User.class,
                                                            user.getUsername());
                    }

                    if (user.isEmailChanged()) {
                        // validate email uniqueness
                        QueryResult qr = queryForUserByEmail(session,
                                                             user.getEmail());
                        NodeIterator i = qr.getNodes();
                        if (i.hasNext() &&
                            ! i.nextNode().getPath().equals(path)) {
                            throw new DuplicateEmailException();
                        }
                    }

                    Node node = (Node) session.getItem(path);
                    user.setDateModified(new Date());
                    userToNode(user, node);

                    node.save();
                    return null;
                }
            });
    }

    /**
     */
    public void removeUser(final String username) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    String path = calculateUserNodePath(username);
                    if (! session.itemExists(path)) {
                        return null;
                    }

                    session.getItem(path).remove();

                    session.save();
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

    /**
     */
    protected Node getUserNodeParentNode(Session session)
        throws RepositoryException {
        return session.getRootNode();
    }

    /**
     */
    protected String calculateUserNodePath(String username) {
        return "/" + username;
    }

    /**
     */
    protected QueryResult executeXPathQuery(Session session,
                                            String statement)
        throws RepositoryException {
        QueryManager qm =
            session.getWorkspace().getQueryManager();
        return qm.createQuery(statement.toString(), Query.XPATH).execute();
    }


    /**
     */
    protected QueryResult queryForUserByEmail(Session session, String email)
        throws RepositoryException {
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root").
            append("//element(*, ").
            append(CosmoJcrConstants.NT_COSMO_USER).
            append(")").
            append("[@").
            append(CosmoJcrConstants.NP_COSMO_EMAIL).
            append(" = '").
            append(email).
            append("']");
        return executeXPathQuery(session, stmt.toString());
    }

    /**
     */
    protected User nodeToUser(Node node)
        throws RepositoryException {
        User user = new User();

        user.setUsername(node.getProperty(CosmoJcrConstants.NP_COSMO_USERNAME).getString());
        user.setPassword(node.getProperty(CosmoJcrConstants.NP_COSMO_PASSWORD).getString());
        user.setFirstName(node.getProperty(CosmoJcrConstants.NP_COSMO_FIRSTNAME).getString());
        user.setLastName(node.getProperty(CosmoJcrConstants.NP_COSMO_LASTNAME).getString());
        user.setEmail(node.getProperty(CosmoJcrConstants.NP_COSMO_EMAIL).getString());
        user.setAdmin(JCRUtils.getBooleanValue(node, CosmoJcrConstants.NP_COSMO_ADMIN));
        user.setDateCreated(JCRUtils.getDateValue(node, CosmoJcrConstants.NP_COSMO_DATECREATED));
        user.setDateModified(JCRUtils.getDateValue(node, CosmoJcrConstants.NP_COSMO_DATEMODIFIED));

        return user;
    }

    /**
     */
    protected void userToNode(User user, Node node)
        throws RepositoryException {
        node.setProperty(CosmoJcrConstants.NP_COSMO_USERNAME,
                         user.getUsername());
        node.setProperty(CosmoJcrConstants.NP_COSMO_PASSWORD,
                         user.getPassword());
        node.setProperty(CosmoJcrConstants.NP_COSMO_FIRSTNAME,
                         user.getFirstName());
        node.setProperty(CosmoJcrConstants.NP_COSMO_LASTNAME,
                         user.getLastName());
        node.setProperty(CosmoJcrConstants.NP_COSMO_EMAIL, user.getEmail());
        node.setProperty(CosmoJcrConstants.NP_COSMO_ADMIN,
                         user.getAdmin().booleanValue());
        JCRUtils.setDateValue(node, CosmoJcrConstants.NP_COSMO_DATECREATED,
                              user.getDateCreated());
        JCRUtils.setDateValue(node, CosmoJcrConstants.NP_COSMO_DATEMODIFIED,
                              user.getDateModified());
    }
}
