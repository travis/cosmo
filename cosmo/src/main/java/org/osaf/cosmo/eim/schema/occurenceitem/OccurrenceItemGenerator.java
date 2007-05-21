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
package org.osaf.cosmo.eim.schema.occurenceitem;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseItemGenerator;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteOccurrence;

/**
 * Generates EIM records from content items.
 *
 * @see ContentItem
 */
public class OccurrenceItemGenerator extends BaseItemGenerator {
    private static final Log log =
        LogFactory.getLog(OccurrenceItemGenerator.class);

    /** */
    public OccurrenceItemGenerator(Item item) {
        super(PREFIX_OCCURRENCE_ITEM, NS_OCCURRENCE_ITEM, item);
        if (! (item instanceof ContentItem))
            throw new IllegalArgumentException("item " + item.getUid() + " not a content item");
    }

    /**
     * Copies contentItem properties and attributes into an occurrence item
     * record.
     */
    public List<EimRecord> generateRecords() {
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        ContentItem contentItem = (ContentItem) getItem();
        
        if(!(contentItem instanceof NoteOccurrence))
            return records;

        EimRecord record = new EimRecord(getPrefix(), getNamespace());
        record.addKeyField(new TextField(FIELD_UUID, contentItem.getUid()));
        records.add(record);

        return records;
    }
}
