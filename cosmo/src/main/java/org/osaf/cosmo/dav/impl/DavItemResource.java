/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import java.util.Set;

import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;

/**
 * An interface for DAV resources that are backed by Cosmo content (e.g.
 * collections or items).
 */
public interface DavItemResource extends DavResource {

    public Item getItem();

    public void setItem(Item item)
        throws DavException;

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    public void saveTicket(Ticket ticket) throws DavException;

    /**
     * Removes the association between the ticket and this resource
     * and deletes the ticket from persistent storage.
     */
    public void removeTicket(Ticket ticket) throws DavException;

    /**
     * Returns the ticket with the given id on this resource.
     */
    public Ticket getTicket(String id);

    /**
     * Returns all visible tickets (those owned by the currently
     * authenticated user) on this resource, or an empty
     * <code>Set</code> if there are no visible tickets.
     */
    public Set<Ticket> getTickets();
}
