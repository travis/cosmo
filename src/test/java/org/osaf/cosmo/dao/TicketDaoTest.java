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
package org.osaf.cosmo.dao;

import java.util.Set;

import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO Test Case for {@link TicketDao}.
 *
 * @author Brian Moseley
 */
public class TicketDaoTest extends BaseCoreTestCase {
    private static final Log log = LogFactory.getLog(TicketDaoTest.class);

    private TicketDao dao;

    public void testCRDTicket() throws Exception {
        User user = TestHelper.makeDummyUser();
        String path = "/";

        // make a ticket
        Ticket ticket = TestHelper.makeDummyTicket();
        ticket.setOwner(user.getUsername());

        if (log.isDebugEnabled()) {
            log.debug("creating ticket on " + path);
        }
        dao.createTicket(path, ticket);

        // make another ticket
        Ticket ticket2 = TestHelper.makeDummyTicket();
        ticket2.setOwner(user.getUsername());

        if (log.isDebugEnabled()) {
            log.debug("creating another ticket on " + path);
        }
        dao.createTicket(path, ticket2);

        // get all of the node's tickets and make sure both of the
        // ones we created are there
        if (log.isDebugEnabled()) {
            log.debug("getting tickets on " + path);
        }
        Set tickets = dao.getTickets(path);
        assertTrue(tickets.contains(ticket));
        assertTrue(tickets.contains(ticket2));
        assertEquals(tickets.size(), 2);

        // get a ticket by id
        if (log.isDebugEnabled()) {
            log.debug("getting ticket " + ticket.getId() + " on " + path);
        }
        Ticket ticket3 = dao.getTicket(path, ticket.getId());
        assertNotNull(ticket3);
        assertEquals(ticket, ticket3);

        // remove a ticket
        if (log.isDebugEnabled()) {
            log.debug("removing ticket " + ticket.getId() + " on " + path);
        }
        dao.removeTicket(path, ticket);

        // get all of the tickets again and make sure the removed one
        // is gone
        tickets = dao.getTickets(path);
        assertTrue(! tickets.contains(ticket));
        assertTrue(tickets.contains(ticket2));
        assertEquals(tickets.size(), 1);

        // get the removed ticket by id again and make sure it's gone
        ticket3 = dao.getTicket(path, ticket.getId());
        assertNull(ticket3);
    }

    public void setTicketDao(TicketDao ticketDao) {
        dao = ticketDao;
    }
}
