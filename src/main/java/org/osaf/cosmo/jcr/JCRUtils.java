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
package org.osaf.cosmo.jcr;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.osaf.cosmo.model.Ticket;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Utilities for working with JCR in Cosmo.
 */
public class JCRUtils {

    /**
     * Adds a child node to the given node with properties set from
     * the given ticket.
     *
     * @param parentNode the node to which the ticket node will be
     * added
     * @param ticket the ticket from which the node's properties will
     * be set
     */
    public static Node ticketToNode(Node parentNode, Ticket ticket)
        throws RepositoryException {
        Node ticketNode = parentNode.addNode(CosmoJcrConstants.NN_TICKET,
                                             CosmoJcrConstants.NT_TICKET);
        ticketNode.setProperty(CosmoJcrConstants.NP_ID, ticket.getId());
        ticketNode.setProperty(CosmoJcrConstants.NP_OWNER, ticket.getOwner());
        ticketNode.setProperty(CosmoJcrConstants.NP_TIMEOUT,
                               ticket.getTimeout());
        ticketNode.setProperty(CosmoJcrConstants.NP_PRIVILEGES,
                               (String[]) ticket.getPrivileges().
                               toArray(new String[0]));
        ticketNode.setProperty(CosmoJcrConstants.NP_CREATED,
                               Calendar.getInstance());
        return ticketNode;
    }

    /**
     * Creates and populates a ticket representing the given node.
     */
    public static Ticket nodeToTicket(Node node)
        throws RepositoryException {
        Ticket ticket = new Ticket();
        ticket.setId(getStringValue(node, CosmoJcrConstants.NP_ID));
        ticket.setOwner(getStringValue(node, CosmoJcrConstants.NP_OWNER));
        ticket.setTimeout(getStringValue(node, CosmoJcrConstants.NP_TIMEOUT));
        Value[] privileges = getValues(node, CosmoJcrConstants.NP_PRIVILEGES);
        for (int i=0; i<privileges.length; i++) {
            ticket.getPrivileges().add(privileges[i].getString());
        }
        ticket.setCreated(getDateValue(node, CosmoJcrConstants.NP_CREATED));
        return ticket;
    }

    /**
     * Returns the child ticket node for the given node with the given
     * id.
     */
    public static Node findChildTicketNode(Node parentNode, String id)
        throws RepositoryException {
        for (NodeIterator i=parentNode.getNodes(CosmoJcrConstants.NN_TICKET);
             i.hasNext();) {
            Node childNode = i.nextNode();
            if (getStringValue(childNode, CosmoJcrConstants.NP_ID).equals(id)) {
                return childNode;
            }
        }
        return null;
    }

    /**
     * Returns the child ticket node for the given node matching the
     * given ticket.
     */
    public static Node findChildTicketNode(Node parentNode, Ticket ticket)
        throws RepositoryException {
        return findChildTicketNode(parentNode, ticket.getId());
    }

    /**
     * Returns a <code>Set</code> of tickets representing the child
     * ticket nodes of the given node owned by the given owner.
     */
    public static Set findTickets(Node node, String owner)
        throws RepositoryException {
        Set tickets = new HashSet();
        for (NodeIterator i=node.getNodes(CosmoJcrConstants.NN_TICKET);
             i.hasNext();) {
            Node childNode = i.nextNode();
            // child node must be owned by the named owner
            if (! getStringValue(childNode, CosmoJcrConstants.NP_OWNER).
                equals(owner)) {
                continue;
            }
            tickets.add(nodeToTicket(childNode));
        }
        return tickets;
    }

    /**
     * Finds the deepest existing node on the given path.
     */
    public static Node findDeepestExistingNode(Session session, String path)
        throws RepositoryException {
        // try for the deepest node first
        try {
            Item item = session.getItem(path);
            if (! item.isNode()) {
                throw new InvalidDataAccessResourceUsageException("item at path " + path + " is not a node");
            }
            return (Node) item;
        } catch (PathNotFoundException e) {
            // will need to step down through the path one by one
        }

        Node node = session.getRootNode();
        if (path.equals("/")) {
            return node;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] names = path.split("/");
        Node parentNode = null;
        for (int i=0; i<names.length; i++) {
            try {
                parentNode = node;
                node = parentNode.getNode(names[i]);
            } catch (PathNotFoundException e) {
                // previous one was the last existing
                node = parentNode;
                break;
            }
        }

        return node;
    }

    /**
     */
    public static Date getDateValue(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getDate().getTime();
    }

    /**
     */
    public static String getStringValue(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getString();
    }

    /**
     */
    public static Value[] getValues(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getValues();
    }
}
