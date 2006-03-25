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
package org.osaf.cosmo.dao.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.model.Ticket;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Mock implementation of {@link TicketDao} useful for testing.
 */
public class MockTicketDao implements TicketDao {

    private HashMap items;

    /**
     */
    public MockTicketDao() {
        items = new HashMap();
    }

    // TicketDao methods

    /**
     * Creates the given ticket in the repository.
     *
     * @param path the repository path of the resource to which the
     * ticket is to be applied
     * @param ticket the ticket to be saved
     */
    public void createTicket(String path,
                             Ticket ticket) {
        Set tickets = findTickets(path);
        tickets.add(ticket);
    }

    /**
     * Returns all tickets for the node at the given path, or an empty
     * <code>Set</code> if the resource does not have any tickets.
     *
     * String path the absolute JCR path of the ticketed node
     * be returned
     */
    public Set getTickets(String path) {
        return findTickets(path);
    }

    /**
     * Returns the identified ticket for the item at the given path,
     * or <code>null</code> if the ticket does not exist. Tickets are
     * inherited, so if the specified item does not have the ticket
     * but an ancestor does, it will still be returned.
     *
     * @param path the path of the ticketed item unique to the repository
     * @param id the id of the ticket unique to the parent item
     */
    public Ticket getTicket(String path,
                            String id) {
        Set tickets = findTickets(path);
        for (Iterator i=tickets.iterator(); i.hasNext();) {
            Ticket ticket = (Ticket) i.next();
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        // the ticket might be on an ancestor, so check the parent
        if (! path.equals("/")) {
            String parentPath = path.substring(0, path.lastIndexOf("/")-1);;
            return getTicket(parentPath, id);
        }
        // this is the root item; the ticket simply doesn't exist
        // anywhere in the given path
        return null;
    }

    /**
     * Removes the assocation between the ticket and the item at the
     * given path and deletes the ticket from persistent storage.
     *
     * @param path the path of the ticketed item unique to the
     * repository
     * @param ticket the <code>Ticket</code> to remove
     */
    public void removeTicket(String path,
                             Ticket ticket) {
        Set tickets = findTickets(path);
        if (tickets.contains(ticket)) {
            tickets.remove(ticket);
            return;
        }
        // the ticket might be on an ancestor, so check the parent
        if (! path.equals("/")) {
            String parentPath = path.substring(0, path.lastIndexOf("/")-1);;
            removeTicket(parentPath, ticket);
            return;
        }
        // this is the root item; the ticket simply doesn't exist
        // anywhere in the given path
        return;
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }

    // our methods

    private Set findTickets(String path) {
        Set tickets = (Set) items.get(path);
        if (tickets == null) {
            tickets = new HashSet();
            items.put(path, tickets);
        }
        return tickets;
    }
}
