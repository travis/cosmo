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

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.repository.PathTranslator;

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

        SessionIdGenerator idGenerator = new SessionIdGenerator();

        dao = new JcrTicketDao();
        dao.setSessionFactory(getSessionFactory());
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
        User u1 = getTestHelper().makeAndStoreDummyUser();

        Ticket t1 = getTestHelper().makeDummyTicket();
        t1.setOwner(u1.getUsername());

        dao.createTicket("/" + u1.getUsername(), t1);
        Ticket ticket = getTestHelper().
            findDummyTicket("/" + u1.getUsername(), t1.getId());
        assertNotNull("Ticket not stored", ticket);

        getTestHelper().removeDummyUser(u1);
    }

    /**
     */
    public void testCreateTicketOnNonExistentNode() throws Exception {
        Ticket t1 = getTestHelper().makeDummyTicket();

        try {
            dao.createTicket("/dead/beef", t1);
            fail("Ticket created on nonexistent node");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    /**
     */
    public void testGetTickets() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        Ticket t1 = getTestHelper().makeAndStoreDummyTicket(u1);
        Ticket t2 = getTestHelper().makeAndStoreDummyTicket(u1);
        Ticket t3 = getTestHelper().makeAndStoreDummyTicket(u1);

        Set tickets = dao.getTickets("/" + u1.getUsername());

        assertTrue("Not 3 tickets", tickets.size() == 3);
        assertTrue("Ticket 1 not found in tickets", tickets.contains(t1));
        assertTrue("Ticket 2 not found in tickets", tickets.contains(t2));
        assertTrue("Ticket 3 not found in tickets", tickets.contains(t3));

        getTestHelper().removeDummyUser(u1);
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
    public void testGetTicket() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        Ticket t1 = getTestHelper().makeAndStoreDummyTicket(u1);

        Ticket ticket = dao.getTicket("/" + u1.getUsername(), t1.getId());
        assertNotNull("Ticket " + t1.getId() + " null", ticket);

        getTestHelper().removeDummyUser(u1);
    }

    /**
     */
    public void testGetTicketNotFound() throws Exception {
        Node n1 = getTestHelper().addTicketableNode();

        Ticket ticket =
            dao.getTicket(PathTranslator.toClientPath(n1.getPath()),
                          "deadbeef");
        assertNull("nonexistent ticket found", ticket);
    }

    /**
     */
    public void testGetInheritedTicket() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        Ticket t1 = getTestHelper().makeAndStoreDummyTicket(u1);

        String path = "/" + u1.getUsername() + "/foobar";
        Ticket ticket = dao.getTicket(path, t1.getId());
        assertNotNull("Ticket " + t1.getId() + " null", ticket);

        getTestHelper().removeDummyUser(u1);
    }

    /**
     */
    public void testGetInheritedTicketNotFound() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();

        String path = "/" + u1.getUsername() + "/foobar";
        Ticket ticket = dao.getTicket(path, "deadbeef");
        assertNull("nonexistent ticket found", ticket);

        getTestHelper().removeDummyUser(u1);
    }

    /**
     */
    public void testRemoveTicket() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        Ticket t1 = getTestHelper().makeAndStoreDummyTicket(u1);

        dao.removeTicket("/" + u1.getUsername(), t1);

        Ticket ticket = getTestHelper().
            findDummyTicket("/" + u1.getUsername(), t1.getId());
        assertNull("Ticket not removed", ticket);

        getTestHelper().removeDummyUser(u1);
    }

    /**
     */
    public void testRemoveInheritedTicket() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        Ticket t1 = getTestHelper().makeAndStoreDummyTicket(u1);

        String path = "/" + u1.getUsername() + "/foobar";
        dao.removeTicket(path, t1);

        Ticket ticket = getTestHelper().
            findDummyTicket("/" + u1.getUsername(), t1.getId());
        assertNull("Ticket not removed", ticket);

        getTestHelper().removeDummyUser(u1);
    }

    /**
     */
    public void testRemoveInheritedTicketNotFound() throws Exception {
        User u1 = getTestHelper().makeAndStoreDummyUser();
        Ticket t1 = getTestHelper().makeDummyTicket();

        String path = "/" + u1.getUsername() + "/foobar";
        // should fail silently
        dao.removeTicket(path, t1);

        getTestHelper().removeDummyUser(u1);
    }
}
