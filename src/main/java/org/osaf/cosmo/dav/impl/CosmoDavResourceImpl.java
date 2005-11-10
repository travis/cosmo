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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import net.fortuna.ical4j.model.Calendar;

import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.jcr.JcrDavException;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.simple.CollectionNodeResource;
import org.apache.jackrabbit.webdav.simple.DavResourceImpl;
import org.apache.jackrabbit.webdav.simple.ChainBasedNodeResource;
import org.apache.jackrabbit.webdav.simple.NodeResource;
import org.apache.jackrabbit.webdav.simple.ResourceConfig;

import org.apache.log4j.Logger;

import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.dao.jcr.JcrCalendarMapper;
import org.osaf.cosmo.dao.jcr.JcrConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResourceFactory;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.dav.property.CalendarComponentRestrictionSet;
import org.osaf.cosmo.dav.property.CalendarDescription;
import org.osaf.cosmo.dav.property.CalendarRestrictions;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;
import org.osaf.cosmo.dav.property.CosmoResourceType;
import org.osaf.cosmo.dav.property.TicketDiscovery;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.dav.report.ReportType;
import org.osaf.cosmo.dav.report.SupportedReportSetProperty;
import org.osaf.cosmo.jackrabbit.io.ApplicationContextAwareImportContext;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 */
public class CosmoDavResourceImpl extends DavResourceImpl 
    implements CosmoDavResource, ApplicationContextAware, JcrConstants {
    private static final Logger log = Logger.getLogger(CosmoDavResource.class);
    private static final String BEAN_TICKET_DAO = "ticketDao";

    private String baseUrl;
    private ApplicationContext applicationContext;
    private boolean initializing;
    private Map tickets;
    private Map ownedTickets;
    protected SupportedReportSetProperty supportedReports =
        new SupportedReportSetProperty();

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

    /**
     */
    public DavResource getCollection() {
        CosmoDavResourceImpl c = (CosmoDavResourceImpl) super.getCollection();
        c.setBaseUrl(baseUrl);
        c.setApplicationContext(applicationContext);
        return c;
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
     * Returns true if this resource represents a calendar home
     * collection.
     */
    public boolean isCalendarHomeCollection() {
        try {
            return exists() && getNode().isNodeType(NT_CALDAV_HOME);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isCalendarCollection() {
        try {
            return exists() && getNode().isNodeType(NT_CALDAV_COLLECTION);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the given resource as an internal member to this resource.
     */
    public void addCalendarCollection(CosmoDavResource child)
        throws DavException {
        if (isLocked(this) || isLocked(child)) {
            throw new DavException(CosmoDavResponse.SC_LOCKED);
        }
        if (isFilteredResource(child)) {
            throw new DavException(CosmoDavResponse.SC_FORBIDDEN);
        }
        try {
            ImportContext ctx = createImportContext();
            ctx.setSystemId(getJcrPathName(child.getLocator().getJcrPath()));
            ctx.setContentType(CosmoDavConstants.
                               CONTENT_TYPE_CALENDAR_COLLECTION);
            createImportCollectionChain().execute(ctx);
            getNode().save();
        } catch (DataIntegrityViolationException e) {
            log.error("resource " + child.getResourcePath() +
                      " already exists", e);
            throw new DavException(CosmoDavResponse.SC_METHOD_NOT_ALLOWED);
        } catch (Exception e) {
            log.error("cannot add calendar collection", e);
            if (e instanceof DataAccessException &&
                e.getCause() instanceof RepositoryException) {
                throw new JcrDavException((RepositoryException) e.getCause());
            }
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * For calendar collection resources, returns a
     * <code>Calendar</code> representing the calendar objects
     * contained within the collection.
     */
    public Calendar getCollectionCalendar()
        throws DavException {
        if (! isCalendarCollection()) {
            return null;
        }
        try {
            return JcrCalendarMapper.nodeToCalendar(getNode());
        } catch (RepositoryException e) {
            throw new JcrDavException(e);
        }
    }

    /**
     * Returns the entity tag for this resource.
     */
    public String getETag() {
        initProperties();
        return getNodeResource() == null ? "" : getNodeResource().getETag();
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

            TicketDao dao = (TicketDao) applicationContext.
                getBean(BEAN_TICKET_DAO, TicketDao.class);
            dao.createTicket(resource.getPath(), ticket);
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
            TicketDao dao = (TicketDao) applicationContext.
                getBean(BEAN_TICKET_DAO, TicketDao.class);
            dao.removeTicket(getNode().getPath(), ticket);
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

            if (isCalendarCollection() ||
                isCalendarHomeCollection()) {

                // override the default resource type property with
                // our own that sets the appropriate resource types
                // for calendar home collections (caldav section 4.2)
                // and calendar collections (caldav section 4.3)
                int[] resourceTypes = new int[2];
                resourceTypes[0] = CosmoResourceType.COLLECTION;
                resourceTypes[1] = isCalendarCollection() ?
                    CosmoResourceType.CALENDAR_COLLECTION :
                    CosmoResourceType.CALENDAR_HOME;
                properties.add(new CosmoResourceType(resourceTypes));
                // Windows XP support
                properties.add(new DefaultDavProperty(DavPropertyName.
                                                      ISCOLLECTION,
                                                      "1"));

                // content-language property
                String contentLanguage = null;
                try {
                    if (getNode().hasProperty(NP_XML_LANG)) {
                        contentLanguage =
                            getNode().getProperty(NP_XML_LANG).getString();
                        setContentLanguage(contentLanguage);
                    }
                } catch (RepositoryException e) {
                    log.warn("Unable to retrieve content language", e);
                }

                // calendar-description property (caldav section
                // 4.4.1)
                try {
                    if (getNode().hasProperty(NP_CALDAV_CALENDARDESCRIPTION)) {
                        String text = getNode().
                            getProperty(NP_CALDAV_CALENDARDESCRIPTION).
                            getString();
                        properties.add(new CalendarDescription(text,
                                                               contentLanguage));
                    }
                } catch (RepositoryException e) {
                    log.warn("Unable to retrieve calendar description", e);
                }
            }

            if (isCalendarCollection()) {
                // calendar-component-restriction-set property (caldav
                // section 4.4.2)
                // the entire Cosmo server allows only the components
                // specified by this constant, and this behavior can
                // not be modified by clients
                DavProperty davprop =
                    new CalendarComponentRestrictionSet(ICALENDAR_COMPONENTS);
                properties.add(davprop);

                // calendar-restrictions property (caldav section
                // 4.4.3)
                // the entire Cosmo server allows non-calendar data
                // within calendar collections, and this behavior can
                // not be modified by clients
                properties.add(new CalendarRestrictions());
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
    protected void initTickets() {
        // this should only happen before CosmoDavServlet.service
        // executes - for instance validating preconditions when
        // locking
        if (applicationContext == null) {
            return;
        }

        if (isTicketable() && tickets == null && exists()) {
            tickets = new HashMap();
            ownedTickets = new HashMap();

            try {
                TicketDao dao = (TicketDao)
                    applicationContext.getBean(BEAN_TICKET_DAO, TicketDao.class);
                for (Iterator i=dao.getTickets(getNode().getPath()).iterator();
                     i.hasNext();) {
                    Ticket ticket = (Ticket) i.next();
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
                    ReportType.CALDAV_QUERY, ReportType.CALDAV_MULTIGET });
        }
    }

    /**
     */
    protected NodeResource createNodeResource()
        throws RepositoryException {
        if (isCollection()) {
            CollectionNodeResource nr = new CollectionNodeResource();
            nr.init(this, getNode());
            return nr;
        }
        ChainBasedNodeResource nr = new ChainBasedNodeResource();
        nr.init(this, getNode());
        return nr;
    }

    /**
     */
    protected ImportContext createImportContext() {
        ApplicationContextAwareImportContext ctx =
            new ApplicationContextAwareImportContext(getNode());
        ctx.setApplicationContext(getApplicationContext());
        return ctx;
    }

    // ApplicationContextAware methods

    /**
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private String getJcrPathName(String path) {
        int pos = path.lastIndexOf('/');
        return pos >= 0 ? path.substring(pos + 1) : "";
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
        if (reportInfo == null) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST,
                    "A REPORT request must provide a valid XML request body.");
        }
        if (!exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }

        if (supportedReports.isSupportedReport(reportInfo)) {
            try {
                Report report = ReportType.getType(reportInfo).createReport();
                report.setInfo(reportInfo);
                report.setResource(this);
                return report;
            } catch (IllegalArgumentException e) {
                // should never occur.
                throw new DavException(
                        DavServletResponse.SC_INTERNAL_SERVER_ERROR, e
                                .getMessage());
            }
        } else {
            throw new DavException(DavServletResponse.SC_UNPROCESSABLE_ENTITY,
                    "Unkown report "
                            + reportInfo.getReportElement()
                                    .getNamespacePrefix()
                            + reportInfo.getReportElement().getName()
                            + "requested.");
        }
    }
}
