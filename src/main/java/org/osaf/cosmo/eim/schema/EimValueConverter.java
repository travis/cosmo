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
import java.util.regex.Pattern;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Trigger;

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

    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    private static final Pattern DURATION_PATTERN = Pattern
            .compile("[+-]?P((\\d+W)|((\\d+D)?(T(\\d+H)?(\\d+M)?(\\d+S)?)?))");

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
     * comma-separated.
     * <p>
     * The text value may optionally be prepended
     * with one or more of these "parameter strings" separated from each
     * other and from the text value proper by a colon:
     * <ul>
     * <li><code>VALUE=DATE</code>, signifying that the value
     * represents a list of dates, or</li>
     * <li><code>VALUE=DATE-TIME</code>, signifying that the value
     * represents a list of date-times</li>
     * <li><code>TZID=&lt;timezone identifier&gt;, signifying a
     * timezone for the dates or date-times
     * </ul>
     * If no parameter strings are provided, the value is assumed to
     * represent a list of date-time values with no associated
     * timezone.
     *
     * @return <code>DateList</code>
     * @throws EimConversionException
     */
    public static DateList toICalDates(String text)
        throws EimConversionException {
        return new ICalDate(text).getDateList();
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
        return new ICalDate(text).getDate();
    }

    public static String fromIcalTrigger(Trigger trigger) {
        
        if(trigger.getDateTime()!=null)
            return "VALUE=DATE-TIME:" + trigger.getDateTime().toString();
        
        Related related = (Related) trigger.getParameters().getParameter(Parameter.RELATED);
        if(related != null)
            return related.toString() + ":" + trigger.getDuration().toString();
        else
            return trigger.getDuration().toString();
    }

    /**
     * Parses a serialized text value and returns a trigger object.
     * <p>
     * The text value must be in the format:
     * <ul>
     * <li><code>-PT15M</code> trigger will execute 15 minutes
     * before the start date </li>
     * <li><code>PT15M</code> trigger will execute 15 minutes
     * after the start date </li>
     * <li><code>RELATIVE=END:-PT15M</code> trigger will execute
     * 15 minutes before the end date</li>
     * <li><code>RELATIVE=END:PT15M</code> trigger will execute
     * 15 minutes after the end date</li>
     * <li><code>VALUE=DATE-TIME:20070101100000Z</code> represents
     * an a trigger that will execute at an absolute time</li>
     * </ul>
     *
     * @return <code>DateList</code>
     * @throws EimConversionException
     */
    public static Trigger toIcalTrigger(String text)
            throws EimConversionException {
        Value value = null;
        Related related = null;
        String propVal = null;

        if (text.startsWith("VALUE=DATE-TIME:")) {
            value = Value.DATE_TIME;
            propVal = text.substring(16);
        } else if (text.startsWith("RELATED=END")){
           related = Related.END;
           propVal = text.substring(12);
        } else {
            propVal = text;
        }
        
        Trigger trigger;
        try {
            trigger = new Trigger();
            if(Related.END.equals(related)) {
                trigger.getParameters().add(Related.END);
                if(validateDuration(propVal)==false)
                    throw new EimConversionException("invalid trigger: " + text);
                trigger.setDuration(new Dur(propVal));
            } else if(Value.DATE_TIME.equals(value)) {
                trigger.getParameters().add(Value.DATE_TIME);
                trigger.setDateTime(new DateTime(propVal));
            } else {
                if(validateDuration(propVal)==false)
                    throw new EimConversionException("invalid trigger: " + text);
                trigger.setDuration(new Dur(propVal));
            }
            trigger.validate();
            return trigger;
        } catch (Exception e) {
            throw new EimConversionException("invalid trigger: " + text);
        }
        
    }
    
    /**
     * Parse duration text.
     * @param value string representation of duration
     * @return ical4j Dur object
     * @throws EimConversionException
     */
    public static Dur toICalDur(String value) throws EimConversionException {
        if(validateDuration(value)==false)
            throw new EimConversionException("invalid duration " + value);
        
        return new Dur(value);
    }
    
    private static boolean validateDuration(String text) {
        return DURATION_PATTERN.matcher(text).matches();
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
        if (dates == null || dates.isEmpty())
            return null;
        return new ICalDate(dates).toString();
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
        return new ICalDate(date).toString();
    }

    private static class ICalDate {
        private Value value;
        private TzId tzid;
        private String text;
        private TimeZone tz;
        private Date date;
        private DateList dates;

        public ICalDate(String text)
            throws EimConversionException {
            for (String param : text.split(":")) {
                String[] parts = param.split("=");
                if (parts.length == 2) {
                    if (parts[0].equals("VALUE"))
                        parseValue(parts[1]);
                    else if (parts[0].equals("TZID"))
                        parseTzId(parts[1]);
                    else
                        throw new EimConversionException("Bad parameter " + parts[0]);
                } else {
                    text = param;
                    parseDates(param);
                }
            }
            if (value == null)
                value = Value.DATE_TIME;
        }

        public ICalDate(Date date) {
            if (date instanceof DateTime) {
                value = Value.DATE_TIME;
                tz = ((DateTime) date).getTimeZone();
                if (tz != null) {
                    String id = tz.getVTimeZone().getProperties().
                        getProperty(Property.TZID).getValue();
                    tzid = new TzId(id);
                }
            } else {
                value = Value.DATE;
            }
            text = date.toString();
            this.date = date;
        }

        public ICalDate(DateList dates) {
            value = dates.getType();
            tz = dates.getTimeZone();
            if (tz != null) {
                String id = tz.getVTimeZone().getProperties().
                    getProperty(Property.TZID).getValue();
                tzid = new TzId(id);
            }
            text = dates.toString();
            this.dates = dates;
        }

        public boolean isDateTime() {
            return value != null && value.equals(Value.DATE_TIME);
        }

        public boolean isDate() {
            return value != null && value.equals(Value.DATE);
        }

        public Value getValue() {
            return value;
        }

        public String getText() {
            return text;
        }

        public TzId getTzId() {
            return tzid;
        }

        public TimeZone getTimeZone() {
            return tz;
        }

        public Date getDate() {
            return date;
        }

        public DateList getDateList() {
            return dates;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(value.toString()).append(":");
            if (tzid != null)
                buf.append("TZID=").append(tzid.getValue()).append(":");
            buf.append(text);
            return buf.toString();
        }

        private void parseValue(String str)
            throws EimConversionException {
            if (str.equals("DATE"))
                value = Value.DATE;
            else if (str.equals("DATE-TIME"))
                value = Value.DATE_TIME;
            else
                throw new EimConversionException("Bad value " + str);
        }

        private void parseTzId(String str)
            throws EimConversionException {
            tzid = new TzId(str);
            tz = TIMEZONE_REGISTRY.getTimeZone(str);
            if (tz == null)
                throw new EimConversionException("Unknown timezone " + str);
        }

        private void parseDates(String str)
            throws EimConversionException {
            String[] strs = str.split(",");
            if (strs.length == 1) {
                try {
                    date = isDate() ? new Date(str) : new DateTime(str, tz);
                } catch (ParseException e) {
                    throw convertParseException(str, e);
                }
            }

            dates = isDate() ?
                new DateList(Value.DATE) :
                new DateList(Value.DATE_TIME, tz);
            for (String s : strs) {
                try {
                    if (isDate())
                        dates.add(new Date(s));
                    else
                        dates.add(new DateTime(s, tz));
                } catch (ParseException e) {
                    throw convertParseException(s, e);
                }
            }
        }

        private EimConversionException convertParseException(String value,
                                                             Exception e) {
            String msg = isDate() ?
                "Invalid iCalendar date value " :
                "Invalid iCalendar date-time value ";
            return new EimConversionException(msg + value, e);
        }
    }
}
