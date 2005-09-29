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
package org.osaf.cosmo.manager.impl;

import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.manager.ProvisioningManager;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.security.MessageDigest;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.id.StringIdentifierGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Basic implementation of ProvisioningManager.
 *
 * @author Brian Moseley
 */
public class ProvisioningManagerImpl
    implements InitializingBean, ProvisioningManager {
    private static final Log log =
        LogFactory.getLog(ProvisioningManagerImpl.class);
    private UserDAO userDao;
    private MessageDigest digest;
    private StringIdentifierGenerator passwordGenerator;

    /**
     */
    public void setUserDAO(UserDAO userDao) {
        this.userDao = userDao;
    }

    /**
     */
    public void setDigest(String algorithm) {
        if (algorithm != null) {
            try {
                digest = MessageDigest.getInstance(algorithm);
            } catch (Exception e) {
                throw new RuntimeException("cannot get message digest for algorithm " + algorithm, e);
            }
        }
    }

    // ProvisioningManager methods

    /**
     */
    public Set getUsers() {
        return userDao.getUsers();
    }

    /**
     */
    public User getUser(String username) {
        return userDao.getUser(username);
    }

    /**
     */
    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    /**
     */
    public User saveUser(User user) {
        user.validateRawPassword();
        user.setPassword(digestPassword(user.getPassword()));

        userDao.saveUser(user);

        return userDao.getUser(user.getUsername());
    }

    /**
     */
    public User updateUser(User user) {
        if (user.getPassword().length() < 32) {
            user.validateRawPassword();
            user.setPassword(digestPassword(user.getPassword()));
        }

        userDao.updateUser(user);

        return userDao.getUser(user.getUsername());
    }

    /**
     */
    public void removeUser(String username) {
        userDao.removeUser(username);
    }

    /**
     */
    public String generatePassword() {
        String password = passwordGenerator.nextStringIdentifier();
        return password.length() <= 16 ? password : password.substring(0, 15);
    }

    // InitializingBean methods

    /**
     * Sanity check the object's properties.
     */
    public void afterPropertiesSet() throws Exception {
        if (userDao == null) {
            throw new IllegalArgumentException("userDAO is required");
        }
    }

    // our methods

    /**
     */
    public void setPasswordGenerator(StringIdentifierGenerator generator) {
        passwordGenerator = generator;
    }

    // private methods

    private String digestPassword(String password) {
        if (digest == null || password == null) {
            return password;
        }
        return new String(Hex.encodeHex(digest.digest(password.getBytes())));
    }
}
