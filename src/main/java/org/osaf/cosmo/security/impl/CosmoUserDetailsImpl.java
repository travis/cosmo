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
package org.osaf.cosmo.security.impl;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoUserDetails;

import java.util.ArrayList;
import java.util.Iterator;

import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class that decorates the Cosmo core {@link User} to provide an
 * implementation of {@link CosmoUserDetails} suitable for use by Acegi
 * Security's {@link AuthenticationProvider}.
 *
 *
 * If the associated user is an administrator, contains an authority
 * named "ROLE_ROOT".
 *
 * If the associated user is not the overlord, contains an authority
 * named "ROLE_USER".
 */
public class CosmoUserDetailsImpl implements CosmoUserDetails {
    private static final Log log =
        LogFactory.getLog(CosmoUserDetailsImpl.class);

    private User user;
    private GrantedAuthority[] authorities;

    /**
     * @param user the wrapped @{link User}
     */
    public CosmoUserDetailsImpl(User user) {
        this.user = user;

        ArrayList authorities = new ArrayList();
        if (user.isAdmin().booleanValue()) {
            authorities.add(new GrantedAuthorityImpl("ROLE_ROOT"));
        }
        if (! user.isOverlord()) {
            authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        }

        this.authorities = (GrantedAuthority[])
            authorities.toArray(new GrantedAuthority[0]);
    }

    /* ----- UserDetails methods ----- */

    /**
     * Indicates whether the user's account has expired. An expired
     * account can not be authenticated.
     *
     * Note: since account expiration has not been implemented in
     * Cosmo, this method always returns <code>true</code>.
     *
     * @returns <code>true</code> if the user's account is valid (ie
     * non-expired), <code>false</code> if no longer valid (ie
     * expired)
     */
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked
     * user can not be authenticated.
     *
     * Note: since user locking has not been implemented in
     * Cosmo, this method always returns <code>true</code>.
     *
     * @returns <code>true</code> if the user is not locked,
     * <code>false</code> otherwise
     */
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Returns the authorities granted to the user. Cannot return
     * <code>null</code>.
     *
     * @returns the authorities (never <code>null</code>)
     */
    public GrantedAuthority[] getAuthorities() {
        return authorities;
    }

    /**
     * Indicates whether the users's credentials (password) has
     * expired. Expired credentials prevent authentication.
     *
     * Note: since credential expiration has not been implemented in
     * Cosmo, this method always returns <code>true</code>.
     *
     * @returns <code>true</code> if the user's credentials are
     * valid (ie non-expired), <code>false</code> if no longer
     * valid (ie expired)
     */
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the users is enabled or disabled. A disabled
     * user cannot be authenticated.
     *
     * Note: since user disablement has not been implemented in
     * Cosmo, this method always returns <code>true</code>.
     *
     * @returns <code>true</code> if the user is enabled,
     * <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Returns the password used to authenticate the user. Cannot
     * return <code>null</code>.
     *
     * @returns the password (never <code>null</code>)
     */
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username used to authenticate the user. Cannot
     * return <code>null</code>.
     *
     * @returns the username (never <code>null</code>)
     */
    public String getUsername() {
        return user.getUsername();
    }

    /* ----- CosmoUserDetails methods ----- */

    /**
     * Returns the underlying @{link User}.
     *
     * @returns the user
     */
    public User getUser() {
        return user;
    }
}
