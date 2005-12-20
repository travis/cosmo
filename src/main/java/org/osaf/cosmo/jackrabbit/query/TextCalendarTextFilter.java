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
package org.osaf.cosmo.jackrabbit.query;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
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

import org.apache.jackrabbit.core.query.TextFilter;
import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.BLOBFileValue;
import org.apache.jackrabbit.core.value.InternalValue;

/**
 * @author cyrusdaboo
 * 
 * This class implements a text filter that generates field/value pairs that are
 * fed to the query indexer for indexing the content of text/calendar objects in
 * a way that makes the iCalendar data appear to be jcr properties, even though
 * the actual jcr properties do not exist. This allows us to use XPath queries
 * with element attributes to query the iCalendar data in a way suitable for
 * CalDAV.
 * 
 * For this to work the iCalendar data is coverted into a special 'flat' format,
 * and then split into key/value pairs, and the key becomes the indexer field.
 */
public class TextCalendarTextFilter implements TextFilter {
    private static final Log log =
        LogFactory.getLog(TextCalendarTextFilter.class);

    static final public String TIME_RANGE_FIELD_SUFFIX = "--TIMERANGE";
    static final public String TIME_RANGE_FIELD_SUFFIX_LOWERCASE = TIME_RANGE_FIELD_SUFFIX
            .toLowerCase();

    static final private String MAX_TIME_RANGE = "20500101T000000Z";

    /**
     * Returns <code>true</code> for <code>text/calendar</code>;
     * <code>false</code> in all other cases.
     * 
     * @param mimeType
     *            the mime-type.
     * @return <code>true</code> for <code>text/calendar</code>;
     *         <code>false</code> in all other cases.
     */
    public boolean canFilter(String mimeType) {
        return "text/calendar".equalsIgnoreCase(mimeType);
    }

    /**
     * Returns a list with items for each iCalendar 'flat' data item.
     * 
     * @param data
     *            the data property.
     * @param encoding
     *            the encoding
     * @param mappings
     *            the namespace mappings in use with for this indexer
     * @return a list
     * @throws RepositoryException
     *             if encoding is not supported or data is a multi-value
     *             property.
     */
    public List doFilter(PropertyState data,
                         String encoding,
                         NamespaceMappings mappings)
        throws RepositoryException {

        InternalValue[] values = data.getValues();
        if (values.length == 1) {
            BLOBFileValue blob = (BLOBFileValue) values[0].internalValue();
            try {
                // Create reader for the raw data
                Reader reader;
                if (encoding == null) {
                    // use platform default
                    reader = new InputStreamReader(blob.getStream());
                } else {
                    reader = new InputStreamReader(blob.getStream(), encoding);
                }

                // Parse the calendar data into an iCalendar object, then write
                // it out in the 'flat' format
                Calendar calendar;
                String calendarData = "";
                try {
                    CalendarBuilder builder = new CalendarBuilder();
                    calendar = builder.build(reader);

                    // Write the calendar object back using the flat format
                    StringWriter out = new StringWriter();
                    CalendarOutputter outputer = new CalendarOutputter();
                    outputer.outputFlat(calendar, out);
                    calendarData = out.toString();
                    out.close();
                } catch (IOException e) {
                    throw new RepositoryException(e);
                } catch (ParserException e) {
                    throw new RepositoryException(e);
                } catch (ValidationException e) {
                    throw new RepositoryException(e);
                }

                // NB ical4j's outputter generates \r\n line ends but we
                // need only \n, so remove all \r's from the string
                calendarData = calendarData.replaceAll("\r", "");

                // Map the icalendar: namespace prefix to the appropriate
                // namespace mapping used with the indexer. This is needed
                // because the XPath query process does the same mapping.
                String namespc = "http://osafoundation.org/icalendar";
                String prefix;
                try {
                    prefix = mappings.getPrefix(namespc);
                } catch (NamespaceException nse) {
                    // Just ignore
                    prefix = namespc;
                }

                // Make this a proper prefix for the indexer, making it look
                // like this item is a JCR property. Note we need two types of
                // prefix: one for the full text index of the actual value, the
                // other for use in the _:PROPERTIES field which lists the
                // availble fields int he document.
                String fullprefix = prefix + ":" + FieldNames.FULLTEXT_PREFIX;
                String propprefix = prefix + ":";

                // Tokenise the flat calendar data into key/value pairs and add
                // those as items for the indexer
                StringTokenizer tokenizer = new StringTokenizer(calendarData,
                        "\n");
                List result = new Vector();
                while (tokenizer.hasMoreTokens()) {

                    String line = tokenizer.nextToken().toLowerCase();
                    int colon = line.indexOf(':');
                    String key = line.substring(0, colon);
                    String fullkey = fullprefix + key;
                    String propkey = propprefix + key;
                    String value = (colon + 1 < line.length()) ? line.substring(colon + 1) : new String();

                    // Add the field for the actual data
                    result.add(new TextFilter.TextFilterIndexString(fullkey,
                            value, true));

                    // Also add the field name to the _:PROPERTIES field so that
                    // queries for the existence of the property name will work
                    result.add(new TextFilter.TextFilterIndexString(
                            FieldNames.PROPERTIES,
                            propkey + '\uFFFF' + "begin", false));
                }

                // Now do time range indexing
                doTimeRange(calendar, prefix, result);

                return result;
            } catch (UnsupportedEncodingException e) {
                throw new RepositoryException(e);
            }
        } else {
            // multi value not supported
            throw new RepositoryException(
                    "Multi-valued binary properties not supported.");
        }
    }

