package org.osaf.cosmo.dao;

import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;

import java.util.List;

/**
 * DAO interface for Roles.
 *
 * @author Brian Moseley
 */
public interface RoleDAO extends DAO {

    /**
     */
    public List getRoles();

    /**
     */
    public Role getRole(Long id);

    /**
     */
    public Role getRole(String rolename);

    /**
     */
    public void saveRole(Role role);

    /**
     */
    public void updateRole(Role role);

    /**
     */
    public void removeRole(Long id);

    /**
     */
    public void removeRole(Role role);
}
