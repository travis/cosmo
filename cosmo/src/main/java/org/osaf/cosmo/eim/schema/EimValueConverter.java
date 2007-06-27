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
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.calendar.ICalDate;
import org.osaf.cosmo.calendar.UnknownTimeZoneException;
import org.osaf.cosmo.eim.schema.text.DurationFormat;

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
     * Recurrence rules in the text value must be colon-separated.
     *
     * @return <code>List<Recur></code>
     * @throws EimValidationException
     */
    public static List<Recur> toICalRecurs(String text)
        throws EimValidationException {
        ArrayList<Recur> recurs = new ArrayList<Recur>();
        if (text != null) {
            for (String s : text.split(":")) {
                try {
                    recurs.add(new Recur(s));
                } catch (ParseException e) {
                    throw new EimValidationException("Invalid iCalendar recurrence rule " + s);
                }
            }
        }
        return recurs;
    }

    /**
     * Serializes a list of recurrence rules.
     * <p>
     * Recurrence rules in the returned value are colon-separated.
     *
     * @return <code>String</code>
     */
    public static String fromICalRecurs(List<Recur> recurs) {
        if (! recurs.iterator().hasNext())
            return null;
        return StringUtils.join(recurs.iterator(), ":");
    }

    /**
     * Parses a serialized text value and returns the contained
     * date or date-time.
     *
     * @return <code>ICalDate</code>
     * @throws EimValidationException
     */
    public static ICalDate toICalDate(String text)
        throws EimValidationException {
        if (text == null)
            return null;
        try {
            return new ICalDate(text);
        } catch (ParseException e) {
            throw new EimValidationException("Invalid date value " + text, e);
        } catch (UnknownTimeZoneException e) {
            throw new EimValidationException("Unknown timezone " + e.getMessage(), e);
        }
    }

    /**
     * Returns the text representation of an iCalendar date or
     * date-time.
     *
     * @return <code>String</code>
     */
    public static String fromICalDate(Date date) {
        return fromICalDate(date, false);
    }

    /**
     * Returns the text representation of an iCalendar date or
     * date-time.
     *
     * @param anytime a flag determining whether or not this
     * represents an anytime date
     * @return <code>String</code>
     */
    public static String fromICalDate(Date date,
                                      boolean anytime) {
        if (date == null)
            return null;
        try {
            return new ICalDate(date, anytime).toString();
        } catch (UnknownTimeZoneException e) {
            throw new IllegalArgumentException("Unknown timezone", e);
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
        if (dates == null || dates.isEmpty())
            return null;
        try {
            return new ICalDate(dates).toString();
        } catch (UnknownTimeZoneException e) {
            throw new IllegalArgumentException("Unknown timezone", e);
        }
    }

    public static String fromIcalTrigger(Trigger trigger) {
        if(trigger==null)
            return null;
        
        if(trigger.getDateTime()!=null)
            return ";VALUE=DATE-TIME:" + trigger.getDateTime().toString();
        
        Related related = (Related) trigger.getParameters().getParameter(Parameter.RELATED);
        if(related != null)
            return ";" + related.toString() + ":" + trigger.getDuration().toString();
        else
            return trigger.getDuration().toString();
    }
    
    /**
     * Generate an icalendar TRIGGER value representation for an absolute trigger
     * with the given absolute trigger time.
     * @param dateTime absolute trigger time
     * @return icalendar TRIGGER value string
     */
    public static String formatTriggerFromDateTime(DateTime dateTime) {
        if(!dateTime.isUtc())
            throw new IllegalArgumentException("date must be UTC");
        return ";VALUE=DATE-TIME:" + dateTime.toString();
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
     * <li><code>RELATIVE=END;-PT15M</code> trigger will execute
     * 15 minutes before the end date</li>
     * <li><code>RELATIVE=END;PT15M</code> trigger will execute
     * 15 minutes after the end date</li>
     * <li><code>VALUE=DATE-TIME;20070101100000Z</code> represents
     * an a trigger that will execute at an absolute time</li>
     * </ul>
     *
     * @return <code>DateList</code>
     * @throws EimConversionException
     */
    public static Trigger toIcalTrigger(String text)
            throws EimConversionException {
        if (text == null)
            return null;

        Value value = null;
        Related related = null;
        String propVal = null;

        if (text.startsWith(";VALUE=DATE-TIME:")) {
            value = Value.DATE_TIME;
            propVal = text.substring(17);
        } else if (text.startsWith(";RELATED=END:")){
           related = Related.END;
           propVal = text.substring(13);
        } else {
            propVal = text;
        }
        
        Trigger trigger;
        try {
            trigger = new Trigger();
            if(Related.END.equals(related)) {
                trigger.getParameters().add(Related.END);
                Dur dur = DurationFormat.getInstance().parse(propVal);
                trigger.setDuration(dur);
            } else if(Value.DATE_TIME.equals(value)) {
                trigger.getParameters().add(Value.DATE_TIME);
                trigger.setDateTime(new DateTime(propVal));
            } else {
                Dur dur = DurationFormat.getInstance().parse(propVal);
                trigger.setDuration(dur);
            }
            trigger.validate();
            return trigger;
        } catch (Exception e) {
            throw new EimConversionException("invalid trigger: " + text);
        }
        
    }
}
