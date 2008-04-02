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
package org.osaf.cosmo.dao;

import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;

import java.util.Set;

/**
 * Interface for DAOs that manage user resources.
 *
 * A user resource stores properties about a user account and acts as
 * the root collection for an account's shared data (its "home
 * directory").
 */
public interface UserDao extends Dao {

    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    public Set<User> getUsers();

    /**
     * Returns the sorted list of user accounts corresponding to the
     * given <code>PageCriteria</code>.
     *
     * @param pageCriteria the pagination criteria
     */
    public PagedList getUsers(PageCriteria<User.SortType> pageCriteria);

    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUser(String username);
    
    
    /**
     * Returns the user account identified by the given uid.
     *
     * @param uid the uid of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUserByUid(String uid);

    /**
     * Returns the user account identified by the given activation id.
     *
     * @param hash the activation hash corresponding to the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUserByActivationId(String id);

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUserByEmail(String email);
    
    /**
     * Returns a set of users that contain a user preference that
     * matches a specific key and value.
     * @param key user preference key to match
     * @param value user preference value to match
     * @return set of users containing a user preference that matches
     *         key and value
     */
    public Set<User> findUsersByPreference(String key, String value);

    /**
     * Creates a user account in the repository. Returns a new
     * instance of <code>User</code> after saving the original one.
     *
     * @param user the account to create
     *
     * @throws DuplicateUsernameException if the username is already
     * in use
     * @throws DuplicateEmailException if the email address is already
     * in use
     */
    public User createUser(User user);

    /**
     * Updates a user account that exists in the repository. Returns a
     * new instance of <code>User</code>  after saving the original
     * one.
     *
     * @param user the account to update
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     * @throws DuplicateUsernameException if the username is already
     * in use
     * @throws DuplicateEmailException if the email address is already
     * in use
     */
    public User updateUser(User user);

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(String username);

    /**
     * Removes a user account from the repository.
     *
     * @param user the user to remove
     */
    public void removeUser(User user);
    
    /**
     * Creates a password recovery entity in the repository. Returns a new
     * instance of <code>PasswordRecovery</code> after saving the original one.
     *
     * @param passwordRecovery the password recovery entity to save
     */
    public void createPasswordRecovery(PasswordRecovery passwordRecovery);
    
    /**
     * Returns the password recovery entity identified by the given key.
     * 
     * @param key
     * @return the passsword recovery entity identified by key
     */
    public PasswordRecovery getPasswordRecovery(String key);
    
    /**
     * Delete <code>passwordRecovery</code> from the database.
     * 
     * @param passwordRecovery the password recovery entity to delete
     */
    public void deletePasswordRecovery(PasswordRecovery passwordRecovery);
}
