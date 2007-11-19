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
package org.osaf.cosmo.eim.schema;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.schema.contentitem.ContentItemApplicator;
import org.osaf.cosmo.eim.schema.contentitem.ContentItemGenerator;
import org.osaf.cosmo.eim.schema.modifiedby.ModifiedByApplicator;
import org.osaf.cosmo.eim.schema.modifiedby.ModifiedByGenerator;
import org.osaf.cosmo.eim.schema.event.EventApplicator;
import org.osaf.cosmo.eim.schema.event.EventGenerator;
import org.osaf.cosmo.eim.schema.event.alarm.DisplayAlarmApplicator;
import org.osaf.cosmo.eim.schema.event.alarm.DisplayAlarmGenerator;
import org.osaf.cosmo.eim.schema.message.MessageApplicator;
import org.osaf.cosmo.eim.schema.message.MessageGenerator;
import org.osaf.cosmo.eim.schema.note.NoteApplicator;
import org.osaf.cosmo.eim.schema.note.NoteGenerator;
import org.osaf.cosmo.eim.schema.occurenceitem.OccurrenceItemGenerator;
import org.osaf.cosmo.eim.schema.task.TaskApplicator;
import org.osaf.cosmo.eim.schema.task.TaskGenerator;
import org.osaf.cosmo.eim.schema.unknown.UnknownApplicator;
import org.osaf.cosmo.eim.schema.unknown.UnknownGenerator;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;

/**
 * Handles the translation of EIM recordsets to/from an
 * <code>Item</code>.
 *
 * @see EimRecordSet
 * @see Item
 */
public class ItemTranslator implements EimSchemaConstants {
    private static final Log log = LogFactory.getLog(ItemTranslator.class);

    private Item item;
    private ContentItemApplicator contentItemApplicator;
    private ContentItemGenerator contentItemGenerator;
    private ModifiedByApplicator modifiedByApplicator;
    private ModifiedByGenerator modifiedByGenerator;
    private NoteApplicator noteApplicator;
    private NoteGenerator noteGenerator;
    private EventApplicator eventApplicator;
    private EventGenerator eventGenerator;
    private DisplayAlarmGenerator alarmGenerator;
    private DisplayAlarmApplicator alarmApplicator;
    private TaskApplicator taskApplicator; 
    private TaskGenerator taskGenerator;
    private MessageApplicator messageApplicator;
    private MessageGenerator messageGenerator;
    private OccurrenceItemGenerator occurrenceGenerator;
    private UnknownApplicator unknownApplicator;
    private UnknownGenerator unknownGenerator;

    /** */
    public ItemTranslator(Item item) {
        this.item = item;

        // If item is an occurrence, then we only need an 
        // occurrence generator
        if(item instanceof NoteOccurrence) {
            occurrenceGenerator = new OccurrenceItemGenerator(item);
            return;
        }
        
        contentItemApplicator = new ContentItemApplicator(item);
        contentItemGenerator = new ContentItemGenerator(item);

        modifiedByApplicator = new ModifiedByApplicator(item);
        modifiedByGenerator = new ModifiedByGenerator(item);

        if (item instanceof NoteItem) {
            noteApplicator = new NoteApplicator(item);
            noteGenerator = new NoteGenerator(item);

            eventApplicator = new EventApplicator(item);
            eventGenerator = new EventGenerator(item);
            
            alarmApplicator = new DisplayAlarmApplicator(item);
            alarmGenerator = new DisplayAlarmGenerator(item);
            
            taskApplicator = new TaskApplicator(item);
            taskGenerator = new TaskGenerator(item);

            messageApplicator = new MessageApplicator(item);
            messageGenerator = new MessageGenerator(item);
        }

        unknownApplicator = new UnknownApplicator(item);
        unknownGenerator = new UnknownGenerator(item);
    }

    /**
     * Copies the record data into the appropriate item and stamp
     * properties and attributes.
     *
     * If the recordset is marked deleted, simply deactivates the item
     * and returns.
     *
     * @throws EimValidationException if a record contains invalid data
     * @throws EimSchemaException
     */
    public void applyRecords(EimRecordSet recordset)
        throws EimSchemaException {
        if (item.getUid() == null)
            item.setUid(recordset.getUuid());

        if (recordset.isDeleted()) {
            item.setIsActive(Boolean.FALSE);
            return;
        }

        for (EimRecord record : recordset.getRecords()) {
            if (record.getNamespace().equals(NS_ITEM)) {
                contentItemApplicator.applyRecord(record);
                continue;
            }
            else if (record.getNamespace().equals(NS_MODIFIEDBY)) {
                modifiedByApplicator.applyRecord(record);
                continue;
            } 
            
            if (item instanceof NoteItem) {
                if (record.getNamespace().equals(NS_NOTE)) {
                    noteApplicator.applyRecord(record);
                    continue;
                } else if (record.getNamespace().equals(NS_EVENT)) {
                    eventApplicator.applyRecord(record);
                    continue;
                } else if (record.getNamespace().equals(NS_DISPLAY_ALARM)) {
                    alarmApplicator.applyRecord(record);
                    continue;
                }
                else if (record.getNamespace().equals(NS_TASK)) {
                    taskApplicator.applyRecord(record);
                    continue;
                } else if (record.getNamespace().equals(NS_MESSAGE)) {
                    messageApplicator.applyRecord(record);
                    continue;
                }
            }

            unknownApplicator.applyRecord(record);

            if (item instanceof ContentItem) {
                ContentItem ci = (ContentItem) item;
                Date now = Calendar.getInstance().getTime();
                // if the item has no client creation date or client
                // modified date, set it to the current time
                if (ci.getClientCreationDate() == null)
                    ci.setClientCreationDate(now);
                if (ci.getClientModifiedDate() == null)
                    ci.setClientModifiedDate(now);
            }
        }
    }

    /**
     * Generates a recordset from the item and all of its stamps.
     *
     * If the item is not active, simply marks the recordset deleted
     * and returns.
     */
    public EimRecordSet generateRecords() {
        return generateRecords(-1);
    }

    /**
     * Generates a recordset from the item and its stamps, including
     * records for only those stamps that have been modified since the
     * given timestamp.
     *
     * If the item is not active, simply marks the recordset deleted
     * and returns.
     */
    public EimRecordSet generateRecords(long timestamp) {
        EimRecordSet recordset = new EimRecordSet();
        recordset.setUuid(item.getUid());

        // If item is an occurrence, then we only need an 
        // occurrence generator
        if(item instanceof NoteOccurrence) {
            recordset.addRecords(occurrenceGenerator.generateRecords());
            return recordset;
        }
        
        if (! BooleanUtils.isTrue(item.getIsActive())) {
            recordset.setDeleted(true);
            return recordset;
        }

        recordset.addRecords(contentItemGenerator.generateRecords());
        recordset.addRecords(modifiedByGenerator.generateRecords());

        if (item instanceof NoteItem) {
            recordset.addRecords(noteGenerator.generateRecords());
            recordset.addRecords(eventGenerator.generateRecords(timestamp));
            recordset.addRecords(alarmGenerator.generateRecords(timestamp));
            recordset.addRecords(taskGenerator.generateRecords(timestamp));
            recordset.addRecords(messageGenerator.generateRecords(timestamp));
        }

        recordset.addRecords(unknownGenerator.generateRecords());

        return recordset;
    }

    /** */
    public Item getItem() {
        return item;
    }
}
