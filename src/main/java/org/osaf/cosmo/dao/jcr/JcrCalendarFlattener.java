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

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Instance;
import net.fortuna.ical4j.model.InstanceList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DateProperty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author cyrusdaboo
 * @author bcm
 * 
 * This class generates field/value pairs from iCalendar components
 * that are suitable for setting as JCR properties on calendar
 * resource nodes. This allows us to use XPath queries with element
 * attributes to query the iCalendar data in a way suitable for
 * CalDAV.
 * 
 * For this to work the iCalendar data is coverted into a special
 * 'flat' format, and then split into key/value pairs, such that the
 * key can be used as the JCR property name and the value as the
 * (string) JCR property value.
 */
public class JcrCalendarFlattener implements JcrConstants {
    private static final Log log =
        LogFactory.getLog(JcrCalendarFlattener.class);

    static final public String TIME_RANGE_FIELD_SUFFIX = "--TIMERANGE";
    static final public String TIME_RANGE_FIELD_SUFFIX_LOWERCASE =
        TIME_RANGE_FIELD_SUFFIX.toLowerCase();

    static final private String MAX_TIME_RANGE = "20500101T000000Z";

    /**
     * Returns a <code>Map</code> of data items, one for each
     * flattened iCalendar property and parameter, and ones for
     * various time ranges and subcomponents.
     */
    public Map flattenCalendarObject(Calendar calendar)
        throws RepositoryException {
        String calendarData = "";
        try {
            // Get the flattened string version of the calendar
            StringWriter out = new StringWriter();
            CalendarOutputter outputer = new CalendarOutputter();
            outputer.outputFlat(calendar, out);
            calendarData = out.toString();
            out.close();
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (ValidationException e) {
            throw new RepositoryException(e);
        }

        // NB ical4j's outputter generates \r\n line ends but we
        // need only \n, so remove all \r's from the string
        calendarData = calendarData.replaceAll("\r", "");

        // Tokenise the flat calendar data and add results for each
        // property and parameter
        StringTokenizer tokenizer = new StringTokenizer(calendarData, "\n");
        Map result = new HashMap();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken().toLowerCase();
            int colon = line.indexOf(':');
            String rawKey = line.substring(0, colon);
            String key = PREFIX_ICALENDAR + ":" + rawKey;
            String value = (colon + 1 < line.length()) ?
                line.substring(colon + 1) :
                new String();

            result.put(key, value);
        }

        // Now add results for all relevant time ranges
        doTimeRange(calendar, result);

        return result;
    }

    private void doTimeRange(Calendar calendar, Map result) {

        // What we do is collect the master instance recurrence set
        // out to our maxmimum cache value. Then we collect the
        // overriden instance set. Then we need to merge the
        // overridden ones with the master ones taking into account
        // the nastiness of THISANDFUTURE.

        // TODO Yes I am ignoring THISANDPRIOR...

        InstanceList instances = new InstanceList();

        String flatPrefix = PREFIX_ICALENDAR + ":" + "vcalendar-";
        String key = null;
        String propKey = null;

        // Do two passes - first one gets the master instance, second
        // one gets the overridden ones. We have to do this because
        // there is no guarantee that the master instance appears
        // before the overridden ones in the iCalendar stream.

        // Look at each VEVENT/VTODO/VJOURNAL/VALARM
        ComponentList overrides = new ComponentList();
        for (Iterator iter = calendar.getComponents().iterator(); iter
                .hasNext();) {
            Component comp = (Component) iter.next();
            if (comp instanceof VEvent) {
                VEvent vcomp = (VEvent) comp;
                if (key == null) {
                    // propKey is used as the base for constructing
                    // the keys for related time ranges (alarms,
                    // properties, etc)
                    propKey = flatPrefix + "vevent";
                    key = propKey + TIME_RANGE_FIELD_SUFFIX_LOWERCASE;
                }

                // See if this is the master instance
                if (vcomp.getReccurrenceId() == null) {
                    addMasterInstances(vcomp, instances);
                } else {
                    overrides.add(vcomp);
                }

                // Now do indexes for the date valued properties
                doEventPropertyTimeRange(vcomp, propKey, result);
            }
            // TODO Handle other components - can reuse recurrence
            // detection and key construction
        }

        for (Iterator iterator = overrides.iterator(); iterator.hasNext();) {
            Component comp = (Component) iterator.next();
            addOverrideInstance(comp, instances);
        }

        // See if there is nothing to do (should not really happen)
        if (instances.size() == 0) {
            return;
        }

        // Now just add each master start/end as a comma separated list,
        // converting DATE values into floating DATE-TIMEs, and doing it in
        // ascending order
        String textPeriod = null;
        TreeSet sortedKeys = new TreeSet(instances.keySet());
        for (Iterator iter = sortedKeys.iterator(); iter.hasNext();) {
            String ikey = (String) iter.next();
            Instance instance = (Instance) instances.get(ikey);
            if (textPeriod == null) {
                textPeriod = new String();
            } else {
                textPeriod += ',';
            }
            String start = normaliseDateTime(instance.getStart()).toString();
            String end = normaliseDateTime(instance.getEnd()).toString();
            textPeriod += start + '/' + end;
        }

        // Add a result for the component's recurrence time ranges
        result.put(key, textPeriod);

        // Handle VALARMs. We do this by looking at each instance to
        // see if it has a VALARM attached to it. If so the trigger
        // information for the VALARM is expanded based on that
        // instance's start time.

        TreeSet sortedAlarms = new TreeSet();
        for (Iterator iter = sortedKeys.iterator(); iter.hasNext();) {
            String ikey = (String) iter.next();
            Instance instance = (Instance) instances.get(ikey);
            DateList dtl = instance.getAlarmTriggers();
            if (dtl != null) {
                for (Iterator iterator = dtl.iterator(); iterator.hasNext();) {
                    Date date = (Date) iterator.next();
                    sortedAlarms.add(date.toString());
                }
            }
        }

        String alarms = null;
        for (Iterator iter = sortedAlarms.iterator(); iter.hasNext();) {
            String date = (String) iter.next();
            if (alarms == null) {
                alarms = new String();
            } else {
                alarms += ',';
            }
            alarms += date;
        }

        // Add a result for the alarm triggers
        if (alarms != null) {
            String alarmKey = propKey + "-valarm" +
                TIME_RANGE_FIELD_SUFFIX_LOWERCASE;
            result.put(alarmKey, alarms);
        }
    }

