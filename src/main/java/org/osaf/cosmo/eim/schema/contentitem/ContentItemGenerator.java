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
package org.osaf.cosmo.eim.schema.contentitem;

import java.util.List;
import java.util.ArrayList;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.TimeStampField;
import org.osaf.cosmo.eim.schema.BaseItemGenerator;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates EIM records from content items.
 *
 * @see ContentItem
 */
public class ContentItemGenerator extends BaseItemGenerator
    implements ContentItemConstants {
    private static final Log log =
        LogFactory.getLog(ContentItemGenerator.class);

    private ContentItem contentItem;

    /** */
    public ContentItemGenerator(ContentItem contentItem) {
        super(PREFIX_ITEM, NS_ITEM, contentItem);
        this.contentItem = contentItem;
    }

    /**
     * Copies contentItem properties and attributes into a contentItem
     * record.
     */
    public List<EimRecord> generateRecords() {
        EimRecord record = new EimRecord(getPrefix(), getNamespace());

        record.addKeyField(new TextField(FIELD_UUID, contentItem.getUid()));

        record.addField(new TextField(FIELD_TITLE,
                                      contentItem.getDisplayName()));
        record.addField(new TextField(FIELD_TRIAGE_STATUS,
                                      contentItem.getTriageStatus()));
        record.addField(new DecimalField(FIELD_TRIAGE_STATUS_CHANGED,
                                         contentItem.getTriageStatusUpdated(),
                                         DIGITS_TRIAGE_STATUS_CHANGED,
                                         DEC_TRIAGE_STATUS_CHANGED));
        record.addField(new TextField(FIELD_LAST_MODIFIED_BY,
                                      contentItem.getLastModifiedBy()));
        record.addField(new TimeStampField(FIELD_CREATED_ON,
                                           contentItem.getClientCreationDate()));

        record.addFields(generateUnknownFields());

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
