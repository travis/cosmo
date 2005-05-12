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
import org.osaf.cosmo.model.User;

import java.util.List;

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
    private static final String DAO_BEAN = "userDAO";
    private UserDAO dao = null;

    /**
     */
    public UserDAOTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = (UserDAO) getAppContext().getBean(DAO_BEAN);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dao = null;
    }

    public void testCRUDUser() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        User user = TestHelper.makeDummyUser();
        dao.saveUser(user);
        assertNotNull(user.getId());
        assertNotNull(user.getDateCreated());
        assertNotNull(user.getDateModified());

        // get by id
        User user2 = dao.getUser(user.getId());
        assertTrue(user2.equals(user));
        assertEquals(user2.hashCode(), user.hashCode());
        assertNotNull(user2.getEmail());

        // get by username
        User user3 = dao.getUser(user.getId());
        assertTrue(user3.equals(user));
        assertEquals(user3.hashCode(), user.hashCode());
        assertNotNull(user3.getEmail());

        user3.setPassword("changed password");
        dao.updateUser(user3);
        assertTrue(user3.hashCode() != user.hashCode());

        User user4 = dao.getUser(user2.getId());
        assertEquals(user4.getPassword(), user3.getPassword());
        assertTrue(! user4.getPassword().equals(user.getPassword()));

        dao.removeUser(user);
        try {
            dao.getUser(user.getId());
            fail("user not removed");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    public void testListUsers() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        List users = dao.getUsers();
    }
}
