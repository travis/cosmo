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

    private Authentication authentication;
    private boolean rootRole;
    private Subject subject;

    /**
     */
    public CosmoSecurityContextImpl(Authentication authentication) {
        this.authentication = authentication;
        this.rootRole = false;

        this.subject = new Subject();
        this.subject.getPrincipals().add(authentication);
        this.subject.getPrivateCredentials().
            add(authentication.getCredentials());

        processRoles();
    }

    /**
     */
    public CosmoSecurityContextImpl(Authentication authentication,
                                    Subject subject) {
        this.authentication = authentication;
        this.rootRole = false;
        this.subject = subject;

        processRoles();
    }

    /* ----- CosmoSecurityContext methods ----- */

    /**
     * Returns an instance of {@link User} describing the user
     * represented by the security context.
     */
    public User getUser() {
        return ((CosmoUserDetails) authentication.getPrincipal()).getUser();
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
    protected Authentication getAuthentication() {
        return authentication;
    }

    private void processRoles() {
        for (Iterator i=getUser().getRoles().iterator(); i.hasNext();) {
            Role role = (Role) i.next();
            // determine if the user is in the root role
            if (role.getName().equals(CosmoSecurityManager.ROLE_ROOT)) {
                this.rootRole = true;
                break;
            }
        }
    }
}
