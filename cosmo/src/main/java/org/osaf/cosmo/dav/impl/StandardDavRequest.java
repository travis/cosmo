/*
 * Copyright 2006 Open Source Applications Foundation
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
import java.util.HashSet;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.CaldavRequest;
import org.osaf.cosmo.dav.caldav.property.CalendarDescription;
import org.osaf.cosmo.dav.caldav.property.CalendarTimezone;
import org.osaf.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.osaf.cosmo.dav.report.ReportRequest;
import org.osaf.cosmo.dav.ticket.TicketConstants;
import org.osaf.cosmo.dav.ticket.TicketDavRequest;
import org.osaf.cosmo.model.Ticket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 */
public class StandardDavRequest extends WebdavRequestImpl
    implements CaldavRequest, ReportRequest, TicketDavRequest,
               DavConstants, CaldavConstants, TicketConstants {
    private static final Log log =
        LogFactory.getLog(StandardDavRequest.class);

    private DavPropertySet mkcalendarSet;
    private Ticket ticket;
    private ReportInfo reportInfo;
    private boolean bufferRequestContent = false;
    private long bufferedContentLength = -1;

    /**
     */
    public StandardDavRequest(HttpServletRequest request,
                              DavLocatorFactory factory) {
        super(request, factory);
    }
    
    /**
     */
    public StandardDavRequest(HttpServletRequest request,
                              DavLocatorFactory factory,
                              boolean bufferRequestContent) {
        super(request, factory);
        this.bufferRequestContent = bufferRequestContent;
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
        if (! getContextPath().equals("/")) {
            buf.append(getContextPath());
        }
        return buf.toString();
    }

    // CaldavRequest methods

    /**
     * Return the list of 'set' entries in the MKCALENDAR request
     * body. The list is empty if the request body did not
     * contain any 'set' elements.
     * An <code>IllegalArgumentException</code> is raised if the request body can not
     * be parsed
     */
    public DavPropertySet getMkCalendarSetProperties() {
        if (mkcalendarSet == null) {
            mkcalendarSet = parseMkCalendarRequest();
        }
        return mkcalendarSet;
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
        String ticketId = getParameter(PARAM_TICKET);
        if (ticketId == null) {
            ticketId = getHeader(HEADER_TICKET);
        }
        return ticketId;
    }

    /**
     * Return the report information, if any, included in the
     * request.
     *
     * @throws DavException if there is no report information in the
     * request or if the report information is invalid
     */
    public ReportInfo getReportInfo()
        throws DavException {
        if (reportInfo == null) {
            reportInfo = parseReportRequest();
        }
        return reportInfo;
    }

  
    // private methods
    private DavPropertySet parseMkCalendarRequest() {
        DavPropertySet propertySet = new DavPropertySet();

        Document requestDocument = getRequestDocument();

        if (requestDocument == null) {
            //The request did not contain an XML body
            log.info("parseMkCalendarRequest requestDocument is null");
            return propertySet;
        }

        Element root = requestDocument.getDocumentElement();
        if (! DomUtil.matches(root, ELEMENT_CALDAV_MKCALENDAR,
                              NAMESPACE_CALDAV)) {
            throw new IllegalArgumentException("MKCALENDAR request missing DAV:mkcalendar");
        }

        Element set =
            DomUtil.getChildElement(root, XML_SET, NAMESPACE);

        if (set == null) {
            throw new IllegalArgumentException("MKCALENDAR request missing DAV:set element");
        }
        Element prop =
            DomUtil.getChildElement(set, XML_PROP, NAMESPACE);
        if (prop == null) {
            throw new IllegalArgumentException("MKCALENDAR request missing DAV:prop element");
        }

        ElementIterator i = DomUtil.getChildren(prop);

        while (i.hasNext()) {
            Element e = i.nextElement();

            //{CALDAV:calendar-timezone}
            if (DomUtil.matches(e, PROPERTY_CALDAV_CALENDAR_TIMEZONE,
                                NAMESPACE_CALDAV)) {
                parseCalendarTimezone(propertySet, e);

            }
            // {CALDAV:supported-calendar-component-set}
            else if (DomUtil.matches(e, PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET,
                                     NAMESPACE_CALDAV)) {
                parseSupportedCalendarSet(propertySet, e);
            }
            //CALDAV:calendar-description
            else if (DomUtil.matches(e, PROPERTY_CALDAV_CALENDAR_DESCRIPTION,
                                     NAMESPACE_CALDAV)) {
                parseDescription(propertySet, e);
            }
            //CALDAV:supported-calendar-data
            else if (DomUtil.matches(e, PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA,
                                     NAMESPACE_CALDAV)) {
                throw new IllegalArgumentException("MKCALENDAR CALDAV:supported-calendar-data " +
                                                   "property is not allowed");

            }
            //CALDAV: namespace
            else if (DomUtil.getNamespace(e).equals(NAMESPACE_CALDAV)) {
                //No other properties should be specifed in the CALDAV namespace.
                //Raise error if one or more found.
                throw new IllegalArgumentException("Invalid MKCALENDAR property in " +
                                                   "CALDAV namespace: " + e.toString());
            }
            //DAV:displayname
            else if (DomUtil.matches(e, PROPERTY_DISPLAYNAME, NAMESPACE)) {
                propertySet.add(DefaultDavProperty.createFromXml(e));
            }
            //DAV: namespace
            else if (DomUtil.getNamespace(e).equals(NAMESPACE)) {
                //No other properties should be specifed in the DAV namespace. Verify and 
                //raise error if one or more found.
                throw new IllegalArgumentException("Invalid MKCALENDAR property in " +
                                                   "DAV namespace: " + e.toString());
            }
            //Handle all dead properties
            else {
                propertySet.add(DefaultDavProperty.createFromXml(e));
            }
        }

        return propertySet;
    }

    private void parseDescription(DavPropertySet propertySet, Element e) {
        DefaultDavProperty d = DefaultDavProperty.createFromXml(e);

        String lang = DomUtil.getAttribute(e, XML_LANG, NAMESPACE_XML);

        if (lang != null) {
            propertySet.add(new CalendarDescription((String) d.getValue(), lang));
        } else {
            propertySet.add(new CalendarDescription((String) d.getValue()));
        }
    }

    private void parseCalendarTimezone(DavPropertySet propertySet, Element e) {
        String icalTz = DomUtil.getTextTrim(e);

        if (icalTz == null)
            throw new IllegalArgumentException("MKCALENDAR request error parsing " +
                                               "calendar:timezone CDATA required");

        propertySet.add(new CalendarTimezone(icalTz));
    }

    private void parseSupportedCalendarSet(DavPropertySet propertySet, Element e) {
        ElementIterator i = DomUtil.getChildren(e);

        if (! i.hasNext()) {
            //No C:comp elements were contained in the supported-calendar-component-set
            throw new IllegalArgumentException("MKCALENDAR request error parsing " +
                                             "supported-calendar-component-set " + 
                                             " no comp elements found");
        }

        HashSet<String> types = new HashSet<String>();

        while (i.hasNext()) {
            Element el = i.nextElement();

            if (!DomUtil.matches(el, ELEMENT_CALDAV_COMP, NAMESPACE_CALDAV)) {
                    throw new IllegalArgumentException("MKCALENDAR request error parsing " +
                                                       "supported-calendar-component-set " +
                                                       "only comp elements allowed");
            }

            String type = el.getAttribute(ATTR_CALDAV_NAME);

            if (type == null) {
                throw new IllegalArgumentException("MKCALENDAR request error parsing " +
                                                   "supported-calendar-component-set " +
                                                   "comp element missing name attribute");
             }

            types.add(type);
        }

        propertySet.add(new SupportedCalendarComponentSet(types));
    }


    private Ticket parseTicketRequest() {
        Document requestDocument = getRequestDocument();
        if (requestDocument == null) {
            throw new IllegalArgumentException("ticket request missing body");
        }

        Element root = requestDocument.getDocumentElement();
        if (! DomUtil.matches(root, ELEMENT_TICKET_TICKETINFO,
                              NAMESPACE_TICKET)) {
            throw new IllegalArgumentException("ticket request has missing ticket:ticketinfo");
        }

        if (DomUtil.hasChildElement(root, ELEMENT_TICKET_ID,
                                    NAMESPACE_TICKET)) {
            throw new IllegalArgumentException("ticket request must not include ticket:id");
        }
        if (DomUtil.hasChildElement(root, XML_OWNER, NAMESPACE)) {
            throw new IllegalArgumentException("ticket request must not include ticket:owner");
        }

        String timeout =
            DomUtil.getChildTextTrim(root, ELEMENT_TICKET_TIMEOUT,
                                     NAMESPACE_TICKET);
        if (timeout != null &&
            ! timeout.equals(TIMEOUT_INFINITE)) {
            try {
                int seconds = Integer.parseInt(timeout.substring(7));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("malformed ticket:timeout value " + timeout);
            }
        } else {
            timeout = TIMEOUT_INFINITE;
        }

        // visit limits are not supported

        Element privilege =
            DomUtil.getChildElement(root, XML_PRIVILEGE, NAMESPACE);
        if (privilege == null) {
            throw new IllegalArgumentException("ticket request missing DAV:privileges");
        }
        Element read =
            DomUtil.getChildElement(privilege, XML_READ, NAMESPACE);
        Element write =
            DomUtil.getChildElement(privilege, XML_WRITE, NAMESPACE);
        Element freebusy =
            DomUtil.getChildElement(privilege, ELEMENT_TICKET_FREEBUSY,
                                    NAMESPACE);
        if (read == null && write == null && freebusy == null) {
            throw new IllegalArgumentException("ticket request contains empty or invalid DAV:privileges");
        }

        Ticket ticket = new Ticket();
        ticket.setTimeout(timeout);
        if (read != null) {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        }
        if (write != null) {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        }
        if (freebusy != null) {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_FREEBUSY);
        }

        return ticket;
    }

    private ReportInfo parseReportRequest()
        throws DavException {
        Document requestDocument = getRequestDocument();
        if (requestDocument == null) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST,
                                   "Report content must not be empty");
        }
        return new ReportInfo(requestDocument.getDocumentElement(),
                              getDepth(DEPTH_0));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if(!bufferRequestContent)
            return super.getInputStream();
        
        BufferedServletInputStream is = 
            new BufferedServletInputStream(super.getInputStream());
        
        bufferedContentLength = is.getLength();
        
        return is;
    }
    
    public boolean isRequestContentBuffered() {
        return bufferRequestContent;
    }

    public long getBufferedContentLength() {
        return bufferedContentLength;
    }
}
