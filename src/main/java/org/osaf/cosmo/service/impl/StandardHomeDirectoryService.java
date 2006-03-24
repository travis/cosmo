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
package org.osaf.cosmo.service.impl;

import org.osaf.cosmo.dao.HomeDirectoryDao;
import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.service.HomeDirectoryService;
import org.osaf.cosmo.model.Resource;
import org.osaf.cosmo.model.Ticket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Standard implementation of {@link HomeDirectoryService}.
 */
public class StandardHomeDirectoryService implements HomeDirectoryService {
    private static final Log log =
        LogFactory.getLog(StandardHomeDirectoryService.class);

    private HomeDirectoryDao homeDirectoryDao;
    private TicketDao ticketDao;

    // HomeDirectoryService methods

    /**
     * Returns the resource at the specified client path.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */
    public Resource getResource(String path) {
        return homeDirectoryDao.getResource(path);
    }

    /**
     * Removes the resource at the specified client path.
     */
    public void removeResource(String path) {
        homeDirectoryDao.removeResource(path);
    }

    /**
     * Creates a ticket on the resource at the specified client path.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */
    public void grantTicket(String path, Ticket ticket) {
        ticketDao.createTicket(path, ticket);
    }

    /**
     * Removes the identified ticket from the resource at the
     * specified client path.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */

    public void revokeTicket(String path, String id) {
        ticketDao.removeTicket(path, getResource(path).getTicket(id));
    }

    // Service methods

    /**
     * Initializes the service, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        // does nothing
    }

    /**
     * Readies the service for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /**
     */
    public HomeDirectoryDao getHomeDirectoryDao() {
        return this.homeDirectoryDao;
    }

    /**
     */
    public void setHomeDirectoryDao(HomeDirectoryDao homeDirectoryDao) {
        this.homeDirectoryDao = homeDirectoryDao;
    }

    /**
     */
    public TicketDao getTicketDao() {
        return this.ticketDao;
    }

    /**
     */
    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }
}
