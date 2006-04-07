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
package org.osaf.cosmo.jackrabbit;

import java.util.Properties;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springmodules.jcr.JcrSessionFactory;
import org.springmodules.jcr.JcrTemplate;
import org.springmodules.jcr.SessionHolder;
import org.springmodules.jcr.SessionHolderProvider;
import org.springmodules.jcr.SessionFactoryUtils;
import org.springmodules.jcr.support.DefaultSessionHolderProvider;

/**
 * Class that provides a simple facade for loading a Jackrabbit
 * repository, executing transactions, and making repository sessions
 * available to test cases.
 */
public class JackrabbitTestSessionManager {
    private static final Log log =
        LogFactory.getLog(JackrabbitTestSessionManager.class);

    private String config;
    private String data;
    private String username;
    private String password;
    private Repository repository;
    private JcrSessionFactory sessionFactory;
    private Session session;

    /**
     * Opens the repository, sets up the session factory, and begins a
     * session.
     */
    public void setUp() throws Exception {
        // set up repository
        try {
            RepositoryConfig rc = RepositoryConfig.create(config, data);
            repository = RepositoryImpl.create(rc);
        } catch (Exception e) {
            throw new RuntimeException("can't open repository", e);
        }

        // set up template
        SimpleCredentials credentials =
            new SimpleCredentials(username, password.toCharArray());

        sessionFactory = new JcrSessionFactory();
        sessionFactory.setRepository(repository);
        sessionFactory.setCredentials(credentials);

        // begin the session
        session = SessionFactoryUtils.getSession(sessionFactory, true);
        SessionHolder sessionHolder =
            new DefaultSessionHolderProvider().createSessionHolder(session);
        TransactionSynchronizationManager.bindResource(sessionFactory,
                                                       sessionHolder);
    }

    /**
     * Drops all pending saves in the session, releases the session,
     * and shuts down the repository.
     */
    public void tearDown() throws Exception {
        // close the session
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        session.refresh(false);
        SessionFactoryUtils.releaseSession(session, sessionFactory);

        ((RepositoryImpl) repository).shutdown();
    }

    /**
     */
    public String getConfig() {
        return config;
    }

    /**
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     */
    public String getData() {
        return data;
    }

    /**
     */
    public void setData(String data) {
        this.data = data;
    }

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

    /**
     */
    public JcrSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     */
    public Session getSession() {
        return session;
    }
}
