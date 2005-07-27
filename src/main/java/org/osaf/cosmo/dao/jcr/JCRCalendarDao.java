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

/**
 * Default implementation of {@link CalendarDao}.
 */
public class JCRCalendarDao extends JCRDaoSupport implements CalendarDao {
    private static final Log log = LogFactory.getLog(JCRCalendarDao.class);

    /**
     * Creates a calendar collection within which calendars may be
     * created.
     *
     * @param path the repository path of the parent node of the new
     * collection
     * @param name the name of the new collection
     */
    public void createCalendarCollection(final String path,
                                         final String name) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    // find parent node
                    Node parent = JCRUtils.findNode(session, path);

                    // add calendar collection node
                    if (log.isDebugEnabled()) {
                        log.debug("adding calendar collection node " + name +
                                  " below " + parent.getPath());
                    }
                    Node cc = parent.
                        addNode(name, CosmoJcrConstants.NT_CALDAV_COLLECTION);

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
     * Returns true if a calendar collection exists at the given path,
     * false otherwise
     *
     * @param path the repository path to test for existence
     */
    public boolean existsCalendarCollection(final String path) {
        return ((Boolean) getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("checking existence of " +
                                      "calendar collection at " + path);
                        }
                        Node cc = JCRUtils.findNode(session, path);
                        if (! cc.isNodeType(CosmoJcrConstants.
                                            NT_CALDAV_COLLECTION)) {
                            throw new InvalidDataAccessResourceUsageException("node at path " + path + " is not a calendar collection");
                        }
                        return Boolean.TRUE;
                    } catch (PathNotFoundException e) {
                        return Boolean.FALSE;
                    }
                }
            })).booleanValue();
    }

    /**
     * Removes the calendar collection at the given path and its
     * entire subtree.
     *
     * @param path the repository path of the calendar collection to
     * remove
     */
    public void deleteCalendarCollection(final String path) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    if (log.isDebugEnabled()) {
                        log.debug("deleting calendar collection at " + path);
                    }
                    JCRUtils.findNode(session, path).remove();
                    session.save();
                    return null;
                }
            });
    }
}
