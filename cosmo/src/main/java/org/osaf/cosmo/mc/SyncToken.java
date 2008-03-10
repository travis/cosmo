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

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Tombstone;

/**
 * Bean class that represents a synchronization token.
 *
 * A sync token is valid for the aggregate state of a collection,
 * including the individual state of the collection state as well as
 * the state of each of the child items of the collection. If the
 * state of the collection or any of its child items changes, the sync
 * token for the previous state becomes invalid and should be replaced
 * by a new sync token.
 *
 * A sync token can be serialized into string form for transmission to
 * a Morse Code client and deserialized back into a token.
 */
public class SyncToken {
    private static final Log log = LogFactory.getLog(SyncToken.class);

    private long timestamp;
    private int hashcode;

    private SyncToken(long timestamp,
                      int hashcode) {
        this.timestamp = timestamp;
        this.hashcode = hashcode;
    }

    /**
     * Determines whether or not the sync token is valid for the
     * current state of the given collection (in other words, has the
     * collection's aggregate state changed since the sync token was
     * generated).
     *
     * @return true or false
     */
    public boolean isValid(CollectionItem collection) {
        int collectionHashcode = collection.generateHash();
        boolean isValid = hashcode == collectionHashcode;
//         if (log.isDebugEnabled())
//             log.debug("token valid for collection " + collection.getUid() +
//                       "? " + isValid);
        return isValid;
    }

    /**
     * Determines whether or not the given item's state has changed
     * since the millisecond when the sync token was generated.
     *
     * @return true or false
     */
    public boolean hasItemChanged(Item item) {
        long itemTimestamp = item.getModifiedDate().getTime();
        boolean hasChanged = itemTimestamp > timestamp;
//         if (log.isDebugEnabled())
//             log.debug("item " + item.getUid() + " changed since " +
//                       item.getModifiedDate() + "? " + hasChanged);
        return hasChanged;
    }

    /**
     * Determines whether or not the given tombstone represents an
     * item that was removed since the millisecond when the sync token
     * was generated.
     *
     * @return true or false
     */
    public boolean isTombstoneRecent(Tombstone tombstone) {
        long tombstoneTimestamp = tombstone.getTimestamp().getTime();
        boolean hasChanged = tombstoneTimestamp > timestamp;
//         if (log.isDebugEnabled())
//             log.debug("tombstone " + tombstone.getItemUid() +
//                       " removed since " + tombstone.getTimestamp() + "? " +
//                       hasChanged);
        return hasChanged;
    }

    /**
     * Converts the token into a serialized token string.
     *
     * @return serialized <code>String</code>
     */
    public String serialize() {
        return timestamp + "-" + hashcode;
    }

    /**
     * Converts a serialized token string into a sync token.
     *
     * @return the deserialized <code>SyncToken</code>
     * @throws SyncTokenException if the given string cannot be
     * deserialized
     */
    public static SyncToken deserialize(String str) {
        String[] chunks = str.split("-", 2);
        if (chunks.length != 2 ||
            StringUtils.isBlank(chunks[0]) ||
            StringUtils.isBlank(chunks[1]))
            throw new SyncTokenException(str);

        long timestamp = -1;
        try {
            timestamp = Long.parseLong(chunks[0]);
        } catch (NumberFormatException e) {
            throw new SyncTokenException(str);
        }

        int hashcode = -1;
        try {
            hashcode = Integer.parseInt(chunks[1]);
        } catch (NumberFormatException e) {
            throw new SyncTokenException(str);
        }

        return new SyncToken(timestamp, hashcode);
    }

    /** */
    public int getHash() {
        return hashcode;
    }

    /** */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Computes a new sync token based on the current state of the
     * given collection.
     *
     * @return a new <code>SyncToken</code>
     */
    public static SyncToken generate(CollectionItem collection) {
        return new SyncToken(collection.getModifiedDate().getTime(),
                             collection.generateHash());
    }
}
