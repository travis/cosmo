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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.CalendarDao;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import org.springmodules.jcr.JcrCallback;
import org.springmodules.jcr.support.JcrDaoSupport;

/**
 * Implementation of <code>CalendarDao</code> that operates against a
 * JCR repository.
 *
 * This implementation extends
 * {@link org.springmodules.jcr.JcrDaoSupport} to gain access to
 * a {@link org.springmodules.jcr.JcrTemplate}, which it uses to
 * obtain repository sessions. See the Spring Modules documentation
 * for more information on how to configure the template with
 * credentials, a repository reference and a workspace name.
 *
 * It uses {@link JcrCalendarMapper} to convert JCR nodes and
 * properties to and from instances of
 * {@link net.fortuna.ical4j.model.Calendar}.
 */
public class JcrCalendarDao extends JcrDaoSupport
    implements JcrConstants, CalendarDao {
    private static final Log log = LogFactory.getLog(JcrCalendarDao.class);

    // CalendarDao methods

    /**
     * Attaches a calendar object to a calendar resource, or updates a
     * calendar object already attached to a resource.
     *
     * This method does not save the calendar resource node. The
     * caller is responsible for saving!
     *
     * @param path the repository path of the resource to which
     * the calendar object is to be attached
     * @param calendar the calendar object to attach
     *
     * @throws DataRetrievalFailureException if the item at the given
     * path is not found
     * @throws InvalidDataResourceUsageException if the item at the
     * given path is not a node
     * @throws UnsupportedCalendarObjectException if the
     * <code>Calendar</code> does not contain any supported components
     * @throws RecurrenceException if recurrence is
     * improperly specified (no master instance, differing uids, etc)
     */
    public void storeCalendarObject(final String path,
                                    final Calendar calendar) {
        getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    if (! session.itemExists(path)) {
                        throw new DataRetrievalFailureException("item at path " + path + " not found");
                    }
                    Item item = session.getItem(path);
                    if (! item.isNode()) {
                        throw new InvalidDataAccessResourceUsageException("item at path " + path + " not a node");
                    }
                    Node node = (Node) item;

                    JcrCalendarMapper.calendarToNode(calendar, node);

                    return null;
                }
            });
    }

    /**
     * Returns the calendar object attached to a calendar resource.
     *
     * @param path the repository path of the calendar resource
     *
     * @throws DataRetrievalFailureException if the item at the given
     * path is not found
     * @throws InvalidDataResourceUsageException if the item at the
     * given path is not a node
     */
    public Calendar getCalendarObject(final String path) {
        return (Calendar) getJcrTemplate().execute(new JcrCallback() {
                public Object doInJcr(Session session)
                    throws RepositoryException {
                    if (! session.itemExists(path)) {
                        throw new DataRetrievalFailureException("item at path " + path + " not found");
                    }
                    Item item = session.getItem(path);
                    if (! item.isNode()) {
                        throw new InvalidDataAccessResourceUsageException("item at path " + path + " not a node");
                    }
                    Node node = (Node) item;

                    return JcrCalendarMapper.nodeToCalendar(node);
                }
            });
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        // does nothing
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods
}
