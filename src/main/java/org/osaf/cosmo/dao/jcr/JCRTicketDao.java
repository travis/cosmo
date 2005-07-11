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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.commons.spring.jcr.JCRCallback;
import org.osaf.commons.spring.jcr.support.JCRDaoSupport;
import org.osaf.cosmo.dao.TicketDao;
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
                    Node ticketNode =
                        JCRUtils.findChildTicketNode(parentNode, id);
                    if (ticketNode == null) {
                        return parentNode.getDepth() > 0 ?
                            getTicket(parentNode.getParent().getPath(), id) :
                            null;
                    }
                    return JCRUtils.nodeToTicket(ticketNode);
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
                    Node ticketNode =
                        JCRUtils.findChildTicketNode(parentNode, ticket);
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
}
