package org.osaf.cosmo.manager.impl;

import org.osaf.cosmo.dao.RoleDAO;
import org.osaf.cosmo.dao.ShareDAO;
import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.manager.ProvisioningManager;
import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.security.MessageDigest;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic implementation of ProvisioningManager.
 *
 * @author Brian Moseley
 */
public class ProvisioningManagerImpl implements ProvisioningManager {
    private static final Log log =
        LogFactory.getLog(ProvisioningManagerImpl.class);
    private RoleDAO roleDao;
    private ShareDAO shareDao;
    private UserDAO userDao;
    private MessageDigest digest;

    /**
     */
    public void setRoleDAO(RoleDAO roleDao) {
        this.roleDao = roleDao;
    }

    /**
     */
    public void setShareDAO(ShareDAO shareDao) {
        this.shareDao = shareDao;
    }

    /**
     */
    public void setUserDAO(UserDAO userDao) {
        this.userDao = userDao;
    }

    /**
     */
    public void setDigest(String algorithm) {
        if (algorithm != null) {
            try {
                digest = MessageDigest.getInstance(algorithm);
            } catch (Exception e) {
                throw new RuntimeException("cannot get message digest for algorithm " + algorithm, e);
            }
        }
    }

    /**
     */
    public List getRoles() {
        return roleDao.getRoles();
    }

    /**
     */
    public Role getRole(String id) {
        return roleDao.getRole(new Long(id));
    }

    /**
     */
    public Role getRoleByName(String name) {
        return roleDao.getRole(name);
    }

    /**
     */
    public List getUsers() {
        return userDao.getUsers();
    }

    /**
     */
    public User getUser(String id) {
        return userDao.getUser(new Long(id));
    }

    /**
     */
    public User getUserByUsername(String username) {
        return userDao.getUser(username);
    }

    /**
     */
    public User saveUser(User user) {
        user.setPassword(digestPassword(user.getPassword()));
        userDao.saveUser(user);

        if (! user.getUsername().equals(CosmoSecurityManager.USER_ROOT)) {
            shareDao.createHomedir(user.getUsername());
        }

        return userDao.getUser(user.getUsername());
    }

    /**
     */
    public User updateUser(User user) {
        if (user.getPassword().length() < 32) {
            user.setPassword(digestPassword(user.getPassword()));
        }
        userDao.updateUser(user);

        return userDao.getUser(user.getUsername());
    }

    /**
     */
    public void removeUser(String id) {
        User user = getUser(id);
        if (! user.getUsername().equals(CosmoSecurityManager.USER_ROOT)) {
            shareDao.deleteHomedir(user.getUsername());
        }
        userDao.removeUser(user);
    }

    private String digestPassword(String password) {
        if (digest == null) {
            return password;
        }
        return new String(Hex.encodeHex(digest.digest(password.getBytes())));
    }
}
