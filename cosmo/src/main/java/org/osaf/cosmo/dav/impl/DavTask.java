/*
 * Copyright 2007Task Open Source Applications Foundation
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
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VToDo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TaskStamp;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>ContentItem</code> with an <code>TaskStamp</code> to 
 * the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavFile
 */
public class DavTask extends DavCalendarResource {
    private static final Log log = LogFactory.getLog(DavTask.class);

    /** */
    public DavTask(DavResourceLocator locator,
                  DavResourceFactory factory)
        throws DavException {
        this(new NoteItem(), locator, factory);
        getItem().addStamp(new TaskStamp());
    }

    /** */
    public DavTask(NoteItem item,
                   DavResourceLocator locator,
                   DavResourceFactory factory)
        throws DavException {
        super(item, locator, factory);
    }
    
    // our methods

    /**
     * <p>
     * Exports the stamp as a calendar object containing a single VTODO.
     * Sets the following properties:
     * </p>
     * <ul>
     * <li>UID: item's icalUid or uid</li>
     * <li>SUMMARY: item's displayName</li>
     * <li>DESCRIPTION: item's body</li>
     * </ul>
     */
    public Calendar getCalendar() {
        return getTaskStamp().getCalendar();
    }
    
    public TaskStamp getTaskStamp() {
        return TaskStamp.getStamp(getItem());
    }

    /**
     * <p>
     * Imports a calendar object containing a VTODO. Sets the
     * following properties:
     * </p>
     * <ul>
     * <li>display name: the VTODO's SUMMARY (or the item's name, if the
     * SUMMARY is blank)</li>
     * <li>icalUid: the VTODO's UID</li>
     * <li>body: the VTODO's DESCRIPTION</li>
     * <li>reminderTime: if the VTODO has a DISPLAY VALARM
     *     the reminderTime will be set to the trigger time</li>
     * </ul>
     */
    public void setCalendar(Calendar cal)
        throws DavException {
        NoteItem note = (NoteItem) getItem();
        String val = null;

        // Store calendar in task stamp
        getTaskStamp().setTaskCalendar(cal);
        
        ComponentList vtodos = cal.getComponents(Component.VTODO);
        if (vtodos.isEmpty())
            throw new UnprocessableEntityException("VCALENDAR does not contain any VTODOS");

        // Pull out relevent data (displayName, body, etc) from icalendar data
        VToDo vtodo = (VToDo) vtodos.get(0);

        val = null;
        if (vtodo.getSummary() != null)
            val = StringUtils.substring(vtodo.getSummary().getValue(), 0, 255);
        if (StringUtils.isBlank(val))
            val = note.getName();
        note.setDisplayName(val);

        val = null;
        if (vtodo.getUid() != null)
            val = vtodo.getUid().getValue();
        if (StringUtils.isBlank(val))
            throw new UnprocessableEntityException("VTODO does not contain a UID");
        note.setIcalUid(val);

        val = null;
        if (vtodo.getDescription() != null)
            val = vtodo.getDescription().getValue();
        if (val == null)
            val = "";
        note.setBody(val);
        
        // look for VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(vtodo);
        if (va != null) {
            Date reminderTime = ICalendarUtils.getTriggerDate(va.getTrigger(),
                    vtodo);
            if (reminderTime != null)
                note.setReminderTime(reminderTime);
        }
    }
}
