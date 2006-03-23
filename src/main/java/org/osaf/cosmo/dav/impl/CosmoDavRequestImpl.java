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

import java.util.Iterator;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.apache.log4j.Logger;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavRequest;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.dav.property.CalendarTimezone;
import org.osaf.cosmo.dav.property.CalendarDescription;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.dav.property.SupportedCalendarComponentSet;
import org.osaf.cosmo.icalendar.ComponentTypes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extends {@link org.apache.jackrabbit.webdav.WebdavRequest}. and
 * implements {@link CosmoDavRequest}.
 */
public class CosmoDavRequestImpl extends WebdavRequestImpl
    implements CosmoDavRequest, DavConstants {
    private static final Logger log =
        Logger.getLogger(CosmoDavRequestImpl.class);

    private DavPropertySet mkcalendarSet;
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
        String ticketId = getParameter(CosmoDavConstants.PARAM_TICKET);
        if (ticketId == null) {
            ticketId = getHeader(CosmoDavConstants.HEADER_TICKET);
        }
        return ticketId;
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
        if (! DomUtil.matches(root, CosmoDavConstants.ELEMENT_CALDAV_MKCALENDAR,
                              CosmoDavConstants.NAMESPACE_CALDAV)) {
            throw new IllegalArgumentException("MKCALENDAR request missing DAV:mkcalendar");
        }

        Element set =
            DomUtil.getChildElement(root, CosmoDavConstants.ELEMENT_SET,
                                    DavConstants.NAMESPACE);

        if (set == null) {
            throw new IllegalArgumentException("MKCALENDAR request missing DAV:set element");
        }
        Element prop =
            DomUtil.getChildElement(set, CosmoDavConstants.ELEMENT_PROP,
                                    DavConstants.NAMESPACE);
        if (prop == null) {
            throw new IllegalArgumentException("MKCALENDAR request missing DAV:prop element");
        }

        ElementIterator i = DomUtil.getChildren(prop);

        while (i.hasNext()) {
            Element e = i.nextElement();

            //{CALDAV:calendar-timezone}
            if (DomUtil.matches(e, CosmoDavConstants.PROPERTY_CALDAV_CALENDAR_TIMEZONE,
                                CosmoDavConstants.NAMESPACE_CALDAV)) {
                parseCalendarTimezone(propertySet, e);

            }
            // {CALDAV:supported-calendar-component-set}
            else if (DomUtil.matches(e, 
                           CosmoDavConstants.PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET,
                                CosmoDavConstants.NAMESPACE_CALDAV)) {
                parseSupportedCalendarSet(propertySet, e);
            }
            //CALDAV:calendar-description
            else if (DomUtil.matches(e, CosmoDavConstants.PROPERTY_CALDAV_CALENDAR_DESCRIPTION,
                                     CosmoDavConstants.NAMESPACE_CALDAV)) {
                parseDescription(propertySet, e);
            }
            //CALDAV:supported-calendar-data
            else if (DomUtil.matches(e, CosmoDavConstants.PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA,
                                     CosmoDavConstants.NAMESPACE_CALDAV)) {
                throw new IllegalArgumentException("MKCALENDAR CALDAV:supported-calendar-data " +
                                                   "property is not allowed");

            }
            //CALDAV: namespace
            else if (DomUtil.getNamespace(e).equals(CosmoDavConstants.NAMESPACE_CALDAV)) {
                //No other properties should be specifed in the CALDAV namespace.
                //Raise error if one or more found.
                throw new IllegalArgumentException("Invalid MKCALENDAR property in " +
                                                   "CALDAV namespace: " + e.toString());
            }
            //DAV:displayname
            else if (DomUtil.matches(e, DavConstants.PROPERTY_DISPLAYNAME,
                                     DavConstants.NAMESPACE)) {
                propertySet.add(DefaultDavProperty.createFromXml(e));
            }
            //DAV: namespace
            else if (DomUtil.getNamespace(e).equals(DavConstants.NAMESPACE)) {
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

        String lang = DomUtil.getAttribute(e, CosmoDavConstants.ATTR_XML_LANG,
                                           CosmoDavConstants.NAMESPACE_XML);

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

        ArrayList a = new ArrayList();

        while (i.hasNext()) {
            Element el = i.nextElement();

            if (!DomUtil.matches(el, CosmoDavConstants.ELEMENT_CALDAV_COMP,
                CosmoDavConstants.NAMESPACE_CALDAV)) {
                    throw new IllegalArgumentException("MKCALENDAR request error parsing " +
                                                       "supported-calendar-component-set " +
                                                       "only comp elements allowed");
            }

            String type = el.getAttribute(CosmoDavConstants.ATTR_CALDAV_NAME);

            if (type == null) {
                throw new IllegalArgumentException("MKCALENDAR request error parsing " +
                                                   "supported-calendar-component-set " +
                                                   "comp element missing name attribute");
             }

             try {
                 a.add(new Integer(ComponentTypes.getComponentType(type.trim())));
             } catch (IllegalArgumentException e1) {
                 throw new IllegalArgumentException("MKCALENDAR request error parsing " +
                                                    "supported-calendar-component-set. " +
                                                    "Unsupported type " + type + " found.");
             }
        }

        int size = a.size();

        int[] compTypes = new int[size];

        for (int j = 0; j < size; j++)
            compTypes[j] = ((Integer) a.get(j)).intValue();

        propertySet.add(new SupportedCalendarComponentSet(compTypes));
    }


    private Ticket parseTicketRequest() {
        Document requestDocument = getRequestDocument();
        if (requestDocument == null) {
            throw new IllegalArgumentException("ticket request missing body");
        }

        Element root = requestDocument.getDocumentElement();
        if (! DomUtil.matches(root, CosmoDavConstants.ELEMENT_TICKETINFO,
                              CosmoDavConstants.NAMESPACE_TICKET)) {
            throw new IllegalArgumentException("ticket request has missing ticket:ticketinfo");
        }

        if (DomUtil.hasChildElement(root, CosmoDavConstants.ELEMENT_ID,
                                    CosmoDavConstants.NAMESPACE_TICKET)) {
            throw new IllegalArgumentException("ticket request must not include ticket:id");
        }
        if (DomUtil.hasChildElement(root, CosmoDavConstants.ELEMENT_OWNER,
                                    CosmoDavConstants.NAMESPACE_TICKET)) {
            throw new IllegalArgumentException("ticket request must not include ticket:owner");
        }

        String timeout =
            DomUtil.getChildTextTrim(root, CosmoDavConstants.ELEMENT_TIMEOUT,
                                     CosmoDavConstants.NAMESPACE_TICKET);
        if (timeout != null &&
            ! timeout.equals(CosmoDavConstants.VALUE_INFINITE)) {
            try {
                int seconds = Integer.parseInt(timeout.substring(7));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("malformed ticket:timeout value " + timeout);
            }
        } else {
            timeout = CosmoDavConstants.VALUE_INFINITE;
        }

        // visit limits are not supported

        Element privilege =
            DomUtil.getChildElement(root, CosmoDavConstants.ELEMENT_PRIVILEGE,
                                    DavConstants.NAMESPACE);
        if (privilege == null) {
            throw new IllegalArgumentException("ticket request missing DAV:privileges");
        }
        Element read =
            DomUtil.getChildElement(privilege, CosmoDavConstants.ELEMENT_READ,
                                    DavConstants.NAMESPACE);
        Element write =
            DomUtil.getChildElement(privilege, CosmoDavConstants.ELEMENT_WRITE,
                                    DavConstants.NAMESPACE);
        if (read == null && write == null) {
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

        return ticket;
    }


    /**
     * This is the Cosmo specific report handling.
     *
     * TODO Eventually this will be punted up into jackrabbit.
     */
    public ReportInfo getCosmoReportInfo() {
        Document requestDocument = getRequestDocument();
        if (requestDocument == null) {
            return null;
        }
        return new ReportInfo(requestDocument.getDocumentElement(),
                              getDepth(DEPTH_0),
                              getDavSession());
    }
}

