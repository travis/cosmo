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
package org.osaf.cosmo;

import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.util.Iterator;
import java.security.Principal;
import javax.security.auth.Subject;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A test implementation of {@link CosmoSecurityContext} that provides
 * dummy instances for use with unit tests.
 */
public class TestSecurityContext implements CosmoSecurityContext {
    private static final Log log =
        LogFactory.getLog(TestSecurityContext.class);

    private boolean anonymous;
    private Principal principal;
    private boolean rootRole;
    private Subject subject;
    private User user;

    /**
     */
    public TestSecurityContext(Principal principal) {
        this.anonymous = false;
        this.principal = principal;
        this.rootRole = false;

        this.subject = new Subject();
        this.subject.getPrincipals().add(principal);

        processPrincipal();
    }

    /**
     */
    public TestSecurityContext(Principal principal,
                               Subject subject) {
        this.anonymous = false;
        this.principal = principal;
        this.rootRole = false;
        this.subject = subject;

        processPrincipal();
    }

    /* ----- CosmoSecurityContext methods ----- */

    /**
     * Returns a name describing the principal for this security
     * context (either the name of the Cosmo user or "anonymous").
     */
    public String getName() {
        if (isAnonymous()) {
            return "anonymous";
        }
        return user.getUsername();
    }

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
    protected Principal getPrincipal() {
        return principal;
    }

    private void processPrincipal() {
        if (principal instanceof TestAnonymousPrincipal) {
            anonymous = true;
        }
        else if (principal instanceof TestUserPrincipal) {
            user = ((TestUserPrincipal) principal).getUser();
            
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
