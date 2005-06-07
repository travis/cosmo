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

import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.CosmoUserDetails;

import java.util.Iterator;
import javax.security.auth.Subject;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import net.sf.acegisecurity.runas.RunAsUserToken;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default implementation of {@link CosmoSecurityContext}. Wraps
 * an instance of Acegi Security's
 * {@link net.sf.acegisecurity.Authentication}.
 */
public class CosmoSecurityContextImpl implements CosmoSecurityContext {
    private static final Log log =
        LogFactory.getLog(CosmoSecurityContextImpl.class);

    private boolean anonymous;
    private Authentication authentication;
    private boolean rootRole;
    private Subject subject;
    private User user;

    /**
     */
    public CosmoSecurityContextImpl(Authentication authentication) {
        this.anonymous = false;
        this.authentication = authentication;
        this.rootRole = false;

        this.subject = new Subject();
        this.subject.getPrincipals().add(authentication);
        this.subject.getPrivateCredentials().
            add(authentication.getCredentials());

        processAuthentication();
    }

    /**
     */
    public CosmoSecurityContextImpl(Authentication authentication,
                                    Subject subject) {
        this.anonymous = false;
        this.authentication = authentication;
        this.rootRole = false;
        this.subject = subject;

        processAuthentication();
    }

    /* ----- CosmoSecurityContext methods ----- */

    /**
     * Determines whether or not the context represents a Cosmo user
     * account or an anonymous user.
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Returns an instance of {@link User} describing the user
     * represented by the security context.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns an instance of {@link javax.security.auth.Subject}
     * describing the user represented by the security context.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Determines whether or not the security context represents a
     * user in the root role.
     */
    public boolean inRootRole() {
        return rootRole;
    }

    /* ----- our methods ----- */

    /**
     */
    public String toString() {
        return ToStringBuilder.
            reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     */
    protected Authentication getAuthentication() {
        return authentication;
    }

    private void processAuthentication() {
        //anonymous principals do not have CosmoUserDetails and by
        //definition are not running as other principals
        if (authentication instanceof AnonymousAuthenticationToken) {
            anonymous = true;
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CosmoUserDetails) {
            user = ((CosmoUserDetails) principal).getUser();

            // determine if the user is in the root role
            for (Iterator i=user.getRoles().iterator(); i.hasNext();) {
                Role role = (Role) i.next();
                if (role.getName().equals(CosmoSecurityManager.ROLE_ROOT)) {
                    rootRole = true;
                    break;
                }
            }
        }

    }
}
