package org.osaf.cosmo.service.account;

import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.User;

public abstract class AbstractCosmoAccountActivator implements
        AccountActivator {

    private UserDao userDao;

    public void setUserDao(UserDao userDao){
        this.userDao = userDao;
    }

    /**
     * Given an activation token, look up and return a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    public User getUserFromToken(String activationToken){
        return this.userDao.getUserByActivationId(activationToken);
    }
}
