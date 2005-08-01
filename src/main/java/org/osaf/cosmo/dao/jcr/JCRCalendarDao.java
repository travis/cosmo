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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.parameter.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.commons.spring.jcr.JCRCallback;
import org.osaf.commons.spring.jcr.support.JCRDaoSupport;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.icalendar.ICalendarUtils;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Default implementation of {@link CalendarDao}.
 */
public class JCRCalendarDao extends JCRDaoSupport implements CalendarDao {
    private static final Log log = LogFactory.getLog(JCRCalendarDao.class);

    // CalendarDao methods

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
                    version.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                        CosmoConstants.ICALENDAR_VERSION);
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
    public void createEvent(String path,
                            String name,
                            VEvent event) {
        createEvent(path, name, event, null);
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
    public void createEvent(final String path,
                            final String name,
                            final VEvent masterEvent,
                            final Set exceptionEvents) {
        getTemplate().execute(new JCRCallback() {
                public Object doInJCR(Session session)
                    throws RepositoryException {
                    // find parent node
                    Node parentNode = JCRUtils.findNode(session, path);

                    // add resource node
                    if (log.isDebugEnabled()) {
                        log.debug("creating calendar resource node " + name +
                                  " below " + parentNode.getPath());
                    }
                    Node resourceNode = parentNode.
                        addNode(name,
                                CosmoJcrConstants.NT_CALDAV_EVENT_RESOURCE);
                    resourceNode.addMixin(CosmoJcrConstants.NT_TICKETABLE);
                    JCRUtils.setDateValue(resourceNode,
                                          CosmoJcrConstants.NP_JCR_LASTMODIFIED,
                                          null);

                    // XXX: check for uids already in use
                    // XXX: alarms
                    // XXX: timezones
                    // XXX: is this the correct way to handle recurrence?

                    // add master event node
                    if (log.isDebugEnabled()) {
                        log.debug("creating revent node below " +
                                  resourceNode.getPath());
                    }
                    Node reventNode = resourceNode.
                        addNode(CosmoJcrConstants.NN_ICAL_REVENT);
                    setEventNodes(masterEvent, reventNode);

                    // add exception event nodes
                    if (exceptionEvents != null) {
                        for (Iterator i=exceptionEvents.iterator();
                             i.hasNext();) {
                            VEvent exevent = (VEvent) i.next();
                            if (log.isDebugEnabled()) {
                                log.debug("creating exception event node" +
                                          " below " + resourceNode.getPath());
                            }
                            Node exeventNode = resourceNode.
                                addNode(CosmoJcrConstants.NN_ICAL_EXEVENT);
                            setEventNodes(exevent, exeventNode);
                        }
                    }

                    parentNode.save();
                    return null;
                }
            });
    }

    // our methods

    /**
     */
    protected void setEventNodes(VEvent event,
                                 Node eventNode)
        throws RepositoryException {
        setClassPropertyNode(event, eventNode);
        setCreatedPropertyNode(event, eventNode);
        setDescriptionPropertyNode(event, eventNode);
        setDtStartPropertyNode(event, eventNode);
        setGeoPropertyNode(event, eventNode);
        setLastModifiedPropertyNode(event, eventNode);
        setLocationPropertyNode(event, eventNode);
        setOrganizerPropertyNode(event, eventNode);
        setPriorityPropertyNode(event, eventNode);
        setDtStampPropertyNode(event, eventNode);
        setSequencePropertyNode(event, eventNode);
        setStatusPropertyNode(event, eventNode);
        setSummaryPropertyNode(event, eventNode);
        setTranspPropertyNode(event, eventNode);
        setUidPropertyNode(event, eventNode);
        setUrlPropertyNode(event, eventNode);
        setRecurrenceIdPropertyNode(event, eventNode);
        setDtEndPropertyNode(event, eventNode);
        setDurationPropertyNode(event, eventNode);
        setAttachPropertyNode(event, eventNode);
        setAttendeePropertyNode(event, eventNode);
        setCategoriesPropertyNode(event, eventNode);
        setCommentPropertyNode(event, eventNode);
        setContactPropertyNode(event, eventNode);
        setExDatePropertyNode(event, eventNode);
        setExRulePropertyNode(event, eventNode);
        setRequestStatusPropertyNode(event, eventNode);
        setRelatedToPropertyNode(event, eventNode);
        setResourcesPropertyNode(event, eventNode);
        setRDatePropertyNode(event, eventNode);
        setRRulePropertyNode(event, eventNode);
    }

    /**
     */
    protected void setClassPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        Clazz clazz = ICalendarUtils.getClazz(component);
        if (clazz != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CLASS,
                                         componentNode);
            setICalendarPropertyValueNode(clazz, propertyNode);
        }
    }

    /**
     */
    protected void setCreatedPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Created created = ICalendarUtils.getCreated(component);
        if (created != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CREATED,
                                         componentNode);
            setICalendarPropertyValueNode(created, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  created.getDateTime());
        }
    }

    /**
     */
    protected void setDescriptionPropertyNode(Component component,
                                              Node componentNode)
        throws RepositoryException {
        Description description = ICalendarUtils.getDescription(component);
        if (description != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DESCRIPTION,
                                         componentNode);
            setICalendarPropertyValueNode(description, propertyNode);
            setICalendarTextPropertyNodes(description, propertyNode);
        }
    }

    /**
     */
    protected void setDtStartPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        DtStart dtStart = ICalendarUtils.getDtStart(component);
        if (dtStart != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTSTART,
                                         componentNode);
            setICalendarPropertyValueNode(dtStart, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  dtStart.getTime());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                     dtStart.isUtc());
            setTzIdParameterNode(dtStart, propertyNode);
        }
    }

    /**
     */
    protected void setGeoPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Geo geo = ICalendarUtils.getGeo(component);
        if (geo != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_GEO,
                                         componentNode);
            setICalendarPropertyValueNode(geo, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LATITUDE,
                                     geo.getLattitude());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LONGITUDE,
                                     geo.getLongitude());
        }
    }

    /**
     */
    protected void setLastModifiedPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        LastModified lastMod = ICalendarUtils.getLastModified(component);
        if (lastMod != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_LASTMODIFIED,
                                         componentNode);
            setICalendarPropertyValueNode(lastMod, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  lastMod.getDateTime());
        }
    }

    /**
     */
    protected void setLocationPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Location location = ICalendarUtils.getLocation(component);
        if (location != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_LOCATION,
                                         componentNode);
            setICalendarPropertyValueNode(location, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                     location.getValue());
        }
    }

    /**
     */
    protected void setOrganizerPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        Organizer organizer = ICalendarUtils.getOrganizer(component);
        if (organizer != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ORGANIZER,
                                         componentNode);
            setICalendarPropertyValueNode(organizer, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CALADDRESS,
                                     organizer.getCalAddress().toString());
            setCnParameterNode(organizer, propertyNode);
            setDirParameterNode(organizer, propertyNode);
            setSentByParameterNode(organizer, propertyNode);
            setLanguageParameterNode(organizer, propertyNode);
        }
    }

    /**
     */
    protected void setPriorityPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Priority priority = ICalendarUtils.getPriority(component);
        if (priority != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_PRIORITY,
                                         componentNode);
            setICalendarPropertyValueNode(priority, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LEVEL,
                                     priority.getLevel());
        }
    }

    /**
     */
    protected void setDtStampPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        DtStamp dtStamp = ICalendarUtils.getDtStamp(component);
        if (dtStamp != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTSTAMP,
                                         componentNode);
            setICalendarPropertyValueNode(dtStamp, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  dtStamp.getDateTime());
        }
    }

    /**
     */
    protected void setSequencePropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Sequence seq = ICalendarUtils.getSequence(component);
        if (seq != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_SEQ,
                                         componentNode);
            setICalendarPropertyValueNode(seq, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_SEQUENCENO,
                                     seq.getSequenceNo());
        }
    }

    /**
     */
    protected void setStatusPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Status status = ICalendarUtils.getStatus(component);
        if (status != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_STATUS,
                                         componentNode);
            setICalendarPropertyValueNode(status, propertyNode);
        }
    }

    /**
     */
    protected void setSummaryPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Summary summary = ICalendarUtils.getSummary(component);
        if (summary != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_SUMMARY,
                                         componentNode);
            setICalendarPropertyValueNode(summary, propertyNode);
            setICalendarTextPropertyNodes(summary, propertyNode);
        }
    }

    /**
     */
    protected void setTranspPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Transp transp = ICalendarUtils.getTransp(component);
        if (transp != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TRANSP,
                                         componentNode);
            setICalendarPropertyValueNode(transp, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                     transp.getValue());
        }
    }

    /**
     */
    protected void setUidPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Uid uid = ICalendarUtils.getUid(component);
        if (uid != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_UID,
                                         componentNode);
            setICalendarPropertyValueNode(uid, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                     uid.getValue());
        }
    }

    /**
     */
    protected void setUrlPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Url url = ICalendarUtils.getUrl(component);
        if (url != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_URL,
                                         componentNode);
            setICalendarPropertyValueNode(url, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_URI,
                                     url.getUri().toString());
        }
    }

    /**
     */
    protected void setRecurrenceIdPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        RecurrenceId recurrenceId = ICalendarUtils.getRecurrenceId(component);
        if (recurrenceId != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RECURRENCEID,
                                         componentNode);
            setICalendarPropertyValueNode(recurrenceId, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  recurrenceId.getTime());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                     recurrenceId.isUtc());
            setRangeParameterNode(recurrenceId, propertyNode);
        }
    }

    /**
     */
    protected void setDtEndPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        DtEnd dtEnd = ICalendarUtils.getDtEnd(component);
        if (dtEnd != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTEND,
                                         componentNode);
            setICalendarPropertyValueNode(dtEnd, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  dtEnd.getTime());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                     dtEnd.isUtc());
            setTzIdParameterNode(dtEnd, propertyNode);
        }
    }

    /**
     */
    protected void setDurationPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Duration duration = ICalendarUtils.getDuration(component);
        if (duration != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DURATION,
                                         componentNode);
            setICalendarPropertyValueNode(duration, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DURATION,
                                     duration.getDuration());
        }
    }

    /**
     */
    protected void setAttachPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Attach attach = ICalendarUtils.getAttach(component);
        if (attach != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ATTACH,
                                         componentNode);
            setICalendarPropertyValueNode(attach, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_URI,
                                     attach.getUri().toString());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_BINARY,
                                     new ByteArrayInputStream(attach.
                                                              getBinary()));
            setFmtTypeParameterNode(attach, propertyNode);
        }
    }

    /**
     */
    protected void setAttendeePropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Attendee attendee = ICalendarUtils.getAttendee(component);
        if (attendee != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ATTENDEE,
                                         componentNode);
            setICalendarPropertyValueNode(attendee, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CALADDRESS,
                                     attendee.getCalAddress().toString());
            setCuTypeParameterNode(attendee, propertyNode);
            setMemberParameterNode(attendee, propertyNode);
            setRoleParameterNode(attendee, propertyNode);
            setPartStatParameterNode(attendee, propertyNode);
            setRsvpParameterNode(attendee, propertyNode);
            setDelToParameterNode(attendee, propertyNode);
            setDelFromParameterNode(attendee, propertyNode);
            setSentByParameterNode(attendee, propertyNode);
            setCnParameterNode(attendee, propertyNode);
            setDirParameterNode(attendee, propertyNode);
            setLanguageParameterNode(attendee, propertyNode);
        }
    }

    /**
     */
    protected void setCategoriesPropertyNode(Component component,
                                             Node componentNode)
        throws RepositoryException {
        Categories categories = ICalendarUtils.getCategories(component);
        if (categories != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CATEGORIES,
                                         componentNode);
            setICalendarPropertyValueNode(categories, propertyNode);
            for (Iterator i=categories.getCategories().iterator();
                 i.hasNext();) {
                String category = (String) i.next();
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CATEGORY,
                                         category);
            }
            setLanguageParameterNode(categories, propertyNode);
        }
    }

    /**
     */
    protected void setCommentPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Comment comment = ICalendarUtils.getComment(component);
        if (comment != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_COMMENT,
                                         componentNode);
            setICalendarPropertyValueNode(comment, propertyNode);
            setICalendarTextPropertyNodes(comment, propertyNode);
        }
    }

    /**
     */
    protected void setContactPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Contact contact = ICalendarUtils.getContact(component);
        if (contact != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CONTACT,
                                         componentNode);
            setICalendarPropertyValueNode(contact, propertyNode);
            setICalendarTextPropertyNodes(contact, propertyNode);
        }
    }

    /**
     */
    protected void setExDatePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        ExDate exDate =ICalendarUtils.getExDate(component);
        if (exDate != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_EXDATE,
                                         componentNode);
            setICalendarPropertyValueNode(exDate, propertyNode);
            for (Iterator i=exDate.getDates().iterator(); i.hasNext();) {
                Date date = (Date) i.next();
                JCRUtils.setDateValue(propertyNode,
                                      CosmoJcrConstants.NP_ICAL_DATETIME, date);
            }
            setTzIdParameterNode(exDate, propertyNode);
        }
    }

    /**
     */
    protected void setExRulePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        ExRule exRule = ICalendarUtils.getExRule(component);
        if (exRule != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_EXRULE,
                                         componentNode);
            setICalendarPropertyValueNode(exRule, propertyNode);
            setICalendarRecurValueNode(exRule.getRecur(), propertyNode);
        }
    }

    /**
     */
    protected void setRequestStatusPropertyNode(Component component,
                                                Node componentNode)
        throws RepositoryException {
        RequestStatus requestStatus =
            ICalendarUtils.getRequestStatus(component);
        if (requestStatus != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.
                                         NN_ICAL_REQUESTSTATUS,
                                         componentNode);
            setICalendarPropertyValueNode(requestStatus, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_STATCODE,
                                     requestStatus.getStatusCode());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DESCRIPTION,
                                     requestStatus.getDescription());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_EXDATA,
                                     requestStatus.getExData());
            setLanguageParameterNode(requestStatus, propertyNode);
        }
    }

    /**
     */
    protected void setRelatedToPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        RelatedTo relatedTo = ICalendarUtils.getRelatedTo(component);
        if (relatedTo != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RELATEDTO,
                                         componentNode);
            setICalendarPropertyValueNode(relatedTo, propertyNode);
            setRelTypeParameterNode(relatedTo, propertyNode);
        }
    }

    /**
     */
    protected void setResourcesPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        Resources resources = ICalendarUtils.getResources(component);
        if (resources != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RESOURCES,
                                         componentNode);
            setICalendarPropertyValueNode(resources, propertyNode);
            for (Iterator i=resources.getResources().iterator(); i.hasNext();) {
                String str = (String) i.next();
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE, str);
            }
            setAltRepParameterNode(resources, propertyNode);
            setLanguageParameterNode(resources, propertyNode);
        }
    }

    /**
     */
    protected void setRDatePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        RDate rdate = ICalendarUtils.getRDate(component);
        if (rdate != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RDATE,
                                         componentNode);
            setICalendarPropertyValueNode(rdate, propertyNode);
            for (Iterator i=rdate.getPeriods().iterator(); i.hasNext();) {
                Period period = (Period) i.next();
                setICalendarPeriodValueNode(period, propertyNode);
            }
            for (Iterator i=rdate.getDates().iterator(); i.hasNext();) {
                Date date = (Date) i.next();
                JCRUtils.setDateValue(propertyNode,
                                      CosmoJcrConstants.NP_ICAL_DATETIME, date);
            }
            setTzIdParameterNode(rdate, propertyNode);
        }
    }

    /**
     */
    protected void setRRulePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        RRule rRule = ICalendarUtils.getRRule(component);
        if (rRule != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RRULE,
                                         componentNode);
            setICalendarPropertyValueNode(rRule, propertyNode);
            setICalendarRecurValueNode(rRule.getRecur(), propertyNode);
        }
    }

    /**
     */
    protected void setAltRepParameterNode(Property property,
                                          Node propertyNode)
        throws RepositoryException {
        AltRep altRep = ICalendarUtils.getAltRep(property);
        if (altRep != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_ALTREP,
                                     altRep.getValue());
        }
    }

    /**
     */
    protected void setCnParameterNode(Property property,
                                      Node propertyNode)
        throws RepositoryException {
        Cn cn = ICalendarUtils.getCn(property);
        if (cn != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CN,
                                     cn.getValue());
        }
    }

    /**
     */
    protected void setCuTypeParameterNode(Property property,
                                          Node propertyNode)
        throws RepositoryException {
        CuType cuType = ICalendarUtils.getCuType(property);
        if (cuType != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CUTYPE,
                                     cuType.getValue());
        }
    }

    /**
     */
    protected void setDelFromParameterNode(Property property,
                                         Node propertyNode)
        throws RepositoryException {
        DelegatedFrom delegatedFrom = ICalendarUtils.getDelegatedFrom(property);
        if (delegatedFrom != null) {
            for (Iterator i=delegatedFrom.getDelegators().iterator();
                 i.hasNext();) {
                URI uri = (URI) i.next();
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DELFROM,
                                         uri.toString());
            }
        }
    }

    /**
     */
    protected void setDelToParameterNode(Property property,
                                         Node propertyNode)
        throws RepositoryException {
        DelegatedTo delegatedTo = ICalendarUtils.getDelegatedTo(property);
        if (delegatedTo != null) {
            for (Iterator i=delegatedTo.getDelegatees().iterator();
                 i.hasNext();) {
                URI uri = (URI) i.next();
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DELTO,
                                         uri.toString());
            }
        }
    }

    /**
     */
    protected void setDirParameterNode(Property property,
                                       Node propertyNode)
        throws RepositoryException {
        Dir dir = ICalendarUtils.getDir(property);
        if (dir != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DIR,
                                     dir.getValue());
        }
    }

    /**
     */
    protected void setFmtTypeParameterNode(Property property,
                                           Node propertyNode)
        throws RepositoryException {
        FmtType fmtType = ICalendarUtils.getFmtType(property);
        if (fmtType != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_FMTTYPE,
                                     fmtType.getValue());
        }
    }

    /**
     */
    protected void setLanguageParameterNode(Property property,
                                            Node propertyNode)
        throws RepositoryException {
        Language language = ICalendarUtils.getLanguage(property);
        if (language != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                     language.getValue());
        }
    }

    /**
     */
    protected void setMemberParameterNode(Property property,
                                            Node propertyNode)
        throws RepositoryException {
        Member member = ICalendarUtils.getMember(property);
        if (member != null) {
            for (Iterator i=member.getGroups().iterator(); i.hasNext();) {
                URI uri = (URI) i.next();
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_MEMBER,
                                         uri.toString());
            }
        }
    }

    /**
     */
    protected void setPartStatParameterNode(Property property,
                                            Node propertyNode)
        throws RepositoryException {
        PartStat partStat = ICalendarUtils.getPartStat(property);
        if (partStat != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_PARTSTAT,
                                     partStat.getValue());
        }
    }

    /**
     */
    protected void setRangeParameterNode(Property property,
                                         Node propertyNode)
        throws RepositoryException {
        Range range = ICalendarUtils.getRange(property);
        if (range != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_RANGE,
                                     range.getValue());
        }
    }

    /**
     */
    protected void setRelTypeParameterNode(Property property,
                                           Node propertyNode)
        throws RepositoryException {
        RelType relType = ICalendarUtils.getRelType(property);
        if (relType != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_RELTYPE,
                                     relType.getValue());
        }
    }

    /**
     */
    protected void setRoleParameterNode(Property property,
                                        Node propertyNode)
        throws RepositoryException {
        Role role = ICalendarUtils.getRole(property);
        if (role != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_ROLE,
                                     role.getValue());
        }
    }

    /**
     */
    protected void setRsvpParameterNode(Property property,
                                        Node propertyNode)
        throws RepositoryException {
        Rsvp rsvp = ICalendarUtils.getRsvp(property);
        if (rsvp != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_RSVP,
                                     rsvp.getValue());
        }
    }

    /**
     */
    protected void setSentByParameterNode(Property property,
                                          Node propertyNode)
        throws RepositoryException {
        SentBy sentBy = ICalendarUtils.getSentBy(property);
        if (sentBy != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_SENTBY,
                                     sentBy.getValue());
        }
    }

    /**
     */
    protected void setTzIdParameterNode(Property property,
                                        Node propertyNode)
        throws RepositoryException {
        SentBy tzId = ICalendarUtils.getSentBy(property);
        if (tzId != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_TZID,
                                     tzId.getValue());
        }
    }

    // low level accessors and mutators for nodes representing
    // icalendar properties and parameters

    /**
     */
    protected Node getICalendarPropertyNode(String propertyNodeName,
                                            Node componentNode)
        throws RepositoryException {
        try {
            return componentNode.getNode(propertyNodeName);
        } catch (PathNotFoundException e) {
            return componentNode.addNode(propertyNodeName);
        }
    }

    /**
     */
    protected void setICalendarPropertyValueNode(Property property,
                                                 Node propertyNode)
        throws RepositoryException {
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 property.getValue());
    }

    /**
     */
    protected Node getICalendarParameterNode(String parameterNodeName,
                                             Node propertyNode)
        throws RepositoryException {
        try {
            return propertyNode.getNode(parameterNodeName);
        } catch (PathNotFoundException e) {
            return propertyNode.addNode(parameterNodeName);
        }
    }

    // low level mutators for nodes representing generic icalendar
    // property types

    /**
     */
    protected void setICalendarTextPropertyNodes(Property property,
                                                 Node propertyNode)
        throws RepositoryException {
        AltRep altRep = ICalendarUtils.getAltRep(property);
        if (altRep != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_ALTREP,
                                     altRep.getValue());
        }
        Language language = ICalendarUtils.getLanguage(property);
        if (language != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                     language.getValue());
        }
    }

    // low level mutators for nodes representing icalendar property
    // and parameter values

    /**
     */
    protected void setICalendarRecurValueNode(Recur recur,
                                              Node propertyNode)
        throws RepositoryException {
        Node recurNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RECUR,
                                     propertyNode);
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_FREQ,
                              recur.getFrequency());
        JCRUtils.setDateValue(recurNode, CosmoJcrConstants.NP_ICAL_UNTIL,
                              recur.getUntil());
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_COUNT,
                              recur.getCount());
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_INTERVAL,
                              recur.getInterval());
        for (Iterator i=recur.getSecondList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYSECOND,
                                  num.longValue());
        }
        for (Iterator i=recur.getMinuteList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYMINUTE,
                                  num.longValue());
        }
        for (Iterator i=recur.getHourList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYHOUR,
                                  num.longValue());
        }
        for (Iterator i=recur.getDayList().iterator(); i.hasNext();) {
            String str = (String) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYDAY, str);
        }
        for (Iterator i=recur.getMonthDayList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYMONTHDAY,
                                  num.longValue());
        }
        for (Iterator i=recur.getYearDayList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYYEARDAY,
                                  num.longValue());
        }
        for (Iterator i=recur.getWeekNoList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYWEEKNO,
                                  num.longValue());
        }
        for (Iterator i=recur.getMonthList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYMONTH,
                                  num.longValue());
        }
        for (Iterator i=recur.getSetPosList().iterator(); i.hasNext();) {
            Integer num = (Integer) i.next();
            recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYSETPOS,
                                  num.longValue());
        }
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_WKST,
                              recur.getWeekStartDay());
    }

    /**
     */
    protected void setICalendarPeriodValueNode(Period period,
                                               Node propertyNode)
        throws RepositoryException {
        Node periodNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_PERIOD,
                                     propertyNode);
        JCRUtils.setDateValue(periodNode, CosmoJcrConstants.NP_ICAL_START,
                              period.getStart());
        JCRUtils.setDateValue(periodNode, CosmoJcrConstants.NP_ICAL_END,
                              period.getEnd());
    }

    /**
     */
    public Node getEventNode(VEvent event,
                             Node resourceNode)
        throws RepositoryException {
        // if there is no revent node, there is no match
        if (! resourceNode.hasNode(CosmoJcrConstants.NN_ICAL_REVENT)) {
            return null;
        }

        // if the revent node's uid does not match the given
        // event's uid, there is no match
        String uid = ICalendarUtils.getUid(event).getValue();
        Node reventNode =
            resourceNode.getNode(CosmoJcrConstants.NN_ICAL_REVENT);
        String reventUid = JCRUtils.
            getStringValue(reventNode.getNode(CosmoJcrConstants.NN_ICAL_UID),
                           CosmoJcrConstants.NP_ICAL_VALUE);
        if (! uid.equals(reventUid)) {
            return null;
        }

        // if the given event has an rrule, return the revent node
        if (ICalendarUtils.getRRule(event) != null) {
            return reventNode;
        }

        // the given event has no rrule, so it must be an exevent
        Date recurrenceId = ICalendarUtils.getRecurrenceId(event).getTime();
        NodeIterator i =
            resourceNode.getNodes(CosmoJcrConstants.NN_ICAL_EXEVENT);
        while (i.hasNext()) {
            Node exeventNode = i.nextNode();

            // if the exception node's recurrence id does not match
            // the given event's recurrence id, there is no match
            Date exRecurrenceId = JCRUtils.
                getDateValue(exeventNode.
                             getNode(CosmoJcrConstants.NN_ICAL_RECURRENCEID),
                             CosmoJcrConstants.NP_ICAL_DATETIME);
            if (recurrenceId.getTime() == exRecurrenceId.getTime()) {
                return exeventNode;
            }
        }

        return null;
    }
}
