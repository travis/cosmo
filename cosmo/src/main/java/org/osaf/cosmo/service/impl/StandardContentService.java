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
package org.osaf.cosmo.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import net.fortuna.ical4j.model.DateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.RecurrenceExpander;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.NoteOccurrenceUtil;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.lock.LockManager;
import org.osaf.cosmo.service.triage.TriageStatusQueryContext;
import org.osaf.cosmo.service.triage.TriageStatusQueryProcessor;

/**
 * Standard implementation of <code>ContentService</code>.
 *
 * @see ContentService
 * @see ContentDao
 */
public class StandardContentService implements ContentService {
    private static final Log log =
        LogFactory.getLog(StandardContentService.class);

    private CalendarDao calendarDao;
    private ContentDao contentDao;
    private LockManager lockManager;
    private TriageStatusQueryProcessor triageStatusQueryProcessor;
  
    private long lockTimeout = 0;

    // ContentService methods

    /**
     * Get the root item for a user
     *
     * @param user
     */
    public HomeCollectionItem getRootItem(User user) {
        if (log.isDebugEnabled()) {
            log.debug("getting root item for " + user.getUsername());
        }
        return contentDao.getRootItem(user);
    }

    /**
     * Find an item with the specified uid.  If the uid is found, return
     * the item found.  If the uid represents a recurring NoteItem occurrence
     * (parentUid:recurrenceId), return a NoteOccurrence.
     *
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    public Item findItemByUid(String uid) {
        if (log.isDebugEnabled()) {
            log.debug("finding item with uid " + uid);
        }
        Item item = contentDao.findItemByUid(uid);
        
        // return item if found
        if(item!=null)
            return item;      
        
        // Handle case where uid represents an occurence of a
        // recurring item.
        if(uid.indexOf(ModificationUid.RECURRENCEID_DELIMITER)!=-1) {
            ModificationUid modUid;
            
            try {
                modUid = new ModificationUid(uid);
            } catch (ModelValidationException e) {
                // If ModificationUid is invalid, item isn't present
                return null;
            }
            // Find the parent, and then verify that the recurrenceId is a valid
            // occurrence date for the recurring item.
            NoteItem parent = (NoteItem) contentDao.findItemByUid(modUid.getParentUid());
            if(parent==null)
                return null;
            else
                return getNoteOccurrence(parent, modUid.getRecurrenceId());
        }
        
        return null;
    }

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     */
    public Item findItemByPath(String path) {
        if (log.isDebugEnabled()) {
            log.debug("finding item at path " + path);
        }
        return contentDao.findItemByPath(path);
    }
    
    /**
     * Find content item by path relative to the identified parent
     * item.
     *
     * @throws NoSuchItemException if a item does not exist at
     * the specified path
     */
    public Item findItemByPath(String path,
                               String parentUid) {
        if (log.isDebugEnabled())
            log.debug("finding item at path " + path + " below parent " +
                      parentUid);
        return contentDao.findItemByPath(path, parentUid);
    }
    
    /**
     * Find content item's parent by path. Path is of the format:
     * /username/parent1/parent2/itemname.  In this example,
     * the item at /username/parent1/parent2 would be returned.
     */
    public Item findItemParentByPath(String path) {
        if (log.isDebugEnabled()) {
            log.debug("finding item's parent at path " + path);
        }
        return contentDao.findItemParentByPath(path);
    }

