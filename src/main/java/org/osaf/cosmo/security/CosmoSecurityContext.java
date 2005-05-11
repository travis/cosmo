package org.osaf.cosmo.security;

import org.osaf.cosmo.model.User;

import javax.security.auth.Subject;

/**
 * An interface that represents a user-specific context for Cosmo
 * security operations. It provides a facade for the Acegi Security
 * system and applies Cosmo-specific security rules.
 */
public interface CosmoSecurityContext {

    /**
     * Returns an instance of {@link User} describing the user
     * represented by the security context.
     */
    public User getUser();

    /**
     * Returns an instance of {@link javax.security.auth.Subject}
     * describing the user represented by the security context.
     */
    public Subject getSubject();

    /**
     * Determines whether or not the security context represents a
     * user in the root role.
     */
    public boolean inRootRole();
}
