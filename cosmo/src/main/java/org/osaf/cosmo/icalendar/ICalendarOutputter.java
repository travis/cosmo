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
package org.osaf.cosmo.icalendar;

import java.io.OutputStream;
import java.io.IOException;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.TaskStamp;

/**
 * A class that writes Cosmo calendar model objects to output streams
 * formatted according to the iCalendar specification (RFC 2445).
 */
public class ICalendarOutputter {
    private static final Log log = LogFactory.getLog(ICalendarOutputter.class);

    /**
     * Writes an iCalendar string representing the calendar items
     * contained within the given calendar collection to the given
     * output stream.
     *
     * Since the calendar content stored with each calendar items
     * is parsed and validated when the item is created, these
     * errors should not reoccur when the calendar is being
     * outputted.
     *
     * @param collection the <code>CollectionItem</code> to format
     *
     * @throws IllegalArgumentException if the collection is not
     * stamped as a calendar collection
     * @throws IOException
     */
    public static void output(CollectionItem collection,
                              OutputStream out)
        throws IOException {
        CalendarCollectionStamp stamp =
            StampUtils.getCalendarCollectionStamp(collection);
        if (stamp == null)
            throw new IllegalArgumentException("non-calendar collection cannot be formatted as iCalendar");

        if (log.isDebugEnabled())
            log.debug("outputting " + collection.getUid() + " as iCalendar");

        CalendarOutputter outputter = new CalendarOutputter();
        outputter.setValidating(false);
        try {
            outputter.output(stamp.getCalendar(), out);
        } catch (ParserException e) {
            throw new IllegalStateException("unable to compose collection calendar from child items' calendars", e);
        } catch (ValidationException e) {
            throw new IllegalStateException("unable to validate collection calendar", e);
        }
    }
    
    /**
     * Returns an icalendar representation of a ContentItem.  If the content
     * is an instance of ICalendarItem, then return the calendar associated
     * with it.  If the content is a NoteItem and the note is
     * stamped as an event, a VEVENT will be returned.  If a non-event is
     * stamped as a task, then a VTODO will be returned.  Otherwise a 
     * VJOURNAL will be returned.
     * @param item 
     * @return icalendar representation of item
     */
    public static Calendar getCalendar(ContentItem item) {
        
        if(item instanceof NoteItem)
            return getCalendarFromNote( (NoteItem) item);
        else if(item instanceof ICalendarItem)
            return ((ICalendarItem) item).getFullCalendar();
        
        return null;
    }
    
    /**
     * Returns an icalendar representation of a NoteItem. note is
     * stamped as an event, a VEVENT will be returned.  If a non-event is
     * stamped as a task, then a VTODO will be returned.  Otherwise a 
     * VJOURNAL will be returned.
     * @param note 
     * @return icalendar representation of item
     */
    public static Calendar getCalendarFromNote(NoteItem note) {
        
        // must be a master note
        if(note.getModifies()!=null)
            return null;
        
        EventStamp event = StampUtils.getEventStamp(note);
        if(event!=null)
            return event.getCalendar();
        
        TaskStamp task = StampUtils.getTaskStamp(note);
        if(task!=null)
            return task.getCalendar();
        
        return note.getFullCalendar();
    }
}
