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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.parameter.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.commons.spring.jcr.support.JCRExceptionTranslator;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.UnsupportedFeatureException;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.icalendar.CosmoICalendarConstants;
import org.osaf.cosmo.icalendar.DuplicateUidException;
import org.osaf.cosmo.icalendar.ICalendarUtils;
import org.osaf.cosmo.icalendar.RecurrenceSet;
import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;

/**
 * Default implementation of {@link CalendarDao}.
 */
public class JCRCalendarDao implements CalendarDao {
    private static final Log log = LogFactory.getLog(JCRCalendarDao.class);

    // CalendarDao methods

    /**
     * Creates a calendar collection in the repository.
     *
     * @param node the <code>Node</code> to which the calendar
     * collection will be attached.
     * @param name the name of the new collection
     */
    public void createCalendarCollection(Node node,
                                         String name) {
        try {
            // add calendar node
            Node cc =
                node.addNode(name, CosmoJcrConstants.NT_DAV_COLLECTION);
            cc.addMixin(CosmoJcrConstants.NT_TICKETABLE);
            cc.addMixin(CosmoJcrConstants.NT_CALDAV_COLLECTION);
            cc.setProperty(CosmoJcrConstants.NP_DAV_DISPLAYNAME, name);
            cc.setProperty(CosmoJcrConstants.
                           NP_CALDAV_CALENDARDESCRIPTION, name);
            cc.setProperty(CosmoJcrConstants.NP_XML_LANG,
                           Locale.getDefault().toString());
        } catch (RepositoryException e) {
            log.error("JCR error creating calendar collection", e);
            throw JCRExceptionTranslator.translate(e);
        }
    }

    /**
     * Attaches a calendar object to a node in the repository, or
     * updates an existing one.

     * A calendar object contains one or more calendar components. The
     * only supported "top level" calendar component is event. Events
     * are typically associated with timezones and alarms. Recurring
     * events are represented as multiple calendar components: one
     * "master" event that defines the recurrence rule, and zero or
     * more "exception" events. All of these components share a uid.
     *
     * Journal, todo and freebusy components are not supported. These
     * components will be ignored.
     *
     * @param node the <code>Node</code> to which the calendar object
     * will be attached
     * @param event the <code>Calendar</code> containing events,
     * timezones and alarms
     *
     * @throws {@link UnsupportedFeatureException} if the
     * <code>Calendar</code> does not contain an event.
     * @throws {@link RecurrenceFeatureException} if a recurring event
     * is improperly specified (no master event, etc)
     */
    public void storeCalendarObject(Node node,
                                    Calendar calendar) {
        // find all supported components within the calendar
        final RecurrenceSet events = new RecurrenceSet();
        final Map timezones = new HashMap();
        for (Iterator i=calendar.getComponents().iterator(); i.hasNext();) {
            Component component = (Component) i.next();
            if (component instanceof VEvent) {
                events.add(component);
            }
            else if (component instanceof VTimeZone) {
                VTimeZone tz = (VTimeZone) component;
                net.fortuna.ical4j.model.property.TzId tzid =
                    ICalendarUtils.getTzId(component);
                timezones.put(tzid, tz);
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("ignoring unsupported component " +
                              component.getName());
                }
            }
        }

        if (events.isEmpty()) {
            throw new UnsupportedFeatureException("No supported components" +
                                                  " found");
        }

