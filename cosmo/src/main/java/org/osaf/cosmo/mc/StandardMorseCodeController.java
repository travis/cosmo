/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.mc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimException;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.EimRecordSetIterator;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.eim.schema.ItemTranslator;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.IcalUidInUseException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.TicketType;
import org.osaf.cosmo.model.Tombstone;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * The standard implementation for
 * <code>MorseCodeController</code> that uses the Cosmo service APIs.
 *
 * @see MorseCodeController
 */
public class StandardMorseCodeController implements MorseCodeController {
    private static final Log log =
        LogFactory.getLog(StandardMorseCodeController.class);

    private ContentService contentService;
    private UserService userService;
    private CosmoSecurityManager securityManager;
    private EntityFactory entityFactory;
    private static final HashSet<String> EMPTY_TICKETS = new HashSet<String>(0);

    /**
     * Returns information about every collection in the user's home
     * collection.
     *
     * @param username the username of the user whose collections are
     * to be described
     * @param locator the service locator used to resolve collection URLs
     *
     * @throws UnknownUserException if the user is not found
     * @throws MorseCodeException if an unknown error occurs
     */
    public CollectionService discoverCollections(String username,
                                                 ServiceLocator locator) {
        if (log.isDebugEnabled())
            log.debug("discovering collections for " + username);

        User user = userService.getUser(username);
        if(user==null)
            throw new UnknownUserException(username);
        
        HomeCollectionItem home = contentService.getRootItem(user);

        return new CollectionService(home, locator,
                                     securityManager.getSecurityContext());
    }

    /**
     * Causes the identified collection and all contained items to be
     * immediately removed from storage.
     *
     * @param uid the uid of the collection to delete
     *
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public void deleteCollection(String uid) {
        if (log.isDebugEnabled())
            log.debug("deleting collection " + uid);

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(item);

        contentService.removeCollection((CollectionItem)item);
    }

    /**
     * Creates a collection identified by the given uid and populates
     * the collection with items with the provided states. If ticket
     * types are provided, creates one ticket of each type on the
     * collection. The publish is atomic; the entire publish fails if
     * the collection or any contained item cannot be created.
     *
     * If a parent uid is provided, the associated collection becomes
     * the parent of the new collection.
     *
     * @param uid the uid of the collection to publish
     * @param parentUid the (optional) uid of the collection to set as
     * the parent for the published collection
     * @param records the EIM record sets describing the collection
     * and the items with which it is initially populated
     * @param ticketTypes a set of ticket types to create on the
     * collection, one per type
     * @returns the initial <code>SyncToken</code> for the collection
     * @throws IllegalArgumentException if the authenticated principal
     * is not a <code>User</code> but no parent uid was specified
     * @throws UidInUseException if the specified uid is already in
     * use by any item
     * @throws UnknownCollectionException if the collection specified
     * by the given parent uid is not found
     * @throws NotCollectionException if the item specified
     * by the given parent uid is not a collection
     * @throws ValidationException if the recordset contains invalid
     * data according to the records' schemas
     * @throws MorseCodeException if an unknown error occurs
     */
    public PubCollection publishCollection(String uid,
                                           String parentUid,
                                           PubRecords records,
                                           Set<TicketType> ticketTypes) {
        if (log.isDebugEnabled()) {
            if (parentUid != null)
                log.debug("publishing collection " + uid +
                          " with parent " + parentUid);
            else
                log.debug("publishing collection " + uid);
        }

        CollectionItem parent = null;
        if (parentUid == null) {
            User user = securityManager.getSecurityContext().getUser();
            if (user == null)
                throw new IllegalArgumentException("Parent uid must be provided if authentication principal is not a user");
            parent = contentService.getRootItem(user);
        }
        else {
            Item parentItem = contentService.findItemByUid(parentUid);
            if (! (parentItem instanceof CollectionItem))
                throw new NotCollectionException(parentItem);
            parent = (CollectionItem) parentItem;
        }
        
        // check for existing collection
        try {
            if(contentService.findItemByUid(uid)!=null)
                throw new CollectionExistsException(uid);
        } catch (CosmoSecurityException e) {
            // Security exception will be thrown for existing collections
            // that aren't owned by user
            throw new CollectionExistsException(uid);
        }

        CollectionItem collection = entityFactory.createCollection();
        User owner = computeItemOwner();
        collection.setUid(uid);
        collection.setOwner(owner);
        collection.setName(uid);
        if (records.getName() != null)
            collection.setDisplayName(records.getName());
        else
            collection.setDisplayName(uid);

        if(records.getHue()!=null)
            collection.setHue(records.getHue());
        
        // stamp it as a calendar
        CalendarCollectionStamp ccs = entityFactory.createCalendarCollectionStamp(collection);
        collection.addStamp(ccs);

        Set<Item> toUpdate = recordsToItems(records.getRecordSets(),
                                            collection);

        for (TicketType type : ticketTypes)
            collection.addTicket(entityFactory.createTicket(type));
        
        // throws UidinUseException
        try {
            collection = contentService.createCollection(parent, collection,
                    toUpdate);
        } catch (IcalUidInUseException e) {
            throw new UidConflictException(e);
        } catch (ModelValidationException e) {
            if(e.getOffendingObject() instanceof Item) {
                Item item = (Item) e.getOffendingObject();
                throw new ValidationException(item.getUid(), e.getMessage());
            } else {
                throw new ValidationException(null, e.getMessage());
            } 
        }
        
        return new PubCollection(collection);
    }