    /**
     * Update an existing item.
     * 
     * @param item
     *            item to update
     * @return updated item
     */
    public Item updateItem(Item item) {
        if (log.isDebugEnabled()) {
            log.debug("updating item " + item.getUid());
        }
        
        if (item instanceof CollectionItem)
            return updateCollection((CollectionItem) item);
        return updateContent((ContentItem) item);
    }
    
    
    /**
     * Add an item to a collection.
     * @param item item to add to collection
     * @param collection collection to add item to
     */
    public void addItemToCollection(Item item, CollectionItem collection) {
        if (log.isDebugEnabled()) {
            log.debug("adding item " + item.getUid() + " to collection "
                    + collection.getUid());
        }
        
        contentDao.addItemToCollection(item, collection);
        contentDao.updateCollectionTimestamp(collection);
    }

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
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if Item is a ContentItem and destination CollectionItem
     *         is lockecd.
     */
    public void copyItem(Item item, String path, boolean deepCopy) {
        
        Item toItem = findItemByPath(path);
        if(toItem!=null)
            throw new DuplicateItemNameException(path + " exists");
        
        // handle case of copying ContentItem (need to sync on dest collection)
        if(item != null && item instanceof ContentItem) {
            
            // need to get exclusive lock to destination collection
            CollectionItem parent = 
                (CollectionItem) contentDao.findItemParentByPath(path);
            
            // only attempt to lock if destination exists
            if(parent!=null) {
                
                // if we can't get lock, then throw exception
                if (!lockManager.lockCollection(parent, lockTimeout))
                    throw new CollectionLockedException(
                            "unable to obtain collection lock");

                try {
                    contentDao.copyItem(item, path, deepCopy);
                } finally {
                    lockManager.unlockCollection(parent);
                }
                
            } else { 
                // let the dao handle throwing an error
                contentDao.copyItem(item, path, deepCopy);
            }
        }
        else { 
            // no need to synchronize if not ContentItem
            contentDao.copyItem(item, path, deepCopy);
        }
    }
  
    /**
     * Move item to the given path
     * @param fromPath path of item to move
     * @param toPath path of item to move
     * @throws org.osaf.cosmo.model.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.model.DuplicateItemNameException
     *         if path points to an item with the same path
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if Item is a ContentItem and source or destination 
     *         CollectionItem is lockecd.
     */
    public void moveItem(String fromPath, String toPath) {
        
        Item toItem = findItemByPath(toPath);
        if(toItem!=null)
            throw new DuplicateItemNameException(toPath + " exists");
        
        CollectionItem oldParent = (CollectionItem) contentDao
                .findItemParentByPath(fromPath);
        
        if(oldParent==null | !(oldParent instanceof CollectionItem))
            throw new ItemNotFoundException("no item found for " + fromPath);
        
        CollectionItem newParent = (CollectionItem) contentDao
                .findItemParentByPath(toPath);

        if(newParent==null || !(newParent instanceof CollectionItem) )
            throw new ItemNotFoundException("no collection found for " + toPath + " found");
        
        Item fromItem = contentDao.findItemByPath(fromPath);
        
        if(fromItem==null)
            throw new ItemNotFoundException("no item found for " + fromPath);
        
        // Only need locking for ContentItem for now
        if(fromItem instanceof ContentItem) {
            Set<CollectionItem> locks = acquireLocks(newParent, fromItem);
            try {
                contentDao.moveItem(fromPath, toPath);
                // update collections involved
                for(CollectionItem parent : locks)
                    contentDao.updateCollectionTimestamp(parent);
                
            } finally {
                releaseLocks(locks);
            }
        } else {
            contentDao.moveItem(fromPath, toPath);
        }
        
    }
    
    /**
     * Remove an item.
     * 
     * @param item
     *            item to remove
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if Item is a ContentItem and parent CollectionItem
     *         is locked
     */
    public void removeItem(Item item) {
        if (log.isDebugEnabled()) {
            log.debug("removing item " + item.getUid());
        }
        
        // Let service handle ContentItems (for sync purposes)
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else
            contentDao.removeItem(item);
    }
    
    /**
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param item item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        if (log.isDebugEnabled()) {
            log.debug("removing item " + item.getUid() + " from collection "
                    + collection.getUid());
        }
        
        contentDao.removeItemFromCollection(item, collection);
        contentDao.updateCollectionTimestamp(collection);
    }

    /**
     * Remove an item.
     * 
     * @param path
     *            path of item to remove
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if Item is a ContentItem and parent CollectionItem
     *         is locked
     */
    public void removeItem(String path) {
        if (log.isDebugEnabled()) {
            log.debug("removing item at path " + path);
        }
        Item item = contentDao.findItemByPath(path);
        if (item == null)
            return;
        
        removeItem(item);
    }
    
