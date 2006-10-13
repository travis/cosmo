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
package org.osaf.cosmo.hcalendar;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.CalendarItem;
import org.osaf.cosmo.util.DateUtil;

/**
 * A class that formats Cosmo calendar model objects according to the
 * hCalendar specification.
 *
 * See {@link http://microformats.org/wiki/hcalendar} for more
 * information about hCalendar.
 *
 * @see CalendarItem
 */
public class HCalendarFormatter {
    private static final Log log = LogFactory.getLog(HCalendarFormatter.class);

    /**
     * Returns an hCalendar string representing the content of the
     * given calendar item.
     *
     * Examines the specific type of calendar item and dispatches to
     * the appropriate method in this class for conversion.
     *
     * @param item the <code>CalendarItem</code> to format
     *
     * @throws IllegalArgumentException if the calendar item is of an
     * unknown type
     */
    public static String toHCal(CalendarItem item) {
        if (item instanceof CalendarEventItem)
            return toHCal((CalendarEventItem) item);
        throw new IllegalArgumentException("unknown calendar item type " + item.getClass().getName());
    }

    /**
     * Returns an hCalendar string representing the content of the
     * given event item.
     *
     * Any recurrence instances or exception instances are ignored in
     * the conversion. Only the master instance is considered.
     *
     * @param event the <code>CalendarEventItem</code> to format
     */
    public static String toHCal(CalendarEventItem event) {
        StringBuffer buf = new StringBuffer();
        buf.append("<div class=\"vevent\">");

        VEvent master = event.getMasterEvent();

        Property summary =
            master.getProperties().getProperty(Property.SUMMARY);
        if (summary != null) {
            buf.append("<span class=\"summary\">").
                append(StringEscapeUtils.escapeHtml(summary.getValue())).
                append("</span>");
        }

        DtStart dtstart = (DtStart)
            master.getProperties().getProperty(Property.DTSTART);
        if (dtstart != null) {
            buf.append(": " ).
                append("<abbr class=\"dtstart\" title=\"").
                append(toRfc3339Date(dtstart.getDate())).
                append("\">").
                append(toHumanReadableDate(dtstart.getDate())).
                append("</abbr>");
        }

        DtEnd dtend = (DtEnd)
            master.getProperties().getProperty(Property.DTEND);
        if (dtend != null) {
            buf.append(" to ").
                append("<abbr class=\"dtend\" title=\"").
                append(toRfc3339Date(dtend.getDate())).
                append("\">").
                append(toHumanReadableDate(dtend.getDate())).
                append("</abbr>");
        }

        Duration duration = (Duration)
            master.getProperties().getProperty(Property.DURATION);
        if (duration != null && dtstart != null) {
            buf.append(" to " ).
                append("<abbr class=\"duration\" title=\"").
                append(duration.getValue()).
                append("\">").
                append(toHumanReadableDate(duration.getDuration().
                                           getTime(dtstart.getDate()))).
                append("</abbr>");
        }

        // XXX: named location with an address and/or geo may be
        // represented by a nested hcard
        // (http://microformats.org/wiki/hcard)

        // XXX: address location may be represented by a nested adr
        // (http://microformats.org/wiki/adr)

        // XXX: geo location may be represented by a nested geo
        // (http://microformats.org/wiki/geo)

        Property location =
            master.getProperties().getProperty(Property.LOCATION);
        if (location != null) {
            buf.append(" at " ).
                append("<span class=\"location\">").
                append(StringEscapeUtils.escapeHtml(location.getValue())).
                append("</span>");
        }

        buf.append("</div>");
        return buf.toString();
    }

    private static String toRfc3339Date(java.util.Date date) {
        TimeZone tz = date instanceof DateTime ?
            ((DateTime) date).getTimeZone() : null;
        return DateUtil.formatRfc3339Date(date, tz);
    }

    private static String toHumanReadableDate(java.util.Date date) {
        String pattern = "MMM d, yyyy h:mm a zzz";
        TimeZone tz = date instanceof DateTime ?
            ((DateTime) date).getTimeZone() : null;
        return DateUtil.formatDate(pattern, date, tz);
    }
}
