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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.User;

/**
 */
public class Migration03Test extends TestCase {
    private static final Log log = LogFactory.getLog(Migration03Test.class);

    private static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
    private static final String DB_URL =
        "jdbc:hsqldb:file:src/test/unit/migrate/db/userdb";
    private static final String DB_USERNAME = "sa";
    private static final String DB_PASSWORD = "";

    private Migration03 migration;
    private Connection connection;

    /**
     */
    protected void setUp() throws Exception {
        migration = new Migration03();
        Class.forName(DB_DRIVER);
        connection = DriverManager.getConnection(DB_URL, DB_USERNAME,
                                                 DB_PASSWORD);
        migration.setConnection(connection);
    }

    /**
     */
    protected void tearDown() throws Exception {
        Statement st = connection.createStatement();
        st.execute("SHUTDOWN");
        connection.close();
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
}
