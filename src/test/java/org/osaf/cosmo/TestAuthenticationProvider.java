package org.osaf.cosmo;

import org.osaf.spring.jcr.JCRAuthenticationProvider;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import java.security.Principal;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test implementation of the {@link JCRAuthenticationProvider}
 * interface that returns simple credentials for testing.
 */
public class TestAuthenticationProvider
    implements JCRAuthenticationProvider {
    private static final Log log =
        LogFactory.getLog(TestAuthenticationProvider.class);

    /**
     * Returns <code>null</code>.
     */
    public Credentials provideCredentials() {
        return null;
    }

    /**
     * Returns a <code>Subject</code> with a single
     * {@link java.security.Principal} representing a test user.
     */
    public Subject provideSubject() {
        Subject subject = new Subject();
        subject.getPrincipals().add(new Principal() {
                public boolean equals(Object another) {
                    return getName().equals(another);
                }
                public String toString() {
                    return getName();
                }
                public int hashCode() {
                    return getName().hashCode();
                }
                public String getName() {
                    return "test";
                }
            });
        return subject;
    }
}
