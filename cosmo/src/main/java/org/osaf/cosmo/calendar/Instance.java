/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar;

import java.util.Iterator;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Trigger;

/**
 * @author cyrusdaboo
 * 
 * This class represents an instance of a (possibly) recurring component.
 */
public class Instance {

    private Component comp;
    private Date start;
    private Date end;
    private Date rid;
    private boolean overridden;
    private boolean future;

    /**
     * @param comp
     * @param end
     * @param start
     */
    public Instance(Component comp, Date start, Date end) {
        this(comp, start, end, start, false, false);
    }

    /**
     * @param comp
     * @param end
     * @param future
     * @param rid
     * @param start
     */
    public Instance(Component comp,
                    Date start,
                    Date end,
                    Date rid,
                    boolean overridden,
                    boolean future) {
        this.comp = comp;
        this.start = copyNormalisedDate(start);
        this.end = copyNormalisedDate(end);
        this.rid = copyNormalisedDate(rid);
        this.overridden = overridden;
        this.future = future;
    }

    /**
     * @return Returns the component.
     */
    public Component getComp() {
        return comp;
    }

    /**
     * @return Returns the start.
     */
    public Date getStart() {
        return start;
    }

    /**
     * @return Returns the end.
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @return Returns the rid.
     */
    public Date getRid() {
        return rid;
    }

    /**
     * @return Returns the overridden.
     */
    public boolean isOverridden() {
        return overridden;
    }

    /**
     * @return Returns the future.
     */
    public boolean isFuture() {
        return future;
    }

    /**
     * Return the set of trigger times for this instance if it has an alarm. The
     * times are returned in UTC, and repeating alarms are expanded.
     * 
     * @return a list of trigger date-times.
     */
    public DateList getAlarmTriggers() {

        DateList result = null;

        // Look for VALARMs
        ComponentList alarms = null;
        if (comp instanceof VEvent) {
            alarms = ((VEvent) comp).getAlarms();
        } else if (comp instanceof VToDo) {
            alarms = ((VToDo) comp).getAlarms();
        }

        if (alarms != null) {
            for (Iterator iter = alarms.iterator(); iter.hasNext();) {
                VAlarm valarm = (VAlarm) iter.next();
                if (result == null)
                    result = new DateList();
                result.addAll(getTriggers(valarm));
            }
        }

        // Make sure we use UTC
        if (result != null)
            result.setUtc(true);

        return result;
    }

    /**
     * Return the list of trigger times for the VALARM.
     * 
     * @param comp
     *            the VALARM component
     * @return
     */
    private DateList getTriggers(Component comp) {

        DateList result = new DateList();

        Trigger propT = (Trigger) comp.getProperties().getProperty(
                Property.TRIGGER);
        Repeat propR = (Repeat) comp.getProperties().getProperty(
                Property.REPEAT);
        Duration propD = (Duration) comp.getProperties().getProperty(
                Property.DURATION);

        boolean relativeToStart = true;
        Parameter related = propT.getParameters().getParameter(
                Parameter.RELATED);
        if (related != null)
            relativeToStart = Related.START.equals(related);

        // Find the first trigger for the alarm
        Date triggerStart = null;
        if (propT.getDateTime() != null) {
            triggerStart = copyNormalisedDate(propT.getDateTime());
        } else if (propT.getDuration() != null) {
            triggerStart = copyNormalisedDate(org.osaf.cosmo.calendar.util.Dates.getInstance(propT
                    .getDuration().getTime(
                            relativeToStart ? getStart() : getEnd()),
                    relativeToStart ? getStart() : getEnd()));
        }

        result.add(triggerStart);

        // Now apply repeats
        if ((propR != null) && (propD != null)) {
            int repeats = propR.getCount();
            Dur duration = propD.getDuration();

            for (int i = 0; i < repeats; i++) {
                triggerStart = copyNormalisedDate(org.osaf.cosmo.calendar.util.Dates.getInstance(duration
                        .getTime(triggerStart), triggerStart));
                result.add(triggerStart);
            }
        }

        return result;
    }

    /**
     * Copy a Date/DateTime and normalise to UTC if its not floating.
     * 
     * @param date
     * @return
     */
    private Date copyNormalisedDate(Date date) {
        if (date instanceof DateTime) {
            DateTime dt = new DateTime(date);
            if (!dt.isUtc() && (dt.getTimeZone() != null)) {
                dt.setUtc(true);
            }
            return dt;
        } else {
            return new Date(date);
        }
    }
}
