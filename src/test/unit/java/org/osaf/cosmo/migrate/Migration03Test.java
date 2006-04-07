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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

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
    private static final String PREV_CONFIG =
        "src/test/unit/migrate/repository.xml";
    private static final String PREV_DATA =
        "src/test/unit/migrate/repository";
    private static final String PREV_USERNAME = "cosmo_repository";
    private static final String PREV_PASSWORD = "";
    private static final String CUR_CONFIG =
        "src/test/unit/config/repository.xml";
    private static final String CUR_DATA =
        "target/test-repository";
    private static final String CUR_USERNAME = "cosmo_repository";
    private static final String CUR_PASSWORD = "";

    private Migration03 migration;
    private Connection connection;
    private JackrabbitTestSessionManager previousSessionManager;
    private JackrabbitTestSessionManager currentSessionManager;
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

        previousSessionManager = new JackrabbitTestSessionManager();
        previousSessionManager.setConfig(PREV_CONFIG);
        previousSessionManager.setData(PREV_DATA);
        previousSessionManager.setUsername(PREV_USERNAME);
        previousSessionManager.setPassword(PREV_PASSWORD);
        previousSessionManager.setUp();

        currentSessionManager = new JackrabbitTestSessionManager();
        currentSessionManager.setConfig(CUR_CONFIG);
        currentSessionManager.setData(CUR_DATA);
        currentSessionManager.setUsername(CUR_USERNAME);
        currentSessionManager.setPassword(CUR_PASSWORD);
        currentSessionManager.setUp();

        // add namespace for a custom property in the previous repo to
        // the current repo - this only needs to happen the first time
        // the test suite is set up
        NamespaceRegistry curNsReg =
            currentSessionManager.getSession().getWorkspace().
            getNamespaceRegistry();
        try {
            curNsReg.getURI("_pre141");
        } catch (NamespaceException e) {
            curNsReg.registerNamespace("_pre141", "myapp:ns");
        }

        testHelper = new JcrTestHelper(currentSessionManager.getSession());
    }

    /**
     */
    protected void tearDown() throws Exception {
        previousSessionManager.tearDown();

        currentSessionManager.tearDown();

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
    public void testRegisterCurrentNamespaces() throws Exception {
        Session previous = previousSessionManager.getSession();
        Session current = currentSessionManager.getSession();

        Long now = new Long(System.currentTimeMillis());
        String prefix = "m03test" + now;
        String uri = "cosmo:0.3:migration:test" + now;

        // add a custom namespace to previous
        NamespaceRegistry prevNsReg =
            previous.getWorkspace().getNamespaceRegistry();
        prevNsReg.registerNamespace(prefix, uri);

        migration.registerCurrentNamespaces(previous, current);

        // confim that the custom namespace is in current
        NamespaceRegistry curNsReg =
            current.getWorkspace().getNamespaceRegistry();
        try {
            curNsReg.getURI(prefix);
        } catch (NamespaceException e) {
            fail("m03test was not registered in previous repository");
        }

        // would like to unregister the namespace but jackrabbit does
        // not support this feature
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
        User user = loadOldUser(2);

        Session previous = previousSessionManager.getSession();
        Session current = currentSessionManager.getSession();

        HomeCollectionResource home =
            migration.createCurrentHome(user, previous, current);
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
                   current.itemExists(repoPath));
        Node homeNode = (Node) current.getItem(repoPath);
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

        // XXX: for some reason the custom property is not found
        // through jcr even though i can see it using webdav with
        // cosmo on top of the same repository data
//         try {
//             homeNode.getProperty("_pre141:test");
//         } catch (PathNotFoundException e) {
//             fail("_pre141:test property not found on home node");
//         }
//         assertNotNull("_pre141:test property not found on home collection",
//                       home.getProperty("_pre141:test"));
    }

    private User loadOldUser(int id)
        throws Exception {
        User user = new User();

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select username, password, firstName, lastName, email, dateCreated, dateModified from user where id = " + new Integer(id));
        rs.next();

        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("firstName"));
        user.setLastName(rs.getString("lastName"));
        user.setEmail(rs.getString("email"));
        user.setAdmin(Boolean.FALSE); // XXX check to see if this is true
        user.setDateCreated(rs.getDate("dateCreated"));
        user.setDateModified(rs.getDate("dateModified"));

        st.close();

        return user;
    }
}
