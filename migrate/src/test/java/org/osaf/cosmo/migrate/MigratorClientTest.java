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
package org.osaf.cosmo.migrate;

import java.util.Properties;
import javax.jcr.Session;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 */
public class MigratorClientTest extends TestCase {
    private static Logger log = Logger.getLogger(MigratorClientTest.class);

    public static final String PROP_CONFIG = "cosmo.migrate.test.config";
    public static final String PROP_DATA = "cosmo.migrate.test.data";
    public static final String PROP_USERNAME = "cosmo.migrate.test.username";
    public static final String PROP_PASSWORD = "cosmo.migrate.test.password";

    private String testConfig;
    private String testData;
    private String testUsername;
    private String testPassword;

    private MigratorClient client;

    /**
     */
    public MigratorClientTest() {
        // load test properties
        Properties testprops = new Properties();
        try {
            testprops.load(MigratorClientTest.class.getClassLoader().
                           getResourceAsStream("test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("can't load test.properties", e);
        }

        // extract test props used by this class
        testConfig = testprops.getProperty(PROP_CONFIG);
        testData = testprops.getProperty(PROP_DATA);
        testUsername = testprops.getProperty(PROP_USERNAME);
        testPassword = testprops.getProperty(PROP_PASSWORD);
    }

    /**
     */
    protected void setUp() throws Exception {
        client = new MigratorClient();
        client.setConfig(testConfig);
        client.setData(testData);
        client.setCredentials(testUsername, testPassword);
    }

    /**
     */
    public void testLifecycle() throws Exception {
        Session session = client.start();
        assertNotNull(session);

        client.stop();
        assertNull(client.getSession());
    }

    /**
     */
    public void testStartAlreadyStarted() throws Exception {
        client.start();
        try {
            client.start();
            fail("Started already started client");
        } catch (IllegalStateException e) {
            // expected
        } finally {
            client.stop();
        }
    }

    /**
     */
    public void testStopNonStarted() throws Exception {
        try {
            client.stop();
            fail("Stopped non-started client");
        } catch (IllegalStateException e) {
            // expected
        }
    }
}
