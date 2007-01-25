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
package org.osaf.cosmo.security.impl;

import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.Permission;
import org.osaf.cosmo.security.PermissionDeniedException;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default implementation of the {@link CosmoSecurityManager}
 * interface that provides a {@link CosmoSecurityContext} from
 * security information contained in JAAS or Acegi Security.
 */
public class CosmoSecurityManagerImpl implements CosmoSecurityManager {
    private static final Log log =
        LogFactory.getLog(CosmoSecurityManagerImpl.class);

    private AuthenticationManager authenticationManager;

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a
     * Cosmo user previously authenticated by the Cosmo security
     * system.
     */
    public CosmoSecurityContext getSecurityContext()
        throws CosmoSecurityException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authen = context.getAuthentication();
        if (authen == null) {
            throw new CosmoSecurityException("no Authentication found in " +
                                             "SecurityContext");
        }

        return createSecurityContext(authen);
    }

    /**
     * Authenticate the given Cosmo credentials and register a
     * <code>CosmoSecurityContext</code> for them. This method is used
     * when Cosmo components need to programatically log in a user
     * rather than relying on a security context already being in
     * place.
     */
    public CosmoSecurityContext initiateSecurityContext(String username,
                                                        String password)
        throws CosmoSecurityException {
        try {
            UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication =
                authenticationManager.authenticate(credentials);
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authentication);
            return createSecurityContext(authentication);
        } catch (AuthenticationException e) {
            throw new CosmoSecurityException("can't establish security context",
                                             e);
        }
    }

    /**
     * Validates that the current security context has the requested
     * permission for the given item.
     *
     * @throws PermissionDeniedException if the security context does
     * not have the required permission
     */
    public void checkPermission(Item item,
                                int permission)
        throws PermissionDeniedException, CosmoSecurityException {
        CosmoSecurityContext ctx = getSecurityContext();

        if (ctx.isAnonymous()) {
            log.warn("Anonymous access attempted to item " + item.getUid());
            throw new PermissionDeniedException("Anonymous principals have no permissions");
        }

        // administrators can do anything to any item
        if (ctx.isAdmin())
            return;

        User user = ctx.getUser();
        if (user != null) {
            // an item's owner can do anything to an item he owns
            if (user.equals(item.getOwner()))
                return;
            log.warn("User " + user.getUsername() + " attempted access to item " + item.getUid() + " owned by " + item.getOwner().getUsername());
            throw new PermissionDeniedException("User does not have appropriate permissions on item " + item.getUid());
        }

        Ticket ticket = ctx.getTicket();
        if (ticket != null) {
            if (! ticket.isGranted(item)) {
                log.warn("Non-granted ticket " + ticket.getKey() + " attempted access to item " + item.getUid());
                throw new PermissionDeniedException("Ticket " + ticket.getKey() + " is not granted on item " + item.getUid());
            }
            // assume that when the security context was initiated the
            // ticket's expiration date was checked
            if (permission == Permission.READ &&
                ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ))
                return;
            if (permission == Permission.WRITE &&
                ticket.getPrivileges().contains(Ticket.PRIVILEGE_WRITE))
                return;
            if (permission == Permission.FREEBUSY &&
                ticket.getPrivileges().contains(Ticket.PRIVILEGE_FREEBUSY))
                return;
            log.warn("Granted ticket " + ticket.getKey() + " attempted access to item " + item.getUid());
            throw new PermissionDeniedException("Ticket " + ticket.getKey() + " does not have appropriate permissions on item " + item.getUid());
        }
    }

    /* ----- our methods ----- */

    /**
     */
    protected CosmoSecurityContext
        createSecurityContext(Authentication authen) {
        return new CosmoSecurityContextImpl(authen);
    }

    /**
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     */
    public void
        setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
