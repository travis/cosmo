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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.commons.spring.jcr.JCRCallback;
import org.osaf.commons.spring.jcr.support.JCRDaoSupport;
import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.jcr.JCRUtils;
import org.osaf.cosmo.model.Ticket;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

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
     * Returns the identified ticket for the item at the given path,
     * or <code>null</code> if the ticket does not exist.
     *
     * @param path the absolute JCR path of the ticketed item
     * @param id the id of the ticket unique to the parent item
     *
     * @throws DataRetrievalFailureException if either the item or the
     * ticket are not found
     * @throws InvalidDataAccessResourceException if the parent item
     * is not a node
     */
    public Ticket getTicket(final String path, final String id) {
        return (Ticket) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    Item parentItem = session.getItem(path);
                    if (! parentItem.isNode()) {
                        throw new InvalidDataAccessResourceUsageException("item at path " + path + " is not a node and therefore cannot have a ticket");
                    }
                    Node parentNode = (Node) parentItem;
                    try {
                        return JCRUtils.nodeToTicket(parentNode.getNode(id));
                    } catch (PathNotFoundException e) {
                        return null;
                    }
                }
            });
    }
}
