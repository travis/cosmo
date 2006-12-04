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
package org.osaf.cosmo.dav.impl;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>ContentItem</code> with an <code>EventStamp</code> to 
 * the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavFile
 */
public class DavEvent extends DavCalendarResource {
    private static final Log log = LogFactory.getLog(DavEvent.class);

   
    /** */
    public DavEvent(DavResourceLocator locator,
                    DavResourceFactory factory,
                    DavSession session) {
        this(new NoteItem(), locator, factory, session);
        getItem().addStamp(new EventStamp());
    }
    
    /** */
    public DavEvent(NoteItem item,
                    DavResourceLocator locator,
                    DavResourceFactory factory,
                    DavSession session) {
        super(item, locator, factory, session);
    }

    
    // our methods

    /**
     * Returns the calendar object associated with this resource.
     */
    public Calendar getCalendar() {
        return getEventStamp().getCalendar();
    }
    
    public EventStamp getEventStamp() {
        return EventStamp.getStamp(getItem());
    }

    protected void setCalendar(Calendar calendar) {
        NoteItem noteItem = (NoteItem) getItem();
        getEventStamp().setCalendar(calendar);
        
        // set NoteItem props (icaluid and body)
        noteItem.setIcalUid(getEventStamp().getIcalUid());
        
        // Set body of note to be description of event
        VEvent event = getEventStamp().getMasterEvent();
        Property description = 
            event.getProperties().getProperty(Property.DESCRIPTION);
        if(description != null)
            noteItem.setBody(description.getValue());
        
    }    
}
