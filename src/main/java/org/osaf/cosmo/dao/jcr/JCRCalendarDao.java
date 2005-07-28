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
                    Node parentNode = JCRUtils.findNode(session, path);

                    // add resource node
                    if (log.isDebugEnabled()) {
                        log.debug("creating calendar resource node " + name +
                                  " below " + parentNode.getPath());
                    }
                    Node resourceNode = parentNode.
                        addNode(name, CosmoJcrConstants.NT_CALDAV_RESOURCE);
                    JCRUtils.setDateValue(resourceNode,
                                          CosmoJcrConstants.NP_JCR_LASTMODIFIED,
                                          null);

                    // XXX: check for uids already in use
                    // XXX: incorporate alarms
                    // XXX: what about timezones? does ical4j handle
                    // them transparently?
                    // XXX: is this the correct way to handle recurrence?

                    // add master event node
                    if (log.isDebugEnabled()) {
                        log.debug("creating revent node below " +
                                  resourceNode.getPath());
                    }
                    Node reventNode = resourceNode.
                        addNode(CosmoJcrConstants.NN_ICAL_REVENT);
                    setClassPropertyNode(masterEvent, reventNode);

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
                            setClassPropertyNode(exevent, exeventNode);
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
    protected void setClassPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CLASS,
                                     componentNode);
        String propertyValue = ICalendarUtils.getClazz(component).getValue();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 propertyValue);
    }

    /**
     */
    protected void setCreatedPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CREATED,
                                     componentNode);
        Date propertyValue = ICalendarUtils.getCreated(component).getDateTime();
        JCRUtils.setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                              propertyValue);
    }

    /**
     */
    protected void setDescriptionPropertyNode(Component component,
                                              Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DESCRIPTION,
                                     componentNode);
        Description description = ICalendarUtils.getDescription(component);
        setICalendarTextPropertyNodes(description, propertyNode);
    }

    /**
     */
    protected void setDtStartPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTSTART,
                                     componentNode);
        DtStart dtStart = ICalendarUtils.getDtStart(component);
        JCRUtils.setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                              dtStart.getTime());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                 dtStart.isUtc());
    }

    /**
     */
    protected void setGeoPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_GEO,
                                     componentNode);
        Geo geo = ICalendarUtils.getGeo(component);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LATITUDE,
                                 geo.getLattitude());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LONGITUDE,
                                 geo.getLongitude());
    }

    /**
     */
    protected void setLastModifiedPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_LASTMODIFIED,
                                     componentNode);
        Date propertyValue =
            ICalendarUtils.getLastModified(component).getDateTime();
        JCRUtils.setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                              propertyValue);
    }

    /**
     */
    protected void setLocationPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_LOCATION,
                                     componentNode);
        String propertyValue = ICalendarUtils.getLocation(component).getValue();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_TEXT,
                                 propertyValue);
    }

    /**
     */
    protected void setOrganizerPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ORGANIZER,
                                     componentNode);
        Organizer organizer = ICalendarUtils.getOrganizer(component);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CALADDRESS,
                                 organizer.getValue());
        Cn cn = ICalendarUtils.getCn(organizer);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CN, cn.getValue());
        Dir dir = ICalendarUtils.getDir(organizer);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DIR, dir.getValue());
        SentBy sentBy = ICalendarUtils.getSentBy(organizer);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_SENTBY,
                                 sentBy.getValue());
        Language language = ICalendarUtils.getLanguage(organizer);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                 language.getValue());
    }

    /**
     */
    protected void setPriorityPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_PRIORITY,
                                     componentNode);
        int propertyValue = ICalendarUtils.getPriority(component).getLevel();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 propertyValue);
    }

    /**
     */
    protected void setDtStampPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTSTAMP,
                                     componentNode);
        DtStamp dtStamp = ICalendarUtils.getDtStamp(component);
        JCRUtils.setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                              dtStamp.getDateTime());
    }

    /**
     */
    protected void setSequencePropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_SEQ,
                                     componentNode);
        int propertyValue =
            ICalendarUtils.getSequence(component).getSequenceNo();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 propertyValue);
    }

    /**
     */
    protected void setStatusPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_STATUS,
                                     componentNode);
        String propertyValue = ICalendarUtils.getStatus(component).getValue();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 propertyValue);
    }

    /**
     */
    protected void setSummaryPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_SUMMARY,
                                     componentNode);
        Summary summary = ICalendarUtils.getSummary(component);
        setICalendarTextPropertyNodes(summary, propertyNode);
    }

    /**
     */
    protected void setTranspPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TRANSP,
                                     componentNode);
        String propertyValue = ICalendarUtils.getTransp(component).getValue();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 propertyValue);
    }

    /**
     */
    protected void setUidPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_UID,
                                     componentNode);
        String propertyValue = ICalendarUtils.getUid(component).getValue();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 propertyValue);
    }

    /**
     */
    protected void setUrlPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_URL,
                                     componentNode);
        String propertyValue = ICalendarUtils.getUrl(component).getValue();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_URI,
                                 propertyValue);
    }

    /**
     */
    protected void setRecurrenceIdPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RECURRENCEID,
                                     componentNode);
        RecurrenceId recurrenceId = ICalendarUtils.getRecurrenceId(component);
        JCRUtils.setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                              recurrenceId.getTime());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                 recurrenceId.isUtc());
        Range range = ICalendarUtils.getRange(recurrenceId);
        propertyNode.setProperty(CosmoJcrConstants.NN_ICAL_RANGE,
                                 range.getValue());
    }

    /**
     */
    protected void setDtEndPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTEND,
                                     componentNode);
        DtEnd dtEnd = ICalendarUtils.getDtEnd(component);
        JCRUtils.setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                              dtEnd.getTime());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                 dtEnd.isUtc());
    }

    /**
     */
    protected void setDurationPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DURATION,
                                     componentNode);
        long propertyValue =
            ICalendarUtils.getDuration(component).getDuration();
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 propertyValue);
    }

    /**
     */
    protected void setAttachPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ATTACH,
                                     componentNode);
        Attach attach = ICalendarUtils.getAttach(component);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_URI,
                                 attach.getUri().toString());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_BINARY,
                                 new ByteArrayInputStream(attach.getBinary()));
        FmtType fmtType = ICalendarUtils.getFmtType(attach);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_FMTTYPE,
                                 fmtType.getValue());
    }

    /**
     */
    protected void setAttendeePropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ATTENDEE,
                                     componentNode);
        Attendee attendee = ICalendarUtils.getAttendee(component);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CALADDRESS,
                                 attendee.getValue());
        CuType cuType = ICalendarUtils.getCuType(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CUTYPE,
                                 cuType.getValue());
        Member member = ICalendarUtils.getMember(attendee);
        for (Iterator i=member.getGroups().iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_MEMBER,
                                     uri.toString());
        }
        Role role = ICalendarUtils.getRole(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_ROLE,
                                 role.getValue());
        PartStat partStat = ICalendarUtils.getPartStat(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_PARTSTAT,
                                 partStat.getValue());
        Rsvp rsvp = ICalendarUtils.getRsvp(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_RSVP,
                                 rsvp.getRsvp().booleanValue());
        DelegatedTo delTo = ICalendarUtils.getDelegatedTo(attendee);
        for (Iterator i=delTo.getDelegatees().iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DELTO,
                                     uri.toString());
        }
        DelegatedFrom delFrom = ICalendarUtils.getDelegatedFrom(attendee);
        for (Iterator i=delFrom.getDelegators().iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DELFROM,
                                     uri.toString());
        }
        SentBy sentBy = ICalendarUtils.getSentBy(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_SENTBY,
                                 sentBy.getValue());
        Cn cn = ICalendarUtils.getCn(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CN, cn.getValue());
        Dir dir = ICalendarUtils.getDir(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DIR, dir.getValue());
        Language language = ICalendarUtils.getLanguage(attendee);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                 language.getValue());
    }

    /**
     */
    protected void setCategoriesPropertyNode(Component component,
                                             Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CATEGORIES,
                                     componentNode);
        Categories categories = ICalendarUtils.getCategories(component);
        for (Iterator i=categories.getCategories().iterator(); i.hasNext();) {
            String category = (String) i.next();
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CATEGORY,
                                     category);
        }
        Language language = ICalendarUtils.getLanguage(categories);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                 language.getValue());
    }

    /**
     */
    protected void setCommentPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_COMMENT,
                                     componentNode);
        Comment comment = ICalendarUtils.getComment(component);
        setICalendarTextPropertyNodes(comment, propertyNode);
    }

    /**
     */
    protected void setContactPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CONTACT,
                                     componentNode);
        Contact contact = ICalendarUtils.getContact(component);
        setICalendarTextPropertyNodes(contact, propertyNode);
    }

    /**
     */
    protected void setExDatePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_EXDATE,
                                     componentNode);
        ExDate exDate =ICalendarUtils.getExDate(component);
        for (Iterator i=exDate.getDates().iterator(); i.hasNext();) {
            Date date = (Date) i.next();
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME, date);
        }
    }

    /**
     */
    protected void setExRulePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_EXRULE,
                                     componentNode);
        ExRule exRule = ICalendarUtils.getExRule(component);
        setICalendarRecurValueNode(exRule.getRecur(), propertyNode);
    }

    /**
     */
    protected void setRequestStatusPropertyNode(Component component,
                                                Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_REQUESTSTATUS,
                                     componentNode);
        RequestStatus requestStatus =
            ICalendarUtils.getRequestStatus(component);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_STATCODE,
                                 requestStatus.getStatusCode());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DESCRIPTION,
                                 requestStatus.getDescription());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_EXDATA,
                                 requestStatus.getExData());
        Language language = ICalendarUtils.getLanguage(requestStatus);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                 language.getValue());
    }

    /**
     */
    protected void setRelatedToPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RELATEDTO,
                                     componentNode);
        RelatedTo relatedTo = ICalendarUtils.getRelatedTo(component);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 relatedTo.getValue());
        RelType relType = ICalendarUtils.getRelType(relatedTo);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_RELTYPE,
                                 relType.getValue());
    }

    /**
     */
    protected void setResourcesPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RESOURCES,
                                     componentNode);
        Resources resources = ICalendarUtils.getResources(component);
        for (Iterator i=resources.getResources().iterator(); i.hasNext();) {
            String str = (String) i.next();
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE, str);
        }
        AltRep altRep = ICalendarUtils.getAltRep(resources);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_ALTREP,
                                 altRep.getValue());
        Language language = ICalendarUtils.getLanguage(resources);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                 language.getValue());
    }

    /**
     */
    protected void setRDatePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RDATE,
                                     componentNode);
        RDate rdate = ICalendarUtils.getRDate(component);
        for (Iterator i=rdate.getPeriods().iterator(); i.hasNext();) {
            Period period = (Period) i.next();
            setICalendarPeriodValueNode(period, propertyNode);
        }
        for (Iterator i=rdate.getDates().iterator(); i.hasNext();) {
            Date date = (Date) i.next();
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME, date);
        }
    }

    /**
     */
    protected void setRRulePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RRULE,
                                     componentNode);
        RRule rRule = ICalendarUtils.getRRule(component);
        setICalendarRecurValueNode(rRule.getRecur(), propertyNode);
    }

    // low level accessor for nodes representing icalendar properties

    /**
     */
    protected Node getICalendarPropertyNode(String propertyNodeName,
                                            Node componentNode)
        throws RepositoryException {
        Node propertyNode = componentNode.getNode(propertyNodeName);
        if (propertyNode == null) {
            propertyNode = componentNode.addNode(propertyNodeName);
        }
        return propertyNode;
    }

    // low level mutators for nodes representing generic icalendar
    // property types

    /**
     */
    protected void setICalendarTextPropertyNodes(Property property,
                                                 Node propertyNode)
        throws RepositoryException {
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_TEXT,
                                 property.getValue());
        AltRep altRep = ICalendarUtils.getAltRep(property);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_ALTREP,
                                 altRep.getValue());
        Language language = ICalendarUtils.getLanguage(property);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_LANGUAGE,
                                 language.getValue());
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
