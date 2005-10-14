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
package org.osaf.cosmo.dao;

import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * DAO Test Case for Users.
 *
 * @author Brian Moseley
 */
public class UserDAOTest extends BaseCoreTestCase {
    private static final Log log = LogFactory.getLog(UserDAOTest.class);

    private UserDAO dao;

    public void testCRUDUser() throws Exception {
        User user = TestHelper.makeDummyUser();
        dao.saveUser(user);
        assertNotNull(user.getDateCreated());
        assertNotNull(user.getDateModified());

        // get by username
        User user2 = dao.getUser(user.getUsername());
        assertTrue(user2.equals(user));
        assertEquals(user2.hashCode(), user.hashCode());

        // get by email
        User user3 = dao.getUserByEmail(user.getEmail());
        assertTrue(user3.equals(user));
        assertEquals(user3.hashCode(), user.hashCode());

        // change password
        user2.setPassword("changed password");
        dao.updateUser(user2);
        assertTrue(user2.hashCode() != user.hashCode());

        //make sure the password was changed
        User user4 = dao.getUser(user.getUsername());
        assertEquals(user4.getPassword(), user2.getPassword());
        assertTrue(! user4.getPassword().equals(user.getPassword()));

        dao.removeUser(user.getUsername());
        try {
            dao.getUser(user.getUsername());
            fail("user not removed");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    public void testCreateDuplicateUsername() throws Exception {
        // put in a user
        User user1 = TestHelper.makeDummyUser();
        dao.saveUser(user1);

        // try to create a new user with the same username
        User user2 = TestHelper.makeDummyUser();
        user2.setUsername(user1.getUsername());

        try {
            dao.saveUser(user2);
            fail("duplicate username accepted");
        } catch (DuplicateUsernameException e) {
            // expected
        }
    }

    public void testCreateDuplicateEmail() throws Exception {
        // put in a user
        User user1 = TestHelper.makeDummyUser();
        dao.saveUser(user1);

        // try to create a new user with the same email
        User user2 = TestHelper.makeDummyUser();
        user2.setEmail(user1.getEmail());

        try {
            dao.saveUser(user2);
            fail("duplicate email accepted");
        } catch (DuplicateEmailException e) {
            // expected
        }
    }

    public void testUpdateDuplicateUsername() throws Exception {
        // put in a user
        User user1 = TestHelper.makeDummyUser();
        dao.saveUser(user1);

        // put in another user
        User user2 = TestHelper.makeDummyUser();
        dao.saveUser(user2);

        // try to update user2 with user1's username
        user2.setUsername(user1.getUsername());

        try {
            dao.updateUser(user2);
            fail("duplicate username accepted");
        } catch (DuplicateUsernameException e) {
            // expected
        }
    }

    public void testUpdateDuplicateEmail() throws Exception {
        // put in a user
        User user1 = TestHelper.makeDummyUser();
        dao.saveUser(user1);

        // put in another user
        User user2 = TestHelper.makeDummyUser();
        dao.saveUser(user2);

        // try to update user2 with user1's email
        user2.setEmail(user1.getEmail());

        try {
            dao.updateUser(user2);
            fail("duplicate email accepted");
        } catch (DuplicateEmailException e) {
            // expected
        }
    }

    public void testGetUsers() throws Exception {
        Set users = dao.getUsers();
    }

    public void setUserDAO(UserDAO userDao) {
        dao = userDao;
    }
}
