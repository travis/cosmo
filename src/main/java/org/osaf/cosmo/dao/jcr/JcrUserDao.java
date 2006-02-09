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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springmodules.jcr.JcrCallback;
import org.springmodules.jcr.JcrTemplate;
import org.springmodules.jcr.support.JcrDaoSupport;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Implementation of <code>UserDao</code> that operates against a
 * JCR repository.
 *
 * This implementation extends
 * {@link org.springmodules.jcr.JcrDaoSupport} to gain access to
 * a {@link org.springmodules.jcr.JcrTemplate}, which it uses to
 * obtain repository sessions. See the Spring Modules documentation
 * for more information on how to configure the template with
 * credentials, a repository reference and a workspace name.
 *
 * A user account is persisted as a <code>nt:folder</code> node with
 * the <code>cosmo:user</code> and <code>calendar:home</code> mixin
 * types. This implementation places all user accounts as children of
 * the root node.
 */
public class JcrUserDao extends JcrDaoSupport
    implements JcrConstants, UserDao {
    private static final Log log = LogFactory.getLog(JcrUserDao.class);

    // UserDao methods

    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    public Set getUsers() {
        return (Set) getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    Set users = new HashSet();
                    for (NodeIterator i=session.getRootNode().getNodes();
                         i.hasNext();) {
                        Node node = i.nextNode();
                        if (node.isNodeType(NT_USER)) {
                            users.add(JcrUserMapper.nodeToUser(node));
                        }
                    }

                    return users;
                }
            });
    }

    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUser(final String username) {
        return (User) getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    String path = calculateUserNodePath(username);
                    if (! session.itemExists(path)) {
                        throw new DataRetrievalFailureException("account " +
                                                                username +
                                                                " not found");
                    }

                    return JcrUserMapper.
                        nodeToUser((Node) session.getItem(path));
                }
            });
    }

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUserByEmail(final String email) {
        return (User) getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    QueryResult qr = queryForUserByEmail(session, email);
                    NodeIterator i = qr.getNodes();
                    if (! i.hasNext()) {
                        throw new DataRetrievalFailureException("account for " +
                                                                " email " +
                                                                email +
                                                                " not found");
                    }

                    return JcrUserMapper.nodeToUser(i.nextNode());
                }
            });
    }

    /**
     * Creates a user account in the repository. Returns a new
     * instance of <code>User</code> after saving the original one.
     *
     * @param user the account to create
     * 
     * @throws DuplicateUsernameException if the username is already
     * in use
     * @throws DuplicateEmailException if the email address is already
     * in use
     */
    public void createUser(final User user) {
        user.validate();
        // bug 5095: synchronize access to the template so that only
        // one thread (and therefore jcr session) is modifying the
        // root node at any given time. a better solution might be to
        // use intermediary nodes between the root node and the
        // homedir node, but even then there is still the possibility,
        // however low, of concurrent modification of a parent node.
        JcrTemplate template = getJcrTemplate();
        synchronized (template) {
            template.execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    Node parent = getUserNodeParentNode(session);
                    String path = calculateUserNodePath(user.getUsername());

                    // validate username uniquess
                    if (session.itemExists(path)) {
                        throw new DuplicateUsernameException(user.getUsername());
                    }
                    // validate email uniqueness
                    QueryResult qr = queryForUserByEmail(session,
                                                         user.getEmail());
                    NodeIterator i = qr.getNodes();
                    if (i.hasNext()) {
                        throw new DuplicateEmailException(user.getEmail());
                    }

                    Node node = parent.addNode(user.getUsername(), NT_FOLDER);
                    node.addMixin(NT_USER);
                    user.setDateModified(new Date());
                    user.setDateCreated(user.getDateModified());
                    JcrUserMapper.userToNode(user, node);

                    node.addMixin(NT_TICKETABLE);
                    node.addMixin(NT_HOME_COLLECTION);
                    node.addMixin(NT_DAV_COLLECTION);
                    node.setProperty(NP_DAV_DISPLAYNAME, user.getUsername());

                    session.save();
                    return null;
                }
            });
        }
    }

    /**
     * Updates a user account that exists in the repository. Returns a
     * new instance of <code>User</code>  after saving the original
     * one.
     *
     * @param user the account to update
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     * @throws DuplicateUsernameException if the username is already
     * in use
     * @throws DuplicateEmailException if the email address is already
     * in use
     */
    public void updateUser(final User user) {
        user.validate();
        getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    String oldPath =
                        calculateUserNodePath(user.getOldUsername());
                    String newPath =
                        calculateUserNodePath(user.getUsername());

                    if (! session.itemExists(oldPath)) {
                        throw new DataRetrievalFailureException("account " + user.getOldUsername() + " not found");
                    }

                    if (user.isUsernameChanged()) {
                        // validate uniqueness of new username
                        if (session.itemExists(newPath)) {
                            throw new DuplicateUsernameException(user.getUsername());
                        }
                    }

                    if (user.isEmailChanged()) {
                        // validate email uniqueness
                        QueryResult qr =
                            queryForUserByEmail(session, user.getEmail());
                        NodeIterator i = qr.getNodes();
                        if (i.hasNext() &&
                            ! i.nextNode().getPath().equals(oldPath)) {
                            throw new DuplicateEmailException(user.getEmail());
                        }
                    }

                    Node node = (Node) session.getItem(oldPath);
                    user.setDateModified(new Date());
                    JcrUserMapper.userToNode(user, node);

                    if (user.isUsernameChanged()) {
                        session.move(oldPath, newPath);
                        session.save();
                    }
                    else {
                        node.save();
                    }

                    return null;
                }
            });
    }

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(final String username) {
        getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
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
     * Returns the JCR node underneath which user account nodes are
     * created.
     *
     * This implementation places user account nodes underneath the
     * root node of the workspace.
     */
    protected Node getUserNodeParentNode(Session session)
        throws RepositoryException {
        return session.getRootNode();
    }

    /**
     * Returns the JCR path to a user account node.
     */
    protected String calculateUserNodePath(String username) {
        return "/" + username;
    }

    /**
     * Simple helper method for executing an XPath query.
     */
    protected QueryResult executeXPathQuery(Session session,
                                            String statement)
        throws RepositoryException {
        QueryManager qm =
            session.getWorkspace().getQueryManager();
        return qm.createQuery(statement.toString(), Query.XPATH).execute();
    }


    /**
     * Executes a query to find all of the user account nodes that
     * match the given email address.
     */
    protected QueryResult queryForUserByEmail(Session session, String email)
        throws RepositoryException {
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root").
            append("//element(*, ").
            append(NT_USER).
            append(")").
            append("[@").
            append(NP_USER_EMAIL).
            append(" = '").
            append(email).
            append("']");
        return executeXPathQuery(session, stmt.toString());
    }
}