        try {
            // this resource node is new, so set it up as an event resource
            if (! node.isNodeType(CosmoJcrConstants.NT_CALDAV_EVENT_RESOURCE)) {
                node.addMixin(CosmoJcrConstants.NT_CALDAV_EVENT_RESOURCE);
            }

            // it's possible (tho pathological) that the client will
            // change the resource's uid on an update, so always
            // verify and set it
            verifyUniqueUid(node, events.getUid());
            node.setProperty(CosmoJcrConstants.NP_CALDAV_UID, events.getUid());

            // add calendar components
            setEventNodes(events, node);
            setTimeZoneNodes(timezones, node);
        } catch (DuplicateUidException e) {
            // no need to warn about this
            throw e;
        } catch (RepositoryException e) {
            log.error("JCR error storing calendar", e);
            throw JCRExceptionTranslator.translate(e);
        } catch (RuntimeException e) {
            log.error("unknown error storing calendar", e);
            throw e;
        }
    }

    /**
     */
    public Calendar getCalendarObject(Node node) {
        try {
            if (node.isNodeType(CosmoJcrConstants.NT_DAV_COLLECTION)) {
                getCollectionCalendarObject(node);
            }
            // XXX: single object
            throw new UnsupportedOperationException("not a collection");
        } catch (RepositoryException e) {
            log.error("JCR error getting calendar", e);
            throw JCRExceptionTranslator.translate(e);
        }
    }

    // our methods

    /**
     */
    protected Calendar getCollectionCalendarObject(Node node)
        throws RepositoryException {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // add x-properties here if we ever need them

        // walk through child nodes and build a list of calendar
        // resources

        for (NodeIterator i=node.getNodes(); i.hasNext();) {
            Node childNode = i.nextNode();
            if (! childNode.isNodeType(CosmoJcrConstants.NT_CALDAV_RESOURCE)) {
                continue;
            }

            // XXX: convert node to components and add to calendar
        }

        return calendar;
    }

    /**
     */
    protected void verifyUniqueUid(Node node, String uid)
        throws RepositoryException {
        // look for nodes anywhere below the parent calendar
        // collection that have this same uid 
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root");
        if (! node.getParent().getPath().equals("/")) {
            stmt.append(node.getParent().getPath());
        }
        stmt.append("//element(*, ").
            append(CosmoJcrConstants.NT_CALDAV_RESOURCE).
            append(")");
        stmt.append("[@").
            append(CosmoJcrConstants.NP_CALDAV_UID).
            append(" = '").
            append(uid).
            append("']");

        QueryManager qm =
            node.getSession().getWorkspace().getQueryManager();
        QueryResult qr =
            qm.createQuery(stmt.toString(), Query.XPATH).execute();

        // if we are updating this node, then we expect it to show up
        // in the result, but nothing else
        for (NodeIterator i=qr.getNodes(); i.hasNext();) {
            Node n = (Node) i.next();
            if (! n.getPath().equals(node.getPath())) {
                throw new DuplicateUidException("Duplicate uid: " + uid);
            }
        }
    }

    /**
     */
    protected void setEventNodes(RecurrenceSet events,
                                 Node resourceNode)
        throws RepositoryException {
        // add or update the master event
        VEvent masterEvent = (VEvent) events.getMaster();
        Node masterNode =
            resourceNode.hasNode(CosmoJcrConstants.NN_ICAL_REVENT) ?
            resourceNode.getNode(CosmoJcrConstants.NN_ICAL_REVENT) :
            resourceNode.addNode(CosmoJcrConstants.NN_ICAL_REVENT);
        setEventPropertyNodes(masterEvent, masterNode);
        setAlarmNodes(masterEvent.getAlarms(), masterNode);

        // remove any exevent nodes for which a corresponding
        // exception event does not exist in the recurrence set.
        // save an index of the remaining exevent nodes so we can
        // update them later.
        Map updateIdx = new HashMap();
        for (NodeIterator i=resourceNode.
                 getNodes(CosmoJcrConstants.NN_ICAL_EXEVENT); i.hasNext();) {
            Node exeventNode = i.nextNode();
            Node recuridNode = exeventNode.
                getNode(CosmoJcrConstants.NN_ICAL_RECURRENCEID);
            Date recurid = recuridNode.
                getProperty(CosmoJcrConstants.NP_ICAL_DATETIME).getDate().
                getTime();
            Component event = events.get(recurid);
            if (event == null) {
                exeventNode.remove();
            }
            else {
                updateIdx.put(recurid, exeventNode);
            }
        }

        // add/update exevent nodes for each exception event in the
        // recurrence set
        for (Iterator i=events.getExceptions().iterator();
             i.hasNext();) {
            VEvent exceptionEvent = (VEvent) i.next();
            Date recurid =
                ICalendarUtils.getRecurrenceId(exceptionEvent).getTime();
            Node eventNode = (Node) updateIdx.get(recurid);
            if (eventNode == null) {
                eventNode =
                    resourceNode.addNode(CosmoJcrConstants.NN_ICAL_EXEVENT);
            }
            setEventPropertyNodes(exceptionEvent, eventNode);
            setAlarmNodes(exceptionEvent.getAlarms(), eventNode);
        }
    }

    /**
     */
    protected void setAlarmNodes(ComponentList alarms,
                                 Node componentNode)
        throws RepositoryException {
        // since there is no way to uniquely identify an
        // already-stored alarm, remove all pre-existing alarm nodes
        // from the component node, which means we don't have to worry
        // about finding one that matches the given alarm
        for (NodeIterator i=componentNode.
                 getNodes(CosmoJcrConstants.NN_ICAL_ALARM); i.hasNext();) {
            i.nextNode().remove();
        }
        for (Iterator i=alarms.iterator(); i.hasNext();) {
            VAlarm alarm = (VAlarm) i.next();
            Node alarmNode =
                componentNode.addNode(CosmoJcrConstants.NN_ICAL_ALARM);
            setAlarmNode(alarm, alarmNode);
        }
    }

    /**
     */
    protected void setAlarmNode(VAlarm alarm,
                                Node alarmNode)
        throws RepositoryException {
        setActionPropertyNode(alarm, alarmNode);
        setTriggerPropertyNode(alarm, alarmNode);
        setDurationPropertyNode(alarm, alarmNode);
        setRepeatPropertyNode(alarm, alarmNode);
        setAttachPropertyNode(alarm, alarmNode);
        setDescriptionPropertyNode(alarm, alarmNode);
        setSummaryPropertyNode(alarm, alarmNode);
        for (Iterator i=ICalendarUtils.getAttendees(alarm).iterator();
             i.hasNext();) {
            setAttendeePropertyNode((Attendee) i.next(), alarmNode);
        }
        setXPropertyNodes(alarm, alarmNode);
    }

    /**
     */
    protected void setEventPropertyNodes(VEvent event,
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
        setXPropertyNodes(event, eventNode);
    }

    /**
     */
    protected void setTimeZoneNodes(Map timezones,
                                    Node resourceNode)
        throws RepositoryException {
        // make a copy of the timezone map so that we can remove items
        // from it
        Map myTimezones = new HashMap(timezones);

        // find all timezone nodes, updating those that have a
        // corresponding timezone in the map (and clearing them out of
        // our local copy of the map) and removing those that don't.
        for (NodeIterator i=resourceNode.
                 getNodes(CosmoJcrConstants.NN_ICAL_TIMEZONE); i.hasNext();) {
            Node tzNode = i.nextNode();
            Node tzidNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TZID,
                                         tzNode);
            String tzid = getValueProperty(tzidNode).getString();
            VTimeZone timezone = (VTimeZone) myTimezones.get(tzid);
            if (timezone != null) {
                setTimeZoneNode(timezone, tzNode);
                myTimezones.remove(timezone);
            }
            else {
                tzNode.remove();
            }
        }

        // our local copy of the timezone map now only contains
        // entries for those timezones that have not already been
        // saved. set timezone nodes for each.
        for (Iterator i=myTimezones.values().iterator(); i.hasNext();) {
            VTimeZone timezone = (VTimeZone) i.next();
            setTimeZoneNode(timezone, resourceNode);
        }
    }

    /**
     */
    protected void setTimeZoneNode(VTimeZone timezone,
                                   Node inputNode)
        throws RepositoryException {
        // the input node can be either a timezone node itself (in
        // case of an update) or the parent resource node on which a
        // new timezone node should be created
        Node timezoneNode =
            inputNode.isNodeType(CosmoJcrConstants.NT_ICAL_TIMEZONE) ?
            inputNode :
            inputNode.addNode(CosmoJcrConstants.NN_ICAL_TIMEZONE);
        setTzIdPropertyNode(timezone, timezoneNode);
        setLastModifiedPropertyNode(timezone, timezoneNode);
        setTzUrlPropertyNode(timezone, timezoneNode);
        for (Iterator i=timezone.getTypes().iterator(); i.hasNext();) {
            setTimeZoneComponentNode((Component) i.next(), timezoneNode);
        }
        setXPropertyNodes(timezone, timezoneNode);
    }

    /**
     */
    protected void setTimeZoneComponentNode(Component component,
                                            Node timezoneNode)
        throws RepositoryException {
        String name = null;
        if (component.getName().equals(CosmoICalendarConstants.COMP_STANDARD)) {
            name = CosmoJcrConstants.NN_ICAL_STANDARD;
        }
        else if (component.getName().
                 equals(CosmoICalendarConstants.COMP_DAYLIGHT)) {
            name = CosmoJcrConstants.NN_ICAL_DAYLIGHT;
        }
        else {
            throw new IllegalArgumentException("unknown timezone component " +
                                               component.getName());
        }

        Node componentNode = null;
        try {
            componentNode = timezoneNode.getNode(name);
        } catch (PathNotFoundException e) {
            componentNode = timezoneNode.addNode(name);
        }

        setDtStartPropertyNode(component, componentNode);
        setTzOffsetToPropertyNode(component, componentNode);
        setTzOffsetFromPropertyNode(component, componentNode);
        for (Iterator i=ICalendarUtils.getComments(component).iterator();
             i.hasNext();) {
            setCommentPropertyNode((Comment) i.next(), componentNode);
        }
        for (Iterator i=ICalendarUtils.getRDates(component).iterator();
             i.hasNext();) {
            setRDatePropertyNode((RDate) i.next(), componentNode);
        }
        for (Iterator i=ICalendarUtils.getRRules(component).iterator();
             i.hasNext();) {
            setRRulePropertyNode((RRule) i.next(), componentNode);
        }
        for (Iterator i=ICalendarUtils.getTzNames(component).iterator();
             i.hasNext();) {
            setTzNamePropertyNode((TzName) i.next(), componentNode);
        }
        setXPropertyNodes(component, componentNode);
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
            setValueProperty(clazz, propertyNode);
            setXParameterProperties(clazz, propertyNode);
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
            setValueProperty(created, propertyNode);
            setXParameterProperties(created, propertyNode);
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
            setValueProperty(description, propertyNode);
            setXParameterProperties(description, propertyNode);
            setTextPropertyNodes(description, propertyNode);
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
            setValueProperty(dtStart, propertyNode);
            setXParameterProperties(dtStart, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  dtStart.getTime());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                     dtStart.isUtc());
            setTzIdParameterProperty(dtStart, propertyNode);
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
            setValueProperty(geo, propertyNode);
            setXParameterProperties(geo, propertyNode);
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
            setValueProperty(lastMod, propertyNode);
            setXParameterProperties(lastMod, propertyNode);
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
            setValueProperty(location, propertyNode);
            setXParameterProperties(location, propertyNode);
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
            setValueProperty(organizer, propertyNode);
            setXParameterProperties(organizer, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CALADDRESS,
                                     organizer.getCalAddress().toString());
            setCnParameterProperty(organizer, propertyNode);
            setDirParameterProperty(organizer, propertyNode);
            setSentByParameterProperty(organizer, propertyNode);
            setLanguageParameterProperty(organizer, propertyNode);
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
            setValueProperty(priority, propertyNode);
            setXParameterProperties(priority, propertyNode);
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
            setValueProperty(dtStamp, propertyNode);
            setXParameterProperties(dtStamp, propertyNode);
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
            setValueProperty(seq, propertyNode);
            setXParameterProperties(seq, propertyNode);
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
            setValueProperty(status, propertyNode);
            setXParameterProperties(status, propertyNode);
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
            setValueProperty(summary, propertyNode);
            setXParameterProperties(summary, propertyNode);
            setTextPropertyNodes(summary, propertyNode);
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
            setValueProperty(transp, propertyNode);
            setXParameterProperties(transp, propertyNode);
        }
    }

    /**
     */
    protected void setTzOffsetFromPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        TzOffsetFrom tzOffsetFrom = ICalendarUtils.getTzOffsetFrom(component);
        if (tzOffsetFrom != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TZOFFSETFROM,
                                         componentNode);
            setValueProperty(tzOffsetFrom, propertyNode);
            setXParameterProperties(tzOffsetFrom, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_OFFSET,
                                     tzOffsetFrom.getOffset());
        }
    }

    /**
     */
    protected void setTzOffsetToPropertyNode(Component component,
                                             Node componentNode)
        throws RepositoryException {
        TzOffsetTo tzOffsetTo = ICalendarUtils.getTzOffsetTo(component);
        if (tzOffsetTo != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TZOFFSETTO,
                                         componentNode);
            setValueProperty(tzOffsetTo, propertyNode);
            setXParameterProperties(tzOffsetTo, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_OFFSET,
                                     tzOffsetTo.getOffset());
        }
    }

    /**
     */
    protected void setTzUrlPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        TzUrl tzUrl = ICalendarUtils.getTzUrl(component);
        if (tzUrl != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TZURL,
                                         componentNode);
            setValueProperty(tzUrl, propertyNode);
            setXParameterProperties(tzUrl, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_URI,
                                     tzUrl.getUri().toString());
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
            setValueProperty(uid, propertyNode);
            setXParameterProperties(uid, propertyNode);
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
            setValueProperty(url, propertyNode);
            setXParameterProperties(url, propertyNode);
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
            setValueProperty(recurrenceId, propertyNode);
            setXParameterProperties(recurrenceId, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  recurrenceId.getTime());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                     recurrenceId.isUtc());
            setTzIdParameterProperty(recurrenceId, propertyNode);
            setRangeParameterProperty(recurrenceId, propertyNode);
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
            setValueProperty(dtEnd, propertyNode);
            setXParameterProperties(dtEnd, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  dtEnd.getTime());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_UTC,
                                     dtEnd.isUtc());
            setTzIdParameterProperty(dtEnd, propertyNode);
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
            setValueProperty(duration, propertyNode);
            setXParameterProperties(duration, propertyNode);
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
            setValueProperty(attach, propertyNode);
            setXParameterProperties(attach, propertyNode);
            if (attach.getUri() != null) {
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_URI,
                                         attach.getUri().toString());
            }
            if (attach.getBinary() != null) {
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_BINARY,
                                         new ByteArrayInputStream(attach.
                                                                  getBinary()));
            }
            setFmtTypeParameterProperty(attach, propertyNode);
        }
    }

    /**
     */
    protected void setAttendeePropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Attendee attendee = ICalendarUtils.getAttendee(component);
        if (attendee != null) {
            setAttendeePropertyNode(attendee, componentNode);
        }
    }

    /**
     */
    protected void setAttendeePropertyNode(Attendee attendee,
                                           Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ATTENDEE,
                                     componentNode);
        setValueProperty(attendee, propertyNode);
        setXParameterProperties(attendee, propertyNode);
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CALADDRESS,
                                 attendee.getCalAddress().toString());
        setCuTypeParameterProperty(attendee, propertyNode);
        setMemberParameterProperty(attendee, propertyNode);
        setRoleParameterProperty(attendee, propertyNode);
        setPartStatParameterProperty(attendee, propertyNode);
        setRsvpParameterProperty(attendee, propertyNode);
        setDelToParameterProperty(attendee, propertyNode);
        setDelFromParameterProperty(attendee, propertyNode);
        setSentByParameterProperty(attendee, propertyNode);
        setCnParameterProperty(attendee, propertyNode);
        setDirParameterProperty(attendee, propertyNode);
        setLanguageParameterProperty(attendee, propertyNode);
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
            setValueProperty(categories, propertyNode);
            setXParameterProperties(categories, propertyNode);
            for (Iterator i=categories.getCategories().iterator();
                 i.hasNext();) {
                String category = (String) i.next();
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_CATEGORY,
                                         category);
            }
            setLanguageParameterProperty(categories, propertyNode);
        }
    }

    /**
     */
    protected void setCommentPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Comment comment = ICalendarUtils.getComment(component);
        if (comment != null) {
            setCommentPropertyNode(comment, componentNode);
        }
    }

    /**
     */
    protected void setCommentPropertyNode(Comment comment,
                                          Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_COMMENT,
                                     componentNode);
        setValueProperty(comment, propertyNode);
        setXParameterProperties(comment, propertyNode);
        setTextPropertyNodes(comment, propertyNode);
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
            setValueProperty(contact, propertyNode);
            setXParameterProperties(contact, propertyNode);
            setTextPropertyNodes(contact, propertyNode);
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
            setValueProperty(exDate, propertyNode);
            setXParameterProperties(exDate, propertyNode);
            for (Iterator i=exDate.getDates().iterator(); i.hasNext();) {
                Date date = (Date) i.next();
                JCRUtils.setDateValue(propertyNode,
                                      CosmoJcrConstants.NP_ICAL_DATETIME, date);
            }
            setTzIdParameterProperty(exDate, propertyNode);
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
            setValueProperty(exRule, propertyNode);
            setXParameterProperties(exRule, propertyNode);
            setRecurValueNode(exRule.getRecur(), propertyNode);
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
            setValueProperty(requestStatus, propertyNode);
            setXParameterProperties(requestStatus, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_STATCODE,
                                     requestStatus.getStatusCode());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DESCRIPTION,
                                     requestStatus.getDescription());
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_EXDATA,
                                     requestStatus.getExData());
            setLanguageParameterProperty(requestStatus, propertyNode);
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
            setValueProperty(relatedTo, propertyNode);
            setXParameterProperties(relatedTo, propertyNode);
            setRelTypeParameterProperty(relatedTo, propertyNode);
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
            setValueProperty(resources, propertyNode);
            setXParameterProperties(resources, propertyNode);
            for (Iterator i=resources.getResources().iterator(); i.hasNext();) {
                String str = (String) i.next();
                propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE, str);
            }
            setAltRepParameterProperty(resources, propertyNode);
            setLanguageParameterProperty(resources, propertyNode);
        }
    }

    /**
     */
    protected void setRDatePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        RDate rDate = ICalendarUtils.getRDate(component);
        if (rDate != null) {
            setRDatePropertyNode(rDate, componentNode);
        }
    }

    /**
     */
    protected void setRDatePropertyNode(RDate rDate,
                                        Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RDATE,
                                     componentNode);
        setValueProperty(rDate, propertyNode);
        setXParameterProperties(rDate, propertyNode);
        for (Iterator i=rDate.getPeriods().iterator(); i.hasNext();) {
            Period period = (Period) i.next();
            setPeriodValueNode(period, propertyNode);
        }
        for (Iterator i=rDate.getDates().iterator(); i.hasNext();) {
            Date date = (Date) i.next();
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME, date);
        }
        setTzIdParameterProperty(rDate, propertyNode);
    }

    /**
     */
    protected void setRRulePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        RRule rRule = ICalendarUtils.getRRule(component);
        if (rRule != null) {
            setRRulePropertyNode(rRule, componentNode);
        }
    }

    /**
     */
    protected void setRRulePropertyNode(RRule rRule,
                                        Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_RRULE,
                                     componentNode);
        setValueProperty(rRule, propertyNode);
        setXParameterProperties(rRule, propertyNode);
        setRecurValueNode(rRule.getRecur(), propertyNode);
    }

    /**
     */
    protected void setTzIdPropertyNode(Component component,
                                       Node componentNode)
        throws RepositoryException {
        Property tzId = ICalendarUtils.getTzId(component);
        if (tzId != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TZID,
                                         componentNode);
            setValueProperty(tzId, propertyNode);
            setXParameterProperties(tzId, propertyNode);
        }
    }

    /**
     */
    protected void setTzNamePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        TzName tzName = ICalendarUtils.getTzName(component);
        if (tzName != null) {
            setTzNamePropertyNode(tzName, componentNode);
        }
    }

    /**
     */
    protected void setTzNamePropertyNode(TzName tzName,
                                         Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TZNAME,
                                     componentNode);
        setValueProperty(tzName, propertyNode);
        setXParameterProperties(tzName, propertyNode);
    }

    /**
     */
    protected void setActionPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Property action = ICalendarUtils.getAction(component);
        if (action != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_ACTION,
                                         componentNode);
            setValueProperty(action, propertyNode);
            setXParameterProperties(action, propertyNode);
        }
    }

    /**
     */
    protected void setRepeatPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Repeat repeat = ICalendarUtils.getRepeat(component);
        if (repeat != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_REPEAT,
                                         componentNode);
            setValueProperty(repeat, propertyNode);
            setXParameterProperties(repeat, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_COUNT,
                                     repeat.getCount());
        }
    }

    /**
     */
    protected void setTriggerPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Trigger trigger = ICalendarUtils.getTrigger(component);
        if (trigger != null) {
            Node propertyNode =
                getICalendarPropertyNode(CosmoJcrConstants.NN_ICAL_TRIGGER,
                                         componentNode);
            setValueProperty(trigger, propertyNode);
            setXParameterProperties(trigger, propertyNode);
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_DURATION,
                                     trigger.getDuration());
            setRelatedParameterProperty(trigger, propertyNode);
            JCRUtils.setDateValue(propertyNode,
                                  CosmoJcrConstants.NP_ICAL_DATETIME,
                                  trigger.getDateTime());
        }
    }

    /**
     */
    protected void setXPropertyNode(String name,
                                    Component component,
                                    Node componentNode)
        throws RepositoryException {
        Property xprop = ICalendarUtils.getXProperty(component, name);
        if (xprop != null) {
            Node propertyNode = getICalendarPropertyNode(name, componentNode);
            setValueProperty(xprop, propertyNode);
            setLanguageParameterProperty(xprop, propertyNode);
        }
    }

    /**
     */
    protected void setXPropertyNodes(Component component,
                                     Node componentNode)
        throws RepositoryException {
        // build a list of names of the x-properties included in the
        // version of the component being set
        Set xPropNames = ICalendarUtils.getXPropertyNames(component);

        // remove any xprop nodes from the stored version of the
        // component that aren't reflected in the new xprop list
        NodeIterator propertyNodes = componentNode.getNodes();
        while (propertyNodes.hasNext()) {
            Node propertyNode = propertyNodes.nextNode();
            if (propertyNode.isNodeType(CosmoJcrConstants.NT_ICAL_XPROPERTY)) {
                if (! xPropNames.contains(propertyNode.getName())) {
                    propertyNode.remove();
                }
            }
        }

        // add xprop nodes for each of the xprops contained in the new
        // version of the component
        for (Iterator i=xPropNames.iterator(); i.hasNext();) {
            String name = (String) i.next();
            setXPropertyNode(name, component, componentNode);
        }
    }

    /**
     */
    protected void setAltRepParameterProperty(Property property,
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
    protected void setCnParameterProperty(Property property,
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
    protected void setCuTypeParameterProperty(Property property,
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
    protected void setDelFromParameterProperty(Property property,
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
    protected void setDelToParameterProperty(Property property,
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
    protected void setDirParameterProperty(Property property,
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
    protected void setFmtTypeParameterProperty(Property property,
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
    protected void setLanguageParameterProperty(Property property,
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
    protected void setMemberParameterProperty(Property property,
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
    protected void setPartStatParameterProperty(Property property,
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
    protected void setRangeParameterProperty(Property property,
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
    protected void setRelatedParameterProperty(Property property,
                                               Node propertyNode)
        throws RepositoryException {
        Related related = ICalendarUtils.getRelated(property);
        if (related != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_RELATED,
                                     related.getValue());
        }
    }

    /**
     */
    protected void setRelTypeParameterProperty(Property property,
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
    protected void setRoleParameterProperty(Property property,
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
    protected void setRsvpParameterProperty(Property property,
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
    protected void setSentByParameterProperty(Property property,
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
    protected void setTzIdParameterProperty(Property property,
                                        Node propertyNode)
        throws RepositoryException {
        net.fortuna.ical4j.model.parameter.TzId tzId =
            ICalendarUtils.getTzId(property);
        if (tzId != null) {
            propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_TZID,
                                     tzId.getValue());
        }
    }

    /**
     */
    protected void setValueProperty(Property property,
                                    Node propertyNode)
        throws RepositoryException {
        propertyNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                                 property.getValue());
    }

    /**
     */
    protected javax.jcr.Property getValueProperty(Node propertyNode)
        throws RepositoryException {
        return propertyNode.getProperty(CosmoJcrConstants.NP_ICAL_VALUE);
    }

    /**
     */
    protected void setXParameterProperty(String name,
                                         Property property,
                                         Node propertyNode)
        throws RepositoryException {
        Parameter xparam = ICalendarUtils.getXParameter(property, name);
        if (xparam != null) {
            propertyNode.setProperty(name, xparam.getValue());
        }
    }
                                    
    /**
     */
    protected void setXParameterProperties(Property property,
                                           Node propertyNode)
        throws RepositoryException {
        // build a list of names of the x-parameters included in the
        // version of the property being set
        Set xParamNames = ICalendarUtils.getXParameterNames(property);

        // remove any xparam properties from the stored version of the
        // property that aren't reflected in the new xparam list
        PropertyIterator parameterProps = propertyNode.getProperties();
        while (parameterProps.hasNext()) {
            javax.jcr.Property parameterProp = parameterProps.nextProperty();
            if (parameterProp.getName().startsWith("X-")) {
                if (! xParamNames.contains(parameterProp.getName())) {
                    parameterProp.remove();
                }
            }
        }

        // add xparam properties for each of the xparams contained in
        // the new version of the property
        for (Iterator i=xParamNames.iterator(); i.hasNext();) {
            String name = (String) i.next();
            setXParameterProperty(name, property, propertyNode);
        }
    }

    // low level utilities

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
    protected void setTextPropertyNodes(Property property,
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

    /**
     */
    protected javax.jcr.Value[] mapLongValues(Iterator i,
                                              ValueFactory vf) {
        List values = new ArrayList();
        while (i.hasNext()) {
            Integer num = (Integer) i.next();
            values.add(vf.createValue(num.longValue()));
        }
        return (javax.jcr.Value[]) values.toArray(new javax.jcr.Value[0]);
    }

    /**
     */
    protected javax.jcr.Value[] mapStringValues(Iterator i,
                                                ValueFactory vf) {
        List values = new ArrayList();
        while (i.hasNext()) {
            values.add(vf.createValue(i.next().toString()));
        }
        return (javax.jcr.Value[]) values.toArray(new javax.jcr.Value[0]);
    }

    /**
     */
    protected void setRecurValueNode(Recur recur,
                                     Node propertyNode)
        throws RepositoryException {
        ValueFactory valueFactory =
            propertyNode.getSession().getValueFactory();
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
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYSECOND,
                              mapLongValues(recur.getSecondList().iterator(),
                                            valueFactory));
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYMINUTE,
                              mapLongValues(recur.getMinuteList().iterator(),
                                            valueFactory));
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYHOUR,
                              mapLongValues(recur.getHourList().iterator(),
                                            valueFactory));
        // weekday is the only bizarro one
        List days = new ArrayList();
        List offsets = new ArrayList();
        for (Iterator i=recur.getDayList().iterator(); i.hasNext();) {
            WeekDay weekday = (WeekDay) i.next();
            days.add(valueFactory.createValue(weekday.getDay()));
            long offset = new Integer(weekday.getOffset()).longValue();
            offsets.add(valueFactory.createValue(offset));
        }
        javax.jcr.Value[] dayvals =
            (javax.jcr.Value[]) days.toArray(new javax.jcr.Value[0]);
        javax.jcr.Value[] offsetvals =
            (javax.jcr.Value[]) offsets.toArray(new javax.jcr.Value[0]);
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYDAY, dayvals);
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYDAYOFFSET,
                              offsetvals);
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYMONTHDAY,
                              mapLongValues(recur.getMonthDayList().iterator(),
                                            valueFactory));
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYYEARDAY,
                              mapLongValues(recur.getYearDayList().iterator(),
                                            valueFactory));
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYWEEKNO,
                              mapLongValues(recur.getWeekNoList().iterator(),
                                            valueFactory));
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYMONTH,
                              mapLongValues(recur.getMonthList().iterator(),
                                            valueFactory));
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_BYSETPOS,
                              mapLongValues(recur.getSetPosList().iterator(),
                                            valueFactory));
        recurNode.setProperty(CosmoJcrConstants.NP_ICAL_WKST,
                              recur.getWeekStartDay());
    }

    /**
     */
    protected void setPeriodValueNode(Period period,
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
}
