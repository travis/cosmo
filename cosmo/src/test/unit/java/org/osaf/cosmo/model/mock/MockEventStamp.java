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
package org.osaf.cosmo.model.mock;

import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Stamp;


/**
 * Mock EventStamp.
 */
public class MockEventStamp extends MockBaseEventStamp implements
        java.io.Serializable, EventStamp {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;
    
    
    /** default constructor */
    public MockEventStamp() {
    }
    
    public MockEventStamp(Item item) {
        this();
        setItem(item);
    }
    
    public String getType() {
        return "event";
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#getEvent()
     */
    @Override
    public VEvent getEvent() {
        return getMasterEvent();
    }
    
  
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#getExceptions()
     */
    public List<Component> getExceptions() {
        ArrayList<Component> exceptions = new ArrayList<Component>();
        
        // add all exception events
        NoteItem note = (NoteItem) getItem();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = MockEventExceptionStamp.getStamp(exception);
            if(exceptionStamp!=null)
                exceptions.add(exceptionStamp.getEvent());
        }
        
        return exceptions;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceEventStamp#getMasterEvent()
     */
    public VEvent getMasterEvent() {
        if(getEventCalendar()==null)
            return null;
        
        ComponentList events = getEventCalendar().getComponents().getComponents(
                Component.VEVENT);
        
        if(events.size()==0)
            return null;
        
        return (VEvent) events.get(0);
    }

    /**
     * Return EventStamp from Item
     * @param item
     * @return EventStamp from Item
     */
    public static EventStamp getStamp(Item item) {
        return (EventStamp) item.getStamp(EventStamp.class);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Stamp#copy()
     */
    public Stamp copy() {
        EventStamp stamp = new MockEventStamp();
        
        // Need to copy Calendar, and indexes
        try {
            stamp.setEventCalendar(new Calendar(getEventCalendar()));
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy calendar", e);
        }
        
        return stamp;
    }
}
