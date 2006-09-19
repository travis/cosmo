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

import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;

import org.apache.log4j.Logger;

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
    private static final Logger log =
        Logger.getLogger(DavCalendarResource.class);

    /** */
    public DavCalendarResource(CalendarItem item,
                    DavResourceLocator locator,
                    DavResourceFactory factory,
                    DavSession session) {
        super(item, locator, factory, session);
    }

    /**
     * Returns the calendar object associated with this resource.
     */
    public Calendar getCalendar() {
        return ((CalendarItem) getItem()).getCalendar();
    }
}