    /**
     * Retrieves the current state of every item contained within the
     * identified collection.
     *
     * @param uid the uid of the collection to subscribe to
     *
     * @returns a <code>SubRecords</code> describing the current
     * state of the collection
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public SubRecords subscribeToCollection(String uid) {
        if (log.isDebugEnabled())
            log.debug("subscribing to collection " + uid);

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(item);
        CollectionItem collection = (CollectionItem) item;

        SubRecords subRecords = new SubRecords(collection, getAllItems(collection));
        
        // Ensure collection has not been modified since reading the data
        Date lastModified = collection.getModifiedDate();
        collection = (CollectionItem) contentService.findItemByUid(uid);
        while(!collection.getModifiedDate().equals(lastModified)) {
            // If it has been modified, then re-read data, otherwise we
            // could return inconsistent data
            if (log.isDebugEnabled())
                log.debug("collection " + uid + " modified while subscribing, retrying");
            subRecords = new SubRecords(collection, getAllItems(collection));
            lastModified = collection.getModifiedDate();
            collection = (CollectionItem) contentService.findItemByUid(uid);
        }
        
        return subRecords;
    }

    /**
     * Retrieves the current state of each non-collection child item
     * from the identified collection that has changed since the time
     * that the given synchronization token was valid.
     *
     * @param uid the uid of the collection to subscribe to
     * @param token the sync token describing the last known state of
     * the collection
     *
     * @returns a <code>SubRecords</code> describing the current
     * state of the changed items
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public SubRecords synchronizeCollection(String uid,
                                            SyncToken token) {
        if (log.isDebugEnabled())
            log.debug("synchronizing collection " + uid + " with token " +
                      token.serialize());

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(item);
        CollectionItem collection = (CollectionItem) item;

        // If collection hasn't changed, don't bother querying for children
        // just return empty SubRecords
        if (token.isValid(collection))
            return new SubRecords(collection, new ArrayList<ContentItem>(0));
        
        SubRecords subRecords = new SubRecords(collection, getModifiedItems(token, collection),
                getRecentTombstones(token, collection), token);
       
        // ensure collection has not been modified since reading the data
        Date lastModified = collection.getModifiedDate();
        collection = (CollectionItem) contentService.findItemByUid(uid);
        while(!collection.getModifiedDate().equals(lastModified)) {
            // If it has been modified, then re-read data, otherwise we
            // could return inconsistent data
            if (log.isDebugEnabled())
                log.debug("collection " + uid + " modified while syncing, retrying");
            subRecords = new SubRecords(collection, getModifiedItems(token, collection),
                    getRecentTombstones(token, collection), token);
            lastModified = collection.getModifiedDate();
            collection = (CollectionItem) contentService.findItemByUid(uid);
        }
        
        return subRecords;
    }

    /**
     * Updates the items within the identified collection that
     * correspond to the provided <code>EimRecordSet</code>s. The
     * update is atomic; the entire update fails if any single item
     * cannot be successfully saved with its new state.
     *
     * The collection is locked at the beginning of the update. Any
     * other update that begins before this update has completed, and
     * the collection unlocked, will fail immediately with a
     * <code>CollectionLockedException</code>. Any subscribe or
     * synchronize operation that begins during this update will
     * return the state of the collection immediately prior to the
     * beginning of this update.
     *
     * @param uid the uid of the collection to subscribe to
     * @param token the sync token describing the last known state of
     * the collection
     * @param records the EIM records describing the collection and
     * the items with which it is updated
     * @returns a new <code>SyncToken</code> that invalidates any
     * previously issued
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws CollectionLockedException if the collection is
     * currently locked by another update
     * @throws StaleCollectionException if the collection has been
     * updated since the provided sync token was generated
     * @throws ValidationException if the recordset contains invalid
     * data according to the records' schemas
     * @throws MorseCodeException if an unknown error occurs
     */
    public PubCollection updateCollection(String uid,
                                          SyncToken token,
                                          PubRecords records) {
        if (log.isDebugEnabled()) {
            log.debug("updating collection " + uid);
        }

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(item);
        CollectionItem collection = (CollectionItem) item;

        if (! token.isValid(collection)) {
            if (log.isDebugEnabled())
                log.debug("collection state is changed");
            throw new StaleCollectionException(uid);
        }

        if (records.getName() != null)
            collection.setDisplayName(records.getName());
        
        if(records.getHue()!=null)
            collection.setHue(records.getHue());

        Set<Item> toUpdate = recordsToItems(records.getRecordSets(),
                                            collection);
        
        try {
            // throws CollectionLockedException, and may throw
            // ConcurrencyFailureException
            collection = contentService.updateCollection(collection,
                    toUpdate);
            
        } catch (OptimisticLockingFailureException cfe) {
            // This means the data has been updated since the last sync token,
            // so a StaleCollectionException should be thrown
            throw new StaleCollectionException(uid);
        } catch (IcalUidInUseException e) {
            throw new UidConflictException(e);
        } catch (ModelValidationException e) {
            if(e.getOffendingObject() instanceof Item) {
                Item oi = (Item) e.getOffendingObject();
                throw new ValidationException(oi.getUid(), e.getMessage());
            } else {
                throw new ValidationException(null, e.getMessage());
            }
        }

        
        return new PubCollection(collection);
    }

