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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.jcr.JcrDavException;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.simple.DavResourceImpl;
import org.apache.jackrabbit.webdav.simple.ResourceConfig;
import org.apache.jackrabbit.webdav.MultiStatusResponse;

import org.apache.log4j.Logger;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResourceFactory;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.dav.property.CalendarDescription;
import org.osaf.cosmo.dav.property.CalendarTimezone;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.dav.property.CosmoResourceType;
import org.osaf.cosmo.dav.property.SupportedCalendarComponentSet;
import org.osaf.cosmo.dav.property.SupportedCalendarData;
import org.osaf.cosmo.dav.property.TicketDiscovery;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.dav.report.ReportType;
import org.osaf.cosmo.dav.report.SupportedReportSetProperty;
import org.osaf.cosmo.io.CosmoImportContext;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.repository.PathTranslator;
import org.osaf.cosmo.repository.SchemaConstants;
import org.osaf.cosmo.icalendar.ComponentTypes;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 */
public class CosmoDavResourceImpl extends DavResourceImpl
    implements CosmoDavResource, SchemaConstants {
    private static final Logger log =
        Logger.getLogger(CosmoDavResourceImpl.class);

    private String baseUrl;
    private boolean initializing;
    private TicketDao ticketDao;
    private Map tickets;
    private Map ownedTickets;
    protected SupportedReportSetProperty supportedReports =
        new SupportedReportSetProperty();
    private boolean isCalendarCollection;
    private boolean isHomeCollection;

    /**
     */
    public CosmoDavResourceImpl(DavResourceLocator locator,
                                CosmoDavResourceFactory factory,
                                DavSession session,
                                ResourceConfig config)
        throws RepositoryException, DavException {
        super(locator, factory, session, config);

        // Initialise the supported reports here. We have to do this now because
        // they are needed when processing the REPORT method.
        initSupportedReports();

        initializing = false;
        isCalendarCollection = exists() &&
            getNode().isNodeType(NT_CALENDAR_COLLECTION);
        isHomeCollection = exists() &&
            getNode().isNodeType(NT_HOME_COLLECTION);
    }

    // DavResource methods

    /**
     */
    public String getComplianceClass() {
        return CosmoDavResource.COMPLIANCE_CLASS;
    }

    /**
     */
    public String getSupportedMethods() {
        // can only make a calendar collection inside a regular
        // collection (NEVER inside another calendar collection)
        if (exists () && isCollection() && ! isCalendarCollection()) {
            return CosmoDavResource.METHODS + ", MKCALENDAR";
        }
        return CosmoDavResource.METHODS;
    }

    // CosmoDavResource methods

    /**
     */
    public boolean isTicketable() {
        try {
            return exists() && getNode().isNodeType(NT_TICKETABLE);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isCalendarCollection() {
        return isCalendarCollection;
    }

    /**
     */
    public void setIsCalendarCollection(boolean isCalendarCollection) {
        this.isCalendarCollection = isCalendarCollection;
    }

    /**
     * Returns true if this resource represents a home
     * collection.
     */
    public boolean isHomeCollection() {
        return isHomeCollection;
    }

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    public void saveTicket(Ticket ticket)
        throws DavException {
        if (!exists()) {
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        }
	if (isLocked(this)) {
            throw new DavException(CosmoDavResponse.SC_LOCKED);
        }
        if (!isTicketable()) {
            throw new DavException(CosmoDavResponse.SC_METHOD_NOT_ALLOWED);
        }

        try {
            Node resource = getNode();
            ticket.setOwner(getLoggedInUser().getUsername());
            ticketDao.createTicket(getResourcePath(), ticket);
        } catch (Exception e) {
            log.error("cannot save ticket for resource " + getResourcePath(),
                      e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }

        // refresh the ticketdiscovery property
        getProperties().add(new TicketDiscovery(this));
    }

    /**
     * Removes the association between the ticket and this resource
     * and deletes the ticket from persistent storage.
     */
    public void removeTicket(Ticket ticket)
        throws DavException {
        if (!exists()) {
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        }
	if (isLocked(this)) {
            throw new DavException(CosmoDavResponse.SC_LOCKED);
        }
        if (!isTicketable()) {
            throw new DavException(CosmoDavResponse.SC_METHOD_NOT_ALLOWED);
        }

        try {
            String clientPath =
                PathTranslator.toClientPath(getNode().getPath());
            ticketDao.removeTicket(clientPath, ticket);
        } catch (Exception e) {
            log.error("cannot remove ticket " + ticket.getId() +
                      " for resource " + getResourcePath(), e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }

        // refresh the ticketdiscovery property
        getProperties().add(new TicketDiscovery(this));
    }

    /**
     * Returns the ticket with the given id on this resource. Does not
     * execute any security checks.
     */
    public Ticket getTicket(String id) {
        initTickets();
        return (Ticket) tickets.get(id);
    }

    /**
     * Returns all tickets owned by the named user on this resource,
     * or an empty <code>Set</code> if the user does not own any
     * tickets.
     *
     * @param username
     */
    public Set getTickets(String username) {
        initTickets();
        Set t = (Set) ownedTickets.get(username);
        return t != null ? t : new HashSet();
    }

    /**
     * Returns all tickets owned by the currently logged in user on
     * this resource, or an empty <code>Set</code> if the user does
     * not own any tickets.
     */
    public Set getLoggedInUserTickets() {
        return getTickets(getLoggedInUser().getUsername());
    }

    /**
     * Returns a resource locator for the named principal's homedir.
     */
    public DavResourceLocator getHomedirLocator(String principal) {
        return getLocator().getFactory().
            createResourceLocator(baseUrl, "/" + principal);
    }

    // DavResourceImpl methods

    /**
     */
    protected void initProperties() {
        if (! initializing) {
            initializing = true;
            super.initProperties();
            DavPropertySet properties = getProperties();

            if (isCalendarCollection()) {
                // override the default resource type property with
                // our own that sets the appropriate resource types
                // for calendar collections (caldav section 4.2)
                int[] resourceTypes = new int[2];
                resourceTypes[0] = CosmoResourceType.COLLECTION;
                resourceTypes[1] = CosmoResourceType.CALENDAR_COLLECTION;
                properties.add(new CosmoResourceType(resourceTypes));

                // Windows XP support
                properties.add(new DefaultDavProperty(DavPropertyName.
                                                      ISCOLLECTION,
                                                      "1"));

                // calendar-description property (caldav section
                // 5.2.1)
                try {
                    if (getNode().hasProperty(NP_CALENDAR_DESCRIPTION)) {
                        String text = getNode().
                            getProperty(NP_CALENDAR_DESCRIPTION).
                            getString();
                        String lang = getNode().
                            getProperty(NP_CALENDAR_LANGUAGE).getString();

                        if (lang != null)
                            properties.add(new CalendarDescription(text, lang));
                        else
                            properties.add(new CalendarDescription(text));
                    }
                } catch (RepositoryException e) {
                    log.warn("Unable to retrieve calendar description", e);
                }

                // calendar-timezone property
                try {
                    if (getNode().hasProperty(NP_CALENDAR_TIMEZONE)) {
                        String text = getNode().
                            getProperty(NP_CALENDAR_TIMEZONE).
                            getString();

                        properties.add(new CalendarTimezone(text));
                    }
                } catch (RepositoryException e) {
                    log.warn("Unable to retrieve calendar timezone", e);
                }

                // supported-calendar-component-set property (caldav
                // section 5.2.3)
                try {
                    if (getNode().hasProperty(NP_CALENDAR_SUPPORTED_COMPONENT_SET)) {
                        Value[] vals = getNode().getProperty(
                                         NP_CALENDAR_SUPPORTED_COMPONENT_SET).getValues();

                        int[] comps = new int[vals.length];

                        for (int i = 0; i < vals.length; i++) {
                            try {
                                comps[i] = ComponentTypes.getComponentType(vals[i].getString());
                            } catch (ValueFormatException e) {
                                throw new IllegalArgumentException(e.getMessage());
                            } catch (IllegalStateException e1) {
                                throw new IllegalArgumentException(e1.getMessage());
                            }
                        }

                        properties.add(new SupportedCalendarComponentSet(comps));
                    }
                    else {
                        properties.add(new SupportedCalendarComponentSet());
                    }

                } catch(IllegalArgumentException e) {
                    log.warn("Invalid component type found using default values", e);
                    properties.add(new SupportedCalendarComponentSet());
                } catch (RepositoryException e) {
                    log.warn("Unable to retrieve calendar component set using default values", e);
                    properties.add(new SupportedCalendarComponentSet());
                }

                // supported-calendar-data property (caldav section
                // 5.2.4)
                properties.add(new SupportedCalendarData());
            }

            if (isTicketable()) {
                initTickets();
                properties.add(new TicketDiscovery(this));
            }

            // Reports properties
            if (supportedReports != null) {
                properties.add(supportedReports);
            }

            initializing = false;
        }
    }

    /**
     */
    protected ImportContext getImportContext(InputContext inputCtx,
                                             String systemId)
        throws IOException {
        return new CosmoImportContext(getNode(), systemId, inputCtx);
    }

    /**
     */
    protected void initTickets() {
        if (tickets != null) {
            return;
        }

        if (isTicketable() && tickets == null && exists()) {
            tickets = new HashMap();
            ownedTickets = new HashMap();

            try {
                String clientPath =
                    PathTranslator.toClientPath(getNode().getPath());
                for (Iterator i=ticketDao.getTickets(clientPath).iterator();
                     i.hasNext();) {
                    Ticket ticket = (Ticket) i.next();

                    if (ticket.hasTimedOut()) {
                        if (log.isDebugEnabled()) {
                            log.debug("removing timed out ticket " +
                                      ticket.getId());
                        }
                        ticketDao.removeTicket(clientPath, ticket);
                        continue;
                    }

                    tickets.put(ticket.getId(), ticket);
                    Set ownedBy = (Set) ownedTickets.get(ticket.getOwner());
                    if (ownedBy == null) {
                        ownedBy = new HashSet();
                        ownedTickets.put(ticket.getOwner(), ownedBy);
                    }
                    ownedBy.add(ticket);
                }
            } catch (RepositoryException e) {
                log.warn("error getting tickets for node", e);
            }
        }
    }

    /**
     * Define the set of reports supported by this resource.
     * 
     * @see org.apache.jackrabbit.webdav.version.report.SupportedReportSetProperty
     * @see AbstractResource#initSupportedReports()
     */
    protected void initSupportedReports() {
        if (exists()) {
            supportedReports = new SupportedReportSetProperty(new ReportType[] {
                    ReportType.CALDAV_QUERY, ReportType.CALDAV_MULTIGET, ReportType.CALDAV_FREEBUSY});
        }
    }

    // our methods

    /**
     */
    protected User getLoggedInUser() {
        CosmoDavResourceFactory cosmoFactory =
            (CosmoDavResourceFactory) getFactory();
        return cosmoFactory.getSecurityManager().getSecurityContext().
            getUser();
    }

    /**
     * Set the base URL for the server on which this resource lives
     * (could be statically configured or dynamically calculated
     * per-request).
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     */
    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    /**
     * Return a DavResource for the specified href.
     * 
     * @param href
     *            MUST be an absolute href to the resource which itself MUST be
     *            a child of this resource
     * @param session
     *            DavSession needed when creating child resource object
     * @return DavResource for child or null if one could not be created.
     */
    public DavResource getChildHref(String href, DavSession session) {
        DavResource child = null;
        if (getResourcePath() != null && !getResourcePath().equals("/")) {
            String childPath = href;
            if (childPath.startsWith(getLocator().getPrefix())) {
                childPath = childPath.substring(getLocator().getPrefix()
                        .length());
            }
            DavResourceLocator childloc = getLocator().getFactory()
                    .createResourceLocator(getLocator().getPrefix(),
                            getLocator().getWorkspacePath(), childPath);
            try {
                child = getFactory().createResource(childloc, session);
            } catch (DavException e) {
                // should not occur
            }
        }
        return child;
    }

    /**
     * Get the report that matches the reportinfo that is supported by this
     * resource.
     * 
     * TODO Eventually this will be punted up into jackrabbit.
     * 
     * @param reportInfo
     * @return the requested report
     * @throws DavException
     * @see DeltaVResource#getReport(org.apache.jackrabbit.webdav.version.report.ReportInfo)
     */
    public Report getReport(ReportInfo reportInfo)
        throws DavException {
        if (!exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        if (supportedReports.isSupportedReport(reportInfo)) {
            Report report = ReportType.getType(reportInfo).createReport();
            report.setResource(this);
            report.setInfo(reportInfo);
            return report;
        } else {
            throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY,
                    "Unkown report " + reportInfo.getReportElement().getTagName() + "requested.");
        }
    }

    /**
     * Adds a new member to this resource and set the member properties.
     */
    public MultiStatusResponse addMember(DavResource member, InputContext inputContext,
                                         DavPropertySet setProperties) throws DavException {
        MultiStatusResponse msr = null;
        DavResource savedMember = null;

        if (setProperties == null || setProperties.isEmpty()) {
            throw new IllegalArgumentException("invalid call to addMember with properties. " +
                                               "The property set is null or empty");
        }

        try {
            addMember(member, inputContext);
            savedMember = getMember(member.getResourcePath());

            if (savedMember == null) {
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                                       "Unable to retrieve member " + member.getResourcePath());
            }
        } catch (Exception e) {
            log.error("Unable to addMember with properties to resource: addMember failed", e);
            if (e instanceof DavException)
                throw (DavException) e;
            else
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        try {
            msr = savedMember.alterProperties(setProperties, new DavPropertyNameSet());
        } catch (Exception e) {
            log.error("Unable to addMember with properties to resource: alterProperties failed", e);
            try {
                //If an exception is raised while altering properties we want to remove the member we just created.
                removeMember(savedMember);
            } catch(DavException e1) {
                log.error("Unable to removeMember after an alterProperties failure ", e1);
            }

            if (e instanceof DavException)
                throw (DavException) e;
            else
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        if (! msr.hasStatusKey(DavServletResponse.SC_OK)) {
            try {
                //The setting of properties failed. We want to remove the member we just created.
                removeMember(savedMember);
            } catch(DavException e) {
                log.error("Unable to removeMember after examining MultiStatusResponse", e);
            }
        }

        return msr;
    }

    /**
     * @param property
     * @throws RepositoryException
     */
    protected void setJcrProperty(DavProperty property) throws RepositoryException {
        //XXX What to do for: dav:contenttype -> jcr:mediaType, jcr:encoding

        String value = property.getValue() != null ?
                property.getValue().toString() :
                null;

        if (property instanceof CalendarTimezone) {
            getNode().setProperty(NP_CALENDAR_TIMEZONE, value);
        }
        else if (property instanceof CalendarDescription) {
            CalendarDescription c = (CalendarDescription) property;

            getNode().setProperty(NP_CALENDAR_DESCRIPTION, value);
            getNode().setProperty(NP_CALENDAR_LANGUAGE, c.getLanguage());
        }
        else if (property instanceof SupportedCalendarComponentSet) {
            Object[] compTypes = ((Set) property.getValue()).toArray();
            String[] values = new String[compTypes.length];

            for(int i = 0; i < compTypes.length; i++)
                values[i] = compTypes[i].toString();

            getNode().setProperty(NP_CALENDAR_SUPPORTED_COMPONENT_SET, values);
        }
        else {
            getNode().setProperty(getJcrName(property.getName()), value);
        }
    }

    protected DavResource getMember(String resourcePath) {
        DavResourceIterator it = getMembers();

        //This is not the most efficient way to do this but since
        //the DavResourceImpl jackrabbit class keeps its member variables private
        //it is the only way to get the Resource.
        while (it.hasNext()) {
            DavResource r = it.nextResource();

            if (r.getResourcePath().equals(resourcePath))
                return r;
        }

        return null;
    }
}

