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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.Item;
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
            outputter.output(getCalendarFromCollection(collection), out);
        } catch (ValidationException e) {
            throw new IllegalStateException("unable to validate collection calendar", e);
        }
    }
    
    /**
     * Returns an icalendar representation of a calendar collection.  
     * @param collection calendar collection
     * @return icalendar representation of collection
     */
    public static Calendar getCalendarFromCollection(CollectionItem collection) {
        
        // verify collection is a calendar
        CalendarCollectionStamp ccs = StampUtils
                .getCalendarCollectionStamp(collection);

        if (ccs == null)
            return null;
        
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // extract the supported calendar components for each child item and
        // add them to the collection calendar object.
        // index the timezones by tzid so that we only include each tz
        // once. if for some reason different calendar items have
        // different tz definitions for a tzid, *shrug* last one wins
        // for this same reason, we use a single calendar builder/time
        // zone registry.
        HashMap tzIdx = new HashMap();
        
        for(Item item: collection.getChildren()) {
           if(!(item instanceof ContentItem))
               continue;
           
           ContentItem contentItem = (ContentItem) item;
           Calendar childCalendar = getCalendar(contentItem);
           
           // ignore items that can't be converted
           if(childCalendar==null)
               continue;
           
           // index VTIMEZONE and add all other components
           for (Iterator j=childCalendar.getComponents().iterator();
               j.hasNext();) {
               Component comp = (Component) j.next();
               if(Component.VTIMEZONE.equals(comp.getName())) {
                   Property tzId = comp.getProperties().getProperty(Property.TZID);
                   if (! tzIdx.containsKey(tzId.getValue()))
                       tzIdx.put(tzId.getValue(), comp);
               } else {
                   calendar.getComponents().add(comp);
               }
           }
        }
        
        // add VTIMEZONEs
        for (Iterator<Component> i=tzIdx.values().iterator(); i.hasNext();) {
            calendar.getComponents().add(0,i.next());
        }
       
        return calendar;
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
