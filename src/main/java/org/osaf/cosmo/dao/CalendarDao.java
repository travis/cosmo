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
package org.osaf.cosmo.dao;

import java.util.Map;
import java.util.Set;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DuplicateEventUidException;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.ModelValidationException;

/**
 * Interface for DAO that provides base functionality for calendaring items.
 *
 * A calendaring item is either a CalendarItem representing a single calendar
 * item or a CalendarCollectionItem, representing a collection of calendar
 * items. Unlike content items, a CalendarCollection cannot contain other
 * CalendarCollection items.
 *
 */
public interface CalendarDao extends ItemDao {

    /**
     * Create new calendar collection. All calendar collections live under the
     * top-level user collection.
     *
     * @param calendar
     *            calendar collection to create
     * @return newly created calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem createCalendar(CalendarCollectionItem calendar);


    /**
     * Create a new calendar collection in the specified collection.
     * @param collection collection where calendar will live
     * @param calendar calendar collection to create
     * @return newly created calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem createCalendar(CollectionItem collection, CalendarCollectionItem calendar);

    /**
     * Update existing calendar collection.
     *
     * @param calendar
     *            calendar collection to update
     * @return updated calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem updateCalendar(CalendarCollectionItem calendar);

    /**
     * Create new calendar event item.
     *
     * @param calendar
     *            calendar collection that new item will belong to
     * @param event
     *            new calendar event item
     * @return newly created calendar event item
     * @throws ModelValidationException
     * @throws DuplicateItemNameException
     * @throws DuplicateEventUidException
     */
    public CalendarEventItem addEvent(CalendarCollectionItem calendar,
            CalendarEventItem event);

    /**
     * Update existing calendar event item.
     *
     * @param event
     *            calendar event to update
     * @return updated calendar event
     * @throws ModelValidationException
     * @throws DuplicateItemNameException
     */
    public CalendarEventItem updateEvent(CalendarEventItem event);

    /**
     * Find calendar collection by path. Path is of the format:
     * /username/calendarname
     *
     * @param path
     *            path of calendar
     * @return calendar represented by path
     */
    public CalendarCollectionItem findCalendarByPath(String path);

    /**
     * Find calendar collection by uid.
     *
     * @param uid
     *            uid of calendar
     * @return calendar represented by uid
     */
    public CalendarCollectionItem findCalendarByUid(String uid);

    /**
     * Find calendar event item by path. Path is of the format:
     * /username/calendarname/eventname
     *
     * @param path
     *            path of calendar event to find
     * @return calendar event represented by path
     */
    public CalendarEventItem findEventByPath(String path);

    /**
     * Find calendar event by uid.
     *
     * @param uid
     *            uid of calendar event
     * @return calendar event represented by uid
     */
    public CalendarEventItem findEventByUid(String uid);

    /**
     * Find calendar event with a specified icalendar uid. The icalendar format
     * requires that an event's uid is unique within a calendar.
     * 
     * @param uid
     *            icalendar uid of calendar event
     * @param calendar
     *            calendar collection to search
     * @return calendar event represented by uid and calendar
     */
    public CalendarEventItem findEventByIcalUid(String uid,
            CalendarCollectionItem calendar);
    
    /**
     * Find calendar events by criteria. Events can be searched based on a set
     * of item attribute criteria. Only events that contain attributes with
     * values equal to those specified in the criteria map will be returned.
     *
     * @param calendar
     *            calendar collection to search
     * @param criteria
     *            criteria to search on.
     * @return set of CalendarEventItem objects matching specified
     *         criteria.
     */
    public Set<CalendarEventItem> findEvents(CalendarCollectionItem calendar,
                                             Map criteria);

    /**
     * Find calendar events by filter.
     *
     * @param calendar
     *            calendar collection to search
     * @param filter
     *            filter to use in search
     * @return set CalendarEventItem objects matching specified
     *         filter.
     */
    public Set<CalendarEventItem> findEvents(CalendarCollectionItem calendar,
                                             CalendarFilter filter);

    /**
     * Remove calendar collection.
     *
     * @param calendar
     *            calendar collection to remove.
     */
    public void removeCalendar(CalendarCollectionItem calendar);

    /**
     * Remove calendar event
     *
     * @param event
     *            calendar event to remove
     */
    public void removeEvent(CalendarEventItem event);
}
