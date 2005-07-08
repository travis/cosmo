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
package org.osaf.cosmo.spring.jcr;

import org.osaf.commons.spring.jcr.JCRAuthenticationProvider;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of the {@link JCRAuthenticationProvider}
 * interface that provides simple username/password credentials.
 *
 * Username is required to be set. If password is not set, the empty
 * string is used.
 */
public class UsernamePasswordAuthenticationProvider
    implements JCRAuthenticationProvider, InitializingBean {
    private static final Log log =
        LogFactory.getLog(UsernamePasswordAuthenticationProvider.class);

    private String username;
    private String password;

    // JCRAuthenticationProvider methods

    /**
     * Returns the provider's username and password packaged in an
     * instance of {@link javax.jcr.SimpleCredentials}.
     */
    public Credentials provideCredentials() {
        return new SimpleCredentials(username, password.toCharArray());
    }

    /**
     * Always returns <code>null</code>.
     */
    public Subject provideSubject() {
        return null;
    }

    // InitializingBean methods

    /**
     * Sanity check the object's properties.
     */
    public void afterPropertiesSet() {
        if (username == null) {
            throw new IllegalArgumentException("username is required");
        }
        if (password == null) {
            password = "";
        }
    }

    // our methods

    /**
     */
    public String getUsername() {
        return username;
    }

    /**
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     */
    public String getPassword() {
        return password;
    }

    /**
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
