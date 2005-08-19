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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Locale;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.observation.EventDiscovery;
import org.apache.jackrabbit.webdav.observation.Subscription;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.model.Ticket;

/**
 * The standard implementation of {@link CosmoDavResponse}. Wraps a
 * {@link org.apache.jackrabbit.webdav.WebdavResponse}.
 */
public class CosmoDavResponseImpl implements CosmoDavResponse {
    private static final Logger log =
        Logger.getLogger(CosmoDavResponseImpl.class);

    private WebdavResponse webdavResponse;

    /**
     */
    public CosmoDavResponseImpl(WebdavResponse response) {
        webdavResponse = response;
    }

    // TicketDavResponse methods

    /**
     * Send the <code>ticketdiscovery</code> response to a
     * <code>MKTICKET</code> request.
     *
     * @param resource the resource on which the ticket was created
     * @param ticketId the id of the newly created ticket
     */
    public void sendMkTicketResponse(CosmoDavResource resource,
                                     String ticketId)
        throws DavException, IOException {
        webdavResponse.setHeader(CosmoDavConstants.HEADER_TICKET, ticketId);

        Element prop = new Element(CosmoDavConstants.ELEMENT_PROP,
                                   DavConstants.NAMESPACE);
        prop.addNamespaceDeclaration(CosmoDavConstants.NAMESPACE_TICKET);

        DavProperty ticketdiscovery =
            resource.getProperties().get(CosmoDavPropertyName.TICKETDISCOVERY);
        prop.addContent(ticketdiscovery.toXml());

        sendXmlResponse(new Document(prop), SC_OK);
    }

    /**
     * Send the response to a <code>DELTICKET</code> request.
     *
     * @param resource the resource on which the ticket was deleted
     * @param ticketId the id of the deleted ticket
     */
    public void sendDelTicketResponse(CosmoDavResource resource,
                                      String ticketId)
        throws DavException, IOException {
        setStatus(SC_NO_CONTENT);
    }

    // WebdavResponse methods

    public void sendErrorResponse(DavException exception) throws IOException {
        webdavResponse.sendErrorResponse(exception);
    }

    public void sendMultiStatusResponse(MultiStatus multistatus)
        throws IOException {
        webdavResponse.sendMultiStatusResponse(multistatus);
    }

    public void sendLockResponse(ActiveLock lock) throws IOException {
        webdavResponse.sendLockResponse(lock);
    }

    public void sendRefreshLockResponse(ActiveLock[] locks) throws IOException {
        webdavResponse.sendRefreshLockResponse(locks);
    }

    public void sendXmlResponse(Document xmlDoc, int status)
        throws IOException {
        webdavResponse.sendXmlResponse(xmlDoc, status);
    }

    public void sendSubscriptionResponse(Subscription subscription)
        throws IOException {
        webdavResponse.sendSubscriptionResponse(subscription);
    }

    public void sendPollResponse(EventDiscovery eventDiscovery)
        throws IOException {
        webdavResponse.sendPollResponse(eventDiscovery);
    }

    public void addCookie(Cookie cookie) {
        webdavResponse.addCookie(cookie);
    }

    public boolean containsHeader(String s) {
        return webdavResponse.containsHeader(s);
    }

    public String encodeURL(String s) {
        return webdavResponse.encodeURL(s);
    }

    public String encodeRedirectURL(String s) {
        return webdavResponse.encodeRedirectURL(s);
    }

    public String encodeUrl(String s) {
        return webdavResponse.encodeUrl(s);
    }

    public String encodeRedirectUrl(String s) {
        return webdavResponse.encodeRedirectURL(s);
    }

    public void sendError(int i, String s) throws IOException {
        webdavResponse.sendError(i, s);
    }

    public void sendError(int i) throws IOException {
        webdavResponse.sendError(i);
    }

    public void sendRedirect(String s) throws IOException {
        webdavResponse.sendRedirect(s);
    }

    public void setCharacterEncoding(String encoding) {
        webdavResponse.setCharacterEncoding(encoding);
    }

    public void setDateHeader(String s, long l) {
        webdavResponse.setDateHeader(s, l);
    }

    public void addDateHeader(String s, long l) {
        webdavResponse.addDateHeader(s, l);
    }

    public void setHeader(String s, String s1) {
        webdavResponse.setHeader(s, s1);
    }

    public void addHeader(String s, String s1) {
        webdavResponse.addHeader(s, s1);
    }

    public void setIntHeader(String s, int i) {
        webdavResponse.setIntHeader(s, i);
    }

    public void addIntHeader(String s, int i) {
        webdavResponse.addIntHeader(s, i);
    }

    public void setStatus(int i) {
        webdavResponse.setStatus(i);
    }

    public void setStatus(int i, String s) {
        webdavResponse.setStatus(i, s);
    }

    public String getCharacterEncoding() {
        return webdavResponse.getCharacterEncoding();
    }

    public String getContentType() {
        return webdavResponse.getContentType();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return webdavResponse.getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
        return webdavResponse.getWriter();
    }

    public void setContentLength(int i) {
        webdavResponse.setContentLength(i);
    }

    public void setContentType(String s) {
        webdavResponse.setContentType(s);
    }

    public void setBufferSize(int i) {
        webdavResponse.setBufferSize(i);
    }

    public int getBufferSize() {
        return webdavResponse.getBufferSize();
    }

    public void flushBuffer() throws IOException {
        webdavResponse.flushBuffer();
    }

    public void resetBuffer() {
        webdavResponse.resetBuffer();
    }

    public boolean isCommitted() {
        return webdavResponse.isCommitted();
    }

    public void reset() {
        webdavResponse.reset();
    }

    public void setLocale(Locale locale) {
        webdavResponse.setLocale(locale);
    }

    public Locale getLocale() {
        return webdavResponse.getLocale();
    }
}
