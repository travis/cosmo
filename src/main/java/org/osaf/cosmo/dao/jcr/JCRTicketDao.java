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

import java.util.HashSet;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.id.StringIdentifierGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.commons.spring.jcr.JCRCallback;
import org.osaf.commons.spring.jcr.support.JCRDaoSupport;
import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;
import org.osaf.cosmo.model.Ticket;

/**
 * Implementation of TicketDao that operates against a JCR Repository.
 *
 * A ticket is persisted as a child node of the node with which the
 * ticket is associated. Properties never have tickets associated with
 * them. Ticket nodes are of type
 * {@link CosmoJcrConstants#NT_TICKET}.
 */
public class JCRTicketDao extends JCRDaoSupport
    implements TicketDao {
    private static final Log log = LogFactory.getLog(JCRTicketDao.class);

    private StringIdentifierGenerator idGenerator;

    // TicketDao methods

    /**
     * Creates the given ticket in the repository.
     *
     * @param path the absolute JCR path of the resource to which the
     * ticket is to be applied
     * @param ticket the ticket to be saved
     */
    public void createTicket(final String path,
                             final Ticket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(idGenerator.nextStringIdentifier());
        }
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Node parentNode = JCRUtils.findNode(session, path);

                    if (log.isDebugEnabled()) {
                        log.debug("adding node for ticket " +
                                  ticket.getId() + " below " +
                                  parentNode.getPath());
                    }
                    Node ticketNode =
                        parentNode.addNode(CosmoJcrConstants.NN_TICKET,
                                           CosmoJcrConstants.NT_TICKET);
                    ticketNode.setProperty(CosmoJcrConstants.NP_ID,
                                           ticket.getId());
                    ticketNode.setProperty(CosmoJcrConstants.NP_OWNER,
                                           ticket.getOwner());
                    ticketNode.setProperty(CosmoJcrConstants.NP_TIMEOUT,
                                           ticket.getTimeout());
                    ticketNode.setProperty(CosmoJcrConstants.NP_PRIVILEGES,
                                           (String[]) ticket.getPrivileges().
                                           toArray(new String[0]));
                    JCRUtils.setDateValue(ticketNode,
                                          CosmoJcrConstants.NP_CREATED, null);

                    parentNode.save();
                    return null;
                }
            });
    }

    /**
     * Returns all tickets for the node at the given path, or an empty
     * <code>Set</code> if the resource does not have any tickets.
     *
     * String path the absolute JCR path of the ticketed node
     */
    public Set getTickets(final String path) {
        return (Set) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Set tickets = new HashSet();

                    Node resource = JCRUtils.findNode(session, path);
                    NodeIterator i =
                        resource.getNodes(CosmoJcrConstants.NN_TICKET);
                    while (i.hasNext()) {
                        Node child = i.nextNode();
                        tickets.add(nodeToTicket(child));
                    }

                    return tickets;
                }
            });
    }

    /**
     * Returns the identified ticket for the node at the given path
     * (or the nearest existing ancestor), or <code>null</code> if the
     * ticket does not exist. Tickets are inherited, so if the
     * specified node does not have the ticket but an ancestor does,
     * it will still be returned.
     *
     * @param path the absolute JCR path of the ticketed node
     * @param id the id of the ticket unique to the parent node
     */
    public Ticket getTicket(final String path,
                            final String id) {
        return (Ticket) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Node parentNode =
                        JCRUtils.findDeepestExistingNode(session, path);
                    Node ticketNode = findChildTicketNode(parentNode, id);
                    if (ticketNode == null) {
                        return parentNode.getDepth() > 0 ?
                            getTicket(parentNode.getParent().getPath(), id) :
                            null;
                    }
                    return nodeToTicket(ticketNode);
                }
            });
    }

    /**
     * Removes the given ticket from the node at the given path.
     *
     * @param path the absolute JCR path of the ticketed node
     * @param ticket the <code>Ticket</code> to remove
     */
    public void removeTicket(final String path,
                             final Ticket ticket) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Node parentNode =
                        JCRUtils.findDeepestExistingNode(session, path);
                    Node ticketNode = findChildTicketNode(parentNode, ticket);
                    if (ticketNode == null) {
                        if (parentNode.getDepth() == 0) {
                            // this is the root node; the ticket
                            // simply doesn't exist in the original
                            // path
                            return null;
                        }
                        // the ticket might be on an ancestor, so step
                        // up the tree and look for it on the parent
                        removeTicket(parentNode.getParent().getPath(), ticket);
                        return null;
                    }
                    ticketNode.remove();
                    parentNode.save();
                    return null;
                }
            });
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /**
     */
    public void setIdGenerator(StringIdentifierGenerator generator) {
        idGenerator = generator;
    }

    /**
     */
    public Ticket nodeToTicket(Node node)
        throws RepositoryException {
        Ticket ticket = new Ticket();

        ticket.setId(JCRUtils.
                     getStringValue(node, CosmoJcrConstants.NP_ID));
        ticket.setOwner(JCRUtils.
                        getStringValue(node, CosmoJcrConstants.NP_OWNER));
        ticket.setTimeout(JCRUtils.
                          getStringValue(node, CosmoJcrConstants.NP_TIMEOUT));
        Value[] privileges = JCRUtils.
            getValues(node, CosmoJcrConstants.NP_PRIVILEGES);
        for (int i=0; i<privileges.length; i++) {
            ticket.getPrivileges().add(privileges[i].getString());
        }
        ticket.setCreated(JCRUtils.
                          getDateValue(node, CosmoJcrConstants.NP_CREATED));

        return ticket;
    }

    /**
     * Returns the child ticket node for the given node with the given
     * id.
     */
    public Node findChildTicketNode(Node parent, String id)
        throws RepositoryException {
        for (NodeIterator i=parent.getNodes(CosmoJcrConstants.NN_TICKET);
             i.hasNext();) {
            Node child = i.nextNode();
            if (JCRUtils.getStringValue(child, CosmoJcrConstants.NP_ID).
                equals(id)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns the child ticket node for the given node matching the
     * given ticket.
     */
    public Node findChildTicketNode(Node parent, Ticket ticket)
        throws RepositoryException {
        return findChildTicketNode(parent, ticket.getId());
    }
}
