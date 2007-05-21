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
package org.osaf.cosmo.model.filter;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;

/**
 * Adds EventStamp specific criteria to StampFilter.
 * Matches only EventStamp instances.
 */
public class EventStampFilter extends StampFilter {
    
    private Period period = null;
    private DateTime dstart, dend, fstart, fend;
    private TimeZone timezone = null;
    private boolean expandRecurringEvents = false;
    
    public Period getPeriod() {
        return period;
    }

    /**
     * Matches events that occur in a given time-range.
     * @param period time-range
     */
    public void setPeriod(Period period) {
        this.period = period;
        // Get fixed start/end time
        dstart = period.getStart();
        dend = period.getEnd();
        
        // set timezone on floating times
        updateFloatingTimes();
    }

    public String getUTCStart() {
        return dstart.toString();
    }

    public String getUTCEnd() {
        return dend.toString();
    }

    public String getFloatStart() {
        return fstart.toString();
    }

    public String getFloatEnd() {
        return fend.toString();
    }
    
    public boolean isExpandRecurringEvents() {
        return expandRecurringEvents;
    }

    /**
     * If a time-range query is specified, the filter can be configured
     * to expand recurring events within the time range.  This results
     * in a <code>NoteOccurrence</code> item returned for each occurrence
     * of a matching recurring event during the specified time range.
     * @param expandRecurringEvents if true and a time-range is specified,
     *        return a NoteOccurrence for each occurence of a recurring event
     *        during the time-range.
     */
    public void setExpandRecurringEvents(boolean expandRecurringEvents) {
        this.expandRecurringEvents = expandRecurringEvents;
    }

    public EventStampFilter() {
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Used in time-range filtering.  If set, and there is a time-range
     * to filter on, the timezone will be used in comparing floating
     * times.  If null, the server time-zone will be used in determining
     * if floating time values match.
     * @param timezone timezone to use in comparing floating times
     */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
        updateFloatingTimes();
    }

    @Override
    public String getType() {
        return "EventStamp";
    }

    private void updateFloatingTimes() {
        if(dstart!=null) {
            Value v = dstart instanceof DateTime ?
                Value.DATE_TIME : Value.DATE;
            fstart = (DateTime) Dates.getInstance(dstart, v);
            // if the timezone is null then default system timezone is used
            fstart.setTimeZone((timezone != null) ? timezone : null);
        }
        if(dend!=null) {
            Value v = dend instanceof DateTime ?
                Value.DATE_TIME : Value.DATE;
            fend = (DateTime) Dates.getInstance(dend, v);
            // if the timezone is null then default system timezone is used
            fend.setTimeZone((timezone != null) ? timezone : null);
        }
    }
 
}
