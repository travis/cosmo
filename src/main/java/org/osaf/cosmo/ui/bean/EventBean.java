/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.ui.bean;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;

import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;

/**
 * A simple bean that translates the information about an event from
 * the iCalendar format represented by iCal4j to one more accessible
 * to JSP.
 */
public class EventBean {

    private VEvent vevent;
    private EventStamp event;

    /**
     */
    public EventBean(EventStamp event)
        throws IOException, ParserException {
        this.event = event;
        this.vevent = event.getMasterEvent();
    }

    /**
     */
    public EventStamp getEventStamp() {
        return event;
    }
    
    
    public Item getItem() {
        return event.getItem();
    }
    

    /**
     * Returns the event summary, or <code>null</code> if one was not
     * specified. 
     */
    public String getSummary() {
        Property summary = (Property)
            vevent.getProperties().getProperty(Property.SUMMARY);
        return summary != null ? summary.getValue() : null;
    }

    /**
     * Returns the event location, or <code>null</code> if one was not
     * specified.
     */
    public String getLocation() {
        Property location = (Property)
            vevent.getProperties().getProperty(Property.LOCATION);
        return location != null ? location.getValue() : null;
    }

    /**
     * Returns the time zone of the event's start date, or
     * <code>null</code> if a start date was not specified or if the
     * start date did not include a time zone specification.
     */
    public TimeZone getTimeZone() {
        DtStart dtstart = (DtStart)
            vevent.getProperties().getProperty(Property.DTSTART);
        if (dtstart != null) {
            if (dtstart.getDate() instanceof DateTime) {
                return ((DateTime) dtstart.getDate()).getTimeZone();
            }
        }
        return null;
    }

    /**
     * Returns the event start date as a <code>Date</code>, or
     * <code>null</code> if one was not specified.
     */
    public Date getStart() {
        DtStart dtstart = (DtStart)
            vevent.getProperties().getProperty(Property.DTSTART);
        return dtstart != null ? dtstart.getDate() : null;
    }

    /**
     * Returns the event start date as a ISO8601-formatted
     * <code>String</code>, or <code>null</code> if one was not
     * specified.
     */
    public String getDtStart() {
        DtStart dtstart = (DtStart)
            vevent.getProperties().getProperty(Property.DTSTART);
        return dtstart != null ? dtstart.getValue() : null;
    }

    /**
     * Returns <code>true</code> if the start date has a time
     * associated with it, <code>false</code> otherwise.
     */
    public boolean getHasStartTime() {
        Date start = getStart();
        return start != null && start instanceof DateTime;
    }

    /**
     * Returns the event end date as a <code>Date</code>, or
     * <code>null</code> if one was not specified. If a duration was
     * provided instead of an end date, an end date is calculated
     * using the duration.
     */
    public Date getEnd() {
        DtEnd dtend = (DtEnd)
            vevent.getProperties().getProperty(Property.DTEND);
        if (dtend == null) {
            Duration duration = (Duration)
                vevent.getProperties().getProperty(Property.DURATION);
            return duration != null ?
                duration.getDuration().getTime(getStart()) :
                null;
        }
        return dtend.getDate();
    }

    /**
     * Returns the event end date as a ISO8601-formatted
     * <code>String</code>, or <code>null</code> if one was not
     * specified.
     */
    public String getDtEnd() {
        DtEnd dtend = (DtEnd)
            vevent.getProperties().getProperty(Property.DTEND);
        return dtend != null ? dtend.getValue() : null;
    }

    /**
     * Returns <code>true</code> if the end date has a time
     * associated with it, <code>false</code> otherwise.
     */
    public boolean getHasEndTime() {
        Date end = getEnd();
        // the date could be a java Date as returned by a
        // Duration as opposed to a ical4j Date or DateTime returned
        // by a DateProperty - if it's a regular java Date, it has a
        // time attached
        return end != null &&
            (end instanceof DateTime ||
             ! (end instanceof net.fortuna.ical4j.model.Date));
    }

    /**
     * Returns the event duration as a <code>String</code> formatted
     * as per RFC 2445, or <code>null</code> if one was not
     * specified.
     */
    public String getDuration() {
        Duration duration = (Duration)
            vevent.getProperties().getProperty(Property.DURATION);
        return duration != null ? duration.getValue() : null;
    }

    /**
     * Returns the original iCalendar-formatted event.
     */
    public String toString() {
        return vevent.toString();
    }
}
