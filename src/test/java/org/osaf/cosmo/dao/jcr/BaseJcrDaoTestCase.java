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
package org.osaf.cosmo.dao.jcr;

import java.util.Properties;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import junit.framework.TestCase;

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
 * Base class for test cases that operate against a JCR repository.
 */
public class BaseJcrDaoTestCase extends TestCase implements JcrConstants {
    private static final Log log = LogFactory.getLog(BaseJcrDaoTestCase.class);

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
    private JcrTestHelper testHelper;

    /**
     */
    public BaseJcrDaoTestCase() {
        // load test properties (accessible to subclasses)
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
     * Open the repository and set up the <code>JcrTemplate</code>.
     */
    protected void setUp() throws Exception {
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

        // open the session
        session = SessionFactoryUtils.getSession(sessionFactory, true);
        SessionHolder sessionHolder =
            new DefaultSessionHolderProvider().createSessionHolder(session);
        TransactionSynchronizationManager.bindResource(sessionFactory,
                                                       sessionHolder);

        testHelper = new JcrTestHelper(session);
    }

    /**
     */
    protected void tearDown() throws Exception {
        testHelper = null;

        // close the session
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        session.refresh(false);
        SessionFactoryUtils.releaseSession(session, sessionFactory);

        ((RepositoryImpl) repository).shutdown();
    }

    /**
     */
    protected JcrSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     */
    protected JcrTestHelper getTestHelper() {
        return testHelper;
    }
}
