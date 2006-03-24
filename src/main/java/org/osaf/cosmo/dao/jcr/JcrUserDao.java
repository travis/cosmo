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
import org.osaf.cosmo.repository.PathTranslator;
import org.osaf.cosmo.repository.SchemaConstants;
import org.osaf.cosmo.repository.UserMapper;

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
    implements SchemaConstants, UserDao {
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
                    for (NodeIterator i=queryForUsers(session).getNodes();
                         i.hasNext();) {
                        Node node = i.nextNode();
                        users.add(UserMapper.nodeToUser(node));
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
                    Node node = findUser(session, username);
                    if (node == null) {
                        throw new DataRetrievalFailureException("account for " +
                                                                " username " +
                                                                username +
                                                                " not found");
                    }
                    return UserMapper.nodeToUser(node);
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

                    return UserMapper.nodeToUser(i.nextNode());
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
                    // validate username uniquess
                    Node un = findUser(session, user.getUsername());
                    if (un != null) {
                        throw new DuplicateUsernameException(user.getUsername());
                    }
                    // validate email uniqueness
                    QueryResult eqr =
                        queryForUserByEmail(session, user.getEmail());
                    if (eqr.getNodes().hasNext()) {
                        throw new DuplicateEmailException(user.getEmail());
                    }

                    // create intermediary structural nodes if necessary
                    String n1 = user.getUsername().substring(0, 1);
                    Node l1 = session.getRootNode().hasNode(n1) ?
                        session.getRootNode().getNode(n1) :
                        session.getRootNode().addNode(n1, NT_UNSTRUCTURED);

                    String n2 = user.getUsername().substring(0, 2);
                    Node l2 = l1.hasNode(n2) ?
                        l1.getNode(n2) :
                        l1.addNode(n2, NT_UNSTRUCTURED);

                    // XXX: use ResourceMapper to make the node a home
                    // collection
                    Node node = l2.addNode(user.getUsername(), NT_FOLDER);
                    node.addMixin(NT_USER);
                    user.setDateModified(new Date());
                    user.setDateCreated(user.getDateModified());
                    UserMapper.userToNode(user, node);

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
                    // find the user node corresponding to the old
                    // username - this must exist
                    Node on = findUser(session, user.getOldUsername());
                    if (on == null) {
                        throw new DataRetrievalFailureException("account " + user.getOldUsername() + " not found");
                    }

                    if (user.isUsernameChanged()) {
                        // find the user node corresponding to the new
                        // username - this must not exist
                        Node nn = findUser(session, user.getUsername());
                        if (nn != null) {
                            throw new DuplicateUsernameException(user.getUsername());
                        }
                    }

                    if (user.isEmailChanged()) {
                        // find the user node corresponding to the new
                        // email address - if one exists, it must be
                        // the node for the old username (in other
                        // words, the one we are updating, not some
                        // random other user's node)
                        QueryResult eqr =
                            queryForUserByEmail(session, user.getEmail());
                        if (eqr.getNodes().hasNext() &&
                            ! eqr.getNodes().nextNode().getPath().
                            equals(on.getPath())) {
                            throw new DuplicateEmailException(user.getEmail());
                        }
                    }

                    // update the old node
                    user.setDateModified(new Date());
                    UserMapper.userToNode(user, on);

                    if (user.isUsernameChanged()) {
                        // move the node to the location for its new
                        // username
                        String newPath = PathTranslator.
                            toRepositoryPath("/" + user.getUsername());
                        session.move(on.getPath(), newPath);
                        session.save();
                    }
                    else {
                        on.save();
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
                    Node n = findUser(session, username);
                    if (n != null) {
                        n.remove();
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

    /**
     */
    protected Node findUser(Session session,
                            String username)
        throws RepositoryException {
        String path = PathTranslator.toRepositoryPath("/" + username);
        return session.itemExists(path) ? (Node) session.getItem(path) : null;
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
     */
    protected QueryResult queryForUsers(Session session)
        throws RepositoryException {
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root//element(*, ").
            append(NT_USER).
            append(")").
            append("[@").
            append(NP_USER_USERNAME).
            append("]");
        return executeXPathQuery(session, stmt.toString());
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
