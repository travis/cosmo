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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.observation.SubscriptionInfo;
import org.apache.jackrabbit.webdav.ordering.OrderPatch;
import org.apache.jackrabbit.webdav.ordering.Position;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.transaction.TransactionInfo;
import org.apache.jackrabbit.webdav.version.LabelInfo;
import org.apache.jackrabbit.webdav.version.MergeInfo;
import org.apache.jackrabbit.webdav.version.OptionsInfo;
import org.apache.jackrabbit.webdav.version.UpdateInfo;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

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
public class CosmoDavRequestImpl implements CosmoDavRequest, DavConstants {
    private static final Logger log =
        Logger.getLogger(CosmoDavRequestImpl.class);

    private Ticket ticket;
    private WebdavRequest webdavRequest;

    /**
     */
    public CosmoDavRequestImpl(WebdavRequest request) {
        webdavRequest = request;
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

    /**
     */
    public WebdavRequest getWebdavRequest() {
        return webdavRequest;
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
        Document requestDocument = webdavRequest.getRequestDocument();
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

    // WebdavRequest methods

    public void setDavSession(DavSession session) {
        webdavRequest.setDavSession(session);
    }

    public DavSession getDavSession() {
        return webdavRequest.getDavSession();
    }

    public DavResourceLocator getRequestLocator() {
        return webdavRequest.getRequestLocator();
    }

    public DavResourceLocator getDestinationLocator() {
        return webdavRequest.getDestinationLocator();
    }

    public boolean isOverwrite() {
        return webdavRequest.isOverwrite();
    }

    public int getDepth(int defaultValue) {
        return webdavRequest.getDepth(defaultValue);
    }

    public int getDepth() {
        return webdavRequest.getDepth();
    }

    public long getTimeout() {
        return webdavRequest.getTimeout();
    }

    public String getLockToken() {
        return webdavRequest.getLockToken();
    }

    public Document getRequestDocument() {
        return webdavRequest.getRequestDocument();
    }

    public int getPropFindType() {
        return webdavRequest.getPropFindType();
    }

    public DavPropertyNameSet getPropFindProperties() {
        return webdavRequest.getPropFindProperties();
    }

    public DavPropertySet getPropPatchSetProperties() {
        return webdavRequest.getPropPatchSetProperties();
    }

    public DavPropertyNameSet getPropPatchRemoveProperties() {
        return webdavRequest.getPropPatchRemoveProperties();
    }

    public LockInfo getLockInfo() {
        return webdavRequest.getLockInfo();
    }

    public boolean matchesIfHeader(DavResource resource) {
        return webdavRequest.matchesIfHeader(resource);
    }

    public boolean matchesIfHeader(String href, String token, String eTag) {
        return webdavRequest.matchesIfHeader(href, token, eTag);
    }
    
    public String getTransactionId() {
        return webdavRequest.getTransactionId();
    }

    public TransactionInfo getTransactionInfo() {
        return webdavRequest.getTransactionInfo();
    }

    public String getSubscriptionId() {
        return webdavRequest.getSubscriptionId();
    }

    public SubscriptionInfo getSubscriptionInfo() {
        return webdavRequest.getSubscriptionInfo();
    }

    public String getOrderingType() {
        return webdavRequest.getOrderingType();
    }

    public Position getPosition() {
        return webdavRequest.getPosition();
    }

    public OrderPatch getOrderPatch() {
        return webdavRequest.getOrderPatch();
    }

    public String getLabel() {
        return webdavRequest.getLabel();
    }

    public LabelInfo getLabelInfo() {
        return webdavRequest.getLabelInfo();
    }

    public MergeInfo getMergeInfo() {
        return webdavRequest.getMergeInfo();
    }

    public UpdateInfo getUpdateInfo() {
        return webdavRequest.getUpdateInfo();
    }

    public ReportInfo getReportInfo() {
        return webdavRequest.getReportInfo();
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

    public OptionsInfo getOptionsInfo() {
        return webdavRequest.getOptionsInfo();
    }

    public String getAuthType() {
        return webdavRequest.getAuthType();
    }

    public Cookie[] getCookies() {
        return webdavRequest.getCookies();
    }

    public long getDateHeader(String s) {
        return webdavRequest.getDateHeader(s);
    }

    public String getHeader(String s) {
        return webdavRequest.getHeader(s);
    }

    public Enumeration getHeaders(String s) {
        return webdavRequest.getHeaders(s);
    }

    public Enumeration getHeaderNames() {
        return webdavRequest.getHeaderNames();
    }

    public int getIntHeader(String s) {
        return webdavRequest.getIntHeader(s);
    }

    public String getMethod() {
        return webdavRequest.getMethod();
    }

    public String getPathInfo() {
        return webdavRequest.getPathInfo();
    }

    public String getPathTranslated() {
        return webdavRequest.getPathTranslated();
    }

    public String getContextPath() {
        return webdavRequest.getContextPath();
    }

    public String getQueryString() {
        return webdavRequest.getQueryString();
    }

    public String getRemoteUser() {
        return webdavRequest.getRemoteUser();
    }

    public boolean isUserInRole(String s) {
        return webdavRequest.isUserInRole(s);
    }

    public Principal getUserPrincipal() {
        return webdavRequest.getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return webdavRequest.getRequestedSessionId();
    }

    public String getRequestURI() {
        return webdavRequest.getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return webdavRequest.getRequestURL();
    }

    public String getServletPath() {
        return webdavRequest.getServletPath();
    }

    public HttpSession getSession(boolean b) {
        return webdavRequest.getSession(b);
    }

    public HttpSession getSession() {
        return webdavRequest.getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return webdavRequest.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return webdavRequest.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return webdavRequest.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl() {
        return webdavRequest.isRequestedSessionIdFromUrl();
    }

    public Object getAttribute(String s) {
        return webdavRequest.getAttribute(s);
    }

    public Enumeration getAttributeNames() {
        return webdavRequest.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return webdavRequest.getCharacterEncoding();
    }

    public void setCharacterEncoding(String s)
        throws UnsupportedEncodingException {
        webdavRequest.setCharacterEncoding(s);
    }

    public int getContentLength() {
        return webdavRequest.getContentLength();
    }

    public String getContentType() {
        return webdavRequest.getContentType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return webdavRequest.getInputStream();
    }

    public String getParameter(String s) {
        return webdavRequest.getParameter(s);
    }

    public Enumeration getParameterNames() {
        return webdavRequest.getParameterNames();
    }

    public String[] getParameterValues(String s) {
        return webdavRequest.getParameterValues(s);
    }

    public Map getParameterMap() {
        return webdavRequest.getParameterMap();
    }

    public String getProtocol() {
        return webdavRequest.getProtocol();
    }

    public String getScheme() {
        return webdavRequest.getScheme();
    }

    public String getServerName() {
        return webdavRequest.getServerName();
    }

    public int getServerPort() {
        return webdavRequest.getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        return webdavRequest.getReader();
    }

    public String getRemoteAddr() {
        return webdavRequest.getRemoteAddr();
    }

    public String getRemoteHost() {
        return webdavRequest.getRemoteHost();
    }

    public void setAttribute(String s, Object o) {
        webdavRequest.setAttribute(s, o);
    }

    public void removeAttribute(String s) {
       webdavRequest.removeAttribute(s);
    }

    public Locale getLocale() {
        return webdavRequest.getLocale();
    }

    public Enumeration getLocales() {
        return webdavRequest.getLocales();
    }

    public boolean isSecure() {
        return webdavRequest.isSecure();
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return webdavRequest.getRequestDispatcher(s);
    }

    public String getRealPath(String s) {
        return webdavRequest.getRealPath(s);
    }
}
