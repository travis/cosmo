/*
 * Copyright 2007 Open Source Applications Foundation
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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;

/**
 * Extends {@link Item} to represent an abstract
 * item that is backed by an icalendar component.
 */
@Entity
@DiscriminatorValue("icalendar")
public abstract class ICalendarItem extends ContentItem {

    public static final QName ATTR_ICALENDAR = new QName(
            ICalendarItem.class, "icalendar");
    
    public ICalendarItem() {
    }
    
    /**
     * Return the Calendar object containing a calendar component.
     * @return calendar
     */
    @Transient
    public Calendar getCalendar() {
        // calendar stored as ICalendarAttribute on Item
        ICalendarAttribute calAttr = (ICalendarAttribute) getAttribute(ATTR_ICALENDAR);
        if(calAttr!=null)
            return calAttr.getValue();
        else
            return null;
    }
    
    /**
     * Set the Calendar object containing a VJOURNAL component.  
     * This allows non-standard icalendar properties to be stored 
     * with the note.
     * @param calendar
     */
    public void setCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        ICalendarAttribute calAttr = (ICalendarAttribute) getAttribute(ATTR_ICALENDAR);
        if(calAttr==null && calendar!=null) {
            calAttr = new ICalendarAttribute(ATTR_ICALENDAR, calendar);
            addAttribute(calAttr);
        }
        
        if(calendar==null)
            removeAttribute(ATTR_ICALENDAR);
        else
            calAttr.setValue(calendar);
    }
    
}
