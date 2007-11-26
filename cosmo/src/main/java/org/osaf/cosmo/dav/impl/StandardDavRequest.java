/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.util.EntityTag;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.dav.BadGatewayException;
import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavRequest;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.DavResourceLocatorFactory;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.UnsupportedMediaTypeException;
import org.osaf.cosmo.dav.acl.AclConstants;
import org.osaf.cosmo.dav.acl.DavPrivilege;
import org.osaf.cosmo.dav.acl.DavPrivilegeSet;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.InvalidCalendarDataException;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.osaf.cosmo.dav.property.StandardDavProperty;
import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.util.BufferedServletInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 */
public class StandardDavRequest extends WebdavRequestImpl
    implements DavRequest, ExtendedDavConstants, AclConstants,
    CaldavConstants, TicketConstants {
    private static final Log log =
        LogFactory.getLog(StandardDavRequest.class);
    private static final MimeType APPLICATION_XML =
        registerMimeType("application/xml");
    private static final MimeType TEXT_XML = registerMimeType("text/xml");

    private static final MimeType registerMimeType(String s) {
        try {
            return new MimeType(s);
        } catch (Exception e) {
            throw new RuntimeException("Can't register MIME type " + s, e);
        }
    }

    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;
    private DavPropertySet proppatchSet;
    private DavPropertyNameSet proppatchRemove;
    private DavPropertySet mkcalendarSet;
    private Ticket ticket;
    private ReportInfo reportInfo;
    private boolean bufferRequestContent = false;
    private long bufferedContentLength = -1;
    private DavResourceLocatorFactory locatorFactory;
    private DavResourceLocator locator;
    private DavResourceLocator destinationLocator;
    private EntityFactory entityFactory;

    public StandardDavRequest(HttpServletRequest request,
                              DavResourceLocatorFactory factory,
                              EntityFactory entityFactory) {
        this(request, factory, entityFactory, false);
    }

    public StandardDavRequest(HttpServletRequest request,
                              DavResourceLocatorFactory factory,
                              EntityFactory entityFactory,
                              boolean bufferRequestContent) {
        super(request, null);
        this.locatorFactory = factory;
        this.bufferRequestContent = bufferRequestContent;
        this.entityFactory = entityFactory;
    }

    // DavRequest methods

    public EntityTag[] getIfMatch() {
      return EntityTag.parseTags(getHeader("If-Match"));
    }

    public Date getIfModifiedSince() {
        long value = getDateHeader("If-Modified-Since");
        return value != -1 ? new Date(value) : null;
    }

    public EntityTag[] getIfNoneMatch() {
      return EntityTag.parseTags(getHeader("If-None-Match"));
    }

    public Date getIfUnmodifiedSince() {
        long value = getDateHeader("If-Unmodified-Since");
        return value != -1 ? new Date(value) : null;
    }

    public int getPropFindType()
        throws DavException {
        if (propfindProps == null)
            parsePropFindRequest();
        return propfindType;
    }

    public DavPropertyNameSet getPropFindProperties()
        throws DavException {
        if (propfindProps == null)
            parsePropFindRequest();
        return propfindProps;
    }

    public DavPropertySet getProppatchSetProperties()
        throws DavException {
        if (proppatchSet == null)
            parsePropPatchRequest();
        return proppatchSet;
    }

    public DavPropertyNameSet getProppatchRemoveProperties()
        throws DavException {
        if (proppatchRemove == null)
            parsePropPatchRequest();
        return proppatchRemove;
    }

    public DavResourceLocator getResourceLocator() {
        if (locator == null) {
            URL context = null;
            try {
                String basePath = getContextPath() + getServletPath();
                context = new URL(getScheme(), getServerName(),
                                  getServerPort(), basePath);

                locator = locatorFactory.
                    createResourceLocatorByUri(context, getRequestURI());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return locator;
    }

    public DavResourceLocator getDestinationResourceLocator()
        throws DavException {
        if (destinationLocator != null)
            return destinationLocator;

        String destination = getHeader(HEADER_DESTINATION);
        if (destination == null)
            return null;

        URL context = ((DavResourceLocator)getResourceLocator()).getContext();

        destinationLocator =
            locatorFactory.createResourceLocatorByUri(context, destination);

        return destinationLocator;
    }

    // CaldavRequest methods

    public DavPropertySet getMkCalendarSetProperties()
        throws DavException {
        if (mkcalendarSet == null)
            mkcalendarSet = parseMkCalendarRequest();
        return mkcalendarSet;
    }

    // TicketDavRequest methods

    public Ticket getTicketInfo()
        throws DavException {
        if (ticket == null)
            ticket = parseTicketRequest();
        return ticket;
    }

    public String getTicketKey()
        throws DavException {
        String key = getParameter(PARAM_TICKET);
        if (key == null)
            key = getHeader(HEADER_TICKET);
        if (key != null)
            return key;
        throw new BadRequestException("Request does not contain a ticket key");
    }

    public ReportInfo getReportInfo()
        throws DavException {
        if (reportInfo == null)
            reportInfo = parseReportRequest();
        return reportInfo;
    }
  
    // private methods

    private Document getSafeRequestDocument()
        throws DavException {
        return getSafeRequestDocument(true);
    }

    private Document getSafeRequestDocument(boolean requireDocument)
        throws DavException {
        try {
            if (StringUtils.isBlank(getContentType())) {
                if (requireDocument)
                    throw new BadRequestException("No Content-Type specified");
                return null;
            }
            MimeType mimeType = new MimeType(getContentType());
            if (! (mimeType.match(APPLICATION_XML) ||
                   mimeType.match(TEXT_XML)))
                throw new UnsupportedMediaTypeException("Expected Content-Type " + APPLICATION_XML + " or " + TEXT_XML);
            return getRequestDocument();
        } catch (MimeTypeParseException e) {
            throw new UnsupportedMediaTypeException(e.getMessage());
        } catch (IllegalArgumentException e) {
            Throwable cause = e.getCause();
            String msg = e.getCause() != null ?
                e.getCause().getMessage() : null;
            if (msg == null)
                msg = "Unknown error parsing request document";
            throw new BadRequestException(msg);
        }
    }

    private void parsePropFindRequest()
        throws DavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null) {
            // treat as allprop
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new DavPropertyNameSet();
            return;
        }

        Element root = requestDocument.getDocumentElement();
        if (! DomUtil.matches(root, XML_PROPFIND, NAMESPACE))
            throw new BadRequestException("Expected " + QN_PROPFIND + " root element");

        Element prop = DomUtil.getChildElement(root, XML_PROP, NAMESPACE);
        if (prop != null) {
            propfindType = PROPFIND_BY_PROPERTY;
            propfindProps = new DavPropertyNameSet(prop);
            return;
        }

        if (DomUtil.getChildElement(root, XML_PROPNAME, NAMESPACE) != null) {
            propfindType = PROPFIND_PROPERTY_NAMES;
            propfindProps = new DavPropertyNameSet();
            return;
        }

        if (DomUtil.getChildElement(root, XML_ALLPROP, NAMESPACE) != null) {
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new DavPropertyNameSet();

            Element include =
                DomUtil.getChildElement(root, "include", NAMESPACE);
            if (include != null) {
                ElementIterator included = DomUtil.getChildren(include);
                while (included.hasNext()) {
                    DavPropertyName name = 
                        DavPropertyName.createFromXml(included.nextElement());
                    propfindProps.add(name);
                }
            }

            return;
        }

        throw new BadRequestException("Expected one of " + XML_PROP + ", " + XML_PROPNAME + ", or " + XML_ALLPROP + " as child of " + QN_PROPFIND);
    }

    private void parsePropPatchRequest()
        throws DavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null)
            throw new BadRequestException("PROPPATCH requires entity body");

        Element root = requestDocument.getDocumentElement();
        if (! DomUtil.matches(root, XML_PROPERTYUPDATE,NAMESPACE))
            throw new BadRequestException("Expected " + QN_PROPERTYUPDATE + " root element");

        ElementIterator sets = DomUtil.getChildren(root, XML_SET, NAMESPACE);
        ElementIterator removes = DomUtil.getChildren(root, XML_REMOVE, NAMESPACE);
        if (! (sets.hasNext() || removes.hasNext()))
            throw new BadRequestException("Expected at least one of " + QN_REMOVE + " and " + QN_SET + " as a child of " + QN_PROPERTYUPDATE);

        Element prop = null;
        ElementIterator i = null;

        proppatchSet = new DavPropertySet();
        while (sets.hasNext()) {
            Element set = sets.nextElement();
            prop = DomUtil.getChildElement(set, XML_PROP, NAMESPACE);
            if (prop == null)
                throw new BadRequestException("Expected " + QN_PROP + " child of " + QN_SET);
            i = DomUtil.getChildren(prop);
            while (i.hasNext()) {
                StandardDavProperty p =
                    StandardDavProperty.createFromXml(i.nextElement());
                proppatchSet.add(p);
            }
        }

        proppatchRemove = new DavPropertyNameSet();
        while (removes.hasNext()) {
            Element remove = removes.nextElement();
            prop = DomUtil.getChildElement(remove, XML_PROP, NAMESPACE);
            if (prop == null)
                throw new BadRequestException("Expected " + QN_PROP + " child of " + QN_REMOVE);
            i = DomUtil.getChildren(prop);
            while (i.hasNext())
                proppatchRemove.add(DavPropertyName.
                                    createFromXml(i.nextElement()));
        }
    }

    private DavPropertySet parseMkCalendarRequest()
        throws DavException {
        DavPropertySet propertySet = new DavPropertySet();

        Document requestDocument = getSafeRequestDocument(false);
        if (requestDocument == null)
            return propertySet;

        Element root = requestDocument.getDocumentElement();
        if (! DomUtil.matches(root, ELEMENT_CALDAV_MKCALENDAR,
                              NAMESPACE_CALDAV))
            throw new BadRequestException("Expected " + QN_MKCALENDAR + " root element");
        Element set = DomUtil.getChildElement(root, XML_SET, NAMESPACE);
        if (set == null)
            throw new BadRequestException("Expected " + QN_SET + " child of " + QN_MKCALENDAR);
        Element prop =DomUtil.getChildElement(set, XML_PROP, NAMESPACE);
        if (prop == null)
            throw new BadRequestException("Expected " + QN_PROP + " child of " + QN_SET);
        ElementIterator i = DomUtil.getChildren(prop);
        while (i.hasNext())
            propertySet.add(StandardDavProperty.
                            createFromXml(i.nextElement()));

        return propertySet;
    }

    private Ticket parseTicketRequest()
        throws DavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null)
            throw new BadRequestException("MKTICKET requires entity body");

        Element root = requestDocument.getDocumentElement();
        if (! DomUtil.matches(root, ELEMENT_TICKET_TICKETINFO,
                              NAMESPACE_TICKET))
            throw new BadRequestException("Expected " + QN_TICKET_TICKETINFO + " root element");
        if (DomUtil.hasChildElement(root, ELEMENT_TICKET_ID,
                                    NAMESPACE_TICKET))
            throw new BadRequestException(QN_TICKET_TICKETINFO + " may not contain child " + QN_TICKET_ID);
        if (DomUtil.hasChildElement(root, XML_OWNER, NAMESPACE))
            throw new BadRequestException(QN_TICKET_TICKETINFO + " may not contain child " + QN_OWNER);

        String timeout =
            DomUtil.getChildTextTrim(root, ELEMENT_TICKET_TIMEOUT,
                                     NAMESPACE_TICKET);
        if (timeout != null && ! timeout.equals(TIMEOUT_INFINITE)) {
            try {
                int seconds = Integer.parseInt(timeout.substring(7));
            } catch (NumberFormatException e) {
                throw new BadRequestException("Malformed " + QN_TICKET_TIMEOUT + " value " + timeout);
            }
        } else
            timeout = TIMEOUT_INFINITE;

        // visit limits are not supported

        Element pe = DomUtil.getChildElement(root, XML_PRIVILEGE, NAMESPACE);
        if (pe == null)
            throw new BadRequestException("Expected " + QN_PRIVILEGE + " child of " + QN_TICKET_TICKETINFO);

        DavPrivilegeSet privileges = DavPrivilegeSet.createFromXml(pe);
        if (! privileges.containsAny(DavPrivilege.READ, DavPrivilege.WRITE,
                                     DavPrivilege.READ_FREE_BUSY))
            throw new BadRequestException("Empty or invalid " + QN_PRIVILEGE);

        Ticket ticket = entityFactory.creatTicket();
        ticket.setTimeout(timeout);
        privileges.setTicketPrivileges(ticket);

        return ticket;
    }

    private ReportInfo parseReportRequest()
        throws DavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null)
            throw new BadRequestException("REPORT requires entity body");

        try {
            return new ReportInfo(requestDocument.getDocumentElement(),
                                  getDepth(DEPTH_0));
        } catch (org.apache.jackrabbit.webdav.DavException e) {
            throw new DavException(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public ServletInputStream getInputStream()
        throws IOException {
        if (! bufferRequestContent)
            return super.getInputStream();
        
        BufferedServletInputStream is = 
            new BufferedServletInputStream(super.getInputStream());
        bufferedContentLength = is.getLength();

        long contentLength = getContentLength();
        if (contentLength != -1 && contentLength != bufferedContentLength)
            throw new IOException("Read only " + bufferedContentLength + " of " + contentLength + " bytes");
        
        return is;
    }
    
    public boolean isRequestContentBuffered() {
        return bufferRequestContent;
    }

    public long getBufferedContentLength() {
        return bufferedContentLength;
    }
}
