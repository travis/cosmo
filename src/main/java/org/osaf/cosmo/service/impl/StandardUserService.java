/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

import java.security.MessageDigest;
import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.id.StringIdentifierGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.OverlordDeletionException;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.account.AccountActivator;
import org.osaf.cosmo.service.account.ActivationContext;
import org.osaf.cosmo.util.PagedList;
import org.osaf.cosmo.util.PageCriteria;

/**
 * Standard implementation of {@link UserService}.
 */
public class StandardUserService implements UserService {
    private static final Log log = LogFactory.getLog(StandardUserService.class);

    /**
     * The service uses MD5 if no digest algorithm is explicitly set.
     */
    public static final String DEFAULT_DIGEST_ALGORITHM = "MD5";

    private MessageDigest digest;
    private String digestAlgorithm;
    private StringIdentifierGenerator passwordGenerator;
    private ContentDao contentDao;
    private UserDao userDao;

    private AccountActivator accountActivator;
    private boolean accountActivationRequired;

    // UserService methods

    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    public Set<User> getUsers() {
        if (log.isDebugEnabled())
            log.debug("getting all users");
        return userDao.getUsers();
    }

    /**
     * Returns the sorted list of user accounts corresponding to the
     * given <code>PageCriteria</code>.
     *
     * @param pageCriteria the pagination criteria
     */
    public PagedList<User, User.SortType> getUsers(PageCriteria<User.SortType> pageCriteria) {
        if (log.isDebugEnabled())
            log.debug("getting users for criteria " + pageCriteria);
        return userDao.getUsers(pageCriteria);
    }

    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUser(String username) {
        if (log.isDebugEnabled())
            log.debug("getting user " + username);
        return userDao.getUser(username);
    }

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUserByEmail(String email) {
        if (log.isDebugEnabled())
            log.debug("getting user with email address " + email);
        return userDao.getUserByEmail(email);
    }

    /**
     * Returns the user account associated with the given activation id.
     *
     * @param activationId the activation id associated with the account to return
     *
     * @return the User associated with activationId
     *
     * @throws DataRetrievalFailureException if there is no user associated with this
     * activation id.
     */
    public User getUserByActivationId(String activationId) {
        if (log.isDebugEnabled())
            log.debug("getting user associated with activation id " +
                    activationId);

        return userDao.getUserByActivationId(activationId);
    }

    /**
     * Creates a user account in the repository. Digests the raw
     * password and uses the result to replace the raw
     * password. Returns a new instance of <code>User</code>
     * after saving the original one.
     *
     * @param user the account to create
     *
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User createUser(User user) {
        if (log.isDebugEnabled())
            log.debug("creating user " + user.getUsername());

        user.validateRawPassword();

        user.setPassword(digestPassword(user.getPassword()));
        user.setDateCreated(new Date());
        user.setDateModified(user.getDateCreated());


        userDao.createUser(user);

        User newUser = userDao.getUser(user.getUsername());

        contentDao.createRootItem(newUser);


        return newUser;
    }

    public User createUser(User user, ActivationContext activationContext) {
        activationContext.setSender(
                this.getUser(User.USERNAME_OVERLORD));
        
        boolean accountActivationRequired =
            (!user.isOverlord() &&
             (this.accountActivator != null) &&
             this.isAccountActivationRequired() &&
             activationContext.isActivationRequired());

        if (accountActivationRequired){
            user.setActivationId(
                    this.accountActivator.generateActivationToken());
        }
        User newUser = createUser(user);

        if (accountActivationRequired){
            accountActivator.sendActivationMessage(newUser, activationContext);
        }


        // TODO Auto-generated method stub
        return newUser;
    }

    /**
     * Updates a user account that exists in the repository. If the
     * password has been changed, digests the raw new password and
     * uses the result to replace the stored password. Returns a new
     * instance of <code>User</code>  after saving the original one.
     *
     * @param user the account to update
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User updateUser(User user) {
        boolean isUsernameChanged = user.isUsernameChanged();
        if (log.isDebugEnabled()) {
            log.debug("updating user " + user.getOldUsername());
            if (isUsernameChanged)
                log.debug("... changing username to " + user.getUsername());
        }

        if (user.getPassword().length() < 32) {
            user.validateRawPassword();
            user.setPassword(digestPassword(user.getPassword()));
        }
        user.setDateModified(new Date());

        userDao.updateUser(user);

        User newUser = userDao.getUser(user.getUsername());

        if (isUsernameChanged) {
            if (log.isDebugEnabled())
                log.debug("renaming root item for user " +
                          newUser.getUsername());
            CollectionItem rootCollection = contentDao.getRootItem(newUser);
            rootCollection.setName(newUser.getUsername());
            contentDao.updateCollection(rootCollection);
        }

        return newUser;
    }

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(String username) {
        if (log.isDebugEnabled())
            log.debug("removing user " + username);
        // seems to automatically remove the user's root item
        userDao.removeUser(username);
    }

    /**
     * Removes a user account from the repository.
     *
     * @param user the account to remove
     */
    public void removeUser(User user) {
        if (log.isDebugEnabled())
            log.debug("removing user " + user.getUsername());
        // seems to automatically remove the user's root item
        userDao.removeUser(user);
    }

