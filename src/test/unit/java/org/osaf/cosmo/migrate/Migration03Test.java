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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

import javax.jcr.Node;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.jcr.JcrTestHelper;
import org.osaf.cosmo.jackrabbit.JackrabbitTestSessionManager;
import org.osaf.cosmo.model.HomeCollectionResource;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.repository.PathTranslator;
import org.osaf.cosmo.repository.SchemaConstants;

/**
 */
public class Migration03Test extends TestCase implements SchemaConstants {
    private static final Log log = LogFactory.getLog(Migration03Test.class);

    private static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
    private static final String DB_URL =
        "jdbc:hsqldb:file:src/test/unit/migrate/db/userdb";
    private static final String DB_USERNAME = "sa";
    private static final String DB_PASSWORD = "";

    private Migration03 migration;
    private Connection connection;
    private JackrabbitTestSessionManager sessionManager;
    private JcrTestHelper testHelper;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        migration = new Migration03();
        Class.forName(DB_DRIVER);
        connection = DriverManager.getConnection(DB_URL, DB_USERNAME,
                                                 DB_PASSWORD);
        migration.setConnection(connection);

        sessionManager = new JackrabbitTestSessionManager();
        sessionManager.setUp();

        testHelper = new JcrTestHelper(sessionManager.getSession());
    }

    /**
     */
    protected void tearDown() throws Exception {
        sessionManager.tearDown();

        Statement st = connection.createStatement();
        st.execute("SHUTDOWN");
        connection.close();

        super.tearDown();
    }

    /**
     */
    public void testInitNoUrlSystemProperty() throws Exception {
        // use our own migration rather than the one set up for us
        Migration03 m = new Migration03();
        try {
            m.init();
            fail("Migration initialized without url system property");
        } catch (MigrationException e) {
            // expected
        }
    }

    /**
     */
    public void testInit() throws Exception {
        // use our own migration rather than the one set up for us
        Migration03 m = new Migration03();
        System.setProperty(Migration03.SYSPROP_USERDB_URL, DB_URL);
        m.init();
    }

    /**
     */
    public void testLoadOverlord() throws Exception {
        User overlord = migration.loadOverlord();
        assertNotNull(overlord);
    }

    /**
     */
    public void testLoadUsers() throws Exception {
        Map users = migration.loadUsers();
        assertNotNull(users);
        assertEquals(2, users.keySet().size());

        User user2 = (User) users.get(new Integer(2));
        assertNotNull("user 2 not found", user2);
        assertEquals("user 2 wrong username", "bcm", user2.getUsername());
        assertEquals("user 2 not admin", Boolean.TRUE, user2.getAdmin());

        User user3 = (User) users.get(new Integer(3));
        assertNotNull("user 3 not found", user3);
        assertEquals("user 3 wrong username", "ixjonez", user3.getUsername());
        assertEquals("user 2 admin", Boolean.FALSE, user3.getAdmin());
    }

    /**
     */
    public void testCreateCurrentHome() throws Exception {
        User user = testHelper.makeDummyUser();

        HomeCollectionResource home =
            migration.createCurrentHome(user, sessionManager.getSession());
        assertNotNull(home);
        assertEquals("resource display name does not match username",
                     user.getUsername(), home.getDisplayName());
        assertEquals("resource client path does not match '/' + username",
                     "/" + user.getUsername(), home.getPath());

        // to some extent these are testing RepositoryMapper as well
        // as Migration03, but since we lack unit tests for
        // RepositoryMapper, that's ok
        String repoPath =
            PathTranslator.toRepositoryPath("/" + user.getUsername());
        assertTrue("home node not found at " + repoPath,
                   sessionManager.getSession().itemExists(repoPath));
        Node homeNode = (Node) sessionManager.getSession().getItem(repoPath);
        assertTrue("home node not home collection node type",
                   homeNode.isNodeType(NT_HOME_COLLECTION));
        assertEquals("displayname prop does not match resource display name",
                     home.getDisplayName(),
                     homeNode.getProperty(NP_DAV_DISPLAYNAME).getString());

        // same for UserMapper
        assertTrue("home node not user node type",
                   homeNode.isNodeType(NT_USER));
        assertEquals("username prop does not match username",
                     user.getUsername(),
                     homeNode.getProperty(NP_USER_USERNAME).getString());
    }
}
