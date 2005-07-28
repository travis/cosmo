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
package org.osaf.cosmo.dao.jcr;

import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.commons.spring.jcr.JCRCallback;
import org.osaf.commons.spring.jcr.support.JCRDaoSupport;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

import net.fortuna.ical4j.model.component.VEvent;

/**
 * Default implementation of {@link CalendarDao}.
 */
public class JCRCalendarDao extends JCRDaoSupport implements CalendarDao {
    private static final Log log = LogFactory.getLog(JCRCalendarDao.class);

    /**
     * Creates a calendar in the repository.
     *
     * @param path the repository path of the parent node of the new
     * collection
     * @param name the name of the new collection
     */
    public void createCalendar(final String path,
                               final String name) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    // find parent node
                    Node parent = JCRUtils.findNode(session, path);

                    // add calendar node
                    if (log.isDebugEnabled()) {
                        log.debug("creating calendar node " + name + " below " +
                                  parent.getPath());
                    }
                    Node cc = parent.
                        addNode(name, CosmoJcrConstants.NT_CALDAV_COLLECTION);
                    cc.addMixin(CosmoJcrConstants.NT_TICKETABLE);

                    // add subnodes representing calendar properties
                    // to the autocreated calendar node

                    Node calendar = cc.
                        getNode(CosmoJcrConstants.NN_ICAL_CALENDAR);

                    if (log.isDebugEnabled()) {
                        log.debug("adding prodid node below " +
                                  calendar.getPath());
                    }
                    Node prodid = calendar.
                        addNode(CosmoJcrConstants.NN_ICAL_PRODID);
                    prodid.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                       CosmoConstants.PRODUCT_ID);
        
                    if (log.isDebugEnabled()) {
                        log.debug("adding version node below " +
                                  calendar.getPath());
                    }
                    Node version = calendar.
                        addNode(CosmoJcrConstants.NN_ICAL_VERSION);
                    version.setProperty(CosmoJcrConstants.NP_ICAL_MAXVERSION,
                                        CosmoConstants.ICALENDAR_VERSION);

                    // CALSCALE: we only support Gregorian, so as per
                    // RFC 2445 section 4.7.1, we don't need to set it
                    
                    // METHOD: only used for iTIP scheduling, not
                    // necessary for provisioning

                    parent.save();

                    return cc;
                }
            });
    }

    /**
     * Returns true if a calendar exists at the given path,
     * false otherwise
     *
     * @param path the repository path to test for existence
     */
    public boolean existsCalendar(final String path) {
        return ((Boolean) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("checking existence of calendar at " +
                                      path);
                        }
                        Node cc = JCRUtils.findNode(session, path);
                        if (! cc.isNodeType(CosmoJcrConstants.
                                            NT_CALDAV_COLLECTION)) {
                            throw new InvalidDataAccessResourceUsageException("node at path " + path + " is not a calendar");
                        }
                        return Boolean.TRUE;
                    } catch (PathNotFoundException e) {
                        return Boolean.FALSE;
                    }
                }
            })).booleanValue();
    }

    /**
     * Removes the calendar at the given path.
     *
     * @param path the repository path of the calendar to
     * remove
     */
    public void deleteCalendar(final String path) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    if (log.isDebugEnabled()) {
                        log.debug("deleting calendar at " + path);
                    }
                    JCRUtils.findNode(session, path).remove();
                    session.save();
                    return null;
                }
            });
    }


    /**
     * Creates an event resource underneath the item at the given
     * path.
     *
     * @param path the repository path of the parent of the new
     * event resource
     * @param name the name of the new event resource
     * @param event the <code>VEvent</code> representing the new event
     */
    public void createEventResource(String path,
                                    String name,
                                    VEvent event) {
        createEventResource(path, name, event, null);
    }

    /**
     * Creates an event resource underneath the item at the given
     * path containing a master event defining a recurrence rule and a
     * set of events that are exceptions to the recurrence.
     *
     * @param path the repository path of the parent of the new
     * event resource
     * @param name the name of the new event resource
     * @param masterEvent the <code>VEvent</code> representing
     * the master event
     * @param exceptionEvents the <code>Set</code> of
     * <code>VEvent</code>s representing the exception events
     */
    public void createEventResource(final String path,
                                    final String name,
                                    final VEvent masterEvent,
                                    final Set exceptionEvents) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    // find parent node
                    Node parent = JCRUtils.findNode(session, path);

                    // add resource node
                    if (log.isDebugEnabled()) {
                        log.debug("creating calendar resource node " + name +
                                  " below " + parent.getPath());
                    }
                    Node resource = parent.
                        addNode(name, CosmoJcrConstants.NT_CALDAV_RESOURCE);
                    JCRUtils.setDateValue(resource,
                                          CosmoJcrConstants.NP_JCR_LASTMODIFIED,
                                          null);

                    // XXX: refactor JCRUtils.setEventNode immediately
                    // XXX: check for uids already in use
                    // XXX: incorporate alarms
                    // XXX: what about timezones? does ical4j handle
                    // them transparently?

                    // add master event node
                    if (log.isDebugEnabled()) {
                        log.debug("creating master event node below " +
                                  resource.getPath());
                    }
                    JCRUtils.setEventNode(masterEvent, resource);

                    // add exception event nodes
                    if (exceptionEvents != null) {
                        for (Iterator i=exceptionEvents.iterator();
                             i.hasNext();) {
                            VEvent exceptionEvent = (VEvent) i.next();
                            if (log.isDebugEnabled()) {
                                log.debug("creating exception event node" +
                                          " below " + resource.getPath());
                            }
                            JCRUtils.setEventNode(exceptionEvent, resource);
                        }
                    }

                    parent.save();
                    return null;
                }
            });
    }
}
