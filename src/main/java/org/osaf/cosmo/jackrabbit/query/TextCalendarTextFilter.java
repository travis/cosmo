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
import java.util.Iterator;
import java.util.List;
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
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;

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

        // TODO For now we just index a single start/end for each component,
        // ignoring recurrence

        String flatPrefix = prefix + ":" + FieldNames.FULLTEXT_PREFIX
                + "VCALENDAR-".toLowerCase();

        // Look at each VEVENT/VTODO/VJOURNAL/VALARM
        for (Iterator iter = calendar.getComponents().iterator(); iter
                .hasNext();) {
            Component comp = (Component) iter.next();
            if (comp instanceof VEvent) {
                VEvent vcomp = (VEvent) comp;
                String key = flatPrefix + "vevent"
                        + TIME_RANGE_FIELD_SUFFIX_LOWERCASE;

                // Get start/end normalised to UTC
                DateTime start = normaliseDateTime((Date) vcomp.getStartDate()
                        .getDate());
                DateTime end = normaliseDateTime((Date) vcomp.getEndDate()
                        .getDate());

                Period period = new Period(start, end);
                String textPeriod = period.toString();

                // Add the field for the actual data
                result.add(new TextFilter.TextFilterIndexString(key,
                        textPeriod, false));
            }
            // TODO Handle other components
        }
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
}
