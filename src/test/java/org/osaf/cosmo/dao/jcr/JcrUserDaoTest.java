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
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Test Case for {@link JcrUserDao}.
 */
public class JcrUserDaoTest extends BaseJcrDaoTestCase {
    private static final Log log = LogFactory.getLog(JcrUserDaoTest.class);

    private JcrUserDao dao;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        dao = new JcrUserDao();
        dao.setTemplate(getTemplate());
        dao.init();
    }

    /**
     */
    protected void tearDown() throws Exception {
        dao.destroy();
        dao = null;
        super.tearDown();
    }

    /**
     */
    public void testGetUsers() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);
        User u2 = JcrTestHelper.makeAndStoreDummyUser(session);
        User u3 = JcrTestHelper.makeAndStoreDummyUser(session);

        Set users = dao.getUsers();

        // account for root user
        assertTrue("Not 4 users", users.size() == 4);
        assertTrue("User 1 not found in users", users.contains(u1));
        assertTrue("User 2 not found in users", users.contains(u2));
        assertTrue("User 3 not found in users", users.contains(u3));

        JcrTestHelper.removeDummyUser(session, u1);
        JcrTestHelper.removeDummyUser(session, u2);
        JcrTestHelper.removeDummyUser(session, u3);

        session.logout();
    }

    /**
     */
    public void testGetUsersNoUsers() throws Exception {
        Set users = dao.getUsers();

        // account for root user
        assertTrue("Found users", users.size() == 1);
    }

    /**
     */
    public void testGetUser() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);

        User user = dao.getUser(u1.getUsername());
        assertNotNull("User " + u1.getUsername() + " null", user);

        JcrTestHelper.removeDummyUser(session, u1);

        session.logout();
    }

    /**
     */
    public void testGetUserNotFound() throws Exception {
        try {
            User user = dao.getUser("deadbeef");
            fail("found user deadbeef");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testGetUserByEmail() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);

        User user = dao.getUserByEmail(u1.getEmail());
        assertNotNull("User " + u1.getEmail() + " null", user);

        JcrTestHelper.removeDummyUser(session, u1);

        session.logout();
    }

    /**
     */
    public void testGetUserByEmailNotFound() throws Exception {
        try {
            User user = dao.getUserByEmail("deadbeef");
            fail("found user deadbeef");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testCreateUser() throws Exception {
        Session session = acquireSession();

        User u1 = TestHelper.makeDummyUser();

        dao.createUser(u1);
        User user = JcrTestHelper.findDummyUser(session, u1.getUsername());
        assertNotNull("User not stored", user);

        JcrTestHelper.removeDummyUser(session, u1);

        session.logout();
    }

    /**
     */
    public void testCreateUserDuplicateUsername() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);

        try {
            dao.createUser(u1);
            fail("User with duplicate username created");
        } catch (DuplicateUsernameException e) {
            // expected
        }

        JcrTestHelper.removeDummyUser(session, u1);

        session.logout();
    }

    /**
     */
    public void testCreateUserDuplicateEmail() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);
        // ensure that username is different but email remains the same
        String oldUsername = u1.getUsername();
        u1.setUsername("deadbeef");

        try {
            dao.createUser(u1);
            fail("User with duplicate email created");
        } catch (DuplicateEmailException e) {
            // expected
        }

        // restore username
        u1.setUsername(oldUsername);
        JcrTestHelper.removeDummyUser(session, u1);

        session.logout();
    }

    /**
     */
    public void testUpdateUser() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);

        // change password
        String oldPassword = u1.getPassword();
        u1.setPassword("changedpwd");

        dao.updateUser(u1);
        User user = JcrTestHelper.findDummyUser(session, u1.getUsername());
        assertFalse("Original and stored password are the same",
                    user.getPassword().equals(oldPassword));

        // leave password
        dao.updateUser(u1);
        User user2 =
            JcrTestHelper.findDummyUser(session, u1.getUsername());
        assertTrue("Original and stored password are different",
                   user2.getPassword().equals(u1.getPassword()));

        JcrTestHelper.removeDummyUser(session, u1);

        session.logout();
    }

    /**
     */
    public void testUpdateUserNotFound() throws Exception {
        try {
            User u1 = TestHelper.makeDummyUser();
            dao.updateUser(u1);
            fail("found user " + u1.getUsername());
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testUpdateUserDuplicateUsername() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);
        User u2 = JcrTestHelper.makeAndStoreDummyUser(session);

        // change u1's username to that of u2 and then try to save
        String oldUsername = u1.getUsername();
        u1.setUsername(u2.getUsername());

        try {
            dao.updateUser(u1);
            fail("User with duplicate username updated");
        } catch (DuplicateUsernameException e) {
            // expected
        }

        u1.setUsername(oldUsername);
        JcrTestHelper.removeDummyUser(session, u1);
        JcrTestHelper.removeDummyUser(session, u2);

        session.logout();
    }

    /**
     */
    public void testUpdateUserDuplicateEmail() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);
        User u2 = JcrTestHelper.makeAndStoreDummyUser(session);

        // change u1's email to that of u2 and then try to save
        u1.setEmail(u2.getEmail());

        try {
            dao.updateUser(u1);
            fail("User with duplicate email updated");
        } catch (DuplicateEmailException e) {
            // expected
        }

        JcrTestHelper.removeDummyUser(session, u1);
        JcrTestHelper.removeDummyUser(session, u2);

        session.logout();
    }

    /**
     */
    public void testRemoveUser() throws Exception {
        Session session = acquireSession();

        User u1 = JcrTestHelper.makeAndStoreDummyUser(session);

        dao.removeUser(u1.getUsername());

        User user = JcrTestHelper.findDummyUser(session, u1.getUsername());
        assertNull("User not removed", user);

        session.logout();
    }
}
