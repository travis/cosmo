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
package org.osaf.cosmo.repository.jackrabbit;

import javax.jcr.Credentials;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.repository.PathTranslator;
import org.osaf.cosmo.repository.SchemaConstants;

/**
 */
public class JackrabbitRepositoryInitializerTest extends TestCase
    implements SchemaConstants {
    private static final Log log =
        LogFactory.getLog(JackrabbitRepositoryInitializerTest.class);

    private static final String CONFIG = "src/test/unit/config/repository.xml";
    private static final String DATA = "target/test-repository-initializer";
    private static final String USERNAME = "cosmo_repository";
    private static final String PASSWORD = "";
    private static final Credentials CREDENTIALS =
        new SimpleCredentials(USERNAME, PASSWORD.toCharArray());

    private JackrabbitRepositoryInitializer initializer;
    private Repository repository;
    private Session session;

    /** */
    protected void setUp() throws Exception {
        RepositoryConfig rc = RepositoryConfig.create(CONFIG, DATA);
        repository = RepositoryImpl.create(rc);

        initializer = new JackrabbitRepositoryInitializer();
        initializer.setRepository(repository);
        initializer.setCredentials(CREDENTIALS);

        session = repository.login(initializer.getCredentials());
    }

    /** */
    public void testNotAlreadyInitialized() throws Exception {
        boolean rv = initializer.isAlreadyInitialized(session);
        assertTrue(! rv);
    }

    /** */
    public void testAlreadyInitialized() throws Exception {
        String name = User.USERNAME_OVERLORD;

        String n1 = PathTranslator.toRepositoryPath(name.substring(0, 1));
        Node l1 = session.getRootNode().addNode(n1, NT_UNSTRUCTURED);

        String n2 = PathTranslator.toRepositoryPath(name.substring(0, 2));
        Node l2 = l1.addNode(n2, NT_UNSTRUCTURED);

        String n3 = PathTranslator.toRepositoryPath(name);
        Node l3 = l2.addNode(n3, NT_BASE);

        boolean rv = initializer.isAlreadyInitialized(session);
        assertTrue(rv);

        session.refresh(false);
    }

    /** */
    public void testRegisterNamespaces() throws Exception {
        initializer.registerNamespaces(session);

        NamespaceRegistry registry =
            session.getWorkspace().getNamespaceRegistry();
        assertEquals(PREFIX_DAV, registry.getPrefix(NS_DAV));
        assertEquals(NS_DAV, registry.getURI(PREFIX_DAV));
        assertEquals(PREFIX_ICALENDAR, registry.getPrefix(NS_ICALENDAR));
        assertEquals(NS_ICALENDAR, registry.getURI(PREFIX_ICALENDAR));
        assertEquals(PREFIX_CALENDAR, registry.getPrefix(NS_CALENDAR));
        assertEquals(NS_CALENDAR, registry.getURI(PREFIX_CALENDAR));
        assertEquals(PREFIX_TICKET, registry.getPrefix(NS_TICKET));
        assertEquals(NS_TICKET, registry.getURI(PREFIX_TICKET));
        assertEquals(PREFIX_COSMO, registry.getPrefix(NS_COSMO));
        assertEquals(NS_COSMO, registry.getURI(PREFIX_COSMO));
    }

    /** */
    public void testRegisterNodeTypes() throws Exception {
        initializer.registerNodeTypes(session);

        NodeTypeManager manager =
            session.getWorkspace().getNodeTypeManager();
        try {
            manager.getNodeType(NT_DAV_COLLECTION);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_DAV_COLLECTION + " not registered");
        }
        try {
            manager.getNodeType(NT_DAV_RESOURCE);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_DAV_RESOURCE + " not registered");
        }
        try {
            manager.getNodeType(NT_TICKETABLE);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_TICKETABLE + " not registered");
        }
        try {
            manager.getNodeType(NT_TICKET);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_TICKET + " not registered");
        }
        try {
            manager.getNodeType(NT_CALENDAR_COLLECTION);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_CALENDAR_COLLECTION + " not registered");
        }
        try {
            manager.getNodeType(NT_CALENDAR_RESOURCE);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_CALENDAR_RESOURCE + " not registered");
        }
        try {
            manager.getNodeType(NT_EVENT_RESOURCE);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_EVENT_RESOURCE + " not registered");
        }
        try {
            manager.getNodeType(NT_USER);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_USER + " not registered");
        }
        try {
            manager.getNodeType(NT_HOME_COLLECTION);
        } catch (NoSuchNodeTypeException e) {
            fail("node type " + NT_HOME_COLLECTION + " not registered");
        }
    }

    /** */
    public void testAddOverlordNode() throws Exception {
        initializer.addOverlordNode(session);

        String path =
            PathTranslator.toRepositoryPath("/" + User.USERNAME_OVERLORD);
        assertTrue(session.itemExists(path));

        Node overlord = (Node) session.getItem(path);
        assertEquals(User.USERNAME_OVERLORD,
                     overlord.getProperty(NP_USER_USERNAME).getString());

        session.refresh(false);
    }

    /** */
    public void testAddSystemNodes() throws Exception {
        initializer.addSystemNodes(session);

        String path = "/" + NN_COSMO_SYSTEM + "/" + NN_COSMO_SCHEMA + "/" +
            NP_COSMO_SCHEMA_VERSION;
        assertTrue(session.itemExists(path));

        Property version = (Property) session.getItem(path);
        assertEquals(CosmoConstants.PRODUCT_VERSION, version.getString());

        session.refresh(false);
    }

    /** */
    public void tearDown() throws Exception {
        session.logout();
        ((RepositoryImpl) repository).shutdown();
    }
}
