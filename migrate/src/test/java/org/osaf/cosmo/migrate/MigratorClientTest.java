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

    private static final String CONFIG = "src/test/config/repository.xml";
    private static final String DATA = "target/test-repository";
    private static final String USERNAME = "cosmo_repository";
    private static final String PASSWORD = "";

    private MigratorClient client;

    /**
     */
    public MigratorClientTest() {
    }

    /**
     */
    protected void setUp() throws Exception {
        client = new MigratorClient();
        client.setConfig(CONFIG);
        client.setData(DATA);
        client.setCredentials(USERNAME, PASSWORD);
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
