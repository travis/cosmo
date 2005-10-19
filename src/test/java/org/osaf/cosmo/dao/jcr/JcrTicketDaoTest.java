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

import java.util.Date;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Test Case for {@link JcrTicketDao}.
 */
public class JcrTicketDaoTest extends BaseJcrDaoTestCase {
    private static final Log log = LogFactory.getLog(JcrTicketDaoTest.class);

    private JcrTicketDao dao;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        dao = new JcrTicketDao();
        dao.setTemplate(getTemplate());

        SessionIdGenerator idGenerator = new SessionIdGenerator();
        dao.setIdGenerator(idGenerator);

        try {
            dao.init();
        } catch (Exception e) {
            tearDown();
            throw e;
        }
    }

    /**
     */
    protected void tearDown() throws Exception {
        try {
            dao.destroy();
        } finally {
            dao = null;
            super.tearDown();
        }
    }

    /**
     */
    public void testCreateTicket() throws Exception {
        Session session = acquireSession();

        User u1 = TestHelper.makeDummyUser();
        Node n1 = JcrTestHelper.addTicketableNode(session);

        Ticket t1 = TestHelper.makeDummyTicket();
        t1.setOwner(u1.getUsername());

        dao.createTicket(n1.getPath(), t1);
        Ticket ticket = JcrTestHelper.findDummyTicket(n1, t1.getId());
        assertNotNull("Ticket not stored", ticket);

        n1.remove();
        session.save();

        session.logout();
    }

    /**
     */
    public void testCreateTicketOnNonExistentNode() throws Exception {
        Ticket t1 = TestHelper.makeDummyTicket();

        try {
            dao.createTicket("/dead/beef", t1);
            fail("Ticket created on nonexistent node");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testCreateTicketOnProperty() throws Exception {
        Session session = acquireSession();

        Node node = JcrTestHelper.addNode(session);
        Property property = JcrTestHelper.addProperty(node);

        Ticket t1 = TestHelper.makeDummyTicket();

        try {
            dao.createTicket(property.getPath(), t1);
            fail("Ticket created on property");
        } catch (InvalidDataAccessResourceUsageException e) {
            // expected
        } finally {
            node.remove();
            session.getRootNode().save();
            session.logout();
        }
    }

    /**
     */
    public void testGetTickets() throws Exception {
        Session session = acquireSession();

        User u1 = TestHelper.makeDummyUser();
        Node n1 = JcrTestHelper.addTicketableNode(session);

        Ticket t1 = JcrTestHelper.makeAndStoreDummyTicket(n1, u1);
        Ticket t2 = JcrTestHelper.makeAndStoreDummyTicket(n1, u1);
        Ticket t3 = JcrTestHelper.makeAndStoreDummyTicket(n1, u1);

        Set tickets = dao.getTickets(n1.getPath());

        assertTrue("Not 3 tickets", tickets.size() == 3);
        assertTrue("Ticket 1 not found in tickets", tickets.contains(t1));
        assertTrue("Ticket 2 not found in tickets", tickets.contains(t2));
        assertTrue("Ticket 3 not found in tickets", tickets.contains(t3));

        n1.remove();
        session.save();

        session.logout();
    }

    /**
     */
    public void testGetTicketsOnNonExistentNode() throws Exception {
        try {
            dao.getTickets("/dead/beef");
            fail("Got tickets on nonexistent node");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testGetTicketsOnProperty() throws Exception {
        Session session = acquireSession();

        Node node = JcrTestHelper.addNode(session);
        Property property = JcrTestHelper.addProperty(node);

        try {
            dao.getTickets(property.getPath());
            fail("Got tickets on property");
        } catch (InvalidDataAccessResourceUsageException e) {
            // expected
        } finally {
            node.remove();
            session.getRootNode().save();
            session.logout();
        }
    }

    /**
     */
    public void testGetTicket() throws Exception {
        Session session = acquireSession();

        User u1 = TestHelper.makeDummyUser();
        Node n1 = JcrTestHelper.addTicketableNode(session);

        Ticket t1 = JcrTestHelper.makeAndStoreDummyTicket(n1, u1);

        Ticket ticket = dao.getTicket(n1.getPath(), t1.getId());
        assertNotNull("Ticket " + t1.getId() + " null", ticket);

        n1.remove();
        session.save();

        session.logout();
    }

    /**
     */
    public void testGetTicketNotFound() throws Exception {
        Session session = acquireSession();

        Node n1 = JcrTestHelper.addTicketableNode(session);

        try {
            dao.getTicket(n1.getPath(), "deadbeef");
            fail("nonexistent ticket found");
        } catch (DataRetrievalFailureException e) {
            // expected
        } finally {
            n1.remove();
            session.save();
            session.logout();
        }
    }

    /**
     */
    public void testGetTicketOnProperty() throws Exception {
        Session session = acquireSession();

        Node node = JcrTestHelper.addNode(session);
        Property property = JcrTestHelper.addProperty(node);

        try {
            dao.getTicket(property.getPath(), "cafebebe");
            fail("Got ticket on property");
        } catch (InvalidDataAccessResourceUsageException e) {
            // expected
        } finally {
            node.remove();
            session.getRootNode().save();
            session.logout();
        }
    }

    /**
     */
    public void testGetInheritedTicket() throws Exception {
        Session session = acquireSession();

        User u1 = TestHelper.makeDummyUser();
        Node n1 = JcrTestHelper.addTicketableNode(session);

        Ticket t1 = JcrTestHelper.makeAndStoreDummyTicket(n1, u1);

        String path = n1.getPath() + "/foobar";
        Ticket ticket = dao.getTicket(path, t1.getId());
        assertNotNull("Ticket " + t1.getId() + " null", ticket);

        n1.remove();
        session.save();

        session.logout();
    }

    /**
     */
    public void testGetInheritedTicketNotFound() throws Exception {
        Session session = acquireSession();

        Node n1 = JcrTestHelper.addTicketableNode(session);

        try {
            String path = n1.getPath() + "/foobar";
            dao.getTicket(path, "deadbeef");
            fail("nonexistent ticket found");
        } catch (DataRetrievalFailureException e) {
            // expected
        } finally {
            n1.remove();
            session.save();
            session.logout();
        }
    }

    /**
     */
    public void testRemoveTicket() throws Exception {
        Session session = acquireSession();

        User u1 = TestHelper.makeDummyUser();
        Node n1 = JcrTestHelper.addTicketableNode(session);

        Ticket t1 = JcrTestHelper.makeAndStoreDummyTicket(n1, u1);

        dao.removeTicket(n1.getPath(), t1);

        Ticket ticket = JcrTestHelper.findDummyTicket(n1, t1.getId());
        assertNull("Ticket not removed", ticket);

        n1.remove();
        session.save();

        session.logout();
    }

    /**
     */
    public void testRemoveTicketOnProperty() throws Exception {
        Session session = acquireSession();

        Node node = JcrTestHelper.addNode(session);
        Property property = JcrTestHelper.addProperty(node);

        Ticket t1 = TestHelper.makeDummyTicket();

        try {
            dao.removeTicket(property.getPath(), t1);
            fail("Removed ticket on property");
        } catch (InvalidDataAccessResourceUsageException e) {
            // expected
        } finally {
            node.remove();
            session.getRootNode().save();
            session.logout();
        }
    }

    /**
     */
    public void testRemoveInheritedTicket() throws Exception {
        Session session = acquireSession();

        User u1 = TestHelper.makeDummyUser();
        Node n1 = JcrTestHelper.addTicketableNode(session);

        Ticket t1 = JcrTestHelper.makeAndStoreDummyTicket(n1, u1);

        String path = n1.getPath() + "/foobar";
        dao.removeTicket(path, t1);

        Ticket ticket = JcrTestHelper.findDummyTicket(n1, t1.getId());
        assertNull("Ticket not removed", ticket);

        n1.remove();
        session.save();

        session.logout();
    }
}