    private void doTimeRange(Calendar calendar, String prefix, List result) {

        // What we do is collect the master instance recurrence set out to our
        // maxmimum cache value. Then we collect the overriden instance set.
        // Then we need to merge the overridden ones with the master ones taking
        // into account the nastiness of THISANDFUTURE.

        // TODO Yes I am ignoring THISANDPRIOR...

        InstanceList instances = new InstanceList();

        String flatPrefix = prefix + ":" + FieldNames.FULLTEXT_PREFIX
                + "VCALENDAR-".toLowerCase();
        String key = null;
        String propKey = null;

        // Do two passes - first one gets the master instance, second one gets
        // the overridden ones. We have to do this because there is no guarantee
        // that the master instance appears before the overridden ones in the
        // iCalendar stream.

        // Look at each VEVENT/VTODO/VJOURNAL/VALARM
        ComponentList overrides = new ComponentList();
        for (Iterator iter = calendar.getComponents().iterator(); iter
                .hasNext();) {
            Component comp = (Component) iter.next();
            if (comp instanceof VEvent) {
                VEvent vcomp = (VEvent) comp;
                if (key == null) {
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
            // TODO Handle other components
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

        // Add the field for the actual data
        result
                .add(new TextFilter.TextFilterIndexString(key, textPeriod,
                        false));

        // Handle VALARMs. We do this by looking at each instance to see if it
        // has a VALARM attached to it. If so the trigger information for the
        // VALARM is expanded based on that instance's start time. A list of
        // sorted date-times for the trigger is then indexed.

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

        // Add the field for the actual data
        if (alarms != null) {
            String alarmKey = propKey + "-valarm"
                    + TIME_RANGE_FIELD_SUFFIX_LOWERCASE;
            result.add(new TextFilter.TextFilterIndexString(alarmKey, alarms,
                    false));
        }
    }

    private void doEventPropertyTimeRange(VEvent vcomp,
                                          String propKey,
                                          List result) {

        // Properties to index are: DTSTAMP, DTSTART, DTEND, CREATED,
        // LAST-MODIFIED

        doDateValueTimeRange(Property.DTSTAMP, vcomp.getProperties()
                .getProperty(Property.DTSTAMP), propKey, result);
        doDateValueTimeRange(Property.DTSTART, vcomp.getProperties()
                .getProperty(Property.DTSTART), propKey, result);
        doDateValueTimeRange(Property.DTEND, vcomp.getProperties().getProperty(
                Property.DTEND), propKey, result);
        doDateValueTimeRange(Property.CREATED, vcomp.getProperties()
                .getProperty(Property.CREATED), propKey, result);
        doDateValueTimeRange(Property.LAST_MODIFIED, vcomp.getProperties()
                .getProperty(Property.LAST_MODIFIED), propKey, result);
    }

    private void doDateValueTimeRange(String propName,
                                      Property property,
                                      String propKey,
                                      List result) {

        // Property must be a DateProperty
        DateProperty dp = (DateProperty) property;
        if (dp == null)
            return;

        // Normalise date to UTC
        DateTime date = normaliseDateTime(dp.getDate());

        // Store in index
        String key = propKey + "_" + propName.toLowerCase()
                + TIME_RANGE_FIELD_SUFFIX_LOWERCASE;

        // Just add the one period to the indexer
        String textDate = date.toString();

        // Add the field for the actual data
        result.add(new TextFilter.TextFilterIndexString(key, textDate, false));
    }

    /**
     * Get the list of periods corresponding to the master recurring instance
     * set. We cache the full range from the start of the event up to a
     * hard-coded maximum.
     * 
     * @param vevent
     *            the master recurring event
     * @param timeRange
     *            the time-range of the original request
     * @return
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
     * 
     * @param vevent
     *            the overridden instance
     * @param instances
     *            the map to add details to
     */
    private void addOverrideInstance(Component comp, InstanceList instances) {
        instances.addComponent(comp, null, null);
    }

    /**
     * Convert a Date/DateTime into a DateTime specified in UTC.
     * 
     * @param date
     * @return
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
