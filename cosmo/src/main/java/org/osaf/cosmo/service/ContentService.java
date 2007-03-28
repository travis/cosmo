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
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
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
     * Find content item by path relative to the identified parent
     * item.
     *
     * @throws NoSuchItemException if a item does not exist at
     * the specified path
     */
    public Item findItemByPath(String path,
                               String parentUid);
    
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
     * @param fromPath path of item to move
     * @param toPath path of item to move
     * @throws org.osaf.cosmo.model.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.model.DuplicateItemNameException
     *         if path points to an item with the same path
     */
    public void moveItem(String fromPath, String toPath);
    
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
     * Create a new collection with an initial set of items.
     * The initial set of items can include new items and
     * existing items.  New items will be created and associated
     * to the new collection and existing items will be updated
     * and associated to the new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @param children
     *            collection children
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection,
                                           Set<Item> children);
    
    /**
     * Update a collection and set child items.  The set of
     * child items to be updated can include updates to existing
     * children, new children, and removed children.  A removal
     * of a child Item is accomplished by setting Item.isActive
     * to false to an existing Item.  When an item is marked
     * for removal, it is removed from the collection and
     * removed from the server only if the item has no parent
     * collections.
     * 
     * @param collection
     *             collection to update
     * @param children
     *             children to update
     * @return updated collection
     */
    public CollectionItem updateCollection(CollectionItem collection,
                                           Set<Item> children);
    
    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection);

    /**
     * Update collection item
     * 
     * @param collection
     *            collection item to update
     * @return updated collection
     */
    public CollectionItem updateCollection(CollectionItem collection);
    
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
     * Find calendar events by filter.
     *
     * @param calendar
     *            calendar collection to search
     * @param filter
     *            filter to use in search
     * @return set CalendarEventItem objects matching specified
     *         filter.
     */
    public Set<ContentItem> findEvents(CollectionItem calendar,
                                             CalendarFilter filter);

    
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
     * Returns the identified ticket on the given item, or
     * <code>null</code> if the ticket does not exists. Tickets are
     * inherited, so if the specified item does not have the ticket
     * but an ancestor does, it will still be returned.
     *
     * @param itemId the uid of the item whose ticket you want
     * @param key the ticket to return
     */
    public Ticket getTicket(String itemId,
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
    
    /**
     * Create a new event based on an ical4j Calendar.  This will 
     * create the master NoteItem and any modification NoteItem's 
     * for each VEVENT modification.
     * 
     * @param parent parent collection
     * @param masterNote master note item
     * @param calendar Calendar containing master/override VEVENTs
     * @return newly created master note item
     */
    public NoteItem createEvent(CollectionItem parent, NoteItem masterNote,
            Calendar calendar);
    
    /**
     * Update existing event (NoteItem with EventStamp) based on 
     * an ical4j Calendar.  This will update the master NoteItem and 
     * any modification NoteItem's for each VEVENT modification, including
     * removing/adding modification NoteItems.
     * 
     * @param note master note item to update
     * @param calendar Calendar containing master/override VEVENTs
     * @return updated master note item
     */
    public NoteItem updateEvent(NoteItem note, Calendar calendar);
}
