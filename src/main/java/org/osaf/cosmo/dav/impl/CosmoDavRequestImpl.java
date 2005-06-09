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
package org.osaf.cosmo.dav.impl;

import org.apache.jackrabbit.webdav.WebdavRequest;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavRequest;
import org.osaf.cosmo.model.Ticket;

/**
 * The standard implementation of {@link CosmoDavRequest}. Wraps a
 * {@link org.apache.jackrabbit.webdav.WebdavRequest}.
 */
public class CosmoDavRequestImpl implements CosmoDavRequest, CosmoDavConstants {
    private static final Logger log =
        Logger.getLogger(CosmoDavRequestImpl.class);

    private Ticket ticket;
    private WebdavRequest wdr;

    /**
     */
    public CosmoDavRequestImpl(WebdavRequest request) {
        wdr = request;
    }

    // TicketDavRequest methods

    /**
     * Return a {@link Ticket} representing the information about a
     * ticket contained in the request.
     *
     * @throws IllegalArgumentException if ticket information exists
     * but is invalid
     */
    public Ticket getTicket() {
        if (ticket == null) {
            ticket = parseTicketRequest();
        }
        return ticket;
    }

    // private methods

    private Ticket parseTicketRequest() {
        Document requestDocument = wdr.getRequestDocument();
        if (requestDocument == null) {
            return null;
        }

        Element root = requestDocument.getRootElement();

        if (! root.getName().equals(ELEMENT_TICKETINFO)) {
            throw new IllegalArgumentException("ticket request missing ticketinfo");
        }
        if (root.getNamespace() == null ||
            ! root.getNamespace().equals(NAMESPACE)) {
            throw new IllegalArgumentException("ticket request contains ticketinfo with missing or incorrect namespace");
        }
        if (root.getChild(ELEMENT_ID, NAMESPACE) != null) {
            throw new IllegalArgumentException("ticket request must not include id");
        }
        if (root.getChild(ELEMENT_OWNER, NAMESPACE) != null) {
            throw new IllegalArgumentException("ticket request must not include owner");
        }

        // XXX: convert to a number of seconds
        String timeout = root.getChildTextNormalize(ELEMENT_TIMEOUT, NAMESPACE);
        if (timeout == null) {
            throw new IllegalArgumentException("ticket request timeout missing or invalid");
        }

        Integer visits = null;
        try {
            String tmp = root.getChildTextNormalize(ELEMENT_VISITS, NAMESPACE);
            if (tmp == null) {
                throw new IllegalArgumentException("ticket request visits missing or invalid");
            }
            visits = tmp.equals(VALUE_INFINITY) ?
                visits = new Integer(Integer.MAX_VALUE) :
                Integer.valueOf(tmp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ticket request contains invalid visits: " + e.getMessage());
        }

        Element privilege = root.getChild(ELEMENT_PRIVILEGE, NAMESPACE);
        if (privilege == null) {
            throw new IllegalArgumentException("ticket request missing privileges");
        }
        if (privilege.getChild(ELEMENT_READ, NAMESPACE) == null &&
            privilege.getChild(ELEMENT_WRITE, NAMESPACE) == null) {
            throw new IllegalArgumentException("ticket request contains empty privileges");
        }

        Ticket ticket = new Ticket();
        ticket.setTimeout(timeout);
        ticket.setVisits(visits);
        if (privilege.getChild(ELEMENT_READ, NAMESPACE) != null) {
            ticket.setRead(Boolean.TRUE);
        }
        if (privilege.getChild(ELEMENT_WRITE, NAMESPACE) != null) {
            ticket.setWrite(Boolean.TRUE);
        }

        return ticket;
    }
}
