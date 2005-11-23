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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RecurrenceId;

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
                        ":\n");
                List result = new Vector();
                while (tokenizer.hasMoreTokens()) {

                    String key = tokenizer.nextToken().toLowerCase();
                    String fullkey = fullprefix + key;
                    String propkey = propprefix + key;
                    String value = tokenizer.nextToken();

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
        // into account
        // the nastiness of THISANDFUTURE.

        // TODO Yes I am ignoring THISANDPRIOR...

        PeriodList masterInstances = new PeriodList();
        Map overriddenInstances = new HashMap();

        String flatPrefix = prefix + ":" + FieldNames.FULLTEXT_PREFIX
                + "VCALENDAR-".toLowerCase();
        String key = null;
        String propKey = null;

        // Look at each VEVENT/VTODO/VJOURNAL/VALARM
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
                    masterInstances = getMasterInstances(vcomp);
                } else {
                    addOverrideInstance(vcomp, overriddenInstances);
                }

                // Now do indexes for the date valued properties
                doEventPropertyTimeRange(vcomp, propKey, result);

                // TODO Handle alarms. This gets problematic with recurrences or
                // repeating alarms.
            }
            // TODO Handle other components
        }

        // See if there is nothing to do (should not really happen)
        if (masterInstances.size() == 0) {
            return;
        }

        // Now check to see if this is a non-recurring item
        if ((masterInstances.size() == 1) && (overriddenInstances.size() == 0)) {

            // Just add the one period to the indexer
            String textPeriod = ((Period) masterInstances.first()).toString();

            // Add the field for the actual data
            result.add(new TextFilter.TextFilterIndexString(key, textPeriod,
                    false));

            return;
        }

        // Now check whether there are no overrides
        if (overriddenInstances.size() != 0) {

            // Now we have the nasty case of merging overridden instances.
            // What we do is put the master instance periods into a map keying
            // off the start of each period. Then we lookup each overridden
            // instance's recurrence-id in the map and replace the map value by
            // the one from the instance.
            // If THISANDFUTURE is specified we then iterate over the remainder
            // of items in the map and adjust those with the appropriate
            // relative offset.

            Map masterMap = new HashMap();
            for (Iterator iter = masterInstances.iterator(); iter.hasNext();) {
                Period period = (Period) iter.next();
                masterMap.put(period.getStart().toString(), period);
            }

            for (Iterator iter = overriddenInstances.keySet().iterator(); iter
                    .hasNext();) {
                String rid = (String) iter.next();
                if (masterMap.containsKey(rid)) {
                    InstanceInfo info = (InstanceInfo) overriddenInstances
                            .get(rid);
                    if (info.isFuture()) {
                        // TODO Handle THISANDFUTURE adjustment
                    } else {
                        masterMap.put(rid, info.getPeriod());
                    }
                }
            }

            masterInstances = new PeriodList();
            for (Iterator iter = masterMap.entrySet().iterator(); iter
                    .hasNext();) {
                Period period = (Period) iter.next();
                masterInstances.add(period);
            }
        }

        // Now just add each master period as a comma separated list
        String textPeriod = null;
        for (Iterator iter = masterInstances.iterator(); iter.hasNext();) {
            Period period = (Period) iter.next();
            if (textPeriod == null) {
                textPeriod = new String();
            } else {
                textPeriod += ", ";
            }
            textPeriod += period.toString();
        }

        // Add the field for the actual data
        result
                .add(new TextFilter.TextFilterIndexString(key, textPeriod,
                        false));
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
    private PeriodList getMasterInstances(VEvent vevent) {

        DateTime maxRange = null;
        try {
            maxRange = new DateTime(MAX_TIME_RANGE);
        } catch (ParseException e) {
            // Never happens
        }
        PeriodList intermediateResult = vevent.getInstances(vevent
                .getStartDate().getDate(), maxRange);

        // NB The periods we get back from ical4j may be in start/duration
        // format, but we want start/end format for our indexer comparisons, so
        // we need to check and convert them here.
        PeriodList result = new PeriodList();
        for (Iterator iter = intermediateResult.iterator(); iter.hasNext();) {
            Period period = (Period) iter.next();
            Period startEnd = new Period(period.getStart(), period.getEnd());

            // ical4j issue: when the start/end times are copied they end up
            // using TZID=GMT, but we want these in UTC, so we have to coerce
            // them directly. Really ical4j should preserve the original UTC
            // state.
            startEnd.getStart().setUtc(true);
            startEnd.getEnd().setUtc(true);
            result.add(startEnd);
        }

        return result;
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
    private void addOverrideInstance(VEvent vevent, Map instances) {

        // First check to see that the appropriate properties are present.

        // We need a RECURRENCE-ID
        RecurrenceId rid = vevent.getReccurrenceId();
        if (rid == null)
            return;

        // We need a DTSTART. Note that in an overridden instance, if the
        // DTSTART has not changed (i.e. some other property has been changed)
        // it may not be rpesent, and if so there is no need to treat this as a
        // seperate instance with regards to time-range.
        DtStart dtstart = vevent.getStartDate();
        if (dtstart == null)
            return;

        // We need either DTEND or DURATION.
        DtEnd dtend = vevent.getEndDate();
        if (dtend == null)
            return;

        // Now create the map entry
        Date riddt = rid.getDate();
        Parameter range = rid.getParameters().getParameter(Parameter.RANGE);
        boolean future = (range != null)
                && "THISANDFUTURE".equals(range.getValue());
        Period period = getNormalisedPeriod(vevent);

        instances.put(riddt.toString(), new InstanceInfo(period, future));
    }

    private Period getNormalisedPeriod(VEvent vevent) {

        // Get start/end normalised to UTC
        DateTime start = normaliseDateTime((Date) vevent.getStartDate()
                .getDate());
        DateTime end = normaliseDateTime((Date) vevent.getEndDate().getDate());

        return new Period(start, end);
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

            // Convert it to UTC
            if (!dt.isUtc()) {
                dt.setUtc(true);
            }
        }

        return dt;
    }

    private class InstanceInfo {

        private Period period;
        private boolean future;

        InstanceInfo(Period period) {
            this(period, false);
        }

        InstanceInfo(Period period, boolean future) {
            this.period = period;
            this.future = future;
        }

        /**
         * @return Returns the future.
         */
        public boolean isFuture() {
            return future;
        }

        /**
         * @return Returns the period.
         */
        public Period getPeriod() {
            return period;
        }

    }
}
