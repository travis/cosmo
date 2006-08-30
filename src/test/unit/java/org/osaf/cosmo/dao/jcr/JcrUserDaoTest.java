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
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;

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
        dao.setSessionFactory(getSessionFactory());
        dao.setJcrXpathQueryBuilder(new JcrXpathQueryBuilder());
        try {
            dao.init();
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    /**
     */
    protected void tearDown() throws Exception {
        try {
            dao.destroy();
        } finally {
            dao = null;
            super.tearDown();
        }
    }

    /**
     */
    public void testGetUsers() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        User u2 = getTestHelper().makeAndStoreDummyUser();
        User u3 = getTestHelper().makeAndStoreDummyUser();

        Set users = dao.getUsers();

        // account for root user
        assertTrue("Not 4 users", users.size() == 4);
        assertTrue("User 1 not found in users", users.contains(u1));
        assertTrue("User 2 not found in users", users.contains(u2));
        assertTrue("User 3 not found in users", users.contains(u3));

        getTestHelper().removeDummyUser(u1);
        getTestHelper().removeDummyUser(u2);
        getTestHelper().removeDummyUser(u3);
    }
    
    /**
     */
    public void testPaginatedAscendingQueryUsers() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        User u2 = getTestHelper().makeAndStoreDummyUser();
        User u3 = getTestHelper().makeAndStoreDummyUser();
        User u4 = getTestHelper().makeAndStoreDummyUser();

        PageCriteria pageCriteria = new PageCriteria();

        pageCriteria.setPageNumber(1);
        pageCriteria.setPageSize(2);

        pageCriteria.setSortAscending(true);

        String nameSort = User.NAME_SORT_STRING;

        pageCriteria.setSortTypeString(nameSort);

        PagedList pagedList = dao.getUsers(pageCriteria);
        List<User> users = pagedList.getList();

        // account for root user
        assertTrue("PageList Size not 5", pagedList.getTotal() == 5);
        assertTrue("Not 2 users", users.size() == 2);
        assertTrue("User 1 not found in users", users.contains(u1));
        assertFalse("User 2 found in users", users.contains(u2));

        pageCriteria.setPageNumber(2);

        pagedList = dao.getUsers(pageCriteria);
        users = pagedList.getList();

        assertTrue("PageList Size not 5", pagedList.getTotal() == 5);
        assertTrue("Not 2 users", users.size() == 2);
        assertTrue("User 2 not found in users", users.contains(u2));
        assertTrue("User 3 not found in users", users.contains(u3));
        assertFalse("User 4 found in users", users.contains(u4));
        
        pageCriteria.setPageNumber(1);
        pageCriteria.setPageSize(PageCriteria.VIEW_ALL);

        pagedList = dao.getUsers(pageCriteria);
        users = pagedList.getList();
        assertTrue("PageList Size not 5", pagedList.getTotal() == 5);
        assertTrue("Not 5 users", users.size() == 5);

        getTestHelper().removeDummyUser(u1);
        getTestHelper().removeDummyUser(u2);
        getTestHelper().removeDummyUser(u3);
        getTestHelper().removeDummyUser(u4);
    }

    /**
     */
    public void testPaginatedDescendingQueryUsers() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        User u2 = getTestHelper().makeAndStoreDummyUser();
        User u3 = getTestHelper().makeAndStoreDummyUser();
        User u4 = getTestHelper().makeAndStoreDummyUser();

        PageCriteria pageCriteria = new PageCriteria();

        String nameSort = User.NAME_SORT_STRING;

        pageCriteria.setSortTypeString(nameSort);
        pageCriteria.setSortAscending(false);
        pageCriteria.setPageNumber(1);
        pageCriteria.setPageSize(2);

        PagedList pagedList = dao.getUsers(pageCriteria);
        List<User> users = pagedList.getList();
        
        // account for root user
        assertTrue("PageList Size not 5", pagedList.getTotal() == 5);
        assertTrue("Not 2 users", users.size() == 2);
        assertTrue("User 2 not found in users", users.contains(u2));
        assertTrue("User 1 not found in users", users.contains(u1));
        assertFalse("User 3 found in users", users.contains(u3));

        pageCriteria.setPageNumber(2);

        pagedList = dao.getUsers(pageCriteria);
        users = pagedList.getList();

        assertTrue("PageList Size not 5", pagedList.getTotal() == 5);
        assertTrue("Not 2 users", users.size() == 2);
        assertTrue("User 3 not found in users", users.contains(u3));
        assertTrue("User 4 not found in users", users.contains(u4));

        pageCriteria.setPageNumber(3);

        pagedList = dao.getUsers(pageCriteria);
        users = pagedList.getList();

        // Should have only root in is
        assertTrue("PageList Size not 5", pagedList.getTotal() == 5);
        assertTrue("Not 1 user", users.size() == 1);
        assertEquals("Root not found in users", users.get(0).getUsername(),
                "root");

        getTestHelper().removeDummyUser(u1);
        getTestHelper().removeDummyUser(u2);
        getTestHelper().removeDummyUser(u3);
        getTestHelper().removeDummyUser(u4);
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
        User u1 = getTestHelper().makeAndStoreDummyUser();

        User user = dao.getUser(u1.getUsername());
        assertNotNull("User " + u1.getUsername() + " null", user);
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
        User u1 = getTestHelper().makeAndStoreDummyUser();

        User user = dao.getUserByEmail(u1.getEmail());
        assertNotNull("User " + u1.getEmail() + " null", user);

        getTestHelper().removeDummyUser(u1);
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
        User u1 = getTestHelper().makeDummyUser();

        dao.createUser(u1);
        User user = getTestHelper().findDummyUser(u1.getUsername());
        assertNotNull("User not stored", user);
    }

    /**
     */
    public void testCreateUserDuplicateUsername() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();

        try {
            dao.createUser(u1);
            fail("User with duplicate username created");
        } catch (DuplicateUsernameException e) {
            // expected
        }
    }

    /**
     */
    public void testCreateUserDuplicateEmail() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();

        // ensure that username is different but email remains the same
        u1.setUsername("deadbeef");

        try {
            dao.createUser(u1);
            fail("User with duplicate email created");
        } catch (DuplicateEmailException e) {
            // expected
        } finally {
            getTestHelper().removeDummyUser(u1);
        }
    }

    /**
     */
    public void testUpdateUser() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();

        // change password
        String oldPassword = u1.getPassword();
        u1.setPassword("changedpwd");

        dao.updateUser(u1);
        User user = getTestHelper().findDummyUser(u1.getUsername());
        assertNotNull("Updated user is null", user);
        assertFalse("Original and stored password are the same",
                    user.getPassword().equals(oldPassword));

        // leave password
        dao.updateUser(u1);
        User user2 =
            getTestHelper().findDummyUser(u1.getUsername());
        assertTrue("Original and stored password are different",
                   user2.getPassword().equals(u1.getPassword()));

        getTestHelper().removeDummyUser(u1);
    }

    /**
     */
    public void testUpdateUserNotFound() throws Exception {
        try {
            User u1 = getTestHelper().makeDummyUser();
            dao.updateUser(u1);
            fail("found user " + u1.getUsername());
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testUpdateUserDuplicateUsername() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        User u2 = getTestHelper().makeAndStoreDummyUser();

        // change u1's username to that of u2 and then try to save
        u1.setUsername(u2.getUsername());

        try {
            dao.updateUser(u1);
            fail("User with duplicate username updated");
        } catch (DuplicateUsernameException e) {
            // expected
        } finally {
            getTestHelper().removeDummyUser(u1);
            getTestHelper().removeDummyUser(u2);
        }
    }

    /**
     */
    public void testUpdateUserDuplicateEmail() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        User u2 = getTestHelper().makeAndStoreDummyUser();

        // change u1's email to that of u2 and then try to save
        u1.setEmail(u2.getEmail());

        try {
            dao.updateUser(u1);
            fail("User with duplicate email updated");
        } catch (DuplicateEmailException e) {
            // expected
        } finally {
            getTestHelper().removeDummyUser(u1);
            getTestHelper().removeDummyUser(u2);
        }
    }

    /**
     */
    public void testRemoveUser() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();

        dao.removeUser(u1.getUsername());

        User user = getTestHelper().findDummyUser(u1.getUsername());
        assertNull("User not removed", user);
    }
}
