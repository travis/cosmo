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
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.DtStart;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;
import org.osaf.cosmo.hibernate.validator.EventException;


/**
 * Represents an event exception.
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
     * Toggle the event exception anytime parameter.
     * @param isAnyTime True if the event occurs anytime<br/>
     *                  False if the event does not occur anytime</br>
     *                  null if the event should inherit the anyTime
     *                  attribute of the master event.
     */
    @Override
    public void setAnyTime(Boolean isAnyTime) {
        // Interpret null as "missing" anyTime, meaning inherited from master
        if(isAnyTime==null) {
            DtStart dtStart = getEvent().getStartDate();
            if (dtStart == null)
                throw new IllegalStateException("event has no start date");
            Parameter parameter = dtStart.getParameters().getParameter(
                    PARAM_X_OSAF_ANYTIME);
            if(parameter!=null)
                dtStart.getParameters().remove(parameter);
            
            // "missing" anyTime is represented as X-OSAF-ANYTIME=MISSING
            dtStart.getParameters().add(getInheritedAnyTimeXParam());
        } else {
            super.setAnyTime(isAnyTime);
        }
    }
    
    /**
     * Is the event exception marked as anytime.
     * @return True if the event is an anytime event<br/>
     *         False if it is not an anytime event<br/>
     *         null if the anyTime attribute is "missing", ie inherited
     *         from the master event.
     */
    @Override
    @Transient
    public Boolean isAnyTime() {
        DtStart dtStart = getEvent().getStartDate();
        if (dtStart == null)
            return Boolean.FALSE;
        Parameter parameter = dtStart.getParameters()
            .getParameter(PARAM_X_OSAF_ANYTIME);
        if (parameter == null) {
            return Boolean.FALSE;
        }
     
        // return null for "missing" anyTime
        if(VALUE_MISSING.equals(parameter.getValue()))
            return null;

        return new Boolean(VALUE_TRUE.equals(parameter.getValue()));
    }
    
    @Transient
    private Parameter getInheritedAnyTimeXParam() {
        return new XParameter(PARAM_X_OSAF_ANYTIME, VALUE_MISSING);
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
