package org.osaf.cosmo.security;

import javax.security.auth.Subject;

/**
 * An interface that represents a server-wide security controller for
 * Cosmo. It provides a simple mechanism for external environments
 * (Jackrabbit, a WebDAV server, a web application) to access a
 * {@link CosmoSecurityContext} and to find information about Cosmo
 * users (as described by {@link CosmoUserDetails}).
 */
public interface CosmoSecurityManager {

    /**
     * The name of the root user.
     */
    public static final String USER_ROOT = "root";
    /**
     * The name of the root role.
     */
    public static final String ROLE_ROOT = "root";
    /**
     * The name of the role which has all users as its members.
     */
    public static final String ROLE_USER = "user";

    /**
     * Provide a <code>CosmoSecurityContext</code> initialized from
     * within the Cosmo security environment (e.g. Acegi Security).
     */
    public CosmoSecurityContext getSecurityContext()
        throws CosmoSecurityException;

    /**
     * Provide a <code>CosmoSecurityContext</code> initialized from a
     * JAAS environment.
     */
    public CosmoSecurityContext getSecurityContext(Subject subject)
        throws CosmoSecurityException;

    /**
     * Returns the <code>CosmoUserDetails</code> for the identified
     * Cosmo user.
     */
    public CosmoUserDetails loadUser(String username);
}
