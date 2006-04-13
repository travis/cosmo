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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import junit.framework.TestCase;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.util.HexEscaper;

import org.apache.log4j.Logger;

/**
 */
public class Migration03Test extends TestCase {
    private static Logger log = Logger.getLogger(Migration03Test.class);

    private static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
    private static final String DB_URL =
        "jdbc:hsqldb:file:src/test/previous/db/userdb";
    private static final String DB_USERNAME = "sa";
    private static final String DB_PASSWORD = "";
    private static final String PREV_CONFIG =
        "src/test/previous/repository.xml";
    private static final String PREV_DATA =
        "src/test/previous/repository";
    private static final String PREV_USERNAME = "cosmo_repository";
    private static final String PREV_PASSWORD = "";
    private static final String CUR_CONFIG =
        "src/test/config/repository.xml";
    private static final String CUR_DATA =
        "target/test-migration03-repository";
    private static final String CUR_USERNAME = "cosmo_repository";
    private static final String CUR_PASSWORD = "";

    private static Session previous;
    private static Session current;

    private Migration03 migration;
    private Connection connection;

    static {
        try {
            // establish sessions for both repositories
            RepositoryConfig prc =
                RepositoryConfig.create(PREV_CONFIG, PREV_DATA);
            Repository pr = RepositoryImpl.create(prc);
            SimpleCredentials pc =
                new SimpleCredentials(PREV_USERNAME,
                                      PREV_PASSWORD.toCharArray());
            previous = pr.login(pc);

            RepositoryConfig crc =
                RepositoryConfig.create(CUR_CONFIG, CUR_DATA);
            Repository cr = RepositoryImpl.create(crc);
            SimpleCredentials cc =
                new SimpleCredentials(CUR_USERNAME, CUR_PASSWORD.toCharArray());
            current = cr.login(cc);

            // register namespaces and node types into the current repo,
            // including any custom namespaces that might have been
            // registered into the previous repo
            Migration03.registerNamespaces(previous, current);
            Migration03.registerNodeTypes(current);
        } catch (Exception e) {
            throw new RuntimeException("cannot set up test environment", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    log.debug("shutting stuff down");
                    previous.logout();
                    ((RepositoryImpl) previous.getRepository()).shutdown();

                    current.logout();
                    ((RepositoryImpl) current.getRepository()).shutdown();
                }
            });
    }

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

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
        Map overlord = migration.loadOverlord();
        assertNotNull(overlord);
    }

    /**
     */
    public void testLoadUsers() throws Exception {
        Map users = migration.loadUsers();
        assertNotNull(users);
        assertEquals(2, users.keySet().size());

        Map user2 = (Map) users.get(new Integer(2));
        assertNotNull("user 2 not found", user2);
        assertEquals("user 2 wrong username", "bcm",
                     (String) user2.get("username"));
        assertEquals("user 2 not admin", Boolean.TRUE,
                     (Boolean) user2.get("admin"));

        Map user3 = (Map) users.get(new Integer(3));
        assertNotNull("user 3 not found", user3);
        assertEquals("user 3 wrong username", "ixjonez",
                     (String) user3.get("username"));
        assertEquals("user 3 not admin", Boolean.FALSE,
                     (Boolean) user3.get("admin"));
    }

    /**
     */
    public void testCopyHomeAndUser() throws Exception {
        Map user = loadOldUser(3);
        String username = (String) user.get("username");

        Node home = migration.copyHome(user, previous, current);
        assertNotNull(home);

        String n1 = HexEscaper.escape(username.substring(0, 1));
        String n2 = HexEscaper.escape(username.substring(0, 2));

        String repoPath =
            "/" + n1 + "/" + n2 + "/" + HexEscaper.escape(username);
        assertEquals("home node path not " + repoPath,
                     repoPath, home.getPath());
        assertTrue("home node not ticketable node type",
                   home.isNodeType("ticket:ticketable"));
        assertTrue("home node not home collection node type",
                   home.isNodeType("cosmo:homecollection"));
        assertEquals("display name prop does not match username", username,
                     home.getProperty("dav:displayname").getString());
        assertTrue("home node not user node type",
                   home.isNodeType("cosmo:user"));
        assertEquals("username prop does not match username", username,
                     home.getProperty("cosmo:username").getString());
        assertTrue("home node does not have custom property _pre141:test",
                   home.hasProperty("_pre141:test"));

        assertTrue("home node does not have calendar child node",
                   home.hasNode("calendar"));
        Node calendar = home.getNode("calendar");
        assertTrue("calendar node not dav collection node type",
                  calendar.isNodeType("dav:collection"));
        assertTrue("calendar node not calendar collection node type",
                   calendar.isNodeType("calendar:collection"));
        assertTrue("calendar node not ticketable node type",
                   calendar.isNodeType("ticket:ticketable"));
        assertTrue("calendar node does not have property dav:displayname",
                   calendar.hasProperty("dav:displayname"));
        assertTrue("calendar node does not have property calendar:description",
                   calendar.hasProperty("calendar:description"));
        assertTrue("calendar node does not have property xml:lang",
                   calendar.hasProperty("xml:lang"));
        assertTrue("calendar node does not have property calendar:supportedComponentSet",
                   calendar.hasProperty("calendar:supportedComponentSet"));
        
        assertTrue("calendar node does not have ticket child node",
                   calendar.hasNode("ticket:ticket"));
        Node calendarTicket = calendar.getNode("ticket:ticket");
        assertTrue("calendar ticket node not ticket node type",
                   calendarTicket.isNodeType("ticket:ticket"));

        assertTrue("calendar node does not have event1.ics child node",
                   calendar.hasNode("event1.ics"));
        Node event = calendar.getNode("event1.ics");
        assertTrue("event node not dav resource node type",
                   event.isNodeType("dav:resource"));
        assertTrue("event node not calendar resource node type",
                   event.isNodeType("calendar:resource"));
        assertTrue("event node not event node type",
                   event.isNodeType("calendar:event"));
        assertTrue("event node not ticketable node type",
                   event.isNodeType("ticket:ticketable"));
        assertTrue("event node does not have property dav:displayname",
                   event.hasProperty("dav:displayname"));
        assertTrue("event node does not have property calendar:uid",
                   event.hasProperty("calendar:uid"));

        assertTrue("event node does not have jcr:content child node",
                   event.hasNode("jcr:content"));
        Node eventContent = event.getNode("jcr:content");
        assertTrue("content node does not have property jcr:data",
                   eventContent.hasProperty("jcr:data"));
        assertTrue("content node does not have property jcr:mimeType",
                   eventContent.hasProperty("jcr:mimeType"));
        assertTrue("content node does not have property jcr:lastModified",
                   eventContent.hasProperty("jcr:lastModified"));

        assertTrue("home node does not have README.txt child node",
                   home.hasNode("README.txt"));
        Node readme = home.getNode("README.txt");
        assertTrue("readme node not dav resource node type",
                   readme.isNodeType("dav:resource"));
        assertTrue("readme node not ticketable node type",
                   readme.isNodeType("ticket:ticketable"));
        assertTrue("readme node does not have property dav:displayname",
                   readme.hasProperty("dav:displayname"));

        assertTrue("readme node does not have ticket child node",
                   readme.hasNode("ticket:ticket"));
        Node readmeTicket = readme.getNode("ticket:ticket");
        assertTrue("readme ticket node not ticket node type",
                   readmeTicket.isNodeType("ticket:ticket"));

        assertTrue("readme node does not have jcr:content child node",
                   readme.hasNode("jcr:content"));
        Node readmeContent = readme.getNode("jcr:content");
        assertTrue("content node does not have property jcr:data",
                   readmeContent.hasProperty("jcr:data"));
        assertTrue("content node does not have property jcr:mimeType",
                   readmeContent.hasProperty("jcr:mimeType"));
        assertTrue("content node does not have property jcr:lastModified",
                   readmeContent.hasProperty("jcr:lastModified"));
   }

    private Map loadOldUser(int id)
        throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select username, password, firstName, lastName, email, dateCreated, dateModified from user where id = " + new Integer(id));
        rs.next();
 
       HashMap user = new HashMap();
        user.put("username", rs.getString("username"));
        user.put("password", rs.getString("password"));
        user.put("firstname", rs.getString("firstName"));
        user.put("lastname", rs.getString("lastName"));
        user.put("email", rs.getString("email"));
        user.put("admin", Boolean.FALSE);
        Calendar created = Calendar.getInstance();
        created.setTime(rs.getDate("dateCreated"));
        user.put("dateCreated", created);
        Calendar modified = Calendar.getInstance();
        modified.setTime(rs.getDate("dateModified"));
        user.put("dateModified", modified);

        return user;
    }
}
