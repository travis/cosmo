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
package org.osaf.cosmo.ui.bean;

import java.util.Date;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.Duration;

/**
 * A simple bean that translates the information about an event from
 * the iCalendar format represented by iCal4j to one more accessible
 * to JSP.
 */
public class EventBean {

    private VEvent vevent;

    /**
     */
    public EventBean(Calendar calendar) {
        vevent = (VEvent) calendar.getComponents().
            getComponents(Component.VEVENT).get(0);
    }

    /**
     */
    public String getSummary() {
        Property summary = (Property)
            vevent.getProperties().getProperty(Property.SUMMARY);
        return summary != null ? summary.getValue() : null;
    }

    /**
     */
    public String getLocation() {
        Property location = (Property)
            vevent.getProperties().getProperty(Property.LOCATION);
        return location != null ? location.getValue() : null;
    }

    /**
     */
    public Date getStart() {
        DtStart dtstart = (DtStart)
            vevent.getProperties().getProperty(Property.DTSTART);
        return dtstart != null ? dtstart.getDate() : null;
    }

    /**
     */
    public String getDtStart() {
        DtStart dtstart = (DtStart)
            vevent.getProperties().getProperty(Property.DTSTART);
        return dtstart != null ? dtstart.getValue() : null;
    }

    /**
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
     */
    public String getDtEnd() {
        DtEnd dtend = (DtEnd)
            vevent.getProperties().getProperty(Property.DTEND);
        return dtend != null ? dtend.getValue() : null;
    }

    /**
     */
    public String getDuration() {
        Duration duration = (Duration)
            vevent.getProperties().getProperty(Property.DURATION);
        return duration != null ? duration.getValue() : null;
    }

    /**
     */
    public String toString() {
        return vevent.toString();
    }
}
