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
package org.osaf.cosmo.dao.mock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CalendarItem;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;

/**
 * Mock implementation of <code>CalendarDao</code> useful for testing.
 *
 * @see CalendarDao
 * @see CalendarItem
 * @see CalendarEventItem
 * @see CalendarCollectionItem
 */
public class MockCalendarDao extends MockItemDao implements CalendarDao {

    private CalendarFilter lastCalendarFilter;
    
    /**
     * Useful for unit tests.
     * @return
     */
    public CalendarFilter getLastCalendarFilter() {
        return lastCalendarFilter;
    }

    /**
     */
    public MockCalendarDao(MockDaoStorage storage) {
        super(storage);
    }

    // CalendarDao methods

    /**
     * Create new calendar collection. All calendar collections live under the
     * top-level user collection.
     *
     * @param calendar
     *            calendar collection to create
     * @return newly created calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem createCalendar(CalendarCollectionItem calendar) {
        return createCalendar(null, calendar);
    }

    /**
     * Create a new calendar collection in the specified collection.
     * @param collection collection where calendar will live
     * @param calendar calendar collection to create
     * @return newly created calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem createCalendar(CollectionItem collection,
                                                 CalendarCollectionItem calendar) {
        if (calendar == null)
            throw new IllegalArgumentException("calendar cannot be null");
        if (calendar.getOwner() == null)
            throw new IllegalArgumentException("owner cannot be null");

        if (collection == null)
            collection = getRootItem(calendar.getOwner());
        calendar.setParent(collection);

        getStorage().storeItem(calendar);

        return calendar;
    }

    /**
     * Update existing calendar collection.
     *
     * @param calendar
     *            calendar collection to update
     * @return updated calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem updateCalendar(CalendarCollectionItem calendar) {
        if (calendar == null)
            throw new IllegalArgumentException("calendar cannot be null");

        getStorage().updateItem(calendar);

        return calendar;
    }

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
                                      CalendarEventItem event) {
        if (calendar == null)
            throw new IllegalArgumentException("calendar cannot be null");
        if (event == null)
            throw new IllegalArgumentException("event cannot be null");

        event.setParent(calendar);
        getStorage().storeItem(event);

        return event;
    }

    /**
     * Create a new calendar event item from an existing content item
     * @param content
     * @return newly created calendar event item
     */
    public CalendarEventItem addEvent(ContentItem content) {
        // XXX
        throw new UnsupportedOperationException();
    }

    /**
     * Update existing calendar event item.
     *
     * @param event
     *            calendar event to update
     * @return updated calendar event
     * @throws ModelValidationException
     * @throws DuplicateItemNameException
     */
    public CalendarEventItem updateEvent(CalendarEventItem event) {
        if (event == null)
            throw new IllegalArgumentException("event cannot be null");

        getStorage().updateItem(event);

        return event;
    }

    /**
     * Find calendar collection by path. Path is of the format:
     * /username/calendarname
     *
     * @param path
     *            path of calendar
     * @return calendar represented by path
     */
    public CalendarCollectionItem findCalendarByPath(String path) {
        return (CalendarCollectionItem) findItemByPath(path);
    }

    /**
     * Find calendar collection by uid.
     *
     * @param uid
     *            uid of calendar
     * @return calendar represented by uid
     */
    public CalendarCollectionItem findCalendarByUid(String uid) {
        return (CalendarCollectionItem) findItemByUid(uid);
    }

    /**
     * Find calendar event item by path. Path is of the format:
     * /username/calendarname/eventname
     *
     * @param path
     *            path of calendar event to find
     * @return calendar event represented by path
     */
    public CalendarEventItem findEventByPath(String path) {
        return (CalendarEventItem) findItemByPath(path);
    }

    /**
     * Find calendar event by uid.
     *
     * @param uid
     *            uid of calendar event
     * @return calendar event represented by uid
     */
    public CalendarEventItem findEventByUid(String uid) {
        return (CalendarEventItem) findItemByUid(uid);
    }

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
                                             Map criteria) {
        // XXX
        throw new UnsupportedOperationException();
    }

    /**
     * Find calendar events by filter.
     * NOTE: This impl always returns an empty set, but has the side effect 
     * of setting the last 
     * @param calendar
     *            calendar collection to search
     * @param filter
     *            filter to use in search
     * @return set CalendarEventItem objects matching specified
     *         filter.
     */
    public Set<CalendarEventItem> findEvents(CalendarCollectionItem calendar,
                                             CalendarFilter filter) {
        lastCalendarFilter = filter;
        return new HashSet<CalendarEventItem>();

    }

    /**
     * Remove calendar collection.
     *
     * @param calendar
     *            calendar collection to remove.
     */
    public void removeCalendar(CalendarCollectionItem calendar) {
        removeItem(calendar);
    }

    /**
     * Remove calendar event
     *
     * @param event
     *            calendar event to remove
     */
    public void removeEvent(CalendarEventItem event) {
        removeItem(event);
    }

    public CalendarEventItem findEventByIcalUid(String uid, CalendarCollectionItem calendar) {
        // TODO implement
        return null;
    }
    
}
