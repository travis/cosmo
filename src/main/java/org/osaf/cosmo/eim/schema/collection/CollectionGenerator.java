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
package org.osaf.cosmo.eim.schema.collection;

import java.util.List;
import java.util.ArrayList;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseItemGenerator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates EIM records from collections.
 *
 * @see CollectionItem
 */
public class CollectionGenerator extends BaseItemGenerator {
    private static final Log log =
        LogFactory.getLog(CollectionGenerator.class);

    private CollectionItem collection;

    /** */
    public CollectionGenerator(CollectionItem collection) {
        super(PREFIX_COLLECTION, NS_COLLECTION, collection);
        this.collection = collection;
    }

    /**
     * Copies collection properties and attributes into a collection
     * record.
     */
    public List<EimRecord> generateRecords() {
        EimRecord record = new EimRecord(getPrefix(), getNamespace());
        record.addKeyField(new TextField(FIELD_UUID, collection.getUid()));
        record.addFields(generateUnknownFields());

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
