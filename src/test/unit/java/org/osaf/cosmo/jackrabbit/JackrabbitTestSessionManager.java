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

    public static final String PROP_CONFIG_FILE_PATH =
        "jackrabbit.repository.config";
    public static final String PROP_REP_HOME_DIR =
        "jackrabbit.repository.homedir";
    public static final String PROP_USERNAME =
        "jackrabbit.repository.username";
    public static final String PROP_PASSWORD =
        "jackrabbit.repository.password";

    private String configFilePath;
    private String repositoryHomedirPath;
    private String username;
    private String password;
    private Repository repository;
    private JcrSessionFactory sessionFactory;
    private Session session;

    /**
     * Loads the <code>test.properties</code> resource and reads
     * repository properties from it.
     */
    public JackrabbitTestSessionManager() {
        // load test properties
        Properties testprops = new Properties();
        try {
            testprops.load(getClass().getClassLoader().
                           getResourceAsStream("test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("can't load test.properties", e);
        }

        // extract test props used by this class
        configFilePath = testprops.getProperty(PROP_CONFIG_FILE_PATH);
        repositoryHomedirPath = testprops.getProperty(PROP_REP_HOME_DIR);
        username = testprops.getProperty(PROP_USERNAME);
        password = testprops.getProperty(PROP_PASSWORD);
    }
    
    /**
     * Opens the repository, sets up the session factory, and begins a
     * session.
     */
    public void setUp() throws Exception {
        // set up repository
        try {
            RepositoryConfig config =
                RepositoryConfig.create(configFilePath, repositoryHomedirPath);
            repository = RepositoryImpl.create(config);
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
    public JcrSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     */
    public Session getSession() {
        return session;
    }
}
