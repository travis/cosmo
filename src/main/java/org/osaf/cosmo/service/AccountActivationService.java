package org.osaf.cosmo.service;

import org.osaf.cosmo.model.User;

public interface AccountActivationService extends Service {

    /**
     * Return a new activation token.
     *
     * @return a new activation token
     */
    public String getActivationToken();

    /**
     * Performs whatever action this service provides for account activation.
     *
     * @param user
     */
    public void initiateActivationProcess(User user);

    /**
     * Given an activation token, look up and activate a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    public void activateUserFromToken(String activationToken);

    /**
     * Given an activation token, look up and return a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    public User getUserFromToken(String activationToken);


}
