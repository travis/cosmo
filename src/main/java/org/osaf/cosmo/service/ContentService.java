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
package org.osaf.cosmo.service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * Interface for services that manage access to user content.
 */
public interface ContentService extends Service {

    /**
     * Get the root item for a user
     *
     * @param user
     */
    public HomeCollectionItem getRootItem(User user);

    /**
     * Find an item with the specified uid. The return type will be one of
     * ContentItem, CollectionItem, CalendarCollectionItem, CalendarItem.
     *
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    public Item findItemByUid(String uid);

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     *
     * @throws NoSuchItemException if a item does not exist at
     * the specified path
     */
    public Item findItemByPath(String path);
    
    /**
     * Find content item's parent by path. Path is of the format:
     * /username/parent1/parent2/itemname.  In this example,
     * the item at /username/parent1/parent2 would be returned.
     *
     * @throws NoSuchItemException if a item does not exist at
     * the specified path
     */
    public Item findItemParentByPath(String path);

    /**
     * Update an existing item.
     * 
     * @param item
     *            item to update
     * @return updated item
     */
    public Item updateItem(Item item);

    /**
     * Copy an item to the given path
     * @param item item to copy
     * @param path path to copy item to
     * @param deepCopy true for deep copy, else shallow copy will
     *                 be performed
     * @throws org.osaf.cosmo.model.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.model.DuplicateItemNameException
     *         if path points to an item with the same path
     */
    public void copyItem(Item item, String path, boolean deepCopy);
  
    /**
     * Move item to the given path
     * @param item item to move
     * @param path path to move item to
     * @throws org.osaf.cosmo.model.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.model.DuplicateItemNameException
     *         if path points to an item with the same path
     */
    public void moveItem(Item item, String path);
    
    /**
     * Remove an item.
     * 
     * @param item
     *            item to remove
     */
    public void removeItem(Item item);

    /**
     * Remove an item.
     * 
     * @param path
     *            path of item to remove
     */
    public void removeItem(String path);

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection);

    /**
     * Find all children for collection. Children can consist of ContentItem and
     * CollectionItem objects.
     * 
     * @param collection
     *            collection to find children for
     * @return collection of child objects for parent collection. Child objects
     *         can be either CollectionItem or ContentItem.
     */
    public Collection findChildren(CollectionItem collection);

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection);

    /**
     * Create new content item. A content item represents a piece of content or
     * file.
     * 
     * @param parent
     *            parent collection of content. If null, content is assumed to
     *            live in the top-level user collection
     * @param content
     *            content to create
     * @return newly created content
     */
    public ContentItem createContent(CollectionItem parent,
                                     ContentItem content);

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    public ContentItem updateContent(ContentItem content);

    /**
     * Remove content item
     * 
     * @param content
     *            content item to remove
     */
    public void removeContent(ContentItem content);

    /**
     * Create a new calendar collection in the specified collection.
     * @param collection collection where calendar will live
     * @param calendar calendar collection to create
     * @return newly created calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem createCalendar(CollectionItem collection,
                                                 CalendarCollectionItem calendar);

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
     * Update existing calendar collection.
     *
     * @param calendar
     *            calendar collection to update
     * @return updated calendar collection
     * @throws DuplicateItemNameException
     */
    public CalendarCollectionItem updateCalendar(CalendarCollectionItem calendar);

    /**
     * Remove calendar collection.
     *
     * @param calendar
     *            calendar collection to remove.
     */
    public void removeCalendar(CalendarCollectionItem calendar);

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
     * Find calendar event by uid.
     *
     * @param uid
     *            uid of calendar event
     * @return calendar event represented by uid
     */
    public CalendarEventItem findEventByUid(String uid);

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
     * Remove calendar event
     *
     * @param event
     *            calendar event to remove
     */
    public void removeEvent(CalendarEventItem event);

    /**
     * Creates a ticket on an item.
     *
     * @param item the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(Item item,
                             Ticket ticket);

    /**
     * Creates a ticket on an item.
     *
     * @param path the path of the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(String path,
                             Ticket ticket);

    /**
     * Returns all tickets on the given item.
     *
     * @param item the item to be ticketed
     */
    public Set getTickets(Item item);

    /**
     * Returns the identified ticket on the given item, or
     * <code>null</code> if the ticket does not exists. Tickets are
     * inherited, so if the specified item does not have the ticket
     * but an ancestor does, it will still be returned.
     *
     * @param item the ticketed item
     * @param key the ticket to return
     */
    public Ticket getTicket(Item item,
                            String key);

    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param ticket the ticket to remove
     */
    public void removeTicket(Item item,
                             Ticket ticket);

    /**
     * Removes a ticket from an item.
     *
     * @param path the path of the item to be de-ticketed
     * @param key the key of the ticket to remove
     */
    public void removeTicket(String path,
                             String key);
}
