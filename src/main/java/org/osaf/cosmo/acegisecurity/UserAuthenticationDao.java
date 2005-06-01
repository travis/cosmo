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
package org.osaf.cosmo.acegisecurity;

import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.impl.CosmoUserDetailsImpl;

import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.AuthenticationDao;
import net.sf.acegisecurity.providers.dao.UsernameNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * A class that implements Acegi Security's
 * {@link net.acegisecurity.providers.dao.AuthenticationDao}
 * interface using the Cosmo {@link UserDAO}.
 */
public class UserAuthenticationDao implements AuthenticationDao {
    private static final Log log =
        LogFactory.getLog(UserAuthenticationDao.class);

    private UserDAO userDAO;

    /**
     * Locates the user based on the username.
     *
     * @param username the username presented to the @{link
     * DaoAuthenticationProvider}
     * @returns a fully populated @{link UserDetails} (never
     * <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be
     * found or the user has no @{link GrantedAuthority}
     * @throws DataAccessException if a system error occurred
     */
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException, DataAccessException {
        try {
            return new
                CosmoUserDetailsImpl(userDAO.getUserByUsername(username));
        } catch (DataRetrievalFailureException e) {
            throw new UsernameNotFoundException("user " + username +
                                                " not found", e);
        }
    }

    /**
     */
    public UserDAO getUserDAO() {
        return userDAO;
    }

    /**
     */
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}
