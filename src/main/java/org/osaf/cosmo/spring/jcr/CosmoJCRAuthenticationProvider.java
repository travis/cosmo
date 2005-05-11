package org.osaf.cosmo.spring.jcr;

import org.osaf.commons.spring.jcr.JCRAuthenticationProvider;
import org.osaf.cosmo.security.CosmoSecurityManager;

import javax.jcr.Credentials;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the {@link JCRAuthenticationProvider}
 * interface that provides authentication information in the form of a
 * {@link javax.security.auth.Subject} retrieved from a
 * {@link org.osaf.cosmo.security.CosmoSecurityContext}.
 */
public class CosmoJCRAuthenticationProvider
    implements JCRAuthenticationProvider {
    private static final Log log =
        LogFactory.getLog(CosmoJCRAuthenticationProvider.class);

    private CosmoSecurityManager securityManager;

    /**
     * Credentials are not used by this implementation.
     *
     * @returns <code>null</code>
     * @see JCRServletAuthenticationProvider#provideCredentials()
     */
    public Credentials provideCredentials() {
        return null;
    }

    /**
     * Obtains a <code>CosmoSecurityContext</code> from the
     * <code>CosmoSecurityManager</code> and returns its
     * <code>Subject</code> representation.
     *
     * @returns the {@link Subject} representing the current
     * <code>CosmoSecurityContext</code>
     * @see JCRServletAuthenticationProvider#provideSubject()
     */
    public Subject provideSubject() {
        return securityManager.getSecurityContext().getSubject();
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
