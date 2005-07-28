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
package org.osaf.cosmo.jcr;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.parameter.*;

import org.osaf.cosmo.icalendar.ICalendarUtils;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Utilities for working with JCR in Cosmo.
 */
public class JCRUtils {

    /**
     * Return the node at the given path.
     */
    public static Node findNode(Session session, String path)
        throws RepositoryException {
        Item item = session.getItem(path);
        if (! item.isNode()) {
            throw new InvalidDataAccessResourceUsageException("item at path " + path + " is not a node");
        }
        return (Node) item;
    }

    /**
     * Find the deepest existing node on the given path.
     */
    public static Node findDeepestExistingNode(Session session, String path)
        throws RepositoryException {
        // try for the deepest node first
        try {
            return findNode(session, path);
        } catch (PathNotFoundException e) {
            // not there, so we'll look for an ancestor
        }

        // walk the path to find the deepest existing node from the
        // top down
        Node node = session.getRootNode();
        if (path.equals("/")) {
            return node;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] names = path.split("/");
        Node parentNode = null;
        for (int i=0; i<names.length; i++) {
            try {
                parentNode = node;
                node = parentNode.getNode(names[i]);
            } catch (PathNotFoundException e) {
                // previous one was the last existing
                node = parentNode;
                break;
            }
        }

        return node;
    }

    // accessors and mutators for nodes representing icalendar
    // components

    /**
     */
    public static Node getEventNode(VEvent event,
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
        String reventUid =
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
            Date exRecurrenceId =
                getDateValue(exeventNode.
                             getNode(CosmoJcrConstants.NN_ICAL_RECURRENCEID),
                             CosmoJcrConstants.NP_ICAL_DATETIME);
            if (recurrenceId.getTime() == exRecurrenceId.getTime()) {
                return exeventNode;
            }
        }

        return null;
    }

    /**
     */
    public static void setEventNode(VEvent event,
                                    Node resourceNode)
        throws RepositoryException {
        Node eventNode = getEventNode(event, resourceNode);
        if (eventNode == null) {
            eventNode =
                resourceNode.addNode(CosmoJcrConstants.NN_ICAL_COMPONENT,
                                     CosmoJcrConstants.NT_ICAL_EVENT);
        }

        setClassPropertyNode(event, eventNode);
    }

    // accessors for nodes representing icalendar properties

    /**
     */
    public static void setClassPropertyNode(Component component,
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
    public static void setCreatedPropertyNode(Component component,
                                              Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_CREATED,
                                     componentNode);
        Date propertyValue = ICalendarUtils.getCreated(component).getDateTime();
        setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                     propertyValue);
    }

    /**
     */
    public static void setDescriptionPropertyNode(Component component,
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
    public static void setDtStartPropertyNode(Component component,
                                              Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTSTART,
                                     componentNode);
        DtStart dtStart = ICalendarUtils.getDtStart(component);
        setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                     dtStart.getTime());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                 dtStart.isUtc());
    }

    /**
     */
    public static void setGeoPropertyNode(Component component,
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
    public static void setLastModifiedPropertyNode(Component component,
                                                   Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_LASTMODIFIED,
                                     componentNode);
        Date propertyValue =
            ICalendarUtils.getLastModified(component).getDateTime();
        setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                     propertyValue);
    }

    /**
     */
    public static void setLocationPropertyNode(Component component,
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
    public static void setOrganizerPropertyNode(Component component,
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
    public static void setPriorityPropertyNode(Component component,
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
    public static void setDtStampPropertyNode(Component component,
                                              Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTSTAMP,
                                     componentNode);
        DtStamp dtStamp = ICalendarUtils.getDtStamp(component);
        setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                     dtStamp.getDateTime());
    }

    /**
     */
    public static void setSequencePropertyNode(Component component,
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
    public static void setStatusPropertyNode(Component component,
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
    public static void setSummaryPropertyNode(Component component,
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
    public static void setTranspPropertyNode(Component component,
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
    public static void setUidPropertyNode(Component component,
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
    public static void setUrlPropertyNode(Component component,
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
    public static void setRecurrenceIdPropertyNode(Component component,
                                                   Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RECURRENCEID,
                                     componentNode);
        RecurrenceId recurrenceId = ICalendarUtils.getRecurrenceId(component);
        setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                     recurrenceId.getTime());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                 recurrenceId.isUtc());
        Range range = ICalendarUtils.getRange(recurrenceId);
        propertyNode.setProperty(CosmoJcrConstants.NN_ICAL_RANGE,
                                 range.getValue());
    }

    /**
     */
    public static void setDtEndPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_DTEND,
                                     componentNode);
        DtEnd dtEnd = ICalendarUtils.getDtEnd(component);
        setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                     dtEnd.getTime());
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                 dtEnd.isUtc());
    }

    /**
     */
    public static void setDurationPropertyNode(Component component,
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
    public static void setAttachPropertyNode(Component component,
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
    public static void setAttendeePropertyNode(Component component,
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
    public static void setCategoriesPropertyNode(Component component,
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
    public static void setCommentPropertyNode(Component component,
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
    public static void setContactPropertyNode(Component component,
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
    public static void setExDatePropertyNode(Component component,
                                             Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_EXDATE,
                                     componentNode);
        ExDate exDate =ICalendarUtils.getExDate(component);
        for (Iterator i=exDate.getDates().iterator(); i.hasNext();) {
            Date date = (Date) i.next();
            setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                         date);
        }
    }

    /**
     */
    public static void setExRulePropertyNode(Component component,
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
    public static void setRequestStatusPropertyNode(Component component,
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
    public static void setRelatedToPropertyNode(Component component,
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
    public static void setResourcesPropertyNode(Component component,
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
    public static void setRDatePropertyNode(Component component,
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
            setDateValue(propertyNode, CosmoJcrConstants.NP_ICAL_DATETIME,
                            date);
        }
    }

    /**
     */
    public static void setRRulePropertyNode(Component component,
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
    protected static Node getICalendarPropertyNode(String propertyNodeName,
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
    protected static void setICalendarTextPropertyNodes(Property property,
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
    protected static void setICalendarRecurValueNode(Recur recur,
                                                     Node propertyNode)
        throws RepositoryException {
        Node recurNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RECUR,
                                     propertyNode);
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_FREQ,
                              recur.getFrequency());
        setDateValue(recurNode, CosmoJcrConstants.NP_ICAL_UNTIL,
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
    protected static void setICalendarPeriodValueNode(Period period,
                                                      Node propertyNode)
        throws RepositoryException {
        Node periodNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_PERIOD,
                                     propertyNode);
        setDateValue(periodNode, CosmoJcrConstants.NP_ICAL_START,
                     period.getStart());
        setDateValue(periodNode, CosmoJcrConstants.NP_ICAL_END,
                     period.getEnd());
    }

    // low level accessors and mutators for specifically-typed JCR
    // property values

    /**
     */
    public static Date getDateValue(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getDate().getTime();
    }

    /**
     */
    public static void setDateValue(Node node, String property, Date value)
        throws RepositoryException {
        Calendar calendar = Calendar.getInstance();
        if (value != null) {
            calendar.setTime(value);
        }
        node.setProperty(property, calendar);
    }

    /**
     */
    public static String getStringValue(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getString();
    }

    /**
     */
    public static Value[] getValues(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getValues();
    }
}
