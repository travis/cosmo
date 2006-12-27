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
package org.osaf.cosmo.eim.schema;

import java.util.List;
import java.util.ArrayList;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Translates collection records to <code>CollectionItem</code>s.
 * <p>
 * Implements the following schema:
 * <p>
 * TBD
 */
public class CollectionTranslator extends BaseItemTranslator {
    private static final Log log =
        LogFactory.getLog(CollectionTranslator.class);

    /** */
    public CollectionTranslator() {
        super(PREFIX_COLLECTION, NS_COLLECTION);
    }

    /**
     * Deactivates the collection.
     */
    protected void applyDeletion(EimRecord record,
                                 Item item)
        throws EimSchemaException {
        if (! (item instanceof CollectionItem))
            throw new IllegalArgumentException("Item is not a collection");
        CollectionItem c = (CollectionItem) item;

        c.setIsActive(false);
    }

    /**
     * Copies the data from the given record field into the collection.
     *
     * @throws IllegalArgumentException if the item is not a collection
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the collection 
     */
    protected void applyField(EimRecordField field,
                              Item item)
        throws EimSchemaException {
        if (! (item instanceof CollectionItem))
            throw new IllegalArgumentException("Item is not a collection");
        CollectionItem c = (CollectionItem) item;

        applyUnknownField(field, item);
    }

    /**
     * Copies collection properties into a collection record.
     *
     * @throws IllegalArgumentException if the item is not a content
     * item
     */
    public List<EimRecord> toRecords(Item item) {
        if (! (item instanceof CollectionItem))
            throw new IllegalArgumentException("Item is not a collection");
        CollectionItem ci = (CollectionItem) item;

        EimRecord record = createRecord(item);

        record.addKeyField(new TextField(FIELD_UUID, item.getUid()));

        addUnknownFields(record, item);

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
