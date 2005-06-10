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
package org.osaf.cosmo.dav;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;

import org.osaf.cosmo.dav.ticket.TicketDavRequest;
import org.osaf.cosmo.model.Ticket;

/**
 * An interface providing resource functionality required by WebDAV
 * extensions implemented by Cosmo.
 */
public interface CosmoDavResource extends DavResource {

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    public void saveTicket(Ticket ticket);

    /**
     * Returns the ticket with the given id associated with this
     * resource.
     */
    public Ticket getTicket(String id);

    /**
     * Returns a resource locator for the named principal.
     */
    public DavResourceLocator getPrincipalLocator(String principal);
}
