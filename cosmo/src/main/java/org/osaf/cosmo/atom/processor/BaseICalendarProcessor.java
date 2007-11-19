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

import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.TriageStatusUtil;

/**
 * A base class for implementations of {@link ContentProcessor} that
 * work with ICalendar objects.
 *
 * @see Calendar
 * @see NoteItem
 */
public abstract class BaseICalendarProcessor extends BaseContentProcessor {
    private static final Log log = LogFactory.getLog(BaseICalendarProcessor.class);

    private EntityFactory entityFactory;
    
    public BaseICalendarProcessor(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
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
        NoteItem item = entityFactory.createNote();

        // let the storage layer assign a uid to the item

        item.setOwner(collection.getOwner());
        item.getParents().add(collection);
        collection.getChildren().add(item);

        item.setClientCreationDate(java.util.Calendar.getInstance().getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(TriageStatusUtil.initialize(entityFactory.createTriageStatus()));
        item.setLastModifiedBy(item.getOwner().getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);

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
                            VEvent event)
        throws ValidationException {
        if (event.getSummary() == null)
            throw new ValidationException("Event summary is required");
        if (event.getStartDate() == null)
            throw new ValidationException("Event start date is required");

        if (event.getEndDate() == null && event.getDuration() == null)
            ICalendarUtils.setDuration(event, new Dur(0, 0, 0, 0));

        item.setDisplayName(event.getSummary().getValue());

        if (item.getIcalUid() == null) {
            if (event.getUid() != null)
                item.setIcalUid(event.getUid().getValue());
            else
                item.setIcalUid(item.getUid());
        }

        if (event.getDescription() != null)
            item.setBody(event.getDescription().getValue());

        java.util.Calendar now = java.util.Calendar.getInstance();
        if (event.getStartDate().getDate().before(now.getTime()))
            item.getTriageStatus().setCode(TriageStatus.CODE_DONE);
        else {
            java.util.Calendar later = java.util.Calendar.getInstance();
            later.add(java.util.Calendar.DAY_OF_MONTH, 2);
            if (event.getStartDate().getDate().after(later.getTime()))
                item.getTriageStatus().setCode(TriageStatus.CODE_LATER);
        }

        EventStamp es = StampUtils.getEventStamp(item);
        if (es == null) {
            es = entityFactory.createEventStamp(item);
            item.addStamp(es);
        }

        es.setEventCalendar(ICalendarUtils.createBaseCalendar(event));
    }

    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }
}
