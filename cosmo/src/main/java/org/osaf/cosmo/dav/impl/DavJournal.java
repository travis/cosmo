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
import net.fortuna.ical4j.model.ComponentList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.NoteItem;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>NoteItem</code> to the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavCalendarResource
 */
public class DavJournal extends DavCalendarResource {
    private static final Log log = LogFactory.getLog(DavJournal.class);
    
    /** */
    public DavJournal(DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws DavException {
        this(entityFactory.createNote(), locator, factory, entityFactory);
    }

    /** */
    public DavJournal(NoteItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws DavException {
        super(item, locator, factory, entityFactory);
    }

    // our methods

    /**
     * <p>
     * Exports the item as a calendar object containing a single VJOURNAL,
     * ignoring any stamps that may be associated with the item. Sets the
     * following properties:
     * </p>
     * <ul>
     * <li>UID: item's icalUid or uid</li>
     * <li>SUMMARY: item's displayName</li>
     * <li>DESCRIPTION: item's body</li>
     * </ul>
     */
    public Calendar getCalendar() {
        NoteItem note = (NoteItem) getItem();
        return new EntityConverter(null).convertNote(note);
    }

    /**
     * <p>
     * Imports a calendar object containing a VJOURNAL. Sets the
     * following properties:
     * </p>
     * <ul>
     * <li>display name: the VJOURNAL's SUMMARY (or the item's name, if the
     * SUMMARY is blank)</li>
     * <li>icalUid: the VJOURNAL's UID</li>
     * <li>body: the VJOURNAL's DESCRIPTION</li>
     * </ul>
     */
    public void setCalendar(Calendar cal)
        throws DavException {
        NoteItem note = (NoteItem) getItem();
      
        ComponentList vjournals = cal.getComponents(Component.VJOURNAL);
        if (vjournals.isEmpty())
            throw new UnprocessableEntityException("VCALENDAR does not contain any VJOURNALS");

        EntityConverter converter = new EntityConverter(getEntityFactory());
        converter.convertJournalCalendar(note, cal);
    }
}
