/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.service.impl;

import java.util.Date;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibPasswordRecovery;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Test Case for {@link StandardUserService}.
 */
public class StandardUserServiceTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(StandardUserServiceTest.class);

    private StandardUserService service;
    private MockDaoStorage storage;
    private MockContentDao contentDao;
    private MockUserDao userDao;
    private TestHelper testHelper;

    /**
     */
    protected void setUp() throws Exception {
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        contentDao = new MockContentDao(storage);
        userDao = new MockUserDao(storage);
        service = new StandardUserService();
        service.setContentDao(contentDao);
        service.setUserDao(userDao);
        service.setPasswordGenerator(new SessionIdGenerator());
        service.init();
    }

    /**
     */
    public void testGetUsers() throws Exception {
        User u1 = testHelper.makeDummyUser();
        userDao.createUser(u1);
        User u2 = testHelper.makeDummyUser();
        userDao.createUser(u2);
        User u3 = testHelper.makeDummyUser();
        userDao.createUser(u3);

        Set users = service.getUsers();

        assertTrue(users.size() == 4); // account for overlord
        assertTrue("User 1 not found in users", users.contains(u1));
        assertTrue("User 2 not found in users", users.contains(u2));
        assertTrue("User 3 not found in users", users.contains(u3));
    }

    /**
     */
    public void testGetUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        String username1 = u1.getUsername();
        userDao.createUser(u1);

        User user = service.getUser(username1);
        assertNotNull("User " + username1 + " null", user);
    }

    /**
     */
    public void testGetUserByEmail() throws Exception {
        User u1 = testHelper.makeDummyUser();
        String email1 = u1.getEmail();
        userDao.createUser(u1);

        User user = service.getUserByEmail(email1);
        assertNotNull("User " + email1 + " null", user);
    }

    /**
     */
    public void testCreateUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        String password = u1.getPassword();

        User user = service.createUser(u1);
        assertNotNull("User not stored", userDao.getUser(u1.getUsername()));
        assertFalse("Original and stored password are the same",
                    user.getPassword().equals(password));
        assertEquals(user.getCreationDate(), user.getModifiedDate());
    }

    /**
     */
    public void testUpdateUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1.setPassword(service.digestPassword(u1.getPassword()));
        String digestedPassword = u1.getPassword();
        
        userDao.createUser(u1);

        // change password
        u1.setPassword("changedpwd");

        Thread.currentThread().sleep(1000); // let modified date change
        User user = service.updateUser(u1);
        try {
            userDao.getUser(user.getUsername());
        } catch (DataRetrievalFailureException e) {
            fail("User not stored");
        }
        assertFalse("Original and stored password are the same",
                    user.getPassword().equals(digestedPassword));
        assertTrue("Created and modified dates are the same",
                   ! user.getCreationDate().equals(user.getModifiedDate()));

        // leave password
        Thread.currentThread().sleep(1000); // let modified date change
        User user2 = service.updateUser(u1);
        try {
            userDao.getUser(user.getUsername());
        } catch (DataRetrievalFailureException e) {
            fail("User not stored");
        }
        assertTrue("Original and stored password are not the same",
                    user2.getPassword().equals(user.getPassword()));
        assertTrue("Created and modified dates are the same",
                   ! user2.getCreationDate().equals(user2.getModifiedDate()));
    }

    /**
     */
    public void testRemoveUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        service.createUser(u1);

        service.removeUser(u1);

        assertFalse("User not removed", userDao.getUsers().contains(u1));
    }

    /**
     */
    public void testRemoveUserByUsername() throws Exception {
        User u1 = testHelper.makeDummyUser();
        service.createUser(u1);

        service.removeUser(u1.getUsername());

        assertFalse("User not removed", userDao.getUsers().contains(u1));
    }

    /**
     */
    public void testGeneratePassword() throws Exception {
        String pwd = service.generatePassword();

        assertTrue("Password too long", pwd.length() <= User.PASSWORD_LEN_MAX);
        assertTrue("Password too short", pwd.length() >= User.PASSWORD_LEN_MIN);
    }

    /**
     */
    public void testNullUserDao() throws Exception {
        service.setUserDao(null);
        try {
            service.init();
            fail("Should not be able to initialize service without userDao");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     */
    public void testNullPasswordGenerator() throws Exception {
        service.setPasswordGenerator(null);
        try {
            service.init();
            fail("Should not be able to initialize service without passwordGenerator");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     */
    public void testDefaultDigestAlgorithm() throws Exception {
        assertEquals(service.getDigestAlgorithm(), "MD5");
    }

    /**
     */
    public void testDigestPassword() throws Exception {
        String password = "deadbeef";

        String digested = service.digestPassword(password);

        // tests MD5
        assertTrue("Digest not correct length", digested.length() == 32);

        // tests hex
        assertTrue("Digest not hex encoded", digested.matches("^[0-9a-f]+$"));
    }
    
    public void testCreatePasswordRecovery(){
        User user = testHelper.makeDummyUser();
        user = userDao.createUser(user);
        
        PasswordRecovery passwordRecovery = 
            new HibPasswordRecovery(user, "pwrecovery1");
        
        passwordRecovery = service.createPasswordRecovery(passwordRecovery);

        PasswordRecovery storedPasswordRecovery = 
            service.getPasswordRecovery(passwordRecovery.getKey());

        assertEquals(passwordRecovery, storedPasswordRecovery);
        
        service.deletePasswordRecovery(storedPasswordRecovery);
        
        storedPasswordRecovery = 
            service.getPasswordRecovery(storedPasswordRecovery.getKey());
        
        assertNull(storedPasswordRecovery);
    }
    
    public void testRecoverPassword(){
        User user = testHelper.makeDummyUser();
        
        userDao.createUser(user);

        PasswordRecovery passwordRecovery = new HibPasswordRecovery(user, "pwrecovery2");
        
        passwordRecovery = service.createPasswordRecovery(passwordRecovery);
        
        assertEquals(user, passwordRecovery.getUser());
        
        // Recover password
        
        PasswordRecovery storedPasswordRecovery = 
            service.getPasswordRecovery(passwordRecovery.getKey());
        
        User changingUser = storedPasswordRecovery.getUser();
        
        String newPassword = service.generatePassword();

        changingUser.setPassword(newPassword);
        
        changingUser = service.updateUser(changingUser);
        
        String changedPassword = changingUser.getPassword();
        
        User changedUser = service.getUser(changingUser.getUsername());
        
        assertEquals(changedUser, changingUser);
        
        assertEquals(changedPassword, changedUser.getPassword());
       
    }
}
