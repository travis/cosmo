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
package org.osaf.spring.jcr;

import java.security.PrivilegedAction;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Helper class that simplifies JCR data access code, and converts
 * checked JCR exceptions into unchecked
 * {@link org.springframework.dao.DataAccessException}s,
 * following the <code>org.springframework.dao</code> exception
 * hierarchy.
 *
 * Typically used to implement data access or business logic services
 * that use JCR within their implementation but are JCR-agnostic in
 * their interface.
 *
 * Requires a {@link JCRAuthenticationProvider} and a
 * {@link javax.jcr.Repository}. A workspace name is optional, as the
 * repository will choose the default workspace if a name is not
 * provided.
 *
 * @author Brian Moseley
 */
public class JCRTemplate implements InitializingBean {
    private static final Log log = LogFactory.getLog(JCRTemplate.class);

    private JCRAuthenticationProvider authenticationProvider;
    private Repository repository;
    private String workspaceName;

    /**
     */
    public JCRTemplate() {
    }

    /**
     */
    public JCRTemplate(JCRAuthenticationProvider authenticationProvider,
                       Repository repository,
                       String workspaceName) {
        setAuthenticationProvider(authenticationProvider);
        setRepository(repository);
        setWorkspaceName(workspaceName);
        afterPropertiesSet();
    }

    /**
     * Execute the action specified by the given action object within
     * a {@link javax.jcr.Session}. Application exceptions thrown by
     * the action object get propagated to the caller (can only be
     * unchecked). JCR exceptions are transformed into appropriate DAO
     * ones. Allows for returning a result object, i.e. a domain
     * object or a collection of domain objects.
     *
     * Note: Callback code does not need to explicitly log out of the
     * <code>Session</code>; this method will handle that itself.
     *
     * @param callback the <code>JCRCallback</code> that executes
     * the client operation
     * @param workspaceName the name of the workspace to login to; if
     * not provided, the workspace named by the
     * <code>workspaceName</code> property will be used. If that
     * property is also <code>null</code>, the repository's default
     * workspace will be used.
     */
    public Object execute(JCRCallback callback,
                          final String workspaceName)
        throws DataAccessException {
        try {
            Session session = null;
            Credentials creds = authenticationProvider.provideCredentials();
            if (creds != null) {
                session = repository.login(creds, workspaceName);
            }
            else {
                Subject subject = authenticationProvider.provideSubject();
                if (subject == null) {
                    throw new IllegalStateException("neither credentials nor subject provided");
                }

                PrivilegedAction action = new PrivilegedAction() {
                        public Object run() {
                            try {
                                return repository.login(workspaceName);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                session = (Session) Subject.doAs(subject, action);
            }

            Object object = callback.doInJCR(session);
            session.logout();
            return object;
        } catch (RepositoryException e) {
            throw convertJCRException(e);
        }
    }

    /**
     * Execute the action specified by the given action object within
     * a {@link javax.jcr.Session}. Application exceptions thrown by
     * the action object get propagated to the caller (can only be
     * unchecked). JCR exceptions are transformed into appropriate DAO
     * ones. Allows for returning a result object, i.e. a domain
     * object or a collection of domain objects.
     *
     * Note: Callback code does not need to explicitly log out of the
     * <code>Session</code>; this method will handle that itself.
     *
     * The workspace logged into will be that named by the
     * <code>workspaceName</code> property; if that property is
     * <code>null</code>, the repository's default workspace will be
     * used.
     *
     * @param callback the <code>JCRCallback</code> that executes
     * the client operation
     */
    public Object execute(JCRCallback callback)
        throws DataAccessException {
        return execute(callback, this.workspaceName);
    }

    /**
     * Sanity check the object's properties.
     */
    public void afterPropertiesSet() {
        if (getAuthenticationProvider() == null) {
            throw new IllegalArgumentException("authenticationProvider is required");
        }
        if (getRepository() == null) {
            throw new IllegalArgumentException("repository is required");
        }
        // workspaceName can be null - allows the workspace name to be
        // set by <code>execute()</code>, defaults to
        // <code>null</code> (the default workspace)
    }

    /**
     * Convert the given Exception thrown by a JCR component to an
     * appropriate exception from the
     * <code>org.springframework.dao</code>  hierarchy.
     */
    protected DataAccessException convertJCRException(RepositoryException e) {
        return new DataAccessResourceFailureException("could not access resource", e);
    }

    /**
     */
    public JCRAuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    /**
     */
    public void setAuthenticationProvider(JCRAuthenticationProvider provider) {
        this.authenticationProvider = provider;
    }

    /**
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     */
    public String getWorkspaceName() {
        return workspaceName;
    }

    /**
     */
    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }
}
