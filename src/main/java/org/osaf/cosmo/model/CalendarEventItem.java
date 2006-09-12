package org.osaf.cosmo.model;

import java.text.SimpleDateFormat;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;

/**
 * Extends {@link CalendarItem} to represent a calendar item containing a single
 * calendar event (VEVENT).
 */
public class CalendarEventItem extends CalendarItem {
    /**
     * 
     */
    private static final long serialVersionUID = 4090512843371394571L;

    public CalendarEventItem() {
    }

    /**
     */
    public VEvent getMasterEvent() {
        return (VEvent) getCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }

    /**
     * Returns the event formatted as an iCalendar component.
     */
    public String toICalendar() {
        return getMasterEvent().toString();
    }

    /**
     * Returns the event formatted as an hCalendar document fragment.
     */
    public String toHCalendar() {
        StringBuffer buf = new StringBuffer();
        buf.append("<div class=\"vevent\">");

        Property summary = getMasterEvent().getProperties().getProperty(
                Property.SUMMARY);
        if (summary != null) {
            buf.append("<span class=\"summary\">").append(summary.getValue()). // XXX
                                                                                // HTML
                                                                                // escape
                    append("</span>");
        }

        DtStart dtstart = (DtStart) getMasterEvent().getProperties()
                .getProperty(Property.DTSTART);
        if (dtstart != null) {
            buf.append(": ").append("<span class=\"dtstart\" title=\"").append(
                    dtstart.getValue()).append("\">").append(
                    formatDate(dtstart.getDate())).append("</span>");
        }

        DtEnd dtend = (DtEnd) getMasterEvent().getProperties().getProperty(
                Property.DTEND);
        if (dtend != null) {
            buf.append(" to ").append("<span class=\"dtend\" title=\"").append(
                    dtend.getValue()).append("\">").append(
                    formatDate(dtend.getDate())).append("</span>");
        }

        Duration duration = (Duration) getMasterEvent().getProperties()
                .getProperty(Property.DURATION);
        if (duration != null && dtstart != null) {
            buf.append(" to  ").append("<span class=\"duration\" title=\"")
                    .append(duration.getValue()).append("\">").append(
                            formatDate(duration.getDuration().getTime(
                                    dtstart.getDate()))).append("</span>");
        }

        Property location = getMasterEvent().getProperties().getProperty(
                Property.LOCATION);
        if (location != null) {
            buf.append(" at ").append("<span class=\"location\">").append(
                    location.getValue()). // XXX HTML escape
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
