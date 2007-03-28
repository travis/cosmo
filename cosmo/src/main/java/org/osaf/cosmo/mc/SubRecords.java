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
import java.util.Iterator;

import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.EimRecordSetIterator;
import org.osaf.cosmo.eim.schema.ItemTranslationIterator;
import org.osaf.cosmo.eim.schema.TombstoneTranslationIterator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Tombstone;

/**
 * Bean class that aggregates all of the EIM records for a subscribe
 * response and provides the corresponding synchronization token.
 *
 * @see EimRecord
 * @see SyncToken
 */
public class SubRecords {

    private SyncToken token;
    private ItemTranslationIterator itemIterator;
    private TombstoneTranslationIterator tombstoneIterator;
    private CollectionItem collection;
    private SyncToken prevToken;

    /** */
    public SubRecords(CollectionItem collection) {
        this(collection, null);
    }

    /** */
    public SubRecords(CollectionItem collection,
                      SyncToken prevToken) {
        this.collection = collection;
        this.prevToken = prevToken;
        this.token = SyncToken.generate(collection);
        this.itemIterator = createItemIterator();
        this.tombstoneIterator = createTombstoneIterator();
    }

    /** */
    public EimRecordSetIterator getItemRecordSets() {
        return itemIterator;
    }

    /** */
    public EimRecordSetIterator getTombstoneRecordSets() {
        return tombstoneIterator;
    }

    /** */
    public SyncToken getToken() {
        return token;
    }

    /** */
    public String getUid() {
        return collection.getUid();
    }

    /** */
    public String getName() {
        return collection.getDisplayName();
    }

    /** */
    public boolean isDeleted() {
        return ! collection.getIsActive();
    }

    /** */
    protected ItemTranslationIterator createItemIterator() {
        ArrayList<ContentItem> items = new ArrayList<ContentItem>();

        if (prevToken != null) {
            addModifiedContentItems(items);
            return new ItemTranslationIterator(items,
                                               prevToken.getTimestamp());
        }

        addAllContentItems(items);
        return new ItemTranslationIterator(items);
    }

    /** */
    protected TombstoneTranslationIterator createTombstoneIterator() {
        ArrayList<ItemTombstone> tombstones = new ArrayList<ItemTombstone>();

        if (prevToken != null)
            addRecentTombstones(tombstones);
        else
            addAllTombstones(tombstones);

        return new TombstoneTranslationIterator(tombstones);
    }

    private void addModifiedContentItems(ArrayList<ContentItem> items) {
        if (prevToken.isValid(collection))
            return;

        for (Item child : collection.getChildren()) {
            if (! isShareableItem(child))
                continue;
            if (prevToken.hasItemChanged(child))
                items.add((ContentItem)child);
        }
    }

    private void addAllContentItems(ArrayList<ContentItem> items) {
        for (Item child : collection.getChildren()) {
            if (! isShareableItem(child))
                continue;
            items.add((ContentItem)child);
        }
    }

    private void addRecentTombstones(ArrayList<ItemTombstone> tombstones) {
        if (prevToken.isValid(collection))
            return;

        for (Tombstone tombstone : collection.getTombstones()) {
            if(tombstone instanceof ItemTombstone)
                if (prevToken.isTombstoneRecent(tombstone))
                    tombstones.add((ItemTombstone) tombstone);
        }
    }

    private void addAllTombstones(ArrayList<ItemTombstone> tombstones) {
        for (Tombstone tombstone : collection.getTombstones())
            if(tombstone instanceof ItemTombstone)
                tombstones.add((ItemTombstone) tombstone);
    }

    private boolean isShareableItem(Item item) {
        // only share NoteItems until Chandler and Cosmo UI can cope
        // with non-Note items
        return item instanceof NoteItem;
    }
}
