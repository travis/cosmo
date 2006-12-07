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
import org.osaf.cosmo.eim.schema.EimSchemaConstants;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EventTranslator;
import org.osaf.cosmo.eim.schema.ItemTranslator;
import org.osaf.cosmo.eim.schema.MessageTranslator;
import org.osaf.cosmo.eim.schema.NoteTranslator;
import org.osaf.cosmo.eim.schema.TaskTranslator;
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

        for (EimRecord record : recordset.getRecords()) {
            if (record.getNamespace().equals(NS_COLLECTION))
                new CollectionTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_ITEM))
                new ItemTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_NOTE))
                new NoteTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_EVENT))
                new EventTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_TASK))
                new TaskTranslator().applyRecord(record, item);
            else if (record.getNamespace().equals(NS_MESSAGE))
                new MessageTranslator().applyRecord(record, item);
//             else
//                 t = new UnknownTranslator();
        }
    }

    /**
     */
    public static List<EimRecordSet> createAggregateRecordSet(CollectionItem collection) {
        ArrayList<EimRecordSet> recordsets = new ArrayList<EimRecordSet>();

        recordsets.add(createRecordSet(collection));

        for (Item child : collection.getChildren()) {
            if (child instanceof CollectionItem)
                continue;
            recordsets.add(createRecordSet(child));
        }

        return recordsets;
    }

    /**
     */
    public static EimRecordSet createRecordSet(Item item) {
        EimRecordSet recordset = new EimRecordSet();
        fillInRecordSet(recordset, item);
        return recordset;
    }

    private static void fillInRecordSet(EimRecordSet recordset,
                                        Item item) {
        if (! recordset.getUuid().equals(item.getUid()))
            throw new IllegalArgumentException("recordset and item uuids do not match");

        if (! BooleanUtils.isTrue(item.getIsActive())) {
            recordset.setDeleted(true);
            return;
        }

        if (item instanceof CollectionItem)
            recordset.addRecord(new CollectionTranslator().createRecord(item));
        if (item instanceof ContentItem)
            recordset.addRecord(new ItemTranslator().createRecord(item));
        if (item instanceof NoteItem)
            recordset.addRecord(new NoteTranslator().createRecord(item));
        for (Stamp stamp : item.getStamps()) {
            if (stamp instanceof EventStamp)
                recordset.addRecord(new EventTranslator().createRecord(stamp));
            else if (stamp instanceof TaskStamp)
                recordset.addRecord(new TaskTranslator().createRecord(stamp));
            else if (stamp instanceof MessageStamp)
                recordset.addRecord(new MessageTranslator().createRecord(stamp));
        }

//         recordset.addRecord(new UnknownTranslator().createRecord(item));
    }
}
