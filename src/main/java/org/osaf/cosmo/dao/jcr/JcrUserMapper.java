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
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.User;

/**
 * Utility class that converts between {@link User}s and
 * {@link javax.jcr.Node}s.
 */
public class JcrUserMapper implements JcrConstants {
    private static final Log log = LogFactory.getLog(JcrUserMapper.class);

    /**
     * Returns a new instance of <code>User</code> populated from a
     * user account node.
     */
    public static User nodeToUser(Node node)
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

    /**
     * Copies the properties of a <code>User</code> into a user
     * account node.
     */
    public static void userToNode(User user, Node node)
        throws RepositoryException {
        node.setProperty(NP_USER_USERNAME, user.getUsername());
        node.setProperty(NP_USER_PASSWORD, user.getPassword());
        node.setProperty(NP_USER_FIRSTNAME, user.getFirstName());
        node.setProperty(NP_USER_LASTNAME, user.getLastName());
        node.setProperty(NP_USER_EMAIL, user.getEmail());
        node.setProperty(NP_USER_ADMIN, user.getAdmin().booleanValue());
        Calendar created = Calendar.getInstance();
        if (user.getDateCreated() != null) {
            created.setTime(user.getDateCreated());
        }
        node.setProperty(NP_USER_DATECREATED, created);
        Calendar modified = Calendar.getInstance();
        if (user.getDateModified() != null) {
            created.setTime(user.getDateModified());
        }
        node.setProperty(NP_USER_DATEMODIFIED, modified);
    }
}
