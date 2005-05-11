package org.osaf.cosmo.security;

import org.osaf.cosmo.model.User;

import net.sf.acegisecurity.UserDetails;

/**
 * An interface that extends Acegi Security's {@link UserDetails}
 * interface to provide additional Cosmo-specific information for use
 * by security components.
 *
 * For now this interface simply allows access to the Cosmo
 * {@link User}. It is anticipated that additional details will be
 * added in the future.
 */
public interface CosmoUserDetails extends UserDetails {

    /**
     * Returns the underlying Cosmo {@link User}.
     *
     * @returns the user
     */
    public User getUser();
}
