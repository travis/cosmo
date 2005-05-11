package org.osaf.acegisecurity.jaas;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Cosmo implementation of
 * {@link javax.security.auth.spi.LoginModule} that uses an Acegi
 * Security
 * {@link net.sf.acegisecurity.context.security.SecureContext} to
 * provide authentication.
 */
public class SecureContextLoginModule implements LoginModule {
    private static final Log log =
        LogFactory.getLog(SecureContextLoginModule.class);

    private Authentication authen;
    private Subject subject;

    /**
     * Initialize this <code>LoginModule</code>.
     *
     * Ignores the callback handler, since the code establishing the
     * <code>LoginContext</code> likely won't provide one that
     * understands Acegi Security.
     *
     * Also ignores the <code>sharedState</code> and
     * <code>options</code> parameters, since none are recognized. 
     */
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map sharedState,
                           Map options) {
        this.subject = subject;
    }

    /**
     * Authenticate the <code>Subject</code> (phase one) by extracting
     * the Acegi Security <code>Authentication</code> from the current
     * <code>SecureContext</code>.
     */
    public boolean login()
        throws LoginException {
        if (ContextHolder.getContext() == null) {
            if (log.isDebugEnabled()) {
                log.debug("no security context found");
            }
            return false;
        }
        if (! (ContextHolder.getContext() instanceof SecureContext)) {
            if (log.isDebugEnabled()) {
                log.debug("security context not instance of SecureContext");
            }
            return false;
        }

        SecureContext context = (SecureContext) ContextHolder.getContext();
        authen = context.getAuthentication();
        if (authen == null) {
            throw new LoginException("Authentication not found in security" +
                                     " context");
        }

        return true;
    }

    /**
     * Authenticate the <code>Subject</code> (phase two) by adding the
     * Acegi Security <code>Authentication</code> to the
     * <code>Subject</code>'s principals.
     */
    public boolean commit()
        throws LoginException {
        if (authen == null) {
            return false;
        }

        subject.getPrincipals().add(authen);

        return true;
    }

    /**
     * Abort the authentication process by forgetting the Acegi
     * Security <code>Authentication</code>.
     */
    public boolean abort()
        throws LoginException {
        if (authen == null) {
            return false;
        }

        authen = null;

        return true;
    }

    /**
     * Log out the <code>Subject</code>.
     */
    public boolean logout()
        throws LoginException {
        if (authen == null) {
            return false;
        }

        subject.getPrincipals().remove(authen);
        authen = null;

        return true;
    }
}
