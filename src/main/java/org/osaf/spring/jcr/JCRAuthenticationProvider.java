package org.osaf.spring.jcr;

import javax.jcr.Credentials;
import javax.security.auth.Subject;

/**
 * <code>JCRAuthenticationProvider</code> is an interface for components
 * that can provide authentication information suitable for presenting
 * to {@link javax.jcr.Credentials#login}.
 *
 * If this component returns a non-<code>null</code> value from
 * <code>provideCredentials()</code>, it implicitly agrees to
 * allow the JCR implementation to undergo its standard authentication
 * process.
 *
 * If this component returns a non-<code>null</code> value from
 * <code>provideSubject</code>, it signals that authentication has
 * already been performed by another service and that Jthe CR
 * implementation's standard authentication process should be
 * bypassed.
 *
 * The order in which these methods are called and whether or not both
 * are called are not specified.
 */
public interface JCRAuthenticationProvider {

    /**
     * Returns authentication information packaged in an instance of
     * {@link javax.jcr.Credentials}.
     *
     * @returns the <code>Credentials</code>, or <code>null</code> if
     * the implementation can't or chooses not to provide it.
     */
    public Credentials provideCredentials();

    /**
     * Returns authentication information packaged in an instance of
     * {@link javax.security.auth.Subject}.
     *
     * @returns the <code>Subject</code>, or <code>null</code> if
     * the implementation can't or chooses not to provide it.
     */
    public Subject provideSubject();
}
