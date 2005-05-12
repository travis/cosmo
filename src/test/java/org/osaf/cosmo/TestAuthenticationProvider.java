/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo;

import org.osaf.commons.spring.jcr.JCRAuthenticationProvider;

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
