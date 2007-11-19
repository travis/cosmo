/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import org.osaf.cosmo.calendar.util.Dates;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.util.DateUtil;

/**
 * Produces text and HTML representations of an event-stamped item for use
 * in an Atom entry's summary and content fields.
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

    private NoteItem master;
    private NoteOccurrence occurrence;
    private EventStamp stamp;
    private Date start;
    private Date end;
    private String summary;
    private String location;

    /**
     * Returns an instance of <code>EventEntryFormatter</code> for the
     * given item and the system default locale.
     */
    public EventEntryFormatter(NoteItem item) {
        if (item instanceof NoteOccurrence) {
            occurrence = (NoteOccurrence) item;
            master = occurrence.getMasterNote();
        } else {
            master = item;
        }    
        stamp = StampUtils.getEventStamp(master);
        if (stamp != null) {
            if (occurrence != null) {
                start = occurrence.getOccurrenceDate();
                end = Dates.getInstance(stamp.getDuration().getTime(start),
                                        start);
            } else {
                start = stamp.getStartDate();
                end = stamp.getEndDate();
            }
            summary = item.getDisplayName();
            location = stamp.getLocation();
        }
    }

    /**
     * Returns an abbreviated plain text summary of the event.
     */
    public String formatTextSummary() {
        if (stamp == null)
            return null;

        StringBuffer buf = new StringBuffer();
    
        buf.append(toSummary(summary)).
            append(": ").
            append(toHumanDate(start));
        if (end != null && start.getTime() != end.getTime())
            buf.append(" to ").
                append(toHumanDate(end));
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
         if (stamp == null)
             return null;

        // XXX: named location with an address and/or geo may be
        // represented by a nested hcard
        // (http://microformats.org/wiki/hcard)

        // XXX: address location may be represented by a nested adr
        // (http://microformats.org/wiki/adr)

        // XXX: geo location may be represented by a nested geo
        // (http://microformats.org/wiki/geo)

        StringBuffer buf = new StringBuffer();

        buf.append("<div class=\"vevent\">").
            append("<span class=\"summary\">").
            append(StringEscapeUtils.escapeHtml(toSummary(summary))).
            append("</span>").
            append(": ").
            append("<abbr class=\"dtstart\" title=\"").
            append(toMachineDate(start)).
            append("\">").
            append(toHumanDate(start)).
            append("</abbr>");
        if (end != null && start.getTime() != end.getTime())
            buf.append(" to ").
                append("<abbr class=\"dtend\" title=\"").
                append(toMachineDate(end)).
                append("\">").
                append(toHumanDate(end)).
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