    /**
     * Load all children for collection that have been updated since a given
     * timestamp. If no timestamp is specified, then return all children.
     * 
     * @param collection
     *            collection
     * @param timestamp
     *            timestamp
     * @return children of collection that have been updated since timestamp, or
     *         all children if timestamp is null
     */
    public Set<ContentItem> loadChildren(CollectionItem collection,
            java.util.Date timestamp) {
        return contentDao.loadChildren(collection, timestamp);
    }

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
                                           CollectionItem collection) {
        if (log.isDebugEnabled()) {
            log.debug("creating collection " + collection.getName() +
                      " in " + parent.getName());
        }
        
        return contentDao.createCollection(parent, collection);
    }

    /**
     * Create a new collection.
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
            CollectionItem collection, Set<Item> children) {
        if (log.isDebugEnabled()) {
            log.debug("creating collection " + collection.getName() + " in "
                    + parent.getName());
        }

        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of the children.  If all children are new
        // items, then no locks are obtained.
        Set<CollectionItem> locks = acquireLocks(collection, children);
        
        try {
            // Create the new collection
            collection = contentDao.createCollection(parent, collection);
            
            Set<ContentItem> childrenToUpdate = new LinkedHashSet<ContentItem>();
            
            // Keep track of NoteItem modifications that need to be processed
            // after the master NoteItem.
            ArrayList<NoteItem> modifications = new ArrayList<NoteItem>(); 
            
            // Either create or update each item
            for (Item item : children) {
                if (item instanceof NoteItem) {
                    
                    NoteItem note = (NoteItem) item;
                    
                    // If item is a modification and the master note
                    // hasn't been created, then we need to process
                    // the master first.
                    if(note.getModifies()!=null)
                        modifications.add(note);
                    else
                        childrenToUpdate.add(note); 
                }
            }
            
            // add modifications to end of set
            for(NoteItem mod: modifications)
                childrenToUpdate.add(mod);
            
            // update all children and collection
            collection = contentDao.updateCollection(collection, childrenToUpdate);
            
            // update timestamps on all collections involved
            for(CollectionItem lockedCollection : locks) {
                lockedCollection = contentDao.updateCollectionTimestamp(lockedCollection);
                if(lockedCollection.getUid().equals(collection.getUid()))
                    collection = lockedCollection;
            }
            
            // get latest timestamp
            return collection;
            
        } finally {
           releaseLocks(locks);
        }
    }
    
    /**
     * Update collection item
     * 
     * @param collection
     *            collection item to update
     * @return updated collection
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if CollectionItem is locked
     */
    public CollectionItem updateCollection(CollectionItem collection) {

        if (! lockManager.lockCollection(collection, lockTimeout))
            throw new CollectionLockedException("unable to obtain collection lock");
        
        try {
            return contentDao.updateCollection(collection);
        } finally {
            lockManager.unlockCollection(collection);
        }
    }

    
    /**
     * Update a collection and set of children.  The set of
     * children to be updated can include updates to existing
     * children, new children, and removed children.  A removal
     * of a child Item is accomplished by setting Item.isActive
     * to false to an existing Item.
     * 
     * The collection is locked at the beginning of the update. Any
     * other update that begins before this update has completed, and
     * the collection unlocked, will fail immediately with a
     * <code>CollectionLockedException</code>.
     *
     * @param collection
     *             collection to update
     * @param children
     *             children to update
     * @return updated collection
     * @throws CollectionLockedException if the collection is
     *         currently locked for an update.
     */
    public CollectionItem updateCollection(CollectionItem collection,
                                           Set<Item> updates) {
        if (log.isDebugEnabled()) {
            log.debug("updating collection " + collection.getUid());
        }

        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of updated items.
        Set<CollectionItem> locks = acquireLocks(collection, updates);
        
        try {
            Set<ContentItem> childrenToUpdate = new LinkedHashSet<ContentItem>();
            
            // Keep track of NoteItem modifications that need to be processed
            // after the master NoteItem.
            ArrayList<NoteItem> modifications = new ArrayList<NoteItem>(); 
            
            // Either create or update each item
            for (Item item : updates) {
                if (item instanceof NoteItem) {
                    
                    NoteItem note = (NoteItem) item;
                    
                    // If item is a modification and the master note
                    // hasn't been created, then we need to process
                    // the master first.
                    if(note.getModifies()!=null)
                        modifications.add(note);
                    else
                        childrenToUpdate.add(note);
                }
            }
            
            for(NoteItem mod: modifications) {
                // Only update modification if master has not been
                // deleted because master deletion will take care
                // of modification deletion.
                if(mod.getModifies().getIsActive()==true)
                    childrenToUpdate.add(mod);
            }
            
            collection = contentDao.updateCollection(collection, childrenToUpdate);
            
            // update collections involved
            for(CollectionItem lockedCollection : locks) {
                lockedCollection = contentDao.updateCollectionTimestamp(lockedCollection);
                if(lockedCollection.getUid().equals(collection.getUid()))
                    collection = lockedCollection;
            }
            
            // get latest timestamp
            return collection;
            
        } finally {
            releaseLocks(locks);
        }
    }

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection) {
        if (log.isDebugEnabled())
            log.debug("removing collection " + collection.getUid());

        contentDao.removeCollection(collection);
    }

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
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public ContentItem createContent(CollectionItem parent,
                                     ContentItem content) {
        if (log.isDebugEnabled()) {
            log.debug("creating content item " + content.getName() +
                      " in " + parent.getName());
        }
        
        // Obtain locks to all collections involved.
        Set<CollectionItem> locks = acquireLocks(parent, content);
        
        try {
            content = contentDao.createContent(parent, content);
            
            // update collections
            for(CollectionItem col : locks)
                contentDao.updateCollectionTimestamp(col);
            
            return content;
        } finally {
            releaseLocks(locks);
        }   
    }
    
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
                                     Set<ContentItem> contentItems) {
        if (log.isDebugEnabled()) {
            log.debug("creating content items in " + parent.getName());
        }
        
        if (! lockManager.lockCollection(parent, lockTimeout))
            throw new CollectionLockedException("unable to obtain collection lock");
        
        try {
            for(ContentItem content : contentItems)
                contentDao.createContent(parent, content);
            
            contentDao.updateCollectionTimestamp(parent);
        } finally {
            lockManager.unlockCollection(parent);
        }   
    }
    
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
    public void updateContentItems(Set<CollectionItem> parents, Set<ContentItem> contentItems) {
        
        if (log.isDebugEnabled()) {
            log.debug("updating content items");
        }
        
        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of updated items.
        Set<CollectionItem> locks = acquireLocks(contentItems);
        
        try {
            
           for(ContentItem content: contentItems) {
               if(content.getCreationDate()==null)
                   contentDao.createContent(parents, content);
               else if(content.getIsActive()==Boolean.FALSE)
                   contentDao.removeContent(content);
               else
                   contentDao.updateContent(content);
           }
           
           // update collections
           for(CollectionItem parent : locks)
               contentDao.updateCollectionTimestamp(parent);
        } finally {
            releaseLocks(locks);
        }
    }
    

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public ContentItem updateContent(ContentItem content) {
        if (log.isDebugEnabled()) {
            log.debug("updating content item " + content.getUid());
        }
        
        Set<CollectionItem> locks = acquireLocks(content);
        
        try {
            content = contentDao.updateContent(content);
            
            // update collections
            for(CollectionItem parent : locks)
                contentDao.updateCollectionTimestamp(parent);
            
            return content;
        } finally {
            releaseLocks(locks);
        }
    }

    /**
     * Remove content item
     * 
     * @param content
     *            content item to remove
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked           
     */
    public void removeContent(ContentItem content) {
        if (log.isDebugEnabled()) {
            log.debug("removing content item " + content.getUid());
        }
        
        Set<CollectionItem> locks = acquireLocks(content);
        
        try {
            contentDao.removeContent(content);
            // update collections
            for(CollectionItem parent : locks)
                contentDao.updateCollectionTimestamp(parent);
        } finally {
            releaseLocks(locks);
        }
    }

    
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
    public Set<ContentItem> findEvents(CollectionItem collection, DateTime rangeStart, DateTime rangeEnd, boolean expandRecurringEvents) {
        return calendarDao.findEvents(collection, rangeStart, rangeEnd, expandRecurringEvents);
    }
    
    
    /**
     * Find note items by triage status that belong to a collection.
     * @param collection collection
     * @param context the query context
     * @return set of notes that match the specified triage status label and
     *         belong to the specified collection
     */
    public SortedSet<NoteItem> findNotesByTriageStatus(CollectionItem collection,
            TriageStatusQueryContext context) {
        return triageStatusQueryProcessor.processTriageStatusQuery(collection,
                context);
    }
    
    /**
     * Find note items by triage status that belong to a recurring note series.
     * @param note recurring note
     * @param context the query context
     * @return set of notes that match the specified triage status label and belong
     *         to the specified recurring note series
     */
    public SortedSet<NoteItem> findNotesByTriageStatus(NoteItem note,
            TriageStatusQueryContext context) {
        return triageStatusQueryProcessor.processTriageStatusQuery(note,
                context);
    }
    
    /**
     * Find items by filter.
     *
     * @param filter
     *            filter to use in search
     * @return set items matching specified
     *         filter.
     */
    public Set<Item> findItems(ItemFilter filter) {
        return contentDao.findItems(filter);
    }

    /**
     * Creates a ticket on an item.
     *
     * @param item the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(Item item,
                             Ticket ticket) {
        if (log.isDebugEnabled()) {
            log.debug("creating ticket on item " + item.getUid());
        }
        contentDao.createTicket(item, ticket);
    }

    /**
     * Creates a ticket on an item.
     *
     * @param path the path of the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(String path,
                             Ticket ticket) {
        if (log.isDebugEnabled()) {
            log.debug("creating ticket on item at path " + path);
        }
        Item item = contentDao.findItemByPath(path);
        if (item == null)
            throw new IllegalArgumentException("item not found for path " + path);
        contentDao.createTicket(item, ticket);
    }

    /**
     * Returns all tickets on the given item.
     *
     * @param item the item to be ticketed
     */
    public Set getTickets(Item item) {
        if (log.isDebugEnabled()) {
            log.debug("getting tickets for item " + item.getUid());
        }
        return contentDao.getTickets(item);
    }

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
                            String key) {
        if (log.isDebugEnabled()) {
            log.debug("getting ticket " + key + " for item " + item.getUid());
        }
        return contentDao.getTicket(item, key);
    }

    public Ticket getTicket(String itemId, String key){
        Item item = findItemByUid(itemId);
        return getTicket(item, key);
    }

    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param ticket the ticket to remove
     */
    public void removeTicket(Item item,
                             Ticket ticket) {
        if (log.isDebugEnabled()) {
            log.debug("removing ticket " + ticket.getKey() + " on item " +
                      item.getUid());
        }
        contentDao.removeTicket(item, ticket);
    }

    /**
     * Removes a ticket from an item.
     *
     * @param path the path of the item to be de-ticketed
     * @param key the key of the ticket to remove
     */
    public void removeTicket(String path,
                             String key) {
        if (log.isDebugEnabled()) {
            log.debug("removing ticket " + key + " on item at path " + path);
        }
        Item item = contentDao.findItemByPath(path);
        if (item == null)
            throw new IllegalArgumentException("item not found for path " + path);
        Ticket ticket = contentDao.getTicket(item, key);
        if (ticket == null)
            return;
        contentDao.removeTicket(item, ticket);
    }

    // Service methods

    /**
     * Initializes the service, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        if (calendarDao == null)
            throw new IllegalStateException("calendarDao must not be null");
        if (contentDao == null)
            throw new IllegalStateException("contentDao must not be null");
        if (lockManager == null)
            throw new IllegalStateException("lockManager must not be null");
        if(triageStatusQueryProcessor == null)
            throw new IllegalStateException("triageStatusQueryProcessor must not be null");
    }

    /**
     * Readies the service for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /** */
    public CalendarDao getCalendarDao() {
        return calendarDao;
    }

    /** */
    public void setCalendarDao(CalendarDao dao) {
        calendarDao = dao;
    }

    /** */
    public ContentDao getContentDao() {
        return contentDao;
    }

    /** */
    public void setContentDao(ContentDao dao) {
        contentDao = dao;
    }
    
    
    public void setTriageStatusQueryProcessor(
            TriageStatusQueryProcessor triageStatusQueryProcessor) {
        this.triageStatusQueryProcessor = triageStatusQueryProcessor;
    }
    
    /** */
    public LockManager getLockManager() {
        return lockManager;
    }

    /** */
    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }
    
    
    /**
     * Sets the maximum ammount of time (in millisecondes) that the
     * service will wait on acquiring an exclusive lock on a CollectionItem.
     * @param lockTimeout
     */
    public void setLockTimeout(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }
    
    /**
     * Given a set of items, aquire a lock on all parents
     */
    private Set<CollectionItem> acquireLocks(Set<ContentItem> children) {
        
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            for(Item child : children)
                acquireLocks(locks, child);
           
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    /**
     * Given a collection and a set of items, aquire a lock on the collection and
     * all 
     */
    private Set<CollectionItem> acquireLocks(CollectionItem collection, Set<Item> children) {
        
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            if (! lockManager.lockCollection(collection, lockTimeout))
                throw new CollectionLockedException("unable to obtain collection lock");
            
            locks.add(collection);
            
            for(Item child : children)
                acquireLocks(locks, child);
           
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private Set<CollectionItem> acquireLocks(CollectionItem collection, Item item) {
        HashSet<Item> items = new HashSet<Item>();
        items.add(item);
        
        return acquireLocks(collection, items);
    }
    
    private Set<CollectionItem> acquireLocks(Item item) {
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        try {
            acquireLocks(locks,item);
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private void acquireLocks(Set<CollectionItem> locks, Item item) {
        for(CollectionItem parent: item.getParents()) {
            if(locks.contains(parent))
                continue;
            if (! lockManager.lockCollection(parent, lockTimeout))
                throw new CollectionLockedException("unable to obtain collection lock");
            locks.add(parent);
        }
        
        // Acquire locks on master item's parents, as an addition/deletion
        // of a modifications item affects all the parents of the master item.
        if(item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null)
                acquireLocks(locks, note.getModifies());
        }
    }
    
    private void releaseLocks(Set<CollectionItem> locks) {
        for(CollectionItem lock : locks)
            lockManager.unlockCollection(lock);
    }
    
    private NoteOccurrence getNoteOccurrence(NoteItem parent, net.fortuna.ical4j.model.Date recurrenceId) {
        EventStamp eventStamp = StampUtils.getEventStamp(parent);
        
        // parent must be a recurring event
        if(eventStamp==null || !eventStamp.isRecurring())
            return null;
        
        // verify that occurrence date is valid
        RecurrenceExpander expander = new RecurrenceExpander();
        if(expander.isOccurrence(eventStamp.getCalendar(), recurrenceId))
            return NoteOccurrenceUtil.createNoteOccurrence(recurrenceId, parent);
        
        return null;
    }
}
