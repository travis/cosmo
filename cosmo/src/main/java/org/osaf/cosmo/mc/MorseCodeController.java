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

import java.util.Set;

import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.TicketType;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Interface for controllers that implement the operations specified
 * by Morse Code.
 */
public interface MorseCodeController {

    /**
     * Returns information about every collection in the user's home
     * collection. Ignores subcollections.
     *
     * @param username the username of the user whose collections are
     * to be described
     * @param locator the service locator used to resolve collection URLs
     *
     * @throws UnknownUserException if the user is not found
     * @throws MorseCodeException if an unknown error occurs
     */
    public CollectionService discoverCollections(String username,
                                                 ServiceLocator locator);

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
    public void deleteCollection(String uid);

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
     * @param records the EIM records with which the published
     * collection is initially populated
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
                                           Set<TicketType> ticketTypes);
   
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
    public SubRecords subscribeToCollection(String uid);

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
                                            SyncToken token);

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
     * @param uid the uid of the collection to subscribe to
     * @param token the sync token describing the last known state of
     * the collection
     * @param records the EIM records with which the published
     * collection is updated
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
                                          PubRecords records);
}
