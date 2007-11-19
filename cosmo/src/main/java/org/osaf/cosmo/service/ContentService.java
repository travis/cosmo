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

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import net.fortuna.ical4j.model.DateTime;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.service.triage.TriageStatusQueryContext;

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
     * Add an item to a collection.
     * @param item item to add to collection
     * @param collection collection to add item to
     */
    public void addItemToCollection(Item item, CollectionItem collection);

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
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param item item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(Item item, CollectionItem collection);

    /**
     * Remove an item.
     * 
     * @param path
     *            path of item to remove
     */
    public void removeItem(String path);
    
    /**
     * Load all children for collection that have been updated since a
     * given timestamp.  If no timestamp is specified, then return all 
     * children.
     * @param collection collection
     * @param timestamp timestamp
     * @return children of collection that have been updated since 
     *         timestamp, or all children if timestamp is null
     */
    public Set<ContentItem> loadChildren(CollectionItem collection, Date timestamp);

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
     * Create new content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to create
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void createContentItems(CollectionItem parent,
                                     Set<ContentItem> contentItems);
    
    /**
     * Update content items.  This includes creating new items, removing
     * existing items, and updating existing items.  ContentItem deletion is
     * represented by setting ContentItem.isActive to false.  ContentItem deletion
     * removes item from system, not just from the parent collections.
     * ContentItem creation adds the item to the specified parent collections.
     * 
     * @param parents
     *            parents that new conten items will be added to.
     * @param contentItems to update
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void updateContentItems(Set<CollectionItem> parents, Set<ContentItem> contentItems);
    
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
     * Find items by filter.
     *
     * @param filter
     *            filter to use in search
     * @return set items matching specified
     *         filter.
     */
    public Set<Item> findItems(ItemFilter filter);
    
   
    /**
     * Find calendar events by time range.
     *
     * @param collection
     *            collection to search
     * @param rangeStart time range start
     * @param rangeEnd time range end
     * @param expandRecurringEvents if true, recurring events will be expanded
     *        and each occurrence will be returned as a NoteItemOccurrence.
     * @return set ContentItem objects that contain EventStamps that occur
     *         int the given timeRange.
     */
    public Set<ContentItem> findEvents(CollectionItem collection,
                                             DateTime rangeStart, DateTime rangeEnd,
                                             boolean expandRecurringEvents);
    
    
    /**
     * Find note items by triage status that belong to a collection.
     * @param collection collection
     * @param context the query context
     * @return set of notes that match the specified triage status label and
     *         belong to the specified collection
     */
    public SortedSet<NoteItem> findNotesByTriageStatus(CollectionItem collection,
            TriageStatusQueryContext context);
    
    /**
     * Find note items by triage status that belong to a recurring note series.
     * @param note recurring note
     * @param context the query context
     * @return set of notes that match the specified triage status label and belong
     *         to the specified recurring note series
     */
    public SortedSet<NoteItem> findNotesByTriageStatus(NoteItem note,
            TriageStatusQueryContext context);
    
    
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
    
}
