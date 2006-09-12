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
package org.osaf.cosmo.service;

import java.util.Set;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Interface for services that manage user accounts.
 */
public interface UserService extends Service {


    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    public Set getUsers();


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
     * Returns the all user accounts meeting the supplied
     * <code>PageCriteria</code>'s requirements
     * 
     * @param pageCriteria the Pagination Criteria for the PagedList
     * 
     * @throws IllegalArgumentException if an invalid pageNumber is supplied in the <code>PageCriteria</code>
     */
    public PagedList getUsers(PageCriteria pageCriteria);

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
     * Creates a user account in the repository. Digests the raw
     * password and uses the result to replace the raw
     * password. Returns a new instance of <code>User</code>
     * after saving the original one.
     *
     * @param user the account to create
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User createUser(User user);

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
    public User updateUser(User user);

    /**
     * Removes a user account from the repository.
     *
     * @param user the account to remove
     */
    public void removeUser(User user);

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(String username);

    /**
     * Generates a random password in a format suitable for
     * presentation as an authentication credential.
     */
    public String generatePassword();

    /**
     * Returns the named preference for a given user.
     * @param username the user whose preferences you are fetching
     * @param preferenceName the name of the preference to retrieve
     */
    public String getPreference(String username, String preferenceName);

    /**
     * Removes a user's preference
     * @param username the user whose preferences you are removing
     * @param preferenceName the name of the preference to obliterate
     */
    public void removePreference(String username, String preferenceName);

    /**
     * Removes a user's preference
     * @param username the user whose preferences you are setting
     * @param preferenceName the name of the preference to set
     */
    public void setPreference(String username, String preferenceName);
}
