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
package org.osaf.cosmo.dao.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.User;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Mock implementation of {@link UserDao} useful for testing.
 */
public class MockUserDao implements UserDao {

    private HashMap usernameIdx;
    private HashMap emailIdx;

    /**
     */
    public MockUserDao() {
        usernameIdx = new HashMap();
        emailIdx = new HashMap();

        // add overlord user
        User overlord = new User();
        overlord.setUsername(User.USERNAME_OVERLORD);
        overlord.setFirstName("Cosmo");
        overlord.setLastName("Administrator");
        overlord.setPassword("32a8bd4d676f4fef0920c7da8db2bad7");
        overlord.setEmail("root@localhost");
        overlord.setAdmin(true);
        overlord.setDateCreated(new Date());
        overlord.setDateModified(new Date());
        createUser(overlord);
    }

    // UserDao methods

    /**
     */
    public Set getUsers() {
        Set tmp = new HashSet();
        for (Iterator i=usernameIdx.values().iterator(); i.hasNext();) {
            tmp.add(i.next());
        }
        return tmp;
    }

    /**
     */
    public User getUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null username");
        }
        if (usernameIdx.containsKey(username)) {
            return (User) usernameIdx.get(username);
        }
        throw new DataRetrievalFailureException(username + " not found");
    }

    /**
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("null email");
        }
        if (emailIdx.containsKey(email)) {
            return (User) emailIdx.get(email);
        }
        throw new DataRetrievalFailureException(email + " not found");
    }

    /**
     */
    public void createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }
        if (usernameIdx.containsKey(user.getUsername())) {
            throw new DataIntegrityViolationException("username in use");
        }
        if (emailIdx.containsKey(user.getEmail())) {
            throw new DataIntegrityViolationException("email in use");
        }
        usernameIdx.put(user.getUsername(), user);
        emailIdx.put(user.getEmail(), user);
    }

    /**
     */
    public void updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }
        String key = user.isUsernameChanged() ?
            user.getOldUsername() :
            user.getUsername();
        if (! usernameIdx.containsKey(key)) {
            throw new DataRetrievalFailureException("not found");
        }
        if (user.isUsernameChanged() &&
            usernameIdx.containsKey(user.getUsername())) {
            throw new DataIntegrityViolationException("username in use");
        }
        if (user.isEmailChanged() && emailIdx.containsKey(user.getEmail())) {
            throw new DataIntegrityViolationException("email in use");
        }
        usernameIdx.put(user.getUsername(), user);
        if (user.isUsernameChanged()) {
            usernameIdx.remove(user.getOldUsername());
        }
        emailIdx.put(user.getEmail(), user);
        if (user.isEmailChanged()) {
            emailIdx.remove(user.getOldEmail());
        }
    }

    /**
     */
    public void removeUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null username");
        }
        if (usernameIdx.containsKey(username)) {
            User user = (User) usernameIdx.get(username);
            usernameIdx.remove(username);
            emailIdx.remove(user.getEmail());
        }
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }
}
