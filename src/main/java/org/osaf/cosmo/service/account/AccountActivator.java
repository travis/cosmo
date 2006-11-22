package org.osaf.cosmo.service.account;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.Service;

public interface AccountActivator extends Service {

    /**
     * Return a new activation token.
     *
     * @return a new activation token
     */
    public String generateActivationToken();

    /**
     * Performs whatever action this service provides for account activation.
     *
     * @param user
     */
    public void sendActivationMessage(User user);

    /**
     * Given an activation token, look up and return a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    public User getUserFromToken(String activationToken);


}
