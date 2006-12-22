/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.eim.schema;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Converts between EIM field values and model values (mainly
 * iCalendar values used for events and tasks).
 */
public class EimValueConverter implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(EimValueConverter.class);

    private EimValueConverter() {}

    /**
     * Parses a serialized text value and returns the contained
     * recurrence rules.
     * <p>
     * Recurrence rules in the text value must be comma-separated.
     *
     * @return <code>List<Recur></code>
     * @throws EimConversionException
     */
    public static List<Recur> toICalRecurs(String text)
        throws EimConversionException {
        ArrayList<Recur> recurs = new ArrayList<Recur>();
        for (String s : text.split(",")) {
            try {
                recurs.add(new Recur(s));
            } catch (ParseException e) {
                throw new EimConversionException("Invalid iCalendar recurrence rule " + s);
            }
        }
        return recurs;
    }

    /**
     * Serializes a list of recurrence rules.
     * <p>
     * Recurrence rules in the returned value are separated with
     * commas.
     *
     * @return <code>String</code>
     * @throws EimConversionException
     */
    public static String fromICalRecurs(List<Recur> recurs) {
        if (! recurs.iterator().hasNext())
            return null;
        return StringUtils.join(recurs.iterator(), ",");
    }

    /**
     * Parses a serialized text value and returns the contained dates
     * or date-times.
     * <p>
     * Dates or date-times in the text value must be
     * comma-separated. The text value may optionally be prepended
     * with one of these "value strings":
     * <ul>
     * <li><code>VALUE=DATE:</code>, signifying that the value
     * represents a list of dates, or</li>
     * <li><code>VALUE=DATE-TIME:</code>, signifying that the value
     * represents a list of date-times</li>
     * </ul>
     * If no value string is provided, the value is assuemd to
     * represent a list of date-times.
     *
     * @return <code>DateList</code>
     * @throws EimConversionException
     */
    public static DateList toICalDates(String text)
        throws EimConversionException {
        ICalDate d = new ICalDate(text);
        DateList dates = d.isDate() ?
            new DateList(Value.DATE) :
            new DateList(Value.DATE_TIME);
        for (String s : d.getText().split(","))
            dates.add(d.isDate() ? toICalDatePreparsed(s) : toICalDateTimePreparsed(s));
        return dates;
    }

    private static Date toICalDatePreparsed(String text)
        throws EimConversionException {
        try {
            return new Date(text);
        } catch (ParseException e) {
            throw new EimConversionException("Invalid iCalendar date " + text);
        }
    }

    private static DateTime toICalDateTimePreparsed(String text)
        throws EimConversionException {
        try {
            return new DateTime(text);
        } catch (ParseException e) {
            throw new EimConversionException("Invalid iCalendar date-time " + text);
        }
    }

    /**
     * Parses a serialized text value and returns the contained
     * date or date-time.
     * <p>
     * Allows a leading value string as per {@link #toICalDates(String)}.
     *
     * @return <code>Date</code> or <code>DateTime</code>
     * @throws EimConversionException
     */
    public static Date toICalDate(String text)
        throws EimConversionException {
        ICalDate d = new ICalDate(text);
        try {
            return d.isDate() ? new Date(text) : new DateTime(text);
        } catch (ParseException e) {
            throw new EimConversionException("Invalid iCalendar date " + text);
        }
    }

    /**
     * Serializes a list of iCalendar dates or date-times.
     * <p>
     * Dates or date-times in the returned value are separated with
     * commas.
     *
     * @return <code>String</code>
     * @throws EimConversionException
     */
    public static String fromICalDates(DateList dates) {
        if (! dates.iterator().hasNext())
            return null;
        return dates.toString();
    }

    /**
     * Returns the text representation of an iCalendar date or
     * date-time.
     *
     * @return <code>String</code>
     * @throws EimConversionException
     */
    public static String fromICalDate(Date date) {
        if (date == null)
            return null;
        return date.toString();
    }

    private static class ICalDate {
        private Value value;
        private String text;

        public ICalDate(String text)
            throws EimConversionException {
            if (text.startsWith("VALUE=")) {
                if (text.startsWith("VALUE=DATE:")) {
                    this.value = Value.DATE;
                    this.text = text.substring(11);
                } else if (text.startsWith("VALUE=DATE-TIME:")) {
                    this.value = Value.DATE_TIME;
                    this.text = text.substring(16);
                } else {
                    throw new EimConversionException("Invalid value parameter " + text);
                }
            } else {
                this.value = Value.DATE_TIME;
                this.text = text;
            }
        }

        public ICalDate(Value value,
                        String text) {
            this.value = value;
            this.text = text;
        }

        public boolean isDateTime() {
            return value.equals(Value.DATE_TIME);
        }

        public boolean isDate() {
            return value.equals(Value.DATE);
        }

        public String getText() {
            return text;
        }
    }
}
