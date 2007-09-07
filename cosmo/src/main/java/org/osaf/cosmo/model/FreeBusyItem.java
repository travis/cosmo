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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.hibernate.validator.FreeBusy;

/**
 * Extends {@link ICalendarItem} to represent a Note item.
 */
@Entity
@DiscriminatorValue("freebusy")
public class FreeBusyItem extends ICalendarItem {

  
    public FreeBusyItem() {
    }

    @Override
    public Item copy() {
        return new FreeBusyItem();
    }
    
    /**
     * Return the Calendar object containing a VFREEBUSY component.
     * @return calendar
     */
    @Transient
    @FreeBusy
    public Calendar getFreeBusyCalendar() {
        return super.getCalendar();
    }
    
    /**
     * Set the Calendar object containing a VFREEBUSY component.
     * @param calendar
     */
    public void setFreeBusyCalendar(Calendar calendar) {
        super.setCalendar(calendar);
    }
}
