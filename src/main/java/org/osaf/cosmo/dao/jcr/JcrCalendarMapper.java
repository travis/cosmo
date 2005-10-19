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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
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

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dao.UnsupportedCalendarObjectException;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.icalendar.DuplicateUidException;
import org.osaf.cosmo.icalendar.ICalendarUtils;
import org.osaf.cosmo.icalendar.RecurrenceSet;

/**
 * Utility class that converts between {@link Calendar}s and
 * {@link javax.jcr.Node}s.
 *
 * A calendar collection is persisted by adding the
 * <code>caldav:collection</code> mixin type to an existing node.
 *
 * An event resource is persisted by adding the
 * <code>caldav:eventresource</code> mixin type to an existing node.
 *
 * Events are the only supported calendar componentss. Todos, journals
 * and freebusys are ignored. Calendars that do not contain any events
 * cannot be stored.
 *
 */
public class JcrCalendarMapper implements ICalendarConstants, JcrConstants {
    private static final Log log = LogFactory.getLog(JcrCalendarMapper.class);

    /**
     * Returns a new instance of <code>Calendar</code> populated from
     * a calendar resource node.
     */
    public static Calendar nodeToCalendar(Node node)
        throws RepositoryException {
        if (node.isNodeType(NT_CALDAV_COLLECTION)) {
            return getCollectionCalendarObject(node);
        }
        return getIndividualCalendarObject(node);
    }

    /**
     * Copies the properties of a <code>Calendar</code> into a
     * calendar resource node.
     *
     * @throws UnsupportedCalendarObjectException if the calendar does
     * not contain any supported components
     * @throws DuplicateUidException if the caldav resource node
     * exists below a caldav collection node and the given calendar
     * object's uid is already in use by another calendar object
     * within the caldav collection
     */
    public static void calendarToNode(Calendar calendar, Node node)
        throws RepositoryException {
        // since we only support event resources, pull the events and
        // timezones out of the calendar, ignoring other components
        RecurrenceSet events = new RecurrenceSet();
        Map timezones = new HashMap();
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
            // ignore other components
        }

        if (events.isEmpty()) {
            throw new UnsupportedCalendarObjectException();
        }

        if (! node.isNodeType(NT_CALDAV_EVENT_RESOURCE)) {
            node.addMixin(NT_CALDAV_EVENT_RESOURCE);
        }

        // it's possible (tho pathological) that
        // the client will change the resource's
        // uid on an update, so always verify and
        // set it
        verifyUniqueUid(node, events.getUid());
        node.setProperty(NP_ICAL_UID, events.getUid());

