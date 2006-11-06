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

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CalendarItem;

/**
 * Base class for extensions of <code>DavFile</code> that adapt the
 * Cosmo <code>CalendarItem</code> subclasses to the DAV resource
 * model.
 *
 * This class does not define any live properties.
 *
 * @see DavFile
 * @see CalendarItem
 */
public class DavCalendarResource extends DavFile {
    private static final Log log =
        LogFactory.getLog(DavCalendarResource.class);

    /** */
    public DavCalendarResource(CalendarItem item,
                    DavResourceLocator locator,
                    DavResourceFactory factory,
                    DavSession session) {
        super(item, locator, factory, session);
    }

    // DavResource methods

    /** */
    public void move(DavResource destination)
        throws DavException {
        validateDestination(destination);
        super.move(destination);
    }

    /** */
    public void copy(DavResource destination,
                     boolean shallow)
        throws DavException {
        validateDestination(destination);
        super.copy(destination, shallow);
    }

    // our methods

    /**
     * Returns the calendar object associated with this resource.
     */
    public Calendar getCalendar() {
        return ((CalendarItem) getItem()).getCalendar();
    }

    private void validateDestination(DavResource destination)
        throws DavException {
        DavResource destinationCollection = destination.getCollection();

        // XXX: we should allow items to be moved/copied out of
        // calendar collections into regular collections, but they
        // need to be stripped of their calendar-ness
        if (! (destinationCollection instanceof DavCalendarCollection))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Destination collection not a calendar collection");

        CalendarCollectionItem collection = (CalendarCollectionItem)
            ((DavResourceBase) destinationCollection).getItem();
        CalendarItem item = (CalendarItem) getItem();

        if (log.isDebugEnabled())
            log.debug("validating destination " +
                      destination.getResourcePath());

        // CALDAV:supported-calendar-data
        // had to be of appropriate media type to be stored in the
        // original calendar collection

        // CALDAV:valid-calendar-data
        // same

        // CALDAV:valid-calendar-object-resource
        // same

        // CALDAV:supported-calendar-component
        // destination collection may support different component set
        if (! collection.supportsCalendar(item.getCalendar()))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object does not contain at least one supported component");

        // CALDAV:calendar-collection-location-ok not required here

        // CALDAV:max-resource-size was already taken care of when
        // DavCollection.addMember called DavResourceBase.populateItem
        // on the item, though it returned a 409 rather than a 412

        // XXX CALDAV:min-date-time

        // XXX CALDAV:max-date-time

        // XXX CALDAV:max-instances

        // XXX CALDAV:max-attendees-per-instance
    }
}
