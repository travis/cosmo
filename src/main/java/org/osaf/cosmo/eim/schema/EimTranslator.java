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

import org.apache.commons.lang.BooleanUtils;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.schema.collection.CollectionApplicator;
import org.osaf.cosmo.eim.schema.collection.CollectionGenerator;
import org.osaf.cosmo.eim.schema.contentitem.ContentItemApplicator;
import org.osaf.cosmo.eim.schema.contentitem.ContentItemGenerator;
import org.osaf.cosmo.eim.schema.note.NoteApplicator;
import org.osaf.cosmo.eim.schema.note.NoteGenerator;
import org.osaf.cosmo.eim.schema.event.EventApplicator;
import org.osaf.cosmo.eim.schema.event.EventGenerator;
import org.osaf.cosmo.eim.schema.task.TaskApplicator;
import org.osaf.cosmo.eim.schema.task.TaskGenerator;
import org.osaf.cosmo.eim.schema.message.MessageApplicator;
import org.osaf.cosmo.eim.schema.message.MessageGenerator;
import org.osaf.cosmo.eim.schema.unknown.UnknownApplicator;
import org.osaf.cosmo.eim.schema.unknown.UnknownGenerator;
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
 * Handles the translation of EIM recordsets to/from an
 * <code>Item</code>.
 *
 * @see EimRecordSet
 * @see Item
 */
public class EimTranslator implements EimSchemaConstants {
    private static final Log log = LogFactory.getLog(EimTranslator.class);

    private Item item;
    private CollectionApplicator collectionApplicator;
    private CollectionGenerator collectionGenerator;
    private ContentItemApplicator contentItemApplicator;
    private ContentItemGenerator contentItemGenerator;
    private NoteApplicator noteApplicator;
    private NoteGenerator noteGenerator;
    private EventApplicator eventApplicator;
    private EventGenerator eventGenerator;
    private TaskApplicator taskApplicator; 
    private TaskGenerator taskGenerator;
    private MessageApplicator messageApplicator;
    private MessageGenerator messageGenerator;
    private UnknownApplicator unknownApplicator;
    private UnknownGenerator unknownGenerator;

    /** */
    public EimTranslator(Item item) {
        this.item = item;

        if (item instanceof CollectionItem) {
            collectionApplicator =
                new CollectionApplicator((CollectionItem) item);
            collectionGenerator =
                new CollectionGenerator((CollectionItem) item);
        }
        else {
            contentItemApplicator =
                new ContentItemApplicator((ContentItem) item);
            contentItemGenerator =
                new ContentItemGenerator((ContentItem) item);

            if (item instanceof NoteItem) {
                noteApplicator = new NoteApplicator((NoteItem) item);
                noteGenerator =  new NoteGenerator((NoteItem) item);
            }

            for (Stamp stamp : item.getStamps()) {
                if (stamp instanceof EventStamp) {
                    eventApplicator = new EventApplicator((EventStamp) stamp);
                    eventGenerator = new EventGenerator((EventStamp) stamp);
                } else if (stamp instanceof TaskStamp) {
                    taskApplicator = new TaskApplicator((TaskStamp) stamp);
                    taskGenerator = new TaskGenerator((TaskStamp) stamp);
                } else if (stamp instanceof MessageStamp) {
                    messageApplicator =
                        new MessageApplicator((MessageStamp) stamp);
                    messageGenerator =
                        new MessageGenerator((MessageStamp) stamp);
                }
            }
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
        if (! recordset.getUuid().equals(item.getUid()))
            throw new IllegalArgumentException("recordset and item uuids do not match");

        if (recordset.isDeleted()) {
            item.setIsActive(Boolean.FALSE);
            return;
        }

        for (EimRecord record : recordset.getRecords())
            applyRecord(record);
    }

    /**
     * Generates a recordset from the item and its stamps.
     *
     * If the item is not active, simply marks the recordset deleted
     * and returns.
     */
    public EimRecordSet generateRecords() {
        EimRecordSet recordset = new EimRecordSet();
        recordset.setUuid(item.getUid());

        if (! BooleanUtils.isTrue(item.getIsActive())) {
            recordset.setDeleted(true);
            return recordset;
        }

        if (collectionGenerator != null) {
            recordset.addRecords(collectionGenerator.generateRecords());
        } else {
            recordset.addRecords(contentItemGenerator.generateRecords());

            if (noteGenerator != null)
                recordset.addRecords(noteGenerator.generateRecords());
            if (eventGenerator != null)
                recordset.addRecords(eventGenerator.generateRecords());
            if (taskGenerator != null)
                recordset.addRecords(taskGenerator.generateRecords());
            if (messageGenerator != null)
                recordset.addRecords(messageGenerator.generateRecords());
        }

        recordset.addRecords(unknownGenerator.generateRecords());

        return recordset;
    }

    /** */
    public Item getItem() {
        return item;
    }

    private void applyRecord(EimRecord record)
        throws EimSchemaException {
        if (record.getNamespace().equals(NS_COLLECTION)) {
            if (collectionApplicator == null)
                throw new EimSchemaException("collection record cannot be applied to non-collection item");
            collectionApplicator.applyRecord(record);
        } else if (record.getNamespace().equals(NS_ITEM)) {
            if (contentItemApplicator == null)
                throw new EimSchemaException("item record cannot be applied to non-content item");
            contentItemApplicator.applyRecord(record);
        } else if (record.getNamespace().equals(NS_NOTE)) {
            if (noteApplicator == null)
                throw new EimSchemaException("note record cannot be applied to non-note item");
            noteApplicator.applyRecord(record);
        } else if (record.getNamespace().equals(NS_EVENT)) {
            if (eventApplicator == null)
                throw new EimSchemaException("event record cannot be applied to non-event item");
            eventApplicator.applyRecord(record);
        } else if (record.getNamespace().equals(NS_TASK)) {
            if (taskApplicator == null)
                throw new EimSchemaException("task record cannot be applied to non-task item");
            taskApplicator.applyRecord(record);
        } else if (record.getNamespace().equals(NS_MESSAGE)) {
            if (messageApplicator == null)
                throw new EimSchemaException("message record cannot be applied to non-message item");
            messageApplicator.applyRecord(record);
        } else {
            unknownApplicator.applyRecord(record);
        }
    }
}