    /**
     * Uses
     * {@link doDateValueTimeRange(String, Property, String, Map)}
     * to add results for an event's date properties (which by
     * definition are a single-valued time range).
     */
    private void doEventPropertyTimeRange(VEvent vcomp,
                                          String propKey,
                                          Map result) {
        // Properties to index are: DTSTAMP, DTSTART, DTEND, CREATED,
        // LAST-MODIFIED
        doDateValueTimeRange(Property.DTSTAMP, vcomp.getProperties().
                             getProperty(Property.DTSTAMP), propKey, result);
        doDateValueTimeRange(Property.DTSTART, vcomp.getProperties().
                             getProperty(Property.DTSTART), propKey, result);
        doDateValueTimeRange(Property.DTEND, vcomp.getProperties().
                             getProperty(Property.DTEND), propKey, result);
        doDateValueTimeRange(Property.CREATED, vcomp.getProperties().
                             getProperty(Property.CREATED), propKey, result);
        doDateValueTimeRange(Property.LAST_MODIFIED, vcomp.getProperties().
                             getProperty(Property.LAST_MODIFIED), propKey,
                             result);
    }

    /**
     * Adds a result for a {@link DateProperty}, using the date
     * normalized to UTC as the value.
     */
    private void doDateValueTimeRange(String propName,
                                      Property property,
                                      String propKey,
                                      Map result) {

        // Property must be a DateProperty
        DateProperty dp = (DateProperty) property;
        if (dp == null) {
            return;
        }

        String key = propKey + "_" + propName.toLowerCase() +
            TIME_RANGE_FIELD_SUFFIX_LOWERCASE;

        // Normalise date to UTC
        DateTime date = normaliseDateTime(dp.getDate());

        result.put(key, date.toString());
    }

    /**
     * Get the list of periods corresponding to the master recurring instance
     * set. We cache the full range from the start of the event up to a
     * hard-coded maximum.
     */
    private void addMasterInstances(VEvent vevent, InstanceList instances) {

        DateTime maxRange = null;
        try {
            maxRange = new DateTime(MAX_TIME_RANGE);
        } catch (ParseException e) {
            // Never happens
        }
        instances.addComponent(vevent, vevent.getStartDate().getDate(),
                               maxRange);
    }

    /**
     * Add an overridden instance to the override map. The map uses the
     * Recurrence-ID as the key and the normlised period for the overridden
     * instance as the value.
     */
    private void addOverrideInstance(Component comp, InstanceList instances) {
        instances.addComponent(comp, null, null);
    }

    /**
     * Convert a Date/DateTime into a DateTime specified in UTC.
     */
    private DateTime normaliseDateTime(Date date) {
        DateTime dt = new DateTime(date);
        if (date instanceof DateTime) {
            // Convert it to UTC if it has a timezone
            if (!dt.isUtc() && (dt.getTimeZone() != null)) {
                dt.setUtc(true);
            }
        }
        return dt;
    }
}
