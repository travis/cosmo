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

import javax.jcr.Session;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.jackrabbit.JackrabbitTestSessionManager;

import org.springmodules.jcr.JcrSessionFactory;

/**
 * Base class for dao test cases that operate against a JCR repository.
 */
public class BaseJcrDaoTestCase extends TestCase {
    private static final Log log = LogFactory.getLog(BaseJcrDaoTestCase.class);

    private static final String CONFIG = "src/test/unit/config/repository.xml";
    private static final String DATA = "target/test-repository";
    private static final String USERNAME = "cosmo_repository";
    private static final String PASSWORD = "";

    private JackrabbitTestSessionManager sessionManager;
    private JcrTestHelper testHelper;

    /**
     */
    protected void setUp() throws Exception {
        sessionManager = new JackrabbitTestSessionManager();
        sessionManager.setConfig(CONFIG);
        sessionManager.setData(DATA);
        sessionManager.setUsername(USERNAME);
        sessionManager.setPassword(PASSWORD);
        sessionManager.setUp();

        testHelper = new JcrTestHelper(sessionManager.getSession());
    }

    /**
     */
    protected void tearDown() throws Exception {
        testHelper = null;

        sessionManager.tearDown();
    }

    /**
     */
    protected JcrSessionFactory getSessionFactory() {
        return sessionManager.getSessionFactory();
    }

    /**
     */
    protected JcrTestHelper getTestHelper() {
        return testHelper;
    }
}
