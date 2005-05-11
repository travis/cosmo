package org.osaf.cosmo.dao.jcr;

import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.model.User;
import org.osaf.spring.jcr.JCRCallback;
import org.osaf.spring.jcr.support.JCRDaoSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * JCR implementation of <code>UserDAO</code>.
 *
 * @author Brian Moseley
 */
public class UserDAOJCR extends JCRDaoSupport implements UserDAO {
    private static final Log log = LogFactory.getLog(UserDAOJCR.class);

    private static final String NODE_USERS = "/users";
    private static final String PROP_USERNAME = "username";
    private static final String PROP_EMAIL = "email";
    private static final String PROP_PASSWORD = "password";

    /**
     */
    public List getUsers() {
        if (log.isDebugEnabled()) {
            log.debug("getting users");
        }
        return (List) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Node rootNode = session.getRootNode();
                    if (! rootNode.hasNode(NODE_USERS)) {
                        rootNode.addNode(NODE_USERS);
                        rootNode.save();
                    }

                    List users = new ArrayList();
                    for (NodeIterator i=rootNode.getNode(NODE_USERS).getNodes();
                         i.hasNext();) {
                        Node userNode = i.nextNode();
                        users.add(populateUser(userNode));
                    }

                    // XXX implement write-time node ordering?
                    Collections.sort(users);

                    return users;
                }
            });
    }

    /**
     */
    public User getUser(Long id) {
        return null;
    }

    /**
     */
    public User getUser(String username) {
        return null;
    }

    /**
     */
    public void saveUser(User user) {
    }

    /**
     */
    public void updateUser(User user) {
    }

    /**
     */
    public void removeUser(Long id) {
    }

    /**
     */
    public void removeUser(User user) {
    }

    private User populateUser(Node node)
        throws RepositoryException {
        User user = new User();

        try {
            user.setUsername(node.getProperty(PROP_USERNAME).getString());
            user.setEmail(node.getProperty(PROP_EMAIL).getString());
            user.setPassword(node.getProperty(PROP_PASSWORD).getString());
            // XXX dateCreated
            // XXX dateModified
        } catch (ValueFormatException e) {
            throw new DataRetrievalFailureException("error populating user from JCR node", e);
        }

        return user;
    }
}
