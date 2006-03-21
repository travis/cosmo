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

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.repository.SchemaConstants;

/**
 * Utility class that converts between {@link Ticket}s and
 * {@link javax.jcr.Node}s.
 */
public class JcrTicketMapper implements SchemaConstants {
    private static final Log log = LogFactory.getLog(JcrTicketMapper.class);

    /**
     * Returns a new instance of <code>Ticket</code> populated from a
     * ticket node.
     */
    public static Ticket nodeToTicket(Node node)
        throws RepositoryException {
        Ticket ticket = new Ticket();

        ticket.setId(node.getProperty(NP_TICKET_ID).getString());
        ticket.setOwner(node.getProperty(NP_TICKET_OWNER).getString());
        ticket.setTimeout(node.getProperty(NP_TICKET_TIMEOUT).getString());
        Value[] privileges = node.getProperty(NP_TICKET_PRIVILEGES).getValues();
        for (int i=0; i<privileges.length; i++) {
            ticket.getPrivileges().add(privileges[i].getString());
        }
        ticket.setCreated(node.getProperty(NP_TICKET_CREATED).
                          getDate().getTime());

        return ticket;
    }

    /**
     * Copies the properties of a <code>Ticket</code> into a ticket
     * node.
     */
    public static void ticketToNode(Ticket ticket,
                                    Node node)
        throws RepositoryException {
        node.setProperty(NP_TICKET_ID, ticket.getId());
        node.setProperty(NP_TICKET_OWNER, ticket.getOwner());
        node.setProperty(NP_TICKET_TIMEOUT, ticket.getTimeout());
        node.setProperty(NP_TICKET_PRIVILEGES,
                         (String[]) ticket.getPrivileges().
                         toArray(new String[0]));
        node.setProperty(NP_TICKET_CREATED, Calendar.getInstance());
    }
}
