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
package org.osaf.cosmo.atom.generator;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.util.DateUtil;

/**
 * Produces text and HTML representations of an
 * <code>EventStamp</code> for use in an Atom entry's summary and
 * content fields.
 *
 * HTML representations use the
 * <a href="http://microformats.org/wiki/hcalendar">hCalendar</a>
 * microformat.
 *
 * The class is not yet localized.
 *
 * @see EventStamp
 */
public class EventEntryFormatter {
    private static final Log log =
        LogFactory.getLog(EventEntryFormatter.class);

    private static final String PATTERN_HUMAN_DATETIME =
        "MMM d, yyyy h:mm a zzz";
    private static final String PATTERN_HUMAN_DATE =
        "MMM d, yyyy";

    private EventStamp stamp;

    /**
     * Returns an instance of <code>EventEntryFormatter</code> for the
     * given event stamp and the system default locale.
     */
    public EventEntryFormatter(EventStamp stamp) {
        this.stamp = stamp;
    }

    /**
     * Returns an abbreviated plain text summary of the event.
     *
     * @param stamp the <code>EventStamp</code> to format
     */
    public String formatTextSummary() {
        StringBuffer buf = new StringBuffer();

        Date start = stamp.getStartDate();
        Date end = stamp.getEndDate();

        String summary = toSummary(stamp.getSummary());
        String startDate = toHumanDate(start);
        String endDate = toHumanDate(end);
        String location = stamp.getLocation();

        buf.append(summary).
            append(": ").
            append(startDate);
        if (end != null && start.getTime() != end.getTime())
            buf.append(" to ").
                append(endDate);
        if (location != null)
            buf.append(" at ").
                append(location);

        return buf.toString();
    }

    /**
     * Returns an HTML/hCalendar description of the event.
     *
     * Any recurrence instances or exception instances are ignored in
     * the conversion. Only the master instance is considered.
     */
    public String formatHtmlContent() {
        // XXX: named location with an address and/or geo may be
        // represented by a nested hcard
        // (http://microformats.org/wiki/hcard)

        // XXX: address location may be represented by a nested adr
        // (http://microformats.org/wiki/adr)

        // XXX: geo location may be represented by a nested geo
        // (http://microformats.org/wiki/geo)

        Date start = stamp.getStartDate();
        Date end = stamp.getEndDate();

        String summary = toSummary(stamp.getSummary());
        String machineStartDate = toMachineDate(start);
        String humanStartDate = toHumanDate(start);
        String machineEndDate = toMachineDate(end);
        String humanEndDate = toHumanDate(end);
        String location = stamp.getLocation();

        StringBuffer buf = new StringBuffer();

        buf.append("<div class=\"vevent\">").
            append("<span class=\"summary\">").
            append(StringEscapeUtils.escapeHtml(summary)).
            append("</span>").
            append(": ").
            append("<abbr class=\"dtstart\" title=\"").
            append(machineStartDate).
            append("\">").
            append(humanStartDate).
            append("</abbr>");
        if (end != null && start.getTime() != end.getTime())
            buf.append(" to ").
                append("<abbr class=\"dtend\" title=\"").
                append(machineEndDate).
                append("\">").
                append(humanEndDate).
                append("</abbr>");
        if (location != null)
            buf.append(" at " ).
                append("<span class=\"location\">").
                append(StringEscapeUtils.escapeHtml(location)).
                append("</span>").
                append("</div>");

        return buf.toString();
    }

    private String toSummary(String summary) {
        return summary != null ? summary : "Unnamed event";
    }

    private static String toMachineDate(Date date) {
        if (date == null)
            return null;
        TimeZone tz = date instanceof DateTime ?
            ((DateTime) date).getTimeZone() : null;
        return DateUtil.formatRfc3339Date(date, tz);
    }

    private static String toHumanDate(Date date) {
        if (date == null)
            return null;
        if (date instanceof DateTime) {
            TimeZone tz = ((DateTime) date).getTimeZone();
            return DateUtil.formatDate(PATTERN_HUMAN_DATETIME, date, tz);
        }
        return DateUtil.formatDate(PATTERN_HUMAN_DATE, date);
    }
}
