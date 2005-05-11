package org.osaf.cosmo.dao;

import org.osaf.cosmo.model.User;

import java.util.List;

/**
 * DAO interface for Users.
 *
 * @author Brian Moseley
 */
public interface UserDAO extends DAO {

    /**
     */
    public List getUsers();

    /**
     */
    public User getUser(Long id);

    /**
     */
    public User getUser(String username);

    /**
     */
    public void saveUser(User user);

    /**
     */
    public void updateUser(User user);

    /**
     */
    public void removeUser(Long id);

    /**
     */
    public void removeUser(User user);
}
