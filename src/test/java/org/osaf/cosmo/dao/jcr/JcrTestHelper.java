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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.osaf.cosmo.model.User;

/**
 */
public class JcrTestHelper implements JcrConstants {
    static int useq = 0;

    private JcrTestHelper() {
    }

    /**
     */
    public static User makeAndStoreDummyUser(Session session)
        throws RepositoryException {
        String serial = new Integer(++useq).toString();
        String username = "dummy" + serial;

        Node node = session.getRootNode().addNode(username);
        node.addMixin(NT_USER);
        node.setProperty(NP_USER_USERNAME, username);
        node.setProperty(NP_USER_PASSWORD, username);
        node.setProperty(NP_USER_FIRSTNAME, username);
        node.setProperty(NP_USER_LASTNAME, username);
        node.setProperty(NP_USER_EMAIL, username);
        node.setProperty(NP_USER_ADMIN, false);
        node.setProperty(NP_USER_DATECREATED, Calendar.getInstance());
        node.setProperty(NP_USER_DATEMODIFIED, Calendar.getInstance());
        session.getRootNode().save();

        return nodeToUser(node);
    }

    /**
     */
    public static User findDummyUser(Session session, String username)
        throws RepositoryException {
        return session.getRootNode().hasNode(username) ?
            nodeToUser(session.getRootNode().getNode(username)) :
            null;
    }

    /**
     */
    public static void removeDummyUser(Session session, User user)
        throws RepositoryException {
        session.getRootNode().getNode(user.getUsername()).remove();
        session.save();
    }

    private static User nodeToUser(Node node)
        throws RepositoryException {
        User user = new User();

        user.setUsername(node.getProperty(NP_USER_USERNAME).getString());
        user.setPassword(node.getProperty(NP_USER_PASSWORD).getString());
        user.setFirstName(node.getProperty(NP_USER_FIRSTNAME).getString());
        user.setLastName(node.getProperty(NP_USER_LASTNAME).getString());
        user.setEmail(node.getProperty(NP_USER_EMAIL).getString());
        user.setAdmin(new Boolean(node.getProperty(NP_USER_ADMIN).
                                  getBoolean()));
        user.setDateCreated(node.getProperty(NP_USER_DATECREATED).
                            getDate().getTime());
        user.setDateModified(node.getProperty(NP_USER_DATEMODIFIED).
                             getDate().getTime());

        return user;
    }
}