    // our methods

    /** */
    public ContentService getContentService() {
        return contentService;
    }

    /** */
    public void setContentService(ContentService service) {
        contentService = service;
    }

    /** */
    public UserService getUserService() {
        return userService;
    }

    /** */
    public void setUserService(UserService service) {
        userService = service;
    }

    /** */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /** */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }
    
    

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    /** */
    public void init() {
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
        if (userService == null)
            throw new IllegalStateException("userService is required");
        if (securityManager == null)
            throw new IllegalStateException("securityManager is required");
        if (entityFactory == null)
            throw new IllegalStateException("entityFactory is required");
    }

    private User computeItemOwner() {
        User owner = securityManager.getSecurityContext().getUser();
        if (owner != null)
            return owner;
        Ticket ticket = securityManager.getSecurityContext().getTicket();
        if (ticket != null)
            return ticket.getOwner();
        throw new MorseCodeException("authenticated principal neither user nor ticket");
    }

    private Set<Item> recordsToItems(EimRecordSetIterator i,
                                     CollectionItem collection) {
       
        // All child item for collection (both current and new) indexed by Uid
        HashMap<String, Item> allChildrenByUid = new HashMap<String, Item>();
        LinkedHashSet<Item> children = new LinkedHashSet<Item>();
        
        // Index all existing children
        for(Item child: collection.getChildren())
            allChildrenByUid.put(child.getUid(), child);
        
        try {
            while (i.hasNext()) {
                EimRecordSet recordset = i.next();
                try {
                    Item item =
                        contentService.findItemByUid(recordset.getUuid());
                    if (item != null && ! (item instanceof ContentItem))
                        throw new ValidationException(recordset.getUuid(), "Child item " + recordset.getUuid() + " is not a content item");
                   
                    ContentItem child = (ContentItem) item;
                    
                    // Handle case where item is a NoteOccurence, in which case
                    // a new modification NoteItem needs to be created
                    if(child instanceof NoteOccurrence) {
                        if(recordset.isDeleted()==false)
                            child = createChildItem((NoteOccurrence) child, collection, recordset, allChildrenByUid);
                        else
                            child = null;
                    }
                    
                    // Handle case where recordset is to be deleted, but the
                    // target item doesn't exist.
                    if(child==null && recordset.isDeleted()==true)
                        throw new ValidationException(recordset.getUuid(),
                                "Tried to delete child item "
                                        + recordset.getUuid()
                                        + " , but it does not exist");
                   
                    // Handle case where item doesn't exist, so create a new one
                    if(child==null)
                        child = createChildItem(collection, recordset, allChildrenByUid);
                   
                    children.add(child);
                    
                    // apply recordset
                    new ItemTranslator(child).applyRecords(recordset);
                    
                } catch (EimValidationException e) {
                    throw new ValidationException(recordset.getUuid(), "could not apply EIM recordset " + recordset.getUuid() + " due to invalid data", e);
                }
            }
            
        } catch (EimException e) {
            throw new MorseCodeException("unknown EIM translation problem", e);
        } 

        return children;
    }

    // creates a new item and adds it as a child of the collection
    private ContentItem createChildItem(CollectionItem collection,
                                        EimRecordSet recordset,
                                        Map<String, Item> allChildrenByUid) {
        ContentItem child = createBaseChildItem(collection, recordset, allChildrenByUid);
        if(child.getUid().contains(ModificationUid.RECURRENCEID_DELIMITER))
            handleModificationItem((NoteItem) child, allChildrenByUid);
        return child;
    }
    
    //  creates a new item based off a NoteOccurrence
    private ContentItem createChildItem(NoteOccurrence occurrence, 
                                        CollectionItem collection,
                                        EimRecordSet recordset,
                                        Map<String, Item> allChildrenByUid) {
        NoteItem child = (NoteItem) createBaseChildItem(collection, recordset, allChildrenByUid);
        child.setModifies(occurrence.getMasterNote());
        return child;
    }
    
    private ContentItem createBaseChildItem(CollectionItem collection,
                                        EimRecordSet recordset,
                                        Map<String, Item> allChildrenByUid) {
        NoteItem child = entityFactory.createNote();

        child.setUid(recordset.getUuid());
        child.setIcalUid(child.getUid());
        child.setOwner(collection.getOwner());
        
        allChildrenByUid.put(child.getUid(), child);
        
        return child;
    }
    
    private boolean handleModificationItem(NoteItem noteMod,
            Map<String, Item> allChildrenByUid) {
        ModificationUid modUid = null;

        try {
            modUid = new ModificationUid(noteMod.getUid());
        } catch (ModelValidationException e) {
            throw new ValidationException(noteMod.getUid(), "invalid modification uid: "
                    + noteMod.getUid());
        }
        
        String parentUid = modUid.getParentUid();
        
        // Find parent note item by looking through collection's children.
        Item parentNote = allChildrenByUid.get(parentUid);
        if (parentNote!=null && parentNote instanceof NoteItem) {
            noteMod.setModifies((NoteItem) parentNote);
            return true;
        }
       
        // mods should not have icaluid as its inherited from the master
        noteMod.setIcalUid(null);
        
        log.debug("could not find parent item for " + noteMod.getUid());
        throw new ValidationException(noteMod.getUid(), "no parent found for " + noteMod.getUid());
    }
     
    private List<ContentItem> getAllItems(CollectionItem collection) {
        ArrayList<ContentItem> itemList = new ArrayList<ContentItem>();
        Set<ContentItem> allItems = contentService.loadChildren(collection,
                null);
        for (ContentItem item : allItems) {
            if (isShareableItem(item))
                itemList.add(item);
        }
        return itemList;
    }

    private List<ContentItem> getModifiedItems(SyncToken prevToken,
            CollectionItem collection) {
        ArrayList<ContentItem> itemList = new ArrayList<ContentItem>();
        Set<ContentItem> items = contentService.loadChildren(collection,
                new Date(prevToken.getTimestamp()));
        for (ContentItem item : items) {
            if (isShareableItem(item))
                itemList.add(item);
        }
        return itemList;
    }
    
    private List<ItemTombstone> getRecentTombstones(SyncToken prevToken,
            CollectionItem collection) {
        ArrayList<ItemTombstone> tombstones = new ArrayList<ItemTombstone>();
        if (prevToken.isValid(collection))
            return tombstones;

        for (Tombstone tombstone : collection.getTombstones()) {
            if (tombstone instanceof ItemTombstone)
                if (prevToken.isTombstoneRecent(tombstone))
                    tombstones.add((ItemTombstone) tombstone);
        }

        return tombstones;
    }
    
    private boolean isShareableItem(Item item) {
        // only share NoteItems until Chandler and Cosmo UI can cope
        // with non-Note items
        return item instanceof NoteItem;
    }
}
