package org.osaf.cosmo.acegisecurity;

import org.osaf.cosmo.security.CosmoSecurityManager;

import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.AuthenticationDao;
import net.sf.acegisecurity.providers.dao.UsernameNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * A class that implements Acegi Security's
 * {@link net.acegisecurity.providers.dao.AuthenticationDao}
 * interface using a {@link CosmoSecurityManager}.
 */
public class UserAuthenticationDao implements AuthenticationDao {
    private static final Log log =
        LogFactory.getLog(UserAuthenticationDao.class);

    private CosmoSecurityManager securityManager;

    /**
     * Locates the user based on the username.
     *
     * @param username the username presented to the @{link
     * DaoAuthenticationProvider}
     * @returns a fully populated @{link UserDetails} (never
     * <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be
     * found or the user has no @{link GrantedAuthority}
     * @throws DataAccessException if a system error occurred
     */
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException, DataAccessException {
        try {
            return securityManager.loadUser(username);
        } catch (DataRetrievalFailureException e) {
            throw new UsernameNotFoundException("user " + username +
                                                " not found", e);
        }
    }

    /**
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }
}
