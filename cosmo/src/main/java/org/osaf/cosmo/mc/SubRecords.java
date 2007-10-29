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
import java.util.List;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSetIterator;
import org.osaf.cosmo.eim.schema.ItemTranslationIterator;
import org.osaf.cosmo.eim.schema.TombstoneTranslationIterator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ItemTombstone;

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
    public SubRecords(CollectionItem collection, List<ContentItem> items) {
        this(collection, items, new ArrayList<ItemTombstone>(0), null);
    }

    /** */
    public SubRecords(CollectionItem collection, 
            List<ContentItem> items, 
            List<ItemTombstone> tombstones,
                      SyncToken prevToken) {
        this.collection = collection;
        this.prevToken = prevToken;
        this.token = SyncToken.generate(collection);
        this.itemIterator = createItemIterator(items);
        this.tombstoneIterator = createTombstoneIterator(tombstones);
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
    public Long getHue() {
        return collection.getHue();
    }

    /** */
    public boolean isDeleted() {
        return ! collection.getIsActive();
    }

    /** */
    protected ItemTranslationIterator createItemIterator(List<ContentItem> items) {
        if(prevToken!=null)
            return new ItemTranslationIterator(items, prevToken.getTimestamp());
        else
            return new ItemTranslationIterator(items);
            
    }

    /** */
    protected TombstoneTranslationIterator createTombstoneIterator(List<ItemTombstone> tombstones) {
        return new TombstoneTranslationIterator(tombstones);
    }
}
