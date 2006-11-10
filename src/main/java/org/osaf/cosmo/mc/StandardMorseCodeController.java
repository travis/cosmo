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
package org.osaf.cosmo.mc;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.service.ContentService;

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

    /**
     * Causes the identified collection and all contained items to be
     * immediately removed from storage.
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
            throw new NotCollectionException(uid);

        contentService.removeCollection((CollectionItem)item);
    }

    /**
     * Creates a collection identified by the given uid and populates
     * the collection with items with the provided states. The publish
     * is atomic; the entire publish fails if the collection or any
     * contained item cannot be created.
     *
     * If a parent uid is provided, the associated collection becomes
     * the parent of the new collection.
     *
     * @returns the initial <code>SyncToken</code> for the collection
     * @throws UidInUseException if the specified uid is already in
     * use by any item
     * @throws UnknownCollectionException if the collection specified
     * by the given parent uid is not found
     * @throws NotCollectionException if the item specified
     * by the given parent uid is not a collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public SyncToken publishCollection(String uid,
                                       String parentUid,
                                       Set<ItemState> itemStates) {
        if (log.isDebugEnabled()) {
            if (parentUid != null)
                log.debug("publishing collection " + uid + " with parent " + uid);
            else
                log.debug("publishing collection " + uid);
        }

        // XXX: home collection
        if (parentUid == null)
            throw new MorseCodeException("publishing to home collection not yet supported");

        Item parentItem = contentService.findItemByUid(parentUid);
        if (! (parentItem instanceof CollectionItem))
            throw new NotCollectionException("Parent item not a collection");
        CollectionItem parent = (CollectionItem) parentItem;

        CollectionItem collection = new CollectionItem();
        collection.setUid(uid);

        HashSet<Item> children = new HashSet<Item>();
        for (ItemState state : itemStates) {
            // XXX: fill in collection data from item states
            // XXX: create and fill in items from item states
        }

        // XXX:
//         collection =
//             contentService.createCollection(parent, collection, children);

        return SyncToken.generate(collection);
    }
   
    /**
     * Retrieves the current state of every item contained within the
     * identified collection.
     *
     * @returns a <code>ItemStateSet</code> describing the current
     * state of the collection
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public ItemStateSet subscribeToCollection(String uid) {
        if (log.isDebugEnabled())
            log.debug("subscribing to collection " + uid);

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(uid);
        CollectionItem collection = (CollectionItem) item;

        HashSet<ItemState> itemStates = new HashSet<ItemState>();

        itemStates.add(new ItemState(collection));

        for (Item child : collection.getChildren())
            itemStates.add(new ItemState(child));

        SyncToken token = SyncToken.generate(collection);

        return new ItemStateSet(itemStates, token);
    }

    /**
     * Retrieves the current state of each non-collection child item
     * from the identified collection that has changed since the time
     * that the given synchronization token was valid.
     *
     * @returns a <code>ItemStateSet</code> describing the current
     * state of the changed items
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public ItemStateSet synchronizeCollection(String uid,
                                              SyncToken token) {
        if (log.isDebugEnabled())
            log.debug("synchronizing collection " + uid + " with token " +
                      token.serialize());

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(uid);
        CollectionItem collection = (CollectionItem) item;

        HashSet<ItemState> itemStates = new HashSet<ItemState>();

        if (token.isValid(collection)) {
            if (log.isDebugEnabled())
                log.debug("collection state is unchanged");
            return new ItemStateSet(itemStates, token);
        }

        if (token.hasItemChanged(collection))
            itemStates.add(new ItemState(collection));

        for (Item child : collection.getChildren()) {
            // ignore subcollections
            if (child instanceof CollectionItem)
                continue;
            if (token.hasItemChanged(child))
                itemStates.add(new ItemState(collection));
        }

        token = SyncToken.generate(collection);

        return new ItemStateSet(itemStates, token);
    }

    /**
     * Updates the items within the identified collection that
     * correspond to the provided <code>ItemState</code>s. The update
     * is atomic; the entire update fails if any single item cannot be
     * successfully saved with its new state.
     *
     * The collection is locked at the beginning of the update. Any
     * other update that begins before this update has completed, and
     * the collection unlocked, will fail with a
     * <code>CollectionLockedException</code>. Any subscribe or
     * synchronize operation that begins during this update will
     * return the state of the collection immediately prior to the
     * beginning of this update.
     *
     * @returns a new <code>SyncToken</code> that invalidates any
     * previously issued
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws CollectionLockedException if the collection is
     * currently locked by another update
     * @throws MorseCodeException if an unknown error occurs
     */
    public SyncToken updateCollection(String uid,
                                      Set<ItemState> itemStates) {
        if (log.isDebugEnabled()) {
            log.debug("updating collection " + uid);
        }

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(uid);
        CollectionItem collection = (CollectionItem) item;

        HashSet<Item> children = new HashSet<Item>();
        for (ItemState state : itemStates) {
            // XXX: fill in collection data from item states
            // XXX: create and fill in items from item states
        }

        // throws CollectionLockedException
        // XXX:
//         collection =
//             contentService.exclusivelyUpdateCollection(collection, children);

        return SyncToken.generate(collection);
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
    public void init() {
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
    }
}
