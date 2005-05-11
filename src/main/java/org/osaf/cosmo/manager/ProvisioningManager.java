package org.osaf.cosmo.manager;

import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;

import java.util.List;

/**
 * Manager interface for the provisioning subsystem.
 *
 * @author Brian Moseley
 */
public interface ProvisioningManager extends Manager {

    /**
     */
    public List getRoles();

    /**
     */
    public Role getRole(String id);

    /**
     */
    public Role getRoleByName(String role);

    /**
     */
    public List getUsers();

    /**
     */
    public User getUser(String id);

    /**
     */
    public User getUserByUsername(String username);

    /**
     */
    public User saveUser(User user);

    /**
     */
    public User updateUser(User user);

    /**
     */
    public void removeUser(String id);
}
