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
package org.osaf.cosmo.atom.processor;

import java.io.Reader;

import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;

/**
 * A base class for implementations of {@link ContentProcessor} that
 * work with ICalendar objects.
 *
 * @see Calendar
 * @see NoteItem
 */
public abstract class BaseICalendarProcessor extends BaseContentProcessor {
    private static final Log log = LogFactory.getLog(BaseICalendarProcessor.class);

    // ContentProcessor methods

    /**
     * Process a content body describing an item to be added as a
     * item of the given collection.
     *
     * @param content the content
     * @param collection the parent of the new item
     * @throws ValidationException if the content does not represent a
     * valid calendar component
     * @throws ProcessorException
     * @return the new item
     */
    public NoteItem processCreation(Reader content,
                                    CollectionItem collection)
        throws ValidationException, ProcessorException {
        CalendarComponent component = readCalendarComponent(content);
        NoteItem item = createChild(collection);

        applyComponent(item, component);

        return item;
    }

    /**
     * Process an EIMML content body describing changes to an item.
     *
     * @param content the content
     * @param item the item which the content represents
     * @throws ValidationException if the content does not represent a
     * valid calendar component
     * @throws ProcessorException
     */
    public void processContent(Reader content,
                               NoteItem item)
        throws ValidationException, ProcessorException {
        CalendarComponent component = readCalendarComponent(content);

        applyComponent(item, component);
    }

    // our methods

    /**
     * Converts the content body into a valid calendar component.
     *
     * @throws ValidationException if the content does not represent a
     * valid calendar component
     * @throws ProcessorException
     */
    protected abstract CalendarComponent readCalendarComponent(Reader content) throws ValidationException, ProcessorException;

    private NoteItem createChild(CollectionItem collection) {
        NoteItem item = new NoteItem();

        // let the storage layer assign a uid to the item

        item.setOwner(collection.getOwner());
        item.getParents().add(collection);
        collection.getChildren().add(item);

        return item;
    }

    private void applyComponent(NoteItem item,
                                CalendarComponent component)
        throws ValidationException {
        // only support events for now
        if (component instanceof VEvent)
            applyEvent(item, (VEvent) component);
        else
            throw new ValidationException("Calendar component must be VEVENT");
    }

    private void applyEvent(NoteItem item,
                            VEvent event) {
        item.setDisplayName(event.getSummary().getValue());

        if (event.getUid() != null)
            item.setIcalUid(event.getUid().getValue());

        EventStamp es = EventStamp.getStamp(item);
        if (es == null) {
            es = new EventStamp();
            item.addStamp(es);
        }

        es.setCalendar(ICalendarUtils.createBaseCalendar(event));
    }
}
