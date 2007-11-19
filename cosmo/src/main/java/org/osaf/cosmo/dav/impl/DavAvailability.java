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
package org.osaf.cosmo.dav.impl;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.AvailabilityItem;
import org.osaf.cosmo.model.EntityFactory;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>AvailabilityItem</code> to the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavCalendarResource
 */
public class DavAvailability extends DavCalendarResource {
    private static final Log log = LogFactory.getLog(DavAvailability.class);
    
    /** */
    public DavAvailability(DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws DavException {
        this(entityFactory.createAvailability(), locator, factory, entityFactory);
    }

    /** */
    public DavAvailability(AvailabilityItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws DavException {
        super(item, locator, factory, entityFactory);
    }

    // our methods

    /**
     * <p>
     * Exports the item as a calendar object containing a single VAVAILABILITY
     * </p>
     */
    public Calendar getCalendar() {
        AvailabilityItem availability = (AvailabilityItem) getItem();
        return availability.getAvailabilityCalendar();
    }

    /**
     * <p>
     * Imports a calendar object containing a VAVAILABILITY. 
     * </p>
     */
    public void setCalendar(Calendar cal)
        throws DavException {
        AvailabilityItem availability = (AvailabilityItem) getItem();
        
        availability.setAvailabilityCalendar(cal);
        
        Component comp = cal.getComponent(ICalendarConstants.COMPONENT_VAVAILABLITY);
        if (comp==null)
            throw new UnprocessableEntityException("VCALENDAR does not contain a VAVAILABILITY");

        String val = null;
        Property prop = comp.getProperty(Property.UID);
        if (prop != null)
            val = prop.getValue();
        if (StringUtils.isBlank(val))
            throw new UnprocessableEntityException("VAVAILABILITY does not contain a UID");
        availability.setIcalUid(val);
    }
}
