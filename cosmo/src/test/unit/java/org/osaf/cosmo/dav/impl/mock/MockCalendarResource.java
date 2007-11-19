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
package org.osaf.cosmo.dav.impl.mock;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.impl.DavCalendarResource;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.NoteItem;

/**
 * <p>
 * Mock extension of {@link DavCalendarResource}. Does not persist the
 * backing calendar. Provides attributes that control filter matching and
 * freebusy generation operations rather than delegating them to the
 * service layer, allowing classes using this mock to be tested in isolation.
 */
public class MockCalendarResource extends DavCalendarResource {
    private Calendar calendar;
    private boolean matchFilters;

    public MockCalendarResource(ContentItem item,
                                DavResourceLocator locator,
                                DavResourceFactory factory,
                                EntityFactory entityFactory)
        throws DavException {
        super(item, locator, factory, entityFactory);
        this.matchFilters = false;
    }

    public MockCalendarResource(DavResourceLocator locator,
                                DavResourceFactory factory,
                                EntityFactory entityFactory)
        throws DavException {
        this(entityFactory.createNote(), locator, factory, entityFactory);
    }

    // DavCalendarResource methods

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public boolean matches(CalendarFilter filter)
        throws DavException {
        return isMatchFilters();
    }

    // our methods

    public boolean isMatchFilters() {
        return matchFilters;
    }

    public void setMatchFilters(boolean matchFilters) {
        this.matchFilters = matchFilters;
    }
}
