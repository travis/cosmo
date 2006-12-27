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
package org.osaf.cosmo.eim;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;

import org.osaf.cosmo.eim.schema.CollectionTranslator;
import org.osaf.cosmo.eim.schema.ContentItemTranslator;
import org.osaf.cosmo.eim.schema.EimSchemaConstants;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EventTranslator;
//import org.osaf.cosmo.eim.schema.ICalExtensionTranslator;
import org.osaf.cosmo.eim.schema.MessageTranslator;
import org.osaf.cosmo.eim.schema.NoteTranslator;
import org.osaf.cosmo.eim.schema.TaskTranslator;
import org.osaf.cosmo.eim.schema.UnknownAttributeTranslator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TaskStamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the translation of EIM records to/from Cosmo model
 * objects.
 */
public class EimTranslator implements EimSchemaConstants {
    private static final Log log = LogFactory.getLog(EimTranslator.class);

    /**
     * @throws EimValidationException if a record contains invalid data
     * @throws EimSchemaException
     */
    public static void applyRecordSet(EimRecordSet recordset,
                                      Item item)
        throws EimSchemaException {
        if (! recordset.getUuid().equals(item.getUid()))
            throw new IllegalArgumentException("recordset and item uuids do not match");

        if (recordset.isDeleted()) {
            item.setIsActive(Boolean.FALSE);
            return;
        }

        //        ArrayList<EimRecord> icalRecords = new ArrayList<EimRecord>();

        for (EimRecord record : recordset.getRecords()) {
            if (record.getNamespace().equals(NS_COLLECTION))
                new CollectionTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_ITEM))
                new ContentItemTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_NOTE))
                new NoteTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_EVENT))
                new EventTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_TASK))
                new TaskTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_MESSAGE))
                new MessageTranslator().applyRecord(record, item);
//             else if (record.getNamespace().equals(NS_ICALEXT))
//                 icalRecords.add(record);
            else
                new UnknownAttributeTranslator().applyRecord(record, item);
        }

        // ical records are special. the entire set of ical records
        // represents all of the icalendar properties defined on the
        // item that aren't otherwise covered by one of the
        // stamps. the translator handles them all as a single batch.
        // new ICalExtensionTranslator().applyRecords(icalRecords, item);
    }

    /**
     */
    public static EimRecordSet toRecordSet(Item item) {
        EimRecordSet recordset = new EimRecordSet();
        recordset.setUuid(item.getUid());

        if (! BooleanUtils.isTrue(item.getIsActive())) {
            recordset.setDeleted(true);
            return recordset;
        }

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        if (item instanceof CollectionItem)
            recordset.addRecords(new CollectionTranslator().toRecords(item));
        if (item instanceof ContentItem)
            recordset.addRecords(new ContentItemTranslator().toRecords(item));
        if (item instanceof NoteItem)
            recordset.addRecords(new NoteTranslator().toRecords(item));
        for (Stamp stamp : item.getStamps()) {
            if (stamp instanceof EventStamp) {
                recordset.addRecords(new EventTranslator().toRecords(stamp));
 //                recordset.addRecords(new ICalExtensionTranslator().toRecords(stamp));
            } else if (stamp instanceof TaskStamp)
                recordset.addRecords(new TaskTranslator().toRecords(stamp));
            else if (stamp instanceof MessageStamp)
                recordset.addRecords(new MessageTranslator().toRecords(stamp));
        }

        recordset.addRecords(new UnknownAttributeTranslator().toRecords(item));

        return recordset;
    }
}
