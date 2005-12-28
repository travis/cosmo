/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this calendar except in compliance with the License.
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
package org.osaf.cosmo.model;

import java.io.IOException;
import java.text.SimpleDateFormat;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;

/**
 * Extends {@link CalendarResource} to represent an event resource.
 */
public class EventResource extends CalendarResource {

    private Calendar calendar;

    /**
     */
    public EventResource() {
        super();
    }

    /**
     * Returns a {@link net.fortuna.ical4j.model.Calendar}
     * representing the content of this resource. This method will
     * only parse the content once, returning the same calendar
     * instance on subsequent invocations.
     */
    public Calendar getCalendar()
        throws IOException, ParserException {
        if (calendar == null) {
            calendar = new CalendarBuilder().build(getContent());
        }
        return calendar;
    }

    /**
     */
    public VEvent getMasterEvent()
        throws IOException, ParserException {
        return (VEvent) getCalendar().getComponents().
            getComponents(Component.VEVENT).get(0);
    }

    /**
     * Returns the event formatted as an iCalendar component.
     */
    public String toICalendar()
        throws IOException, ParserException {
        return getMasterEvent().toString();
    }

    /**
     * Returns the event formatted as an hCalendar document fragment.
     */
    public String toHCalendar()
        throws IOException, ParserException {
        StringBuffer buf = new StringBuffer();
        buf.append("<div class=\"vevent\">");

        Property summary = getMasterEvent().getProperties().
            getProperty(Property.SUMMARY);
        if (summary != null) {
            buf.append("<span class=\"summary\">").
                append(summary.getValue()). // XXX HTML escape
                append("</span>");
        }

        DtStart dtstart = (DtStart) getMasterEvent().getProperties().
            getProperty(Property.DTSTART);
        if (dtstart != null) {
            buf.append(": " ).
                append("<span class=\"dtstart\" title=\"").
                append(dtstart.getValue()).
                append("\">").
                append(formatDate(dtstart.getDate())).
                append("</span>");
        }

        DtEnd dtend = (DtEnd) getMasterEvent().getProperties().
            getProperty(Property.DTEND);
        if (dtend != null) {
            buf.append(" to " ).
                append("<span class=\"dtend\" title=\"").
                append(dtend.getValue()).
                append("\">").
                append(formatDate(dtend.getDate())).
                append("</span>");
        }

        Duration duration = (Duration) getMasterEvent().getProperties().
            getProperty(Property.DURATION);
        if (duration != null && dtstart != null) {
            buf.append(" to  " ).
                append("<span class=\"duration\" title=\"").
                append(duration.getValue()).
                append("\">").
                append(formatDate(duration.getDuration().
                                  getTime(dtstart.getDate()))).
                append("</span>");
        }

        Property location = getMasterEvent().getProperties().
            getProperty(Property.LOCATION);
        if (location != null) {
            buf.append(" at " ).
                append("<span class=\"location\">").
                append(location.getValue()). // XXX HTML escape
                append("</span>");
        }

        buf.append("</div>");
        return buf.toString();
    }

    private String formatDate(java.util.Date date) {
        String pattern = "MMM d, yyyy";
        TimeZone tz = null;
        if (date instanceof DateTime) {
            pattern = "MMM d, yyyy h:mm a zzz";
            tz = ((DateTime) date).getTimeZone();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        if (tz != null) {
            formatter.setTimeZone(tz);
        }
        return formatter.format(date);
    }
}
