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
package org.osaf.cosmo.dao.jcr.mock;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;

import org.osaf.commons.spring.jcr.JCRAuthenticationProvider;

/**
 * Mock implementation of
 * {@link org.osaf.commons.spring.jcr.JCRAuthenticationProvider} that
 * accepts a username and password to provide credentials.
 */
public class MockAuthenticationProvider
    implements JCRAuthenticationProvider {

    private String username;
    private String password;

    /**
     */
    public Credentials provideCredentials() {
        return new SimpleCredentials(username, password.toCharArray());
    }

    /**
     */
    public Subject provideSubject() {
        return null;
    }

    // our methods

    /**
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