    /**
     * Removes a set of user accounts from the repository. Will not
     * remove the overlord.
     * @param users
     */
    public void removeUsers(Set<User> users) throws OverlordDeletionException{
        for (User user : users){
            if (user.isOverlord())
                throw new OverlordDeletionException();
            userDao.removeUser(user);
        }
        // Only log if all removes were successful
        if (log.isDebugEnabled()) {
            for (User user : users){
                log.debug("removing user " + user.getUsername());
            }
        }


    }

    /**
     * Removes the user accounts identified by the given usernames from
     * the repository. Will not remove overlord.
     * @param usernames
     */
    public void removeUsersByName(Set<String> usernames) throws OverlordDeletionException{
        for (String username : usernames){
            if (username.equals(User.USERNAME_OVERLORD))
                    throw new OverlordDeletionException();
            userDao.removeUser(username);
        }
        // Only log if all removes were successful
        if (log.isDebugEnabled()) {
            for (String username : usernames){
                log.debug("removing user " + username);
            }
        }
    }

    /**
     * Generates a random password in a format suitable for
     * presentation as an authentication credential.
     */
    public String generatePassword() {
        String password = passwordGenerator.nextStringIdentifier();
        return password.length() <= User.PASSWORD_LEN_MAX ?
            password :
            password.substring(0, User.PASSWORD_LEN_MAX - 1);
    }

    // Service methods

    /**
     * Initializes the service, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        if (contentDao == null) {
            throw new IllegalStateException("contentDao is required");
        }
        if (userDao == null) {
            throw new IllegalStateException("userDao is required");
        }
        if (passwordGenerator == null) {
            throw new IllegalStateException("passwordGenerator is required");
        }
        if (digestAlgorithm == null) {
            digestAlgorithm = DEFAULT_DIGEST_ALGORITHM;
        }

        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
        } catch (Exception e) {
            throw new RuntimeException("cannot get digest for algorithm " +
                                               digestAlgorithm, e);
        }
    }

    /**
     * Readies the service for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /**
     * Digests the given password using the set message digest and hex
     * encodes it.
     */
    protected String digestPassword(String password) {
        if (password == null) {
            return password;
        }
        return new String(Hex.encodeHex(digest.digest(password.getBytes())));
    }

    /**
     */
    public MessageDigest getDigest() {
        return this.digest;
    }

    /**
     */
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    /**
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     */
    public StringIdentifierGenerator getPasswordGenerator() {
        return this.passwordGenerator;
    }

    /**
     */
    public void setPasswordGenerator(StringIdentifierGenerator generator) {
        this.passwordGenerator = generator;
    }

    public void setAccountActivator(AccountActivator accountActivator){
        this.accountActivator = accountActivator;
    }

    /**
     */
    public ContentDao getContentDao() {
        return this.contentDao;
    }

    /**
     */
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    /**
     */
    public UserDao getUserDao() {
        return this.userDao;
    }

    /**
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean isAccountActivationRequired() {
        return accountActivationRequired;
    }

    public void setAccountActivationRequired(boolean accountActivationRequired) {
        this.accountActivationRequired = accountActivationRequired;
    }

}
