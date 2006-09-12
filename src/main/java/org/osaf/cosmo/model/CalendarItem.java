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
package org.osaf.cosmo.model;

import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.calendar.util.CalendarBuilderDispenser;

/**
 * Extends {@link ContentItem} to represent a content item containing calendar
 * content. The content is in the form of iCalendar data.
 */
public abstract class CalendarItem extends ContentItem {

    private Calendar calendar;
    private Set<CalendarTimeRangeIndex> timeRangIndexes = new HashSet<CalendarTimeRangeIndex>(0);
    private Set<CalendarPropertyIndex> propertyIndexes = new HashSet<CalendarPropertyIndex>(0);
    /**
     * Returns a {@link net.fortuna.ical4j.model.Calendar} representing the
     * content of this resource. This method will only parse the content once,
     * returning the same calendar instance on subsequent invocations.
     */
    public Calendar getCalendar() {
        if (calendar == null) {
            try {
                calendar = CalendarBuilderDispenser.getCalendarBuilder()
                    .build(getContentInputStream());
            } catch (Exception e) {
                throw new ModelConversionException("cannot parse iCalendar stream: " + e.getMessage(), e);
            }
        }
        return calendar;
    }
    
    public Set<CalendarTimeRangeIndex> getTimeRangeIndexes() {
        return timeRangIndexes;
    }

    private void setTimeRangeIndexes(Set<CalendarTimeRangeIndex> indexes) {
        this.timeRangIndexes = indexes;
    }
    
    public Set<CalendarPropertyIndex> getPropertyIndexes() {
        return propertyIndexes;
    }

    private void setPropertyIndexes(Set<CalendarPropertyIndex> propertyIndexes) {
        this.propertyIndexes = propertyIndexes;
    }

    public void addTimeRangeIndex(CalendarTimeRangeIndex index) {
        index.setItem(this);
        timeRangIndexes.add(index);
    }
    
    public void addPropertyIndex(CalendarPropertyIndex index) {
        index.setItem(this);
        propertyIndexes.add(index);
    }

}
