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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
    
    @Column(name="icaluid", length=255)
    //@Index(name="idx_icaluid")
    private String icalUid = null;
    
    public ICalendarItem() {
    }
    
    public String getIcalUid() {
        return icalUid;
    }

    /**
     * Set the icalendar uid for this icalendar item.  The icalUid
     * is separate from the uid.  A uid is unique across all items.
     * The icalUid only has to be unique within a collection.
     * @param icalUid
     */
    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }
    
    /**
     * Return the full icalendar representation of the item.  
     * Subclasses can override this to manipulate the calendar
     * object before returning.
     */
    public Calendar getFullCalendar() {
        return getCalendar();
    }
    
    /**
     * Return the Calendar object containing a calendar component.
     * Used by sublcasses to store specific components.
     * @return calendar
     */
    protected Calendar getCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return ICalendarAttribute.getValue(this, ATTR_ICALENDAR);
    }
    
    /**
     * Set the Calendar object containing a calendar component.
     * Used by sublcasses to store specific components.
     * @param calendar
     */
    protected void setCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        ICalendarAttribute.setValue(this, ATTR_ICALENDAR, calendar);
    }
    
    @Override
    protected void copyToItem(Item item) {
        
        if(!(item instanceof ICalendarItem))
            return;
        
        super.copyToItem(item);
        
        // copy icalUid
        ICalendarItem icalItem = (ICalendarItem) item;
        icalItem.setIcalUid(getIcalUid());
    }
    
}
