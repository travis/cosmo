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

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.server.io.MimeResolver;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.jcr.JcrDavException;
import org.apache.jackrabbit.webdav.simple.DavResourceImpl;

import org.apache.log4j.Logger;

import org.osaf.cosmo.UnsupportedFeatureException;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;
import org.osaf.cosmo.dav.CosmoDavResourceFactory;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.icalendar.ICalendarUtils;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 */
public class CosmoDavResourceImpl extends DavResourceImpl 
    implements CosmoDavResource , ApplicationContextAware {
    private static final Logger log = Logger.getLogger(CosmoDavResource.class);
    private static final String BEAN_CALENDAR_DAO = "calendarDao";
    private static final String BEAN_TICKET_DAO = "ticketDao";

    private boolean isCollection;
    private String baseUrl;
    private ApplicationContext applicationContext;
    private MimeResolver mimeResolver;

    /**
     */
    public CosmoDavResourceImpl(DavResourceLocator locator,
                                CosmoDavResourceFactory factory,
                                DavSession session)
        throws RepositoryException {
        super(locator, factory, session);
        mimeResolver = new MimeResolver();
    }

    // DavResource methods

    /**
     */
    public boolean isCollection() {
        try {
            // required because super.isCollection is private
            Node node = getNode();
            if (node == null) {
                return false;
            }
            if (node.isNodeType(CosmoJcrConstants.NT_DAV_COLLECTION)) {
                return true;
            }
            if (node.isNodeType(CosmoJcrConstants.NT_TICKET)) {
                return false;
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return super.isCollection();
    }

    /**
     */
    public String getComplianceClass() {
        return CosmoDavResource.COMPLIANCE_CLASS;
    }

    /**
     */
    public String getSupportedMethods() {
        // can only make a calendar collection inside a regular
        // collection (NEVER inside another calendar collection).
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

    /**
     */
    public void addMember(DavResource member,
                          InputStream in)
        throws DavException {
        CosmoDavResource cdr = (CosmoDavResource) member;
        if (cdr.isCalendarResource()) {
            addCalendarResource(cdr, in);
        }
        else {
            super.addMember(member, in);
        }
        return;
    }

    // CosmoDavResource methods

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isCalendarCollection() {
        try {
            return getNode().
                isNodeType(CosmoJcrConstants.NT_CALDAV_COLLECTION);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the given resource as an internal member to this resource.
     */
    public void addCalendarCollection(CosmoDavResource child)
        throws DavException {
        if (!exists()) {
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        }
	if (isLocked(this)) {
            throw new DavException(CosmoDavResponse.SC_LOCKED);
        }
        try {
            Node parent = getNode();
            CalendarDao dao = (CalendarDao) applicationContext.
                getBean(BEAN_CALENDAR_DAO, CalendarDao.class);
            dao.createCalendar(parent.getPath(), child.getDisplayName());
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
     * Returns true if this resource represents a (non-collection)
     * calendar resource.
     */
    public boolean isCalendarResource() {
        // XXX figure out a way to examine the request's Content-Type
        // header
        return (! isCalendarCollection() &&
                mimeResolver.getMimeType(getDisplayName()).
                equals(CosmoDavConstants.CT_ICALENDAR));
    }

    /**
     * Adds the given calendar resource as an internal member to this
     * resource.
     */
    public void addCalendarResource(CosmoDavResource resource,
                                    InputStream in)
        throws DavException {
        if (!exists()) {
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        }
	if (isLocked(this)) {
            throw new DavException(CosmoDavResponse.SC_LOCKED);
        }
        try {
            Node parent = getNode();
            if (! isCalendarCollection()) {
                throw new DavException(CosmoDavResponse.SC_FORBIDDEN,
                                       "Parent collection is not a calendar collection");
            }

            // parse the calendar resource
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(in);

            // store the resource in the repository
            CalendarDao dao = (CalendarDao) applicationContext.
                getBean(BEAN_CALENDAR_DAO, CalendarDao.class);
            // XXX: different dao method for existing resource?
            dao.createCalendarResource(parent.getPath(),
                                       resource.getDisplayName(),
                                       calendar);
        } catch (UnsupportedFeatureException e) {
            log.error("calendar resource not supported", e);
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        } catch (DataIntegrityViolationException e) {
            log.error("resource " + resource.getResourcePath() +
                      " already exists", e);
            throw new DavException(CosmoDavResponse.SC_METHOD_NOT_ALLOWED);
        } catch (Exception e) {
            log.error("cannot add calendar resource", e);
            if (e instanceof DataAccessException &&
                e.getCause() instanceof RepositoryException) {
                throw new JcrDavException((RepositoryException) e.getCause());
            }
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
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

        try {
            Node resource = getNode();
            if (! resource.isNodeType(CosmoJcrConstants.NT_TICKETABLE)) {
                throw new DavException(CosmoDavResponse.SC_METHOD_NOT_ALLOWED,
                                       "Resource is not ticketable");
            }

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
    }

    /**
     * Returns the ticket with the given id on this resource. Does not
     * execute any security checks.
     */
    public Ticket getTicket(String id)
        throws DavException {
        if (!exists()) {
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        }

        try {
            TicketDao dao = (TicketDao) applicationContext.
                getBean(BEAN_TICKET_DAO, TicketDao.class);
            return dao.getTicket(getNode().getPath(), id);
        } catch (Exception e) {
            log.error("cannot get ticket " + id + " for resource " +
                      getResourcePath(), e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Returns all tickets owned by the named user on this resource,
     * or an empty <code>Set</code> if the user does not own any
     * tickets.
     *
     * @param username
     */
    public Set getTickets(String username)
        throws DavException {
        if (!exists()) {
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        }

        try {
            TicketDao dao = (TicketDao) applicationContext.
                getBean(BEAN_TICKET_DAO, TicketDao.class);
            return dao.getTickets(getNode().getPath(), username);
        } catch (Exception e) {
            log.error("cannot get tickets owned by " + username +
                      " for resource " + getResourcePath(), e);
            throw new DavException(CosmoDavResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Returns all tickets owned by the currently logged in user on
     * this resource, or an empty <code>Set</code> if the user does
     * not own any tickets.
     */
    public Set getLoggedInUserTickets()
        throws DavException {
        return getTickets(getLoggedInUser().getUsername());
    }

    /**
     * Returns a resource locator for the named principal's homedir.
     */
    public DavResourceLocator getHomedirLocator(String principal) {
        return getLocator().getFactory().
            createResourceLocator(baseUrl, "/" + principal);
    }

    // ApplicationContextAware methods

    /**
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // our methods

    /**
     * Return true if the given item should not be included in the
     * members list.
     *
     * @see DavResource#getMembers()
     */
    protected boolean isPrivateItem(Node node) {
        try {
            return node.isNodeType(CosmoJcrConstants.NT_ICAL_CALENDAR) ||
                node.isNodeType(CosmoJcrConstants.NT_TICKET);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

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
}
