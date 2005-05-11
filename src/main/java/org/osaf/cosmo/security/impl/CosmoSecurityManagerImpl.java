package org.osaf.cosmo.security.impl;

import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.CosmoUserDetails;

import java.security.Principal;
import java.util.Iterator;

import javax.security.auth.Subject;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default implementation of the {@link CosmoSecurityManager}
 * interface that provides a {@link CosmoSecurityContext} from
 * security information contained in JAAS or Acegi Security.
 */
public class CosmoSecurityManagerImpl implements CosmoSecurityManager {
    private static final Log log =
        LogFactory.getLog(CosmoSecurityManagerImpl.class);

    private UserDAO userDAO;

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> initialized from
     * within the Cosmo security environment (e.g. Acegi Security).
     */
    public CosmoSecurityContext getSecurityContext()
        throws CosmoSecurityException {
        if (ContextHolder.getContext() == null) {
            throw new CosmoSecurityException("no Acegi Security context found");
        }
        if (! (ContextHolder.getContext() instanceof SecureContext)) {
            throw new CosmoSecurityException("Acegi Security context not " +
                                            "instance of SecureContext");
        }

        SecureContext context = (SecureContext) ContextHolder.getContext();
        Authentication authen = context.getAuthentication();
        if (authen == null) {
            throw new CosmoSecurityException("no Authentication found in " +
                                            "SecureContext");
        }

        return createSecurityContext(authen);
    }

    /**
     * Provide a <code>CosmoSecurityContext</code> initialized from a
     * JAAS environment.
     */
    public CosmoSecurityContext getSecurityContext(Subject subject)
        throws CosmoSecurityException {
        for (Iterator i=subject.getPrincipals().iterator(); i.hasNext();) {
            Principal principal = (Principal) i.next();
            if (principal instanceof Authentication) {
                return createSecurityContext((Authentication) principal,
                                             subject);
            }
        }
        throw new CosmoSecurityException("no Authentication principal " +
                                         "found for subject");
    }

    /**
     * Returns the <code>CosmoUserDetails</code> for the identified
     * Cosmo user.
     */
    public CosmoUserDetails loadUser(String username) {
        return createUserDetails(userDAO.getUser(username));
    }

    /* ----- our methods ----- */

    /**
     */
    protected CosmoSecurityContext
        createSecurityContext(Authentication authen) {
        return new CosmoSecurityContextImpl(authen);
    }

    /**
     */
    protected CosmoSecurityContext
        createSecurityContext(Authentication authen, Subject subject) {
        return new CosmoSecurityContextImpl(authen, subject);
    }

    /**
     */
    protected CosmoUserDetails createUserDetails(User user) {
        return new CosmoUserDetailsImpl(user);
    }

    /**
     */
    public UserDAO getUserDAO() {
        return userDAO;
    }

    /**
     */
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}
