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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimTranslator;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
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
    private CosmoSecurityManager securityManager;

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
     * @param uid the uid of the collection to publish
     * @param parentUid the (optional) uid of the collection to set as
     * the parent for the published collection
     * @param records the EIM record sets describing the collection
     * and the items with which it is initially populated
     *
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
    public SyncToken publishCollection(String uid,
                                       String parentUid,
                                       Iterator<EimRecordSet> recordsets) {
        if (log.isDebugEnabled()) {
            if (parentUid != null)
                log.debug("publishing collection " + uid + " with parent " + parentUid);
            else
                log.debug("publishing collection " + uid);
        }

        CollectionItem parent = null;
        if (parentUid == null) {
            User user = securityManager.getSecurityContext().getUser();
            if (user == null)
                throw new IllegalArgumentException("Parent uid must be provided if authentication principal is not a user");
            parent = contentService.getRootItem(user);
            log.debug("parent is root item for " + user.getUsername());
        }
        else {
            Item parentItem = contentService.findItemByUid(parentUid);
            if (! (parentItem instanceof CollectionItem))
                throw new NotCollectionException("Parent item not a collection");
            parent = (CollectionItem) parentItem;
            log.debug("parent is collection " + parent.getName());
        }

        CollectionItem collection = new CollectionItem();
        User owner = computeItemOwner();
        collection.setUid(uid);
        collection.setOwner(owner);

        HashSet<Item> children = new HashSet<Item>();
        EimTranslator translator = null;
        while (recordsets.hasNext()) {
            EimRecordSet recordset = recordsets.next();
            try {
                if (recordset.getUuid().equals(collection.getUid())) {
                    // XXX: set collection name from recordset
                    translator = new EimTranslator(collection);
                    translator.applyRecords(recordset);
                } else {
                    Item child = createChildItem(collection, recordset);
                    translator = new EimTranslator(child);
                    translator.applyRecords(recordset);
                }
            } catch (EimValidationException e) {
                throw new ValidationException("could not apply EIM recordset " + recordset.getUuid() + " due to invalid data", e);
            } catch (EimSchemaException e) {
                throw new MorseCodeException("unknown EIM schema problem", e);
            }
        }

        // throws UidinUseException
        collection =
            contentService.createCollection(parent, collection, children);

        return SyncToken.generate(collection);
    }
   
    /**
     * Retrieves the current state of every item contained within the
     * identified collection.
     *
     * @param uid the uid of the collection to subscribe to
     *
     * @returns a <code>SyncRecords</code> describing the current
     * state of the collection
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public SyncRecords subscribeToCollection(String uid) {
        if (log.isDebugEnabled())
            log.debug("subscribing to collection " + uid);

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(uid);
        CollectionItem collection = (CollectionItem) item;

        Iterator<EimRecordSet> recordsets =
            new SubscriptionIterator(collection);
        SyncToken token = SyncToken.generate(collection);

        return new SyncRecords(recordsets, token);
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
     * @returns a <code>SyncRecords</code> describing the current
     * state of the changed items
     * @throws UnknownCollectionException if the specified collection
     * is not found
     * @throws NotCollectionException if the specified item is not a
     * collection
     * @throws MorseCodeException if an unknown error occurs
     */
    public SyncRecords synchronizeCollection(String uid,
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

        Iterator<EimRecordSet> recordsets =
            new SynchronizationIterator(collection, token);

        token = SyncToken.generate(collection);

        return new SyncRecords(recordsets, token);
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
     *
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
    public SyncToken updateCollection(String uid,
                                      SyncToken token,
                                      Iterator<EimRecordSet> recordsets) {
        if (log.isDebugEnabled()) {
            log.debug("updating collection " + uid);
        }

        Item item = contentService.findItemByUid(uid);
        if (item == null)
            throw new UnknownCollectionException(uid);
        if (! (item instanceof CollectionItem))
            throw new NotCollectionException(uid);
        CollectionItem collection = (CollectionItem) item;

        if (! token.isValid(collection)) {
            if (log.isDebugEnabled())
                log.debug("collection state is changed");
            throw new StaleCollectionException(uid);
        }

        HashSet<Item> children = new HashSet<Item>();
        EimTranslator translator = null;
        while (recordsets.hasNext()) {
            EimRecordSet recordset = recordsets.next();
            try {
                if (recordset.getUuid().equals(collection.getUid())) {
                    // XXX: set collection name from recordset
                    translator = new EimTranslator(collection);
                    translator.applyRecords(recordset);
                } else {
                    Item child = collection.getChild(recordset.getUuid());
                    if (child == null)
                        child = createChildItem(collection, recordset);
                    translator = new EimTranslator(child);
                    translator.applyRecords(recordset);
                }
            } catch (EimValidationException e) {
                throw new ValidationException("could not apply EIM recordset " + recordset.getUuid() + " due to invalid data", e);
            } catch (EimSchemaException e) {
                throw new MorseCodeException("unknown EIM schema problem", e);
            }
        }

        // throws CollectionLockedException
        collection = contentService.updateCollection(collection, children);

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
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /** */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /** */
    public void init() {
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
        if (securityManager == null)
            throw new IllegalStateException("securityManager is required");
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


    // creates a new item and adds it as a child of the collection
    private ContentItem createChildItem(CollectionItem collection,
                                        EimRecordSet recordset) {
        NoteItem child = new NoteItem();
        child.setName(recordset.getUuid());
        child.setUid(recordset.getUuid());
        child.setOwner(collection.getOwner());
        collection.getChildren().add(child);
        return child;
    }

    private class SubscriptionIterator implements Iterator {
        private Iterator<Item> items;

        public SubscriptionIterator(CollectionItem collection) {
            ArrayList<Item> tmp = new ArrayList<Item>();

            tmp.add(collection);

            for (Item child : collection.getChildren()) {
                if (child instanceof CollectionItem)
                    continue;
                tmp.add(child);
            }

            items = tmp.iterator();
        }

        public boolean hasNext() {
            return items.hasNext();
        }

        public EimRecordSet next() {
            return new EimTranslator(items.next()).generateRecords();
        }

        public void remove() {
            throw new UnsupportedOperationException("remove method not supported");
        }
    }

    private class SynchronizationIterator implements Iterator {
        private Iterator<Item> items;

        public SynchronizationIterator(CollectionItem collection,
                                       SyncToken token) {
            ArrayList<Item> tmp = new ArrayList<Item>();

            if (! token.isValid(collection)) {
                if (token.hasItemChanged(collection))
                    tmp.add(collection);

                for (Item child : collection.getChildren()) {
                    if (child instanceof CollectionItem)
                        continue;
                    if (token.hasItemChanged(child))
                        tmp.add(child);
                }
            }

            items = tmp.iterator();
        }

        public boolean hasNext() {
            return items.hasNext();
        }

        public EimRecordSet next() {
            return new EimTranslator(items.next()).generateRecords();
        }

        public void remove() {
            throw new UnsupportedOperationException("remove method not supported");
        }
    }
}
