/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;

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
                    EntityFactory entityFactory)
        throws DavException {
        this(entityFactory.createNote(), locator, factory, entityFactory);
        getItem().addStamp(entityFactory.createEventStamp((NoteItem) getItem()));
    }
    
    /** */
    public DavEvent(NoteItem item,
                    DavResourceLocator locator,
                    DavResourceFactory factory,
                    EntityFactory entityFactory)
        throws DavException {
        super(item, locator, factory, entityFactory);
    }

    // our methods

    /**
     * Returns the calendar object associated with this resource.
     */
    public Calendar getCalendar() {
        Calendar calendar = new EntityConverter(null).convertNote((NoteItem)getItem());
        // run through client filter because unfortunatley
        // all clients don't adhere to the spec
        getClientFilterManager().filterCalendar(calendar);
        return calendar;
    }
    
    public EventStamp getEventStamp() {
        return StampUtils.getEventStamp(getItem());
    }

    protected void setCalendar(Calendar calendar)
        throws DavException {
        
        ComponentList vevents = calendar.getComponents(Component.VEVENT);
        if (vevents.isEmpty())
            throw new UnprocessableEntityException("VCALENDAR does not contain any VEVENTs");

        getEventStamp().setEventCalendar(calendar);
    }    
}