        setEventNodes(events, node);
        setTimeZoneNodes(timezones, node);
    }

    /**
     */
    protected static Calendar createEmptyCalendar() {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // add x-properties here if we ever need them

        return calendar;
    }

    /**
     */
    protected static Calendar getCollectionCalendarObject(Node node)
        throws RepositoryException {
        Calendar calendar = createEmptyCalendar();

        // walk through child nodes and convert to calendar components
        // index the timezones by tzid so that we don't include an
        // individual timezone more than once
        HashMap tzIdx = new HashMap();
        for (NodeIterator i=node.getNodes(); i.hasNext();) {
            Node childNode = i.nextNode();
            if (! childNode.isNodeType(NT_CALDAV_RESOURCE)) {
                continue;
            }
            if (! childNode.isNodeType(NT_CALDAV_EVENT_RESOURCE)) {
                log.warn("ignoring non-event calendar resource node");
                continue;
            }

            for (Iterator j=getEventComponents(childNode).iterator();
                 j.hasNext();) {
                Component component = (Component) j.next();
                if (component instanceof VTimeZone) {
                    net.fortuna.ical4j.model.property.TzId tzId =
                        ICalendarUtils.getTzId(component);
                    if (! tzIdx.containsKey(tzId.getValue())) {
                        tzIdx.put(tzId.getValue(), component);
                    }
                }
                else {
                    calendar.getComponents().add(component);
                }
            }
        }
        // put timezones at the end
        for (Iterator i=tzIdx.values().iterator(); i.hasNext();) {
            calendar.getComponents().add((Component) i.next());
        }

        // if the calendar is empty (no components), add the default
        // timezone to the calendar so that it has a component (and
        // therefore is a valid calendar object)

        if (calendar.getComponents().isEmpty()) {
            calendar.getComponents().add(VTimeZone.getDefault());
        }

        return calendar;
    }

    /**
     */
    protected static Calendar getIndividualCalendarObject(Node node)
        throws RepositoryException {
        if (! node.isNodeType(NT_CALDAV_EVENT_RESOURCE)) {
            throw new UnsupportedCalendarObjectException("not a supported calendar resource node");
        }

        Calendar calendar = createEmptyCalendar();

        // convert to calendar components
        for (Iterator i=getEventComponents(node).iterator(); i.hasNext();) {
            calendar.getComponents().add((Component) i.next());
        }

        return calendar;
    }

    /**
     */
    protected static void verifyUniqueUid(Node node, String uid)
        throws RepositoryException {
        // monolithic calendar resources published into cosmo don't
        // have to worry about uid duplication, so if the parent node
        // is not a calendar collection, skip out
        if (! node.getParent().isNodeType(NT_CALDAV_COLLECTION)) {
            return;
        }

        // look for nodes anywhere below the parent calendar
        // collection that have this same uid 
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root");
        if (! node.getParent().getPath().equals("/")) {
            stmt.append(JcrEscapist.xmlEscapeJcrPath(node.getParent().
                                                     getPath()));
        }
        stmt.append("//element(*, ").
            append(NT_CALDAV_RESOURCE).
            append(")");
        stmt.append("[@").
            append(NP_ICAL_UID).
            append(" = '").
            append(uid).
            append("']");

        QueryManager qm = node.getSession().getWorkspace().getQueryManager();
        QueryResult qr = qm.createQuery(stmt.toString(), Query.XPATH).execute();

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
    protected static Set getEventComponents(Node resourceNode)
        throws RepositoryException {
        Set components = new HashSet();

        for (Iterator j=getTimeZones(resourceNode).iterator(); j.hasNext();) {
            VTimeZone timeZone = (VTimeZone) j.next();
            components.add(timeZone);
        }

        for (Iterator j=getEvents(resourceNode).iterator(); j.hasNext();) {
            VEvent event = (VEvent) j.next();
            components.add(event);
        }

        return components;
    }

    /**
     */
    protected static void setEventNodes(RecurrenceSet events,
                                 Node resourceNode)
        throws RepositoryException {
        // add or update the master event
        VEvent masterEvent = (VEvent) events.getMaster();
        Node masterNode =
            resourceNode.hasNode(NN_ICAL_REVENT) ?
            resourceNode.getNode(NN_ICAL_REVENT) :
            resourceNode.addNode(NN_ICAL_REVENT);
        setEventPropertyNodes(masterEvent, masterNode);
        setAlarmNodes(masterEvent.getAlarms(), masterNode);

        // remove any exevent nodes for which a corresponding
        // exception event does not exist in the recurrence set.
        // save an index of the remaining exevent nodes so we can
        // update them later.
        Map updateIdx = new HashMap();
        for (NodeIterator i=resourceNode.getNodes(NN_ICAL_EXEVENT);
             i.hasNext();) {
            Node exeventNode = i.nextNode();
            Node recuridNode = exeventNode.getNode(NN_ICAL_RECURRENCEID);
            java.util.Date recurid = recuridNode.getProperty(NP_ICAL_DATETIME).
                getDate().getTime();
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
        for (Iterator i=events.getExceptions().iterator(); i.hasNext();) {
            VEvent exceptionEvent = (VEvent) i.next();
            java.util.Date recurid =
                ICalendarUtils.getRecurrenceId(exceptionEvent).getTime();
            Node eventNode = (Node) updateIdx.get(recurid);
            if (eventNode == null) {
                eventNode = resourceNode.addNode(NN_ICAL_EXEVENT);
            }
            setEventPropertyNodes(exceptionEvent, eventNode);
            setAlarmNodes(exceptionEvent.getAlarms(), eventNode);
        }
    }

    /**
     */
    protected static Set getEvents(Node resourceNode)
        throws RepositoryException {
        Set events = new HashSet();

        Node masterNode = resourceNode.getNode(NN_ICAL_REVENT);
        PropertyList properties = getEventProperties(masterNode);
        ComponentList alarms = getAlarms(masterNode);
        events.add(new VEvent(properties, alarms));

        for (NodeIterator i = resourceNode.getNodes(NN_ICAL_EXEVENT);
             i.hasNext();) {
            Node exeventNode = i.nextNode();
            properties = getEventProperties(exeventNode);
            alarms = getAlarms(exeventNode);
            events.add(new VEvent(properties, alarms));
        }

        return events;
    }

    /**
     */
    protected static void setAlarmNodes(ComponentList alarms,
                                 Node componentNode)
        throws RepositoryException {
        // since there is no way to uniquely identify an
        // already-stored alarm, remove all pre-existing alarm nodes
        // from the component node, which means we don't have to worry
        // about finding one that matches the given alarm
        for (NodeIterator i=componentNode.getNodes(NN_ICAL_ALARM);
             i.hasNext();) {
            i.nextNode().remove();
        }
        for (Iterator i=alarms.iterator(); i.hasNext();) {
            VAlarm alarm = (VAlarm) i.next();
            Node alarmNode = componentNode.addNode(NN_ICAL_ALARM);
            setAlarmNode(alarm, alarmNode);
        }
    }

    /**
     */
    protected static ComponentList getAlarms(Node componentNode)
        throws RepositoryException {
        ComponentList alarms = new ComponentList();

        for (NodeIterator i=componentNode.getNodes(NN_ICAL_ALARM);
             i.hasNext();) {
            alarms.add(getAlarm(i.nextNode()));
        }

        return alarms;
    }

    /**
     */
    protected static void setAlarmNode(VAlarm alarm,
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
    protected static VAlarm getAlarm(Node alarmNode)
        throws RepositoryException {
        PropertyList properties = getXProperties(alarmNode);
        Action action = getActionProperty(alarmNode);
        if (action != null) {
            properties.add(action);
        }
        Trigger trigger = getTriggerProperty(alarmNode);
        if (trigger != null) {
            properties.add(trigger);
        }
        Duration duration = getDurationProperty(alarmNode);
        if (duration != null) {
            properties.add(duration);
        }
        Repeat repeat = getRepeatProperty(alarmNode);
        if (repeat != null) {
            properties.add(repeat);
        }
        Attach attach = getAttachProperty(alarmNode);
        if (attach != null) {
            properties.add(attach);
        }
        Description description = getDescriptionProperty(alarmNode);
        if (description != null) {
            properties.add(description);
        }
        Summary summary = getSummaryProperty(alarmNode);
        if (summary != null) {
            properties.add(summary);
        }
        for (Iterator i=getAttendeeProperties(alarmNode).iterator();
             i.hasNext();) {
            properties.add((Attendee) i.next());
        }
        return new VAlarm(properties);
    }

    /**
     */
    protected static void setEventPropertyNodes(VEvent event,
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
        setAttachPropertyNodes(event, eventNode);
        setAttendeePropertyNodes(event, eventNode);
        setCategoriesPropertyNodes(event, eventNode);
        setCommentPropertyNodes(event, eventNode);
        setContactPropertyNodes(event, eventNode);
        setExDatePropertyNodes(event, eventNode);
        setExRulePropertyNodes(event, eventNode);
        setRequestStatusPropertyNodes(event, eventNode);
        setRelatedToPropertyNodes(event, eventNode);
        setResourcesPropertyNodes(event, eventNode);
        setRDatePropertyNodes(event, eventNode);
        setRRulePropertyNodes(event, eventNode);
        setXPropertyNodes(event, eventNode);
    }

    /**
     */
    protected static PropertyList getEventProperties(Node eventNode)
        throws RepositoryException {
        PropertyList properties = getXProperties(eventNode);

        Clazz clazz = getClassProperty(eventNode);
        if (clazz != null) {
            properties.add(clazz);
        }
        Created created = getCreatedProperty(eventNode);
        if (created != null) {
            properties.add(created);
        }
        Description description = getDescriptionProperty(eventNode);
        if (description != null) {
            properties.add(description);
        }
        DtStart dtStart = getDtStartProperty(eventNode);
        if (dtStart != null) {
            properties.add(dtStart);
        }
        Geo geo = getGeoProperty(eventNode);
        if (geo != null) {
            properties.add(geo);
        }
        LastModified lastModified = getLastModifiedProperty(eventNode);
        if (lastModified != null) {
            properties.add(lastModified);
        }
        Location location = getLocationProperty(eventNode);
        if (location != null) {
            properties.add(location);
        }
        Organizer organizer = getOrganizerProperty(eventNode);
        if (organizer != null) {
            properties.add(organizer);
        }
        Priority priority = getPriorityProperty(eventNode);
        if (priority != null) {
            properties.add(priority);
        }
        DtStamp dtStamp = getDtStampProperty(eventNode);
        if (dtStamp != null) {
            properties.add(dtStamp);
        }
        Sequence sequence = getSequenceProperty(eventNode);
        if (sequence != null) {
            properties.add(sequence);
        }
        Status status = getStatusProperty(eventNode);
        if (status != null) {
            properties.add(status);
        }
        Summary summary = getSummaryProperty(eventNode);
        if (summary != null) {
            properties.add(summary);
        }
        Transp transp = getTranspProperty(eventNode);
        if (transp != null) {
            properties.add(transp);
        }
        Uid uid = getUidProperty(eventNode);
        if (uid != null) {
            properties.add(uid);
        }
        Url url = getUrlProperty(eventNode);
        if (url != null) {
            properties.add(url);
        }
        RecurrenceId recurrenceId = getRecurrenceIdProperty(eventNode);
        if (recurrenceId != null) {
            properties.add(recurrenceId);
        }
        DtEnd dtEnd = getDtEndProperty(eventNode);
        if (dtEnd != null) {
            properties.add(dtEnd);
        }
        Duration duration = getDurationProperty(eventNode);
        if (duration != null) {
            properties.add(duration);
        }
        for (Iterator i=getAttachProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getAttendeeProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getCategoriesProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getCommentProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add( i.next());
        }
        for (Iterator i=getContactProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getExDateProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getExRuleProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getRequestStatusProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getRelatedToProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getResourcesProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getRDateProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }
        for (Iterator i=getRRuleProperties(eventNode).iterator();
             i.hasNext();) {
            properties.add(i.next());
        }

        return properties;
    }

    /**
     */
    protected static void setTimeZoneNodes(Map timezones,
                                    Node resourceNode)
        throws RepositoryException {
        // make a copy of the timezone map so that we can remove items
        // from it
        Map myTimezones = new HashMap(timezones);

        // find all timezone nodes, updating those that have a
        // corresponding timezone in the map (and clearing them out of
        // our local copy of the map) and removing those that don't.
        for (NodeIterator i=resourceNode.getNodes(NN_ICAL_TIMEZONE);
             i.hasNext();) {
            Node tzNode = i.nextNode();
            Node tzidNode = getICalendarPropertyNode(NN_ICAL_TZID, tzNode);
            String tzid = getValue(tzidNode).getString();
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
    protected static Set getTimeZones(Node resourceNode)
        throws RepositoryException {
        Set timeZones = new HashSet();

        for (NodeIterator i=resourceNode.getNodes(NN_ICAL_TIMEZONE);
             i.hasNext();) {
            timeZones.add(getTimeZone(i.nextNode()));
        }

        return timeZones;
    }

    /**
     */
    protected static void setTimeZoneNode(VTimeZone timezone,
                                   Node inputNode)
        throws RepositoryException {
        // the input node can be either a timezone node itself (in
        // case of an update) or the parent resource node on which a
        // new timezone node should be created
        Node timezoneNode =
            inputNode.isNodeType(NT_ICAL_TIMEZONE) ?
            inputNode :
            inputNode.addNode(NN_ICAL_TIMEZONE);
        setTzIdPropertyNode(timezone, timezoneNode);
        setLastModifiedPropertyNode(timezone, timezoneNode);
        setTzUrlPropertyNode(timezone, timezoneNode);
        // remove existing component nodes since there is no way to
        // uniquely identify one other than a string comparison of
        // every property and parameter
        NodeIterator componentNodes =
            timezoneNode.getNodes(NN_ICAL_STANDARD + " | " +  NN_ICAL_DAYLIGHT);
        while (componentNodes.hasNext()) {
            componentNodes.nextNode().remove();
        }
        // set new component nodes
        for (Iterator i=timezone.getTypes().iterator(); i.hasNext();) {
            setTimeZoneComponentNode((Component) i.next(), timezoneNode);
        }
        setXPropertyNodes(timezone, timezoneNode);
    }

    /**
     */
    protected static VTimeZone getTimeZone(Node timeZoneNode)
        throws RepositoryException {
        PropertyList properties = getXProperties(timeZoneNode);
        properties.add(getTzIdProperty(timeZoneNode));
        LastModified lastModified = getLastModifiedProperty(timeZoneNode);
        if (lastModified != null) {
            properties.add(lastModified);
        }
        TzUrl tzUrl = getTzUrlProperty(timeZoneNode);
        if (tzUrl != null) {
            properties.add(tzUrl);
        }

        ComponentList components = getTimeZoneComponents(timeZoneNode);

        return new VTimeZone(properties, components);
    }

    /**
     */
    protected static ComponentList getTimeZoneComponents(Node timezoneNode)
        throws RepositoryException {
        ComponentList components = new ComponentList();

        NodeIterator componentNodes =
            timezoneNode.getNodes(NN_ICAL_STANDARD + " | " + NN_ICAL_DAYLIGHT);
        while (componentNodes.hasNext()) {
            Component component =
                getTimeZoneComponent(componentNodes.nextNode());
            if (component != null) {
                components.add(component);
            }
        }

        return components;
    }

    /**
     */
    protected static void setTimeZoneComponentNode(Component component,
                                                   Node timezoneNode)
        throws RepositoryException {
        String name = null;
        if (component.getName().equals(COMP_STANDARD)) {
            name = NN_ICAL_STANDARD;
        }
        else if (component.getName().equals(COMP_DAYLIGHT)) {
            name = NN_ICAL_DAYLIGHT;
        }
        else {
            log.warn("ignoring unknown timezone component " +
                     component.getName());
            return;
        }

        // we don't have to look for an existing one since we removed
        // all of the component nodes when updating an existing
        // timezone node
        Node componentNode = timezoneNode.addNode(name);

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
    protected static Component getTimeZoneComponent(Node componentNode)
        throws RepositoryException {
        PropertyList properties = getXProperties(componentNode);
        properties.add(getDtStartProperty(componentNode));
        properties.add(getTzOffsetToProperty(componentNode));
        properties.add(getTzOffsetFromProperty(componentNode));
        for (Iterator i=getTzNameProperties(componentNode).iterator();
             i.hasNext();) {
            properties.add((TzName) i.next());
        }
        for (Iterator i=getCommentProperties(componentNode).iterator();
             i.hasNext();) {
            properties.add((Comment) i.next());
        }
        for (Iterator i=getRDateProperties(componentNode).iterator();
             i.hasNext();) {
            properties.add((RDate) i.next());
        }
        for (Iterator i=getRRuleProperties(componentNode).iterator();
             i.hasNext();) {
            properties.add((RRule) i.next());
        }

        Component component = null;
        if (componentNode.getName().equals(NN_ICAL_STANDARD)) {
            return new Standard(properties);
        }
        else if (componentNode.getName().equals(NN_ICAL_DAYLIGHT)) {
            return new Daylight(properties);
        }
        log.warn("ignoring unknown timezone component " + component.getName());
        return null;
    }

    // icalendar properties

    /**
     */
    protected static void setClassPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        Clazz clazz = ICalendarUtils.getClazz(component);
        if (clazz != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_CLASS, componentNode);
            setValueProperty(clazz, propertyNode);
            setXParameterProperties(clazz, propertyNode);
        }
    }

    /**
     */
    protected static Clazz getClassProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_CLASS);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Clazz(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setCreatedPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Created created = ICalendarUtils.getCreated(component);
        if (created != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_CREATED, componentNode);
            setValueProperty(created, propertyNode);
            setXParameterProperties(created, propertyNode);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(created.getDateTime());
            propertyNode.setProperty(NP_ICAL_DATETIME, c);
        }
    }

    /**
     */
    protected static Created getCreatedProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_CREATED);
            ParameterList parameters = getXParameters(propertyNode);
            java.util.Date datetime =
                propertyNode.getProperty(NP_ICAL_DATETIME).getDate().getTime();
            return new Created(parameters, new DateTime(datetime));
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDescriptionPropertyNode(Component component,
                                              Node componentNode)
        throws RepositoryException {
        Description description = ICalendarUtils.getDescription(component);
        if (description != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_DESCRIPTION, componentNode);
            setValueProperty(description, propertyNode);
            setXParameterProperties(description, propertyNode);
            setTextPropertyNodes(description, propertyNode);
        }
    }

    /**
     */
    protected static Description getDescriptionProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_DESCRIPTION);
            ParameterList parameters = getXParameters(propertyNode);
            AltRep altRep = getAltRepParameter(propertyNode);
            if (altRep != null) {
                parameters.add(altRep);
            }
            Language language = getLanguageParameter(propertyNode);
            if (language != null) {
                parameters.add(language);
            }
            String value = getValue(propertyNode).getString();
            return new Description(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDtStartPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        DtStart dtStart = ICalendarUtils.getDtStart(component);
        if (dtStart != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_DTSTART, componentNode);
            setValueProperty(dtStart, propertyNode);
            setXParameterProperties(dtStart, propertyNode);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(dtStart.getTime());
            propertyNode.setProperty(NP_ICAL_DATETIME, c);
            propertyNode.setProperty(NP_ICAL_UTC, dtStart.isUtc());
            setTzIdParameterProperty(dtStart, propertyNode);
            setValueParameterProperty(dtStart, propertyNode);
        }
    }

    /**
     */
    protected static DtStart getDtStartProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_DTSTART, componentNode);
            ParameterList parameters = getXParameters(propertyNode);
            Value value = getValueParameter(propertyNode);
            if (value != null) {
                parameters.add(value);
            }
            net.fortuna.ical4j.model.parameter.TzId tzid =
                getTzIdParameter(propertyNode);
            if (tzid != null) {
                parameters.add(tzid);
            }
            java.util.Date date = propertyNode.getProperty(NP_ICAL_DATETIME).
                getDate().getTime();
            if (value != null && value.equals(Value.DATE)) {
                return new DtStart(parameters, new Date(date));
            }
            return new DtStart(parameters, new DateTime(date));
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setGeoPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Geo geo = ICalendarUtils.getGeo(component);
        if (geo != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_GEO, componentNode);
            setValueProperty(geo, propertyNode);
            setXParameterProperties(geo, propertyNode);
            propertyNode.setProperty(NP_ICAL_LATITUDE,
                                     geo.getLattitude());
            propertyNode.setProperty(NP_ICAL_LONGITUDE,
                                     geo.getLongitude());
        }
    }

    /**
     */
    protected static Geo getGeoProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_GEO);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Geo(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setLastModifiedPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        LastModified lastMod = ICalendarUtils.getLastModified(component);
        if (lastMod != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_LASTMODIFIED, componentNode);
            setValueProperty(lastMod, propertyNode);
            setXParameterProperties(lastMod, propertyNode);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(lastMod.getDateTime());
            propertyNode.setProperty(NP_ICAL_DATETIME, c);
        }
    }

    /**
     */
    protected static LastModified getLastModifiedProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_LASTMODIFIED);
            java.util.Date datetime =
                propertyNode.getProperty(NP_ICAL_DATETIME).
                getDate().getTime();
            ParameterList parameters = getXParameters(propertyNode);
            return new LastModified(parameters, new DateTime(datetime));
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setLocationPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Location location = ICalendarUtils.getLocation(component);
        if (location != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_LOCATION, componentNode);
            setValueProperty(location, propertyNode);
            setXParameterProperties(location, propertyNode);
        }
    }

    /**
     */
    protected static Location getLocationProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_LOCATION);
            ParameterList parameters = getXParameters(propertyNode);
            AltRep altRep = getAltRepParameter(propertyNode);
            if (altRep != null) {
                parameters.add(altRep);
            }
            Language language = getLanguageParameter(propertyNode);
            if (language != null) {
                parameters.add(language);
            }
            String value = getValue(propertyNode).getString();
            return new Location(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setOrganizerPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        Organizer organizer = ICalendarUtils.getOrganizer(component);
        if (organizer != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_ORGANIZER, componentNode);
            setValueProperty(organizer, propertyNode);
            setXParameterProperties(organizer, propertyNode);
            propertyNode.setProperty(NP_ICAL_CALADDRESS,
                                     organizer.getCalAddress().toString());
            setCnParameterProperty(organizer, propertyNode);
            setDirParameterProperty(organizer, propertyNode);
            setSentByParameterProperty(organizer, propertyNode);
            setLanguageParameterProperty(organizer, propertyNode);
        }
    }

    /**
     */
    protected static Organizer getOrganizerProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_ORGANIZER);
            ParameterList parameters = getXParameters(propertyNode);
            Cn cn = getCnParameter(propertyNode);
            if (cn != null) {
                parameters.add(cn);
            }
            Dir dir = getDirParameter(propertyNode);
            if (dir != null) {
                parameters.add(dir);
            }
            SentBy sentBy = getSentByParameter(propertyNode);
            if (sentBy != null) {
                parameters.add(sentBy);
            }
            Language language = getLanguageParameter(propertyNode);
            if (language != null) {
                parameters.add(language);
            }
            String calAddress = getValue(propertyNode).getString();
            try {
                return new Organizer(parameters, new URI(calAddress));
            } catch (URISyntaxException e) {
                log.warn("node " + propertyNode.getPath() +
                         " has malformed cal-address value " + calAddress +
                         " for property " +
                         NN_ICAL_ORGANIZER, e);
                return null;
            }
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setPriorityPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Priority priority = ICalendarUtils.getPriority(component);
        if (priority != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_PRIORITY, componentNode);
            setValueProperty(priority, propertyNode);
            setXParameterProperties(priority, propertyNode);
            propertyNode.setProperty(NP_ICAL_LEVEL, priority.getLevel());
        }
    }

    /**
     */
    protected static Priority getPriorityProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_PRIORITY);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Priority(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDtStampPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        DtStamp dtStamp = ICalendarUtils.getDtStamp(component);
        if (dtStamp != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_DTSTAMP, componentNode);
            setValueProperty(dtStamp, propertyNode);
            setXParameterProperties(dtStamp, propertyNode);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(dtStamp.getDateTime());
            propertyNode.setProperty(NP_ICAL_DATETIME, c);
        }
    }

    /**
     */
    protected static DtStamp getDtStampProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_DTSTAMP);
            ParameterList parameters = getXParameters(propertyNode);
            java.util.Date datetime =
                propertyNode.getProperty(NP_ICAL_DATETIME).getDate().getTime();
            return new DtStamp(parameters, new DateTime(datetime));
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setSequencePropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Sequence seq = ICalendarUtils.getSequence(component);
        if (seq != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_SEQ, componentNode);
            setValueProperty(seq, propertyNode);
            setXParameterProperties(seq, propertyNode);
            propertyNode.setProperty(NP_ICAL_SEQUENCENO, seq.getSequenceNo());
        }
    }

    /**
     */
    protected static Sequence getSequenceProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_SEQ);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Sequence(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setStatusPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Status status = ICalendarUtils.getStatus(component);
        if (status != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_STATUS, componentNode);
            setValueProperty(status, propertyNode);
            setXParameterProperties(status, propertyNode);
        }
    }

    /**
     */
    protected static Status getStatusProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_STATUS);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Status(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setSummaryPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        Summary summary = ICalendarUtils.getSummary(component);
        if (summary != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_SUMMARY, componentNode);
            setValueProperty(summary, propertyNode);
            setXParameterProperties(summary, propertyNode);
            setTextPropertyNodes(summary, propertyNode);
        }
    }

    /**
     */
    protected static Summary getSummaryProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_SUMMARY);
            ParameterList parameters = getXParameters(propertyNode);
            AltRep altRep = getAltRepParameter(propertyNode);
            if (altRep != null) {
                parameters.add(altRep);
            }
            Language language = getLanguageParameter(propertyNode);
            if (language != null) {
                parameters.add(language);
            }
            String value = getValue(propertyNode).getString();
            return new Summary(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setTranspPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Transp transp = ICalendarUtils.getTransp(component);
        if (transp != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TRANSP, componentNode);
            setValueProperty(transp, propertyNode);
            setXParameterProperties(transp, propertyNode);
        }
    }

    /**
     */
    protected static Transp getTranspProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_TRANSP);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Transp(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setTzOffsetFromPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        TzOffsetFrom tzOffsetFrom = ICalendarUtils.getTzOffsetFrom(component);
        if (tzOffsetFrom != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZOFFSETFROM, componentNode);
            setValueProperty(tzOffsetFrom, propertyNode);
            setXParameterProperties(tzOffsetFrom, propertyNode);
            propertyNode.setProperty(NP_ICAL_OFFSET,
                                     tzOffsetFrom.getOffset().getOffset());
        }
    }

    /**
     */
    protected static TzOffsetFrom getTzOffsetFromProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZOFFSETFROM, componentNode);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new TzOffsetFrom(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setTzOffsetToPropertyNode(Component component,
                                             Node componentNode)
        throws RepositoryException {
        TzOffsetTo tzOffsetTo = ICalendarUtils.getTzOffsetTo(component);
        if (tzOffsetTo != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZOFFSETTO, componentNode);
            setValueProperty(tzOffsetTo, propertyNode);
            setXParameterProperties(tzOffsetTo, propertyNode);
            propertyNode.setProperty(NP_ICAL_OFFSET,
                                     tzOffsetTo.getOffset().getOffset());
        }
    }

    /**
     */
    protected static TzOffsetTo getTzOffsetToProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZOFFSETTO, componentNode);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new TzOffsetTo(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setTzUrlPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        TzUrl tzUrl = ICalendarUtils.getTzUrl(component);
        if (tzUrl != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZURL, componentNode);
            setValueProperty(tzUrl, propertyNode);
            setXParameterProperties(tzUrl, propertyNode);
            propertyNode.setProperty(NP_ICAL_URI, tzUrl.getUri().toString());
        }
    }

    /**
     */
    protected static TzUrl getTzUrlProperty(Node componentNode)
        throws RepositoryException {
        Node propertyNode = null;
        String value = null;
        try {
            propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZURL, componentNode);
            value = getValue(propertyNode).getString();
            ParameterList parameters = getXParameters(propertyNode);
            return new TzUrl(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        } catch (URISyntaxException e) {
            log.warn("node " + propertyNode.getPath() +
                     " has malformed uri value " + value + " for property " +
                     NN_ICAL_TZURL, e);
            return null;
        }
    }

    /**
     */
    protected static void setUidPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Uid uid = ICalendarUtils.getUid(component);
        if (uid != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_UID, componentNode);
            setValueProperty(uid, propertyNode);
            setXParameterProperties(uid, propertyNode);
        }
    }

    /**
     */
    protected static Uid getUidProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_UID);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Uid(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setUrlPropertyNode(Component component,
                                      Node componentNode)
        throws RepositoryException {
        Url url = ICalendarUtils.getUrl(component);
        if (url != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_URL, componentNode);
            setValueProperty(url, propertyNode);
            setXParameterProperties(url, propertyNode);
            propertyNode.setProperty(NP_ICAL_URI,
                                     url.getUri().toString());
        }
    }

    /**
     */
    protected static Url getUrlProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_URL);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            try {
                return new Url(parameters, new URI(value));
            } catch (URISyntaxException e) {
                log.warn("node " + propertyNode.getPath() +
                         " has malformed uri value " + value +
                         " for property " + NN_ICAL_URL, e);
                return null;
            }
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setRecurrenceIdPropertyNode(Component component,
                                               Node componentNode)
        throws RepositoryException {
        RecurrenceId recurrenceId = ICalendarUtils.getRecurrenceId(component);
        if (recurrenceId != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_RECURRENCEID, componentNode);
            setValueProperty(recurrenceId, propertyNode);
            setXParameterProperties(recurrenceId, propertyNode);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(recurrenceId.getTime());
            propertyNode.setProperty(NP_ICAL_DATETIME, c);
            propertyNode.setProperty(NP_ICAL_UTC, recurrenceId.isUtc());
            setValueParameterProperty(recurrenceId, propertyNode);
            setTzIdParameterProperty(recurrenceId, propertyNode);
            setRangeParameterProperty(recurrenceId, propertyNode);
        }
    }

    /**
     */
    protected static RecurrenceId getRecurrenceIdProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_RECURRENCEID);
            ParameterList parameters = getXParameters(propertyNode);
            Value value = getValueParameter(propertyNode);
            if (value != null) {
                parameters.add(value);
            }
            net.fortuna.ical4j.model.parameter.TzId tzId =
                getTzIdParameter(propertyNode);
            if (tzId != null) {
                parameters.add(tzId);
            }
            Range range = getRangeParameter(propertyNode);
            if (range != null) {
                parameters.add(range);
            }
            java.util.Date datetime =
                propertyNode.getProperty(NP_ICAL_DATETIME).getDate().getTime();
            if (value != null && value.equals(Value.DATE)) {
                return new RecurrenceId(parameters, new Date(datetime));
            }
            return new RecurrenceId(parameters, new DateTime(datetime));
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDtEndPropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        DtEnd dtEnd = ICalendarUtils.getDtEnd(component);
        if (dtEnd != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_DTEND, componentNode);
            setValueProperty(dtEnd, propertyNode);
            setXParameterProperties(dtEnd, propertyNode);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(dtEnd.getDate());
            propertyNode.setProperty(NP_ICAL_DATETIME, c);
            propertyNode.setProperty(NP_ICAL_UTC, dtEnd.isUtc());
            setValueParameterProperty(dtEnd, propertyNode);
            setTzIdParameterProperty(dtEnd, propertyNode);
        }
    }

    /**
     */
    protected static DtEnd getDtEndProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_DTEND, componentNode);
            ParameterList parameters = getXParameters(propertyNode);
            Value value = getValueParameter(propertyNode);
            if (value != null) {
                parameters.add(value);
            }
            net.fortuna.ical4j.model.parameter.TzId tzid =
                getTzIdParameter(propertyNode);
            if (tzid != null) {
                parameters.add(tzid);
            }
            java.util.Date date =
                propertyNode.getProperty(NP_ICAL_DATETIME).getDate().getTime();
            if (value != null && value.equals(Value.DATE)) {
                return new DtEnd(parameters, new Date(date));
            }
            return new DtEnd(parameters, new DateTime(date));
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDurationPropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        Duration duration = ICalendarUtils.getDuration(component);
        if (duration != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_DURATION, componentNode);
            setValueProperty(duration, propertyNode);
            setXParameterProperties(duration, propertyNode);
            setDurationValueNode(duration.getDuration(), propertyNode);
        }
    }

    /**
     */
    protected static Duration getDurationProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_DURATION);
            ParameterList parameters = getXParameters(propertyNode);
            Dur dur = getDurationValue(propertyNode);
            return new Duration(parameters, dur);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setAttachPropertyNodes(Component component,
                                          Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_ATTACH, componentNode);
        for (Iterator i=ICalendarUtils.getAttaches(component).iterator();
             i.hasNext();) {
            setAttachPropertyNode((Attach) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setAttachPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_ATTACH, componentNode);
        setAttachPropertyNode(ICalendarUtils.getAttach(component),
                              componentNode);
    }

    /**
     */
    protected static void setAttachPropertyNode(Attach attach,
                                         Node componentNode)
        throws RepositoryException {
        if (attach == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_ATTACH);
        setValueProperty(attach, propertyNode);
        setXParameterProperties(attach, propertyNode);
        setFmtTypeParameterProperty(attach, propertyNode);
        setValueParameterProperty(attach, propertyNode);
        Value value = ICalendarUtils.getValue(attach);
        if (value != null && value.equals(Value.BINARY)) {
            propertyNode.setProperty(NP_ICAL_BINARY,
                                     new ByteArrayInputStream(attach.
                                                              getBinary()));
            setEncodingParameterProperty(attach, propertyNode);
        }
        else {
            propertyNode.setProperty(NP_ICAL_URI,
                                     attach.getUri().toString());
        }
    }

    /**
     */
    protected static Set getAttachProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_ATTACH);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getAttachProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static Attach getAttachProperty(Node parentNode)
        throws RepositoryException {
        try {
            Node propertyNode =
                parentNode.isNodeType(NN_ICAL_ATTACH) ?
                parentNode :
                parentNode.getNode(NN_ICAL_ATTACH);
            ParameterList parameters = getXParameters(propertyNode);
            Value value = getValueParameter(propertyNode);
            if (value != null) {
                parameters.add(value);
            }
            FmtType fmtType = getFmtTypeParameter(propertyNode);
            if (fmtType != null) {
                parameters.add(fmtType);
            }
            // binary value
            if (value != null && value.equals(Value.BINARY)) {
                parameters.add(getEncodingParameter(propertyNode));
                javax.jcr.Property prop = propertyNode.
                    getProperty(NP_ICAL_BINARY);
                InputStream stream = prop.getStream();
                byte[] data = new byte[(int) prop.getLength()];
                try {
                    stream.read(data, 0, data.length);
                } catch (IOException e) {
                    log.warn("error reading binary attachment for node " +
                             propertyNode.getPath(), e);
                    return null;
                }
                return new Attach(parameters, data);
            }
            // uri value
            String uri = propertyNode.getProperty(NP_ICAL_URI).getString();
            try {
                return new Attach(parameters, new URI(uri));
            } catch (URISyntaxException e) {
                log.warn("node " + propertyNode.getPath() +
                         " has malformed uri value " + uri, e);
                return null;
            }
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setAttendeePropertyNodes(Component component,
                                            Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_ATTENDEE, componentNode);
        for (Iterator i=ICalendarUtils.getAttendees(component).iterator();
             i.hasNext();) {
            setAttendeePropertyNode((Attendee) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setAttendeePropertyNode(Component component,
                                           Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_ATTENDEE, componentNode);
        setAttendeePropertyNode(ICalendarUtils.getAttendee(component),
                                componentNode);
    }

    /**
     */
    protected static void setAttendeePropertyNode(Attendee attendee,
                                           Node componentNode)
        throws RepositoryException {
        if (attendee == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_ATTENDEE);
        setValueProperty(attendee, propertyNode);
        setXParameterProperties(attendee, propertyNode);
        propertyNode.setProperty(NP_ICAL_CALADDRESS,
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
    protected static Set getAttendeeProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_ATTENDEE);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getAttendeeProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static Attendee getAttendeeProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        CuType cuType = getCuTypeParameter(propertyNode);
        if (cuType != null) {
            parameters.add(cuType);
        }
        Member member = getMemberParameter(propertyNode);
        if (member != null) {
            parameters.add(member);
        }
        Role role = getRoleParameter(propertyNode);
        if (role != null) {
            parameters.add(role);
        }
        PartStat partStat = getPartStatParameter(propertyNode);
        if (partStat != null) {
            parameters.add(partStat);
        }
        Rsvp rsvp = getRsvpParameter(propertyNode);
        if (rsvp != null) {
            parameters.add(rsvp);
        }
        DelegatedTo delTo = getDelToParameter(propertyNode);
        if (delTo != null) {
            parameters.add(delTo);
        }
        DelegatedFrom delFrom = getDelFromParameter(propertyNode);
        if (delFrom != null) {
            parameters.add(delFrom);
        }
        SentBy sentBy = getSentByParameter(propertyNode);
        if (sentBy != null) {
            parameters.add(sentBy);
        }
        Cn cn = getCnParameter(propertyNode);
        if (cn != null) {
            parameters.add(cn);
        }
        Dir dir = getDirParameter(propertyNode);
        if (dir != null) {
            parameters.add(dir);
        }
        Language language = getLanguageParameter(propertyNode);
        if (language != null) {
            parameters.add(language);
        }
        String calAddress = getValue(propertyNode).getString();
        try {
            return new Attendee(parameters, new URI(calAddress));
        } catch (URISyntaxException e) {
            log.warn("node " + propertyNode.getPath() +
                     " has malformed cal-address value " + calAddress, e);
            return null;
        }
    }

    /**
     */
    protected static void setCategoriesPropertyNodes(Component component,
                                              Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_CATEGORIES, componentNode);
        for (Iterator i=ICalendarUtils.getCategorieses(component).iterator();
             i.hasNext();) {
            setCategoriesPropertyNode((Categories) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setCategoriesPropertyNode(Component component,
                                             Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_CATEGORIES, componentNode);
        setCategoriesPropertyNode(ICalendarUtils.getCategories(component),
                                  componentNode);
    }

    /**
     */
    protected static void setCategoriesPropertyNode(Categories categories,
                                             Node componentNode)
        throws RepositoryException {
        if (categories == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_CATEGORIES);
        setValueProperty(categories, propertyNode);
        setXParameterProperties(categories, propertyNode);
        for (Iterator i=categories.getCategories().iterator();
             i.hasNext();) {
            String category = (String) i.next();
            propertyNode.setProperty(NP_ICAL_CATEGORY, category);
        }
        setLanguageParameterProperty(categories, propertyNode);
    }

    /**
     */
    protected static PropertyList getCategoriesProperties(Node componentNode)
        throws RepositoryException {
        PropertyList properties = new PropertyList();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_CATEGORIES);
             i.hasNext();) {
            properties.add(getCategoriesProperty(i.nextNode()));
        }
        return properties;
    }

    /**
     */
    protected static Categories getCategoriesProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        Language language = getLanguageParameter(propertyNode);
        if (language != null) {
            parameters.add(language);
        }
        CategoryList categories = new CategoryList();
        for (PropertyIterator i = propertyNode.getProperties(NP_ICAL_CATEGORY);
             i.hasNext();) {
            categories.add(i.nextProperty().getString());
        }
        return new Categories(parameters, categories);
    }

    /**
     */
    protected static void setCommentPropertyNodes(Component component,
                                           Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_COMMENT, componentNode);
        for (Iterator i=ICalendarUtils.getComments(component).iterator();
             i.hasNext();) {
            setCommentPropertyNode((Comment) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setCommentPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_COMMENT, componentNode);
        setCommentPropertyNode(ICalendarUtils.getComment(component),
                               componentNode);
    }

    /**
     */
    protected static void setCommentPropertyNode(Comment comment,
                                          Node componentNode)
        throws RepositoryException {
        if (comment == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_COMMENT);
        setValueProperty(comment, propertyNode);
        setXParameterProperties(comment, propertyNode);
        setTextPropertyNodes(comment, propertyNode);
    }

    /**
     */
    protected static Set getCommentProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_COMMENT);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getCommentProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static Comment getCommentProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        AltRep altRep = getAltRepParameter(propertyNode);
        if (altRep != null) {
            parameters.add(altRep);
        }
        Language language = getLanguageParameter(propertyNode);
        if (language != null) {
            parameters.add(language);
        }
        String value = getValue(propertyNode).getString();
        return new Comment(parameters, value);
    }

    /**
     */
    protected static void setContactPropertyNodes(Component component,
                                           Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_CONTACT, componentNode);
        for (Iterator i=ICalendarUtils.getContacts(component).iterator();
             i.hasNext();) {
            setContactPropertyNode((Contact) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setContactPropertyNode(Component component,
                                          Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_CONTACT, componentNode);
        setContactPropertyNode(ICalendarUtils.getContact(component),
                               componentNode);
    }

    /**
     */
    protected static void setContactPropertyNode(Contact contact,
                                          Node componentNode)
        throws RepositoryException {
        if (contact == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_CONTACT);
        setValueProperty(contact, propertyNode);
        setXParameterProperties(contact, propertyNode);
        setTextPropertyNodes(contact, propertyNode);
    }

    /**
     */
    protected static Set getContactProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_CONTACT);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getContactProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static Contact getContactProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        AltRep altRep = getAltRepParameter(propertyNode);
        if (altRep != null) {
            parameters.add(altRep);
        }
        Language language = getLanguageParameter(propertyNode);
        if (language != null) {
            parameters.add(language);
        }
        String value = getValue(propertyNode).getString();
        return new Contact(parameters, value);
    }

    /**
     */
    protected static void setExDatePropertyNodes(Component component,
                                          Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_EXDATE, componentNode);
        for (Iterator i=ICalendarUtils.getExDates(component).iterator();
             i.hasNext();) {
            setExDatePropertyNode((ExDate) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setExDatePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_EXDATE, componentNode);
        setExDatePropertyNode(ICalendarUtils.getExDate(component),
                              componentNode);
    }

    /**
     */
    protected static void setExDatePropertyNode(ExDate exDate,
                                         Node componentNode)
        throws RepositoryException {
        if (exDate == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_EXDATE);
        setValueProperty(exDate, propertyNode);
        setXParameterProperties(exDate, propertyNode);
        ValueFactory valueFactory = propertyNode.getSession().getValueFactory();
        propertyNode.setProperty(NP_ICAL_DATETIME,
                                 mapDateValues(exDate.getDates().iterator(),
                                               valueFactory));
        setValueParameterProperty(exDate, propertyNode);
        setTzIdParameterProperty(exDate, propertyNode);
    }

    /**
     */
    protected static Set getExDateProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_EXDATE);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getExDateProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static ExDate getExDateProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        Value value = getValueParameter(propertyNode);
        if (value != null) {
            parameters.add(value);
        }
        net.fortuna.ical4j.model.parameter.TzId tzId =
            getTzIdParameter(propertyNode);
        if (tzId != null) {
            parameters.add(tzId);
        }
        if (value != null && value.equals(Value.DATE)) {
            DateList dates = getDateValues(propertyNode);
            return new ExDate(parameters, dates);
        }
        DateList datetimes = getDateTimeValues(propertyNode);
        return new ExDate(parameters, datetimes);
    }

    /**
     */
    protected static void setExRulePropertyNodes(Component component,
                                          Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_EXRULE, componentNode);
        for (Iterator i=ICalendarUtils.getExRules(component).iterator();
             i.hasNext();) {
            setExRulePropertyNode((ExRule) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setExRulePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_EXRULE, componentNode);
        setExRulePropertyNode(ICalendarUtils.getExRule(component),
                              componentNode);
    }

    /**
     */
    protected static void setExRulePropertyNode(ExRule exRule,
                                         Node componentNode)
        throws RepositoryException {
        if (exRule == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_EXRULE);
        setValueProperty(exRule, propertyNode);
        setXParameterProperties(exRule, propertyNode);
        setRecurValueNode(exRule.getRecur(), propertyNode);
    }

    /**
     */
    protected static Set getExRuleProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_EXRULE);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getExRuleProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static ExRule getExRuleProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        Recur recur = getRecurValue(propertyNode);
        return new ExRule(parameters, recur);
    }

    /**
     */
    protected static void setRequestStatusPropertyNodes(Component component,
                                                 Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_REQUESTSTATUS, componentNode);
        for (Iterator i=ICalendarUtils.getRequestStatuses(component).iterator();
             i.hasNext();) {
            setRequestStatusPropertyNode((RequestStatus) i.next(),
                                         componentNode);
        }
    }

    /**
     */
    protected static void setRequestStatusPropertyNode(Component component,
                                                Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_REQUESTSTATUS, componentNode);
        setRequestStatusPropertyNode(ICalendarUtils.getRequestStatus(component),
                                     componentNode);
    }

    /**
     */
    protected static void setRequestStatusPropertyNode(RequestStatus requestStatus,
                                                Node componentNode)
        throws RepositoryException {
        if (requestStatus == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_REQUESTSTATUS);
        setValueProperty(requestStatus, propertyNode);
        setXParameterProperties(requestStatus, propertyNode);
        propertyNode.setProperty(NP_ICAL_STATCODE,
                                 requestStatus.getStatusCode());
        propertyNode.setProperty(NP_ICAL_DESCRIPTION,
                                 requestStatus.getDescription());
        propertyNode.setProperty(NP_ICAL_EXDATA,
                                 requestStatus.getExData());
        setLanguageParameterProperty(requestStatus, propertyNode);
    }

    /**
     */
    protected static PropertyList getRequestStatusProperties(Node componentNode)
        throws RepositoryException {
        PropertyList properties = new PropertyList();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_REQUESTSTATUS);
             i.hasNext();) {
            properties.add(getRequestStatusProperty(i.nextNode()));
        }
        return properties;
    }

    /**
     */
    protected static RequestStatus getRequestStatusProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        Language language = getLanguageParameter(propertyNode);
        if (language != null) {
            parameters.add(language);
        }
        String statusCode = propertyNode.
            getProperty(NP_ICAL_STATCODE).getString();
        String description = propertyNode.
            getProperty(NP_ICAL_DESCRIPTION).getString();
        String data = propertyNode.
            getProperty(NP_ICAL_EXDATA).getString();
        return new RequestStatus(parameters, statusCode, description, data);
    }

    /**
     */
    protected static void setRelatedToPropertyNodes(Component component,
                                             Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RELATEDTO, componentNode);
        for (Iterator i=ICalendarUtils.getRelatedTos(component).iterator();
             i.hasNext();) {
            setRelatedToPropertyNode((RelatedTo) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setRelatedToPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RELATEDTO, componentNode);
        setRelatedToPropertyNode(ICalendarUtils.getRelatedTo(component),
                                 componentNode);
    }

    /**
     */
    protected static void setRelatedToPropertyNode(RelatedTo relatedTo,
                                            Node componentNode)
        throws RepositoryException {
        if (relatedTo == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_RELATEDTO);
        setValueProperty(relatedTo, propertyNode);
        setXParameterProperties(relatedTo, propertyNode);
        setRelTypeParameterProperty(relatedTo, propertyNode);
    }
    
    /**
     */
    protected static PropertyList getRelatedToProperties(Node componentNode)
        throws RepositoryException {
        PropertyList properties = new PropertyList();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_RELATEDTO);
             i.hasNext();) {
            properties.add(getRelatedToProperty(i.nextNode()));
        }
        return properties;
    }

    /**
     */
    protected static RelatedTo getRelatedToProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        RelType relType = getRelTypeParameter(propertyNode);
        if (relType != null) {
            parameters.add(relType);
        }
        String value = getValue(propertyNode).getString();
        return new RelatedTo(parameters, value);
    }

    /**
     */
    protected static void setResourcesPropertyNodes(Component component,
                                             Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RESOURCES, componentNode);
        for (Iterator i=ICalendarUtils.getResourceses(component).iterator();
             i.hasNext();) {
            setResourcesPropertyNode((Resources) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setResourcesPropertyNode(Component component,
                                            Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RESOURCES, componentNode);
        setResourcesPropertyNode(ICalendarUtils.getResources(component),
                                 componentNode);
    }

    /**
     */
    protected static void setResourcesPropertyNode(Resources resources,
                                            Node componentNode)
        throws RepositoryException {
        if (resources == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_RESOURCES);
        setValueProperty(resources, propertyNode);
        setXParameterProperties(resources, propertyNode);
        for (Iterator i=resources.getResources().iterator(); i.hasNext();) {
            String str = (String) i.next();
            propertyNode.setProperty(NP_ICAL_RESOURCE, str);
        }
        setAltRepParameterProperty(resources, propertyNode);
        setLanguageParameterProperty(resources, propertyNode);
    }

    /**
     */
    protected static PropertyList getResourcesProperties(Node componentNode)
        throws RepositoryException {
        PropertyList properties = new PropertyList();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_RESOURCES);
             i.hasNext();) {
            properties.add(getResourcesProperty(i.nextNode()));
        }
        return properties;
    }

    /**
     */
    protected static Resources getResourcesProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        AltRep altRep = getAltRepParameter(propertyNode);
        if (altRep != null) {
            parameters.add(altRep);
        }
        Language language = getLanguageParameter(propertyNode);
        if (language != null) {
            parameters.add(language);
        }
        ResourceList resources = new ResourceList();
        for (PropertyIterator i = propertyNode.getProperties(NP_ICAL_RESOURCE);
             i.hasNext();) {
            resources.add(i.nextProperty().getString());
        }
        return new Resources(parameters, resources);
    }

    /**
     */
    protected static void setRDatePropertyNodes(Component component,
                                          Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RDATE, componentNode);
        for (Iterator i=ICalendarUtils.getRDates(component).iterator();
             i.hasNext();) {
            setRDatePropertyNode((RDate) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setRDatePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RDATE, componentNode);
        setRDatePropertyNode(ICalendarUtils.getRDate(component),
                             componentNode);
    }

    /**
     */
    protected static void setRDatePropertyNode(RDate rDate,
                                        Node componentNode)
        throws RepositoryException {
        if (rDate == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_RDATE);
        setValueProperty(rDate, propertyNode);
        setXParameterProperties(rDate, propertyNode);
        setValueParameterProperty(rDate, propertyNode);
        setTzIdParameterProperty(rDate, propertyNode);
        Value value = ICalendarUtils.getValue(rDate);
        if (value != null && value.equals(Value.PERIOD)) {
            for (Iterator i=rDate.getPeriods().iterator(); i.hasNext();) {
                Period period = (Period) i.next();
                setPeriodValueNode(period, propertyNode);
            }
        }
        else {
            // this handles both date and date-time values
            ValueFactory valueFactory =
                propertyNode.getSession().getValueFactory();
            propertyNode.setProperty(NP_ICAL_DATETIME,
                                     mapDateValues(rDate.getDates().iterator(),
                                                   valueFactory));
        }
    }

    /**
     */
    protected static Set getRDateProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_RDATE);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getRDateProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static RDate getRDateProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        Value value = getValueParameter(propertyNode);
        if (value != null) {
            parameters.add(value);
        }
        net.fortuna.ical4j.model.parameter.TzId tzId =
            getTzIdParameter(propertyNode);
        if (tzId != null) {
            parameters.add(tzId);
        }
        if (value != null && value.equals(Value.PERIOD)) {
            PeriodList periods = getPeriodValues(propertyNode);
            return new RDate(parameters, periods);
        }
        else if (value != null && value.equals(Value.DATE)) {
            DateList dates = getDateValues(propertyNode);
            return new RDate(parameters, dates);
        }
        DateList datetimes = getDateTimeValues(propertyNode);
        return new RDate(parameters, datetimes);
    }

    /**
     */
    protected static void setRRulePropertyNodes(Component component,
                                          Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RRULE, componentNode);
        for (Iterator i=ICalendarUtils.getRRules(component).iterator();
             i.hasNext();) {
            setRRulePropertyNode((RRule) i.next(), componentNode);
        }
    }

    /**
     */
    protected static void setRRulePropertyNode(Component component,
                                        Node componentNode)
        throws RepositoryException {
        nukeChildren(NN_ICAL_RRULE, componentNode);
        setRRulePropertyNode(ICalendarUtils.getRRule(component),
                             componentNode);
    }

    /**
     */
    protected static void setRRulePropertyNode(RRule rRule,
                                        Node componentNode)
        throws RepositoryException {
        if (rRule == null) {
            return;
        }
        Node propertyNode = componentNode.addNode(NN_ICAL_RRULE);
        setValueProperty(rRule, propertyNode);
        setXParameterProperties(rRule, propertyNode);
        setRecurValueNode(rRule.getRecur(), propertyNode);
    }

    /**
     */
    protected static Set getRRuleProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_RRULE);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getRRuleProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static RRule getRRuleProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        Recur recur = getRecurValue(propertyNode);
        return new RRule(parameters, recur);
    }

    /**
     */
    protected static void setTzIdPropertyNode(Component component,
                                       Node componentNode)
        throws RepositoryException {
        Property tzId = ICalendarUtils.getTzId(component);
        if (tzId != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZID, componentNode);
            setValueProperty(tzId, propertyNode);
            setXParameterProperties(tzId, propertyNode);
        }
    }

    /**
     */
    protected static net.fortuna.ical4j.model.property.TzId
        getTzIdProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TZID, componentNode);
            String value = getValue(propertyNode).getString();
            ParameterList parameters = getXParameters(propertyNode);
            return new net.fortuna.ical4j.model.property.TzId(parameters,
                                                              value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setTzNamePropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        TzName tzName = ICalendarUtils.getTzName(component);
        if (tzName != null) {
            setTzNamePropertyNode(tzName, componentNode);
        }
    }

    /**
     */
    protected static void setTzNamePropertyNode(TzName tzName,
                                         Node componentNode)
        throws RepositoryException {
        Node propertyNode =
            getICalendarPropertyNode(NN_ICAL_TZNAME, componentNode);
        setValueProperty(tzName, propertyNode);
        setXParameterProperties(tzName, propertyNode);
    }

    /**
     */
    protected static Set getTzNameProperties(Node componentNode)
        throws RepositoryException {
        Set properties = new HashSet();
        for (NodeIterator i = componentNode.getNodes(NN_ICAL_TZNAME);
             i.hasNext();) {
            Node propertyNode = i.nextNode();
            properties.add(getTzNameProperty(propertyNode));
        }
        return properties;
    }

    /**
     */
    protected static TzName getTzNameProperty(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = getXParameters(propertyNode);
        Language language = getLanguageParameter(propertyNode);
        if (language != null) {
            parameters.add(language);
        }
        String value = getValue(propertyNode).getString();
        return new TzName(parameters, value);
    }

    /**
     */
    protected static void setActionPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Property action = ICalendarUtils.getAction(component);
        if (action != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_ACTION, componentNode);
            setValueProperty(action, propertyNode);
            setXParameterProperties(action, propertyNode);
        }
    }

    /**
     */
    protected static Action getActionProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_ACTION);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Action(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setRepeatPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Repeat repeat = ICalendarUtils.getRepeat(component);
        if (repeat != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_REPEAT, componentNode);
            setValueProperty(repeat, propertyNode);
            setXParameterProperties(repeat, propertyNode);
            propertyNode.setProperty(NP_ICAL_COUNT,
                                     repeat.getCount());
        }
    }

    /**
     */
    protected static Repeat getRepeatProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_REPEAT);
            ParameterList parameters = getXParameters(propertyNode);
            String value = getValue(propertyNode).getString();
            return new Repeat(parameters, value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setTriggerPropertyNode(Component component,
                                         Node componentNode)
        throws RepositoryException {
        Trigger trigger = ICalendarUtils.getTrigger(component);
        if (trigger != null) {
            Node propertyNode =
                getICalendarPropertyNode(NN_ICAL_TRIGGER, componentNode);
            setValueProperty(trigger, propertyNode);
            setXParameterProperties(trigger, propertyNode);
            setValueParameterProperty(trigger, propertyNode);
            Value value = ICalendarUtils.getValue(trigger);
            if (value != null && value.equals(Value.DATE_TIME)) {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTime(trigger.getDateTime());
                propertyNode.setProperty(NP_ICAL_DATETIME, c);
            }
            else {
                setRelatedParameterProperty(trigger, propertyNode);
                setDurationValueNode(trigger.getDuration(), propertyNode);
            }
        }
    }

    /**
     */
    protected static Trigger getTriggerProperty(Node componentNode)
        throws RepositoryException {
        try {
            Node propertyNode = componentNode.getNode(NN_ICAL_TRIGGER);
            ParameterList parameters = getXParameters(propertyNode);
            Value value = getValueParameter(propertyNode);
            if (value != null) {
                parameters.add(value);
            }
            // absolute date-time trigger
            if (value != null && value.equals(Value.DATE_TIME)) {
                java.util.Date datetime =
                    propertyNode.getProperty(NP_ICAL_DATETIME).
                    getDate().getTime();
                return new Trigger(parameters, new DateTime(datetime));
            }
            // relative to the dtstart of the related component
            Related related = getRelatedParameter(propertyNode);
            if (related != null) {
                parameters.add(related);
            }
            Dur dur = getDurationValue(propertyNode);
            return new Trigger(parameters, dur);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setXPropertyNode(String name,
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
    protected static XProperty getXProperty(Node propertyNode)
        throws RepositoryException {
        String value = getValue(propertyNode).getString();
        ParameterList parameters = getXParameters(propertyNode);
        return new XProperty(propertyNode.getName(), parameters, value);
    }

    /**
     */
    protected static void setXPropertyNodes(Component component,
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
            if (propertyNode.isNodeType(NT_ICAL_XPROPERTY)) {
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
    protected static PropertyList getXProperties(Node componentNode)
        throws RepositoryException {
        PropertyList properties = new PropertyList();
        NodeIterator propertyNodes = componentNode.getNodes("X-* | x-*");
        while (propertyNodes.hasNext()) {
            properties.add(getXProperty(propertyNodes.nextNode()));
        }
        return properties;
    }

    // icalendar property values

    /**
     */
    protected static void setValueProperty(Property property,
                                    Node propertyNode)
        throws RepositoryException {
        propertyNode.setProperty(NP_ICAL_PROPVALUE, property.getValue());
    }

    /**
     */
    protected static javax.jcr.Property getValue(Node propertyNode)
        throws RepositoryException {
        return propertyNode.getProperty(NP_ICAL_PROPVALUE);
    }

    // icalendar parameters

    /**
     */
    protected static void setAltRepParameterProperty(Property property,
                                              Node propertyNode)
        throws RepositoryException {
        AltRep altRep = ICalendarUtils.getAltRep(property);
        if (altRep != null) {
            propertyNode.setProperty(NP_ICAL_ALTREP, altRep.getValue());
        }
    }

    /**
     */
    protected static AltRep getAltRepParameter(Node propertyNode)
        throws RepositoryException {
        String value = null;
        try {
            value = propertyNode.getProperty(NP_ICAL_ALTREP).getString();
            return new AltRep(value);
        } catch (PathNotFoundException e) {
            return null;
        } catch (URISyntaxException e) {
            log.warn("node " + propertyNode.getPath() +
                     " has malformed uri value " + value + " for property " +
                     NP_ICAL_ALTREP, e);
            return null;
        }
    }

    /**
     */
    protected static void setCnParameterProperty(Property property,
                                          Node propertyNode)
        throws RepositoryException {
        Cn cn = ICalendarUtils.getCn(property);
        if (cn != null) {
            propertyNode.setProperty(NP_ICAL_CN, cn.getValue());
        }
    }

    /**
     */
    protected static Cn getCnParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value = propertyNode.getProperty(NP_ICAL_CN).getString();
            return new Cn(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setCuTypeParameterProperty(Property property,
                                              Node propertyNode)
        throws RepositoryException {
        CuType cuType = ICalendarUtils.getCuType(property);
        if (cuType != null) {
            propertyNode.setProperty(NP_ICAL_CUTYPE, cuType.getValue());
        }
    }

    /**
     */
    protected static CuType getCuTypeParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value = propertyNode.getProperty(NP_ICAL_CUTYPE).getString();
            return new CuType(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDelFromParameterProperty(Property property,
                                               Node propertyNode)
        throws RepositoryException {
        DelegatedFrom delegatedFrom = ICalendarUtils.getDelegatedFrom(property);
        if (delegatedFrom != null) {
            for (Iterator i=delegatedFrom.getDelegators().iterator();
                 i.hasNext();) {
                URI uri = (URI) i.next();
                propertyNode.setProperty(NP_ICAL_DELFROM, uri.toString());
            }
        }
    }

    /**
     */
    protected static DelegatedFrom getDelFromParameter(Node propertyNode)
        throws RepositoryException {
        try {
            AddressList addresses = new AddressList();
            for (PropertyIterator i =
                     propertyNode.getProperties(NP_ICAL_DELFROM);
                 i.hasNext();) {
                String address = i.nextProperty().getString();
                try {
                    addresses.add(new URI(address));
                } catch (URISyntaxException e) {
                    log.warn("node " + propertyNode.getPath() +
                             " has malformed address value " + address +
                             " for property " +
                             NP_ICAL_DELFROM, e);
                    continue;
                }
            }
            return new DelegatedFrom(addresses);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDelToParameterProperty(Property property,
                                             Node propertyNode)
        throws RepositoryException {
        DelegatedTo delegatedTo = ICalendarUtils.getDelegatedTo(property);
        if (delegatedTo != null) {
            for (Iterator i=delegatedTo.getDelegatees().iterator();
                 i.hasNext();) {
                URI uri = (URI) i.next();
                propertyNode.setProperty(NP_ICAL_DELTO, uri.toString());
            }
        }
    }

    /**
     */
    protected static DelegatedTo getDelToParameter(Node propertyNode)
        throws RepositoryException {
        try {
            AddressList addresses = new AddressList();
            for (PropertyIterator i = propertyNode.getProperties(NP_ICAL_DELTO);
                 i.hasNext();) {
                String address = i.nextProperty().getString();
                try {
                    addresses.add(new URI(address));
                } catch (URISyntaxException e) {
                    log.warn("node " + propertyNode.getPath() +
                             " has malformed address value " + address +
                             " for property " +
                             NP_ICAL_DELTO, e);
                    continue;
                }
            }
            return new DelegatedTo(addresses);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setDirParameterProperty(Property property,
                                           Node propertyNode)
        throws RepositoryException {
        Dir dir = ICalendarUtils.getDir(property);
        if (dir != null) {
            propertyNode.setProperty(NP_ICAL_DIR, dir.getValue());
        }
    }

    /**
     */
    protected static Dir getDirParameter(Node propertyNode)
        throws RepositoryException {
        String value = null;
        try {
            value = propertyNode.getProperty(NP_ICAL_DIR).getString();
            return new Dir(new URI(value));
        } catch (PathNotFoundException e) {
            return null;
        } catch (URISyntaxException e) {
            log.warn("node " + propertyNode.getPath() +
                     " has malformed uri value " + value + " for property " +
                     NP_ICAL_DIR, e);
            return null;
        }
    }

    /**
     */
    protected static void setEncodingParameterProperty(Property property,
                                                Node propertyNode)
        throws RepositoryException {
        Encoding encoding = ICalendarUtils.getEncoding(property);
        if (encoding != null) {
            propertyNode.setProperty(NP_ICAL_ENCODING, encoding.getValue());
        }
    }

    /**
     */
    protected static Encoding getEncodingParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_ENCODING).getString();
            return new Encoding(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setFmtTypeParameterProperty(Property property,
                                               Node propertyNode)
        throws RepositoryException {
        FmtType fmtType = ICalendarUtils.getFmtType(property);
        if (fmtType != null) {
            propertyNode.setProperty(NP_ICAL_FMTTYPE, fmtType.getValue());
        }
    }

    /**
     */
    protected static FmtType getFmtTypeParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_FMTTYPE).getString();
            return new FmtType(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setLanguageParameterProperty(Property property,
                                                Node propertyNode)
        throws RepositoryException {
        Language language = ICalendarUtils.getLanguage(property);
        if (language != null) { 
           propertyNode.setProperty(NP_ICAL_LANGUAGE, language.getValue());
        }
    }

    /**
     */
    protected static Language getLanguageParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_LANGUAGE).getString();
            return new Language(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setMemberParameterProperty(Property property,
                                              Node propertyNode)
        throws RepositoryException {
        Member member = ICalendarUtils.getMember(property);
        if (member != null) {
            for (Iterator i=member.getGroups().iterator(); i.hasNext();) {
                URI uri = (URI) i.next();
                propertyNode.setProperty(NP_ICAL_MEMBER, uri.toString());
            }
        }
    }

    /**
     */
    protected static Member getMemberParameter(Node propertyNode)
        throws RepositoryException {
        try {
            AddressList addresses = new AddressList();
            for (PropertyIterator i =
                     propertyNode.getProperties(NP_ICAL_MEMBER);
                 i.hasNext();) {
                String address = i.nextProperty().getString();
                try {
                    addresses.add(new URI(address));
                } catch (URISyntaxException e) {
                    log.warn("node " + propertyNode.getPath() +
                             " has malformed address value " + address +
                             " for property " +
                             NP_ICAL_MEMBER, e);
                    continue;
                }
            }
            return new Member(addresses);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setPartStatParameterProperty(Property property,
                                                Node propertyNode)
        throws RepositoryException {
        PartStat partStat = ICalendarUtils.getPartStat(property);
        if (partStat != null) {
            propertyNode.setProperty(NP_ICAL_PARTSTAT, partStat.getValue());
        }
    }

    /**
     */
    protected static PartStat getPartStatParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_PARTSTAT).getString();
            return new PartStat(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setRangeParameterProperty(Property property,
                                             Node propertyNode)
        throws RepositoryException {
        Range range = ICalendarUtils.getRange(property);
        if (range != null) {
            propertyNode.setProperty(NP_ICAL_RANGE, range.getValue());
        }
    }

    /**
     */
    protected static Range getRangeParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value = propertyNode.getProperty(NP_ICAL_RANGE).getString();
            return new Range(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setRelatedParameterProperty(Property property,
                                               Node propertyNode)
        throws RepositoryException {
        Related related = ICalendarUtils.getRelated(property);
        if (related != null) {
            propertyNode.setProperty(NP_ICAL_RELATED, related.getValue());
        }
    }

    /**
     */
    protected static Related getRelatedParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_RELATED).getString();
            return new Related(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setRelTypeParameterProperty(Property property,
                                               Node propertyNode)
        throws RepositoryException {
        RelType relType = ICalendarUtils.getRelType(property);
        if (relType != null) {
            propertyNode.setProperty(NP_ICAL_RELTYPE, relType.getValue());
        }
    }

    /**
     */
    protected static RelType getRelTypeParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_RELTYPE).getString();
            return new RelType(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setRoleParameterProperty(Property property,
                                            Node propertyNode)
        throws RepositoryException {
        Role role = ICalendarUtils.getRole(property);
        if (role != null) {
            propertyNode.setProperty(NP_ICAL_ROLE, role.getValue());
        }
    }

    /**
     */
    protected static Role getRoleParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value = propertyNode.getProperty(NP_ICAL_ROLE).getString();
            return new Role(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setRsvpParameterProperty(Property property,
                                            Node propertyNode)
        throws RepositoryException {
        Rsvp rsvp = ICalendarUtils.getRsvp(property);
        if (rsvp != null) {
            propertyNode.setProperty(NP_ICAL_RSVP, rsvp.getValue());
        }
    }

    /**
     */
    protected static Rsvp getRsvpParameter(Node propertyNode)
        throws RepositoryException {
        try {
            boolean value = propertyNode.getProperty(NP_ICAL_RSVP).getBoolean();
            return new Rsvp(new Boolean(value));
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setSentByParameterProperty(Property property,
                                              Node propertyNode)
        throws RepositoryException {
        SentBy sentBy = ICalendarUtils.getSentBy(property);
        if (sentBy != null) {
            propertyNode.setProperty(NP_ICAL_SENTBY, sentBy.getValue());
        }
    }

    /**
     */
    protected static SentBy getSentByParameter(Node propertyNode)
        throws RepositoryException {
        String value = null;
        try {
            value = propertyNode.getProperty(NP_ICAL_SENTBY).getString();
            return new SentBy(new URI(value));
        } catch (PathNotFoundException e) {
            return null;
        } catch (URISyntaxException e) {
            log.warn("node " + propertyNode.getPath() +
                     " has malformed uri value " + value + " for property " +
                     NP_ICAL_SENTBY, e);
            return null;
        }
    }

    /**
     */
    protected static void setTzIdParameterProperty(Property property,
                                            Node propertyNode)
        throws RepositoryException {
        net.fortuna.ical4j.model.parameter.TzId tzId =
            ICalendarUtils.getTzId(property);
        if (tzId != null) {
            propertyNode.setProperty(NP_ICAL_TZID, tzId.getValue());
        }
    }

    /**
     */
    protected static net.fortuna.ical4j.model.parameter.TzId
        getTzIdParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_TZID).getString();
            return new net.fortuna.ical4j.model.parameter.TzId(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setValueParameterProperty(Property property,
                                             Node propertyNode)
        throws RepositoryException {
        Value value = ICalendarUtils.getValue(property);
        if (value != null) {
            propertyNode.setProperty(NP_ICAL_VALUE, value.getValue());
        }
    }

    /**
     */
    protected static Value getValueParameter(Node propertyNode)
        throws RepositoryException {
        try {
            String value =
                propertyNode.getProperty(NP_ICAL_VALUE).getString();
            return new Value(value);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    /**
     */
    protected static void setXParameterProperty(String name,
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
    protected static XParameter getXParameter(javax.jcr.Property property)
        throws RepositoryException {
        return new XParameter(property.getName(),
                              property.getValue().toString());
    }
                                    
    /**
     */
    protected static void setXParameterProperties(Property property,
                                           Node propertyNode)
        throws RepositoryException {
        // build a list of names of the x-parameters included in the
        // version of the property being set
        Set xParamNames = ICalendarUtils.getXParameterNames(property);

        // remove any xparam properties from the stored version of the
        // property that aren't reflected in the new xparam list
        PropertyIterator parameterProps = propertyNode.getProperties("X- | x-");
        while (parameterProps.hasNext()) {
            javax.jcr.Property parameterProp = parameterProps.nextProperty();
            if (! xParamNames.contains(parameterProp.getName())) {
                parameterProp.remove();
            }
        }

        // add xparam properties for each of the xparams contained in
        // the new version of the property
        for (Iterator i=xParamNames.iterator(); i.hasNext();) {
            String name = (String) i.next();
            setXParameterProperty(name, property, propertyNode);
        }
    }

    /**
     */
    protected static ParameterList getXParameters(Node propertyNode)
        throws RepositoryException {
        ParameterList parameters = new ParameterList();
        PropertyIterator parameterProps = propertyNode.getProperties("X- | x-");
        while (parameterProps.hasNext()) {
            parameters.add(getXParameter(parameterProps.nextProperty()));
        }
        return parameters;
    }

    // low level utilities

    /**
     */
    protected static Node getICalendarPropertyNode(String propertyNodeName,
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
    protected static void nukeChildren(String name,
                                Node node)
        throws RepositoryException {
        for (NodeIterator i=node.getNodes(name); i.hasNext();) {
            i.nextNode().remove();
        }
    }

    /**
     */
    protected static void setTextPropertyNodes(Property property,
                                        Node propertyNode)
        throws RepositoryException {
        AltRep altRep = ICalendarUtils.getAltRep(property);
        if (altRep != null) {
            propertyNode.setProperty(NP_ICAL_ALTREP, altRep.getValue());
        }
        Language language = ICalendarUtils.getLanguage(property);
        if (language != null) {
            propertyNode.setProperty(NP_ICAL_LANGUAGE, language.getValue());
        }
    }

    /**
     */
    protected static javax.jcr.Value[] mapLongValues(Iterator i,
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
    protected static javax.jcr.Value[] mapStringValues(Iterator i,
                                                ValueFactory vf) {
        List values = new ArrayList();
        while (i.hasNext()) {
            values.add(vf.createValue(i.next().toString()));
        }
        return (javax.jcr.Value[]) values.toArray(new javax.jcr.Value[0]);
    }

    /**
     */
    protected static javax.jcr.Value[] mapDateValues(Iterator i,
                                              ValueFactory vf) {
        List values = new ArrayList();
        while (i.hasNext()) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime((Date) i.next());
            values.add(vf.createValue(calendar));
        }
        return (javax.jcr.Value[]) values.toArray(new javax.jcr.Value[0]);
    }

    /**
     */
    protected static void setDurationValueNode(Dur dur,
                                        Node propertyNode)
        throws RepositoryException {
        Node durationNode =
            getICalendarPropertyNode(NN_ICAL_DUR, propertyNode);
        durationNode.setProperty(NP_ICAL_DAYS, dur.getDays());
        durationNode.setProperty(NP_ICAL_HOURS, dur.getHours());
        durationNode.setProperty(NP_ICAL_MINUTES, dur.getMinutes());
        durationNode.setProperty(NP_ICAL_SECONDS, dur.getSeconds());
        durationNode.setProperty(NP_ICAL_WEEKS, dur.getWeeks());
        durationNode.setProperty(NP_ICAL_NEGATIVE, dur.isNegative());
    }

    /**
     */
    protected static Dur getDurationValue(Node propertyNode)
        throws RepositoryException {
        String value = getValue(propertyNode).getString();
        return new Dur(value);
    }

    /**
     */
    protected static void setRecurValueNode(Recur recur,
                                     Node propertyNode)
        throws RepositoryException {
        ValueFactory valueFactory =
            propertyNode.getSession().getValueFactory();
        Node recurNode =
            getICalendarPropertyNode(NN_ICAL_RECUR, propertyNode);
        recurNode.setProperty(NP_ICAL_FREQ, recur.getFrequency());
        java.util.Calendar c = java.util.Calendar.getInstance();
        if (recur.getUntil() != null) {
            c.setTime(recur.getUntil());
        }
        propertyNode.setProperty(NP_ICAL_UNTIL, c);
        recurNode.setProperty(NP_ICAL_COUNT, recur.getCount());
        recurNode.setProperty(NP_ICAL_INTERVAL, recur.getInterval());
        recurNode.setProperty(NP_ICAL_BYSECOND,
                              mapLongValues(recur.getSecondList().iterator(),
                                            valueFactory));
        recurNode.setProperty(NP_ICAL_BYMINUTE,
                              mapLongValues(recur.getMinuteList().iterator(),
                                            valueFactory));
        recurNode.setProperty(NP_ICAL_BYHOUR,
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
        recurNode.setProperty(NP_ICAL_BYDAY, dayvals);
        recurNode.setProperty(NP_ICAL_BYDAYOFFSET, offsetvals);
        recurNode.setProperty(NP_ICAL_BYMONTHDAY,
                              mapLongValues(recur.getMonthDayList().iterator(),
                                            valueFactory));
        recurNode.setProperty(NP_ICAL_BYYEARDAY,
                              mapLongValues(recur.getYearDayList().iterator(),
                                            valueFactory));
        recurNode.setProperty(NP_ICAL_BYWEEKNO,
                              mapLongValues(recur.getWeekNoList().iterator(),
                                            valueFactory));
        recurNode.setProperty(NP_ICAL_BYMONTH,
                              mapLongValues(recur.getMonthList().iterator(),
                                            valueFactory));
        recurNode.setProperty(NP_ICAL_BYSETPOS,
                              mapLongValues(recur.getSetPosList().iterator(),
                                            valueFactory));
        recurNode.setProperty(NP_ICAL_WKST, recur.getWeekStartDay());
    }

    /**
     */
    protected static Recur getRecurValue(Node propertyNode)
        throws RepositoryException {
        String value = null;
        try {
            value = getValue(propertyNode).getString();
            return new Recur(value);
        } catch (ParseException e) {
            log.warn("node " + propertyNode.getPath() +
                     " has malformed value of type recur: " + value);
            return null;
        }
    }

    /**
     */
    protected static void setPeriodValueNode(Period period,
                                      Node propertyNode)
        throws RepositoryException {
        Node periodNode =
            getICalendarPropertyNode(NN_ICAL_PERIOD, propertyNode);
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(period.getStart());
        propertyNode.setProperty(NP_ICAL_START, c);
        c.clear();
        c.setTime(period.getEnd());
        propertyNode.setProperty(NP_ICAL_START, c);
    }

    /**
     */
    protected static PeriodList getPeriodValues(Node propertyNode)
        throws RepositoryException {
        PeriodList periods = new PeriodList();
        for (NodeIterator i = propertyNode.getNodes(NN_ICAL_PERIOD);
             i.hasNext();) {
            periods.add(getPeriodValue(i.nextNode()));
        }
        return periods;
    }

    /**
     */
    protected static Period getPeriodValue(Node periodNode)
        throws RepositoryException {
        java.util.Date start =
            periodNode.getProperty(NP_ICAL_START).getDate().getTime();
        java.util.Date end =
            periodNode.getProperty(NP_ICAL_END).getDate().getTime();
        return new Period(new DateTime(start), new DateTime(end));
    }

    /**
     */
    protected static DateList getDateValues(Node propertyNode)
        throws RepositoryException {
        DateList dates = new DateList(Value.DATE);
        javax.jcr.Property property =
            propertyNode.getProperty(NP_ICAL_DATETIME);
        javax.jcr.Value[] values = property.getValues();
        for (int i=0; i<values.length; i++) {
            dates.add(new Date(values[i].getDate().getTime()));
        }
        return dates;
    }

    /**
     */
    protected static DateList getDateTimeValues(Node propertyNode)
        throws RepositoryException {
        DateList dates = new DateList(Value.DATE_TIME);
        javax.jcr.Property property =
            propertyNode.getProperty(NP_ICAL_DATETIME);
        javax.jcr.Value[] values = property.getValues();
        for (int i=0; i<values.length; i++) {
            dates.add(new DateTime(values[i].getDate().getTime()));
        }
        return dates;
    }
}
