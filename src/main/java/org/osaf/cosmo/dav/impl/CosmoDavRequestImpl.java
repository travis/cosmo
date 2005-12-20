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

import javax.servlet.http.HttpServletRequest;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavRequest;
import org.osaf.cosmo.model.Ticket;

/**
 * Extends {@link org.apache.jackrabbit.webdav.WebdavRequest}. and
 * implements {@link CosmoDavRequest}.
 */
public class CosmoDavRequestImpl extends WebdavRequestImpl
    implements CosmoDavRequest, DavConstants {
    private static final Logger log =
        Logger.getLogger(CosmoDavRequestImpl.class);

    private Ticket ticket;

    /**
     */
    public CosmoDavRequestImpl(HttpServletRequest request,
                               DavLocatorFactory factory) {
        super(request, factory);
    }

    // CosmoDavRequest methods

    /**
     * Return the base URL for this request (including scheme, server
     * name, and port if not the scheme's default port).
     */
    public String getBaseUrl() {
        StringBuffer buf = new StringBuffer();
        buf.append(getScheme());
        buf.append("://");
        buf.append(getServerName());
        if ((isSecure() && getServerPort() != 443) ||
            getServerPort() != 80) {
            buf.append(":");
            buf.append(getServerPort());
        }
        return buf.toString();
    }

    // TicketDavRequest methods

    /**
     * Return a {@link Ticket} representing the information about a
     * ticket to be created by a <code>MKTICKET</code> request.
     *
     * @throws IllegalArgumentException if there is no ticket
     * information in the request or if the ticket information exists
     * but is invalid
     */
    public Ticket getTicketInfo() {
        if (ticket == null) {
            ticket = parseTicketRequest();
        }
        return ticket;
    }

    /**
     * Return the ticket id included in this request, if any. If
     * different ticket ids are included in the headers and URL, the
     * one from the URL is used.
     */
    public String getTicketId() {
        String ticketId = getParameter(CosmoDavConstants.PARAM_TICKET);
        if (ticketId == null) {
            ticketId = getHeader(CosmoDavConstants.HEADER_TICKET);
        }
        return ticketId;
    }

    // private methods

    private Ticket parseTicketRequest() {
        Document requestDocument = getRequestDocument();
        if (requestDocument == null) {
            throw new IllegalArgumentException("ticket request missing body");
        }

        Element root = requestDocument.getRootElement();

        if (! root.getName().equals(CosmoDavConstants.ELEMENT_TICKETINFO)) {
            throw new IllegalArgumentException("ticket request missing ticketinfo");
        }
        if (root.getNamespace() == null ||
            ! root.getNamespace().equals(CosmoDavConstants.NAMESPACE_TICKET)) {
            throw new IllegalArgumentException("ticket request contains ticketinfo with missing or incorrect namespace");
        }
        if (root.getChild(CosmoDavConstants.ELEMENT_ID,
                          CosmoDavConstants.NAMESPACE_TICKET) != null) {
            throw new IllegalArgumentException("ticket request must not include id");
        }
        if (root.getChild(CosmoDavConstants.ELEMENT_OWNER,
                          CosmoDavConstants.NAMESPACE_TICKET) != null) {
            throw new IllegalArgumentException("ticket request must not include owner");
        }

        String timeout = root.
            getChildTextNormalize(CosmoDavConstants.ELEMENT_TIMEOUT,
                                  CosmoDavConstants.NAMESPACE_TICKET);
        if (timeout == null) {
            timeout = CosmoDavConstants.VALUE_INFINITE;
        }

        // visit limits are not supported

        Element privilege =
            root.getChild(CosmoDavConstants.ELEMENT_PRIVILEGE,
                          DavConstants.NAMESPACE);
        if (privilege == null) {
            throw new IllegalArgumentException("ticket request missing privileges");
        }
        if (privilege.getChild(CosmoDavConstants.ELEMENT_READ,
                               DavConstants.NAMESPACE) == null &&
            privilege.getChild(CosmoDavConstants.ELEMENT_WRITE,
                               DavConstants.NAMESPACE) == null) {
            throw new IllegalArgumentException("ticket request contains empty or invalid privileges");
        }

        Ticket ticket = new Ticket();
        ticket.setTimeout(timeout);
        if (privilege.getChild(CosmoDavConstants.ELEMENT_READ,
                               DavConstants.NAMESPACE) != null) {
            ticket.getPrivileges().add(CosmoDavConstants.PRIVILEGE_READ);
        }
        if (privilege.getChild(CosmoDavConstants.ELEMENT_WRITE,
                               DavConstants.NAMESPACE) != null) {
            ticket.getPrivileges().add(CosmoDavConstants.PRIVILEGE_WRITE);
        }

        return ticket;
    }

    /**
     * This is the Cosmo specific report handling.
     * 
     * TODO Eventually this will be punted up into jackrabbit.
     * 
     * @return
     */
    public org.osaf.cosmo.dav.report.ReportInfo getCosmoReportInfo() {
        org.osaf.cosmo.dav.report.ReportInfo rInfo = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            rInfo = new org.osaf.cosmo.dav.report.ReportInfo(requestDocument
                    .getRootElement(), getDepth(DEPTH_0), getDavSession());
        }
        return rInfo;
    }
}
