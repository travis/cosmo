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
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.osaf.cosmo.hibernate.validator.EventException;


/**
 * Represents a calendar event.
 */
@Entity
@DiscriminatorValue("eventexception")
@SecondaryTable(name="event_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventExceptionStamp extends BaseEventStamp implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;

    private Calendar calendar = null;
 
    public static final String RECURRENCEID_DELIMITER = "::";
    
    /** default constructor */
    public EventExceptionStamp() {
    }
    
    public EventExceptionStamp(Item item) {
        setItem(item);
    }
    
    @Transient
    public String getType() {
        return "eventexception";
    }
    
    @Override
    @Transient
    public VEvent getEvent() {
        return getExceptionEvent();
    }
     
    @Override
    public void setCalendar(Calendar calendar) {
        setExceptionCalendar(calendar);
    }

    @Column(table="event_stamp", name = "icaldata", length=102400000, nullable = false)
    @Type(type="calendar_clob")
    @NotNull
    @EventException
    public Calendar getExceptionCalendar() {
        return calendar;
    }
    
    public void setExceptionCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
    
    /**
     * Returns the exception event extracted from the underlying
     * icalendar object. Changes to the exception event will be persisted
     * when the stamp is saved.
     */
    @Transient
    public VEvent getExceptionEvent() {
        return (VEvent) getExceptionCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }
    
    public void setExceptionEvent(VEvent event) {
        if(calendar==null)
            createCalendar();
        
        // remove all events
        calendar.getComponents().removeAll(
                calendar.getComponents().getComponents(Component.VEVENT));
        
        // add event exception
        calendar.getComponents().add(event);
    }

    /**
     * Return EventExceptionStamp from Item
     * @param item
     * @return EventExceptionStamp from Item
     */
    public static EventExceptionStamp getStamp(Item item) {
        return (EventExceptionStamp) item.getStamp(EventExceptionStamp.class);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Stamp#copy()
     */
    public Stamp copy(Item item) {
        EventExceptionStamp stamp = new EventExceptionStamp(item);
        
        // Need to copy Calendar
        try {
            stamp.setExceptionCalendar(new Calendar(calendar));
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy calendar", e);
        }
        
        return stamp;
    }
}
