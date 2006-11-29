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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteStamp;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TaskStamp;

/**
 * Factory class for EIM records.
 */
public final class EimRecordFactory {
    private static final Log log = LogFactory.getLog(EimRecordFactory.class);

    /**
     * Returns a list of EIM records describing the entire state of
     * the collection and each of its child items (ignoring
     * subcollections).
     *
     * @param collection the collection to convert to EIM
     */
    public static List<EimRecord> createRecords(CollectionItem collection) {
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        records.add(createRecord(collection));

        for (Item child : collection.getChildren()) {
            if (child instanceof CollectionItem)
                continue;
            records.addAll(createRecords((ContentItem) child));
        }

        return records;
    }
    /**
     * Returns a list of EIM records describing the entire state of
     * the item and its stamps.
     *
     * @param item the item to convert to EIM
     */
    public static List<EimRecord> createRecords(ContentItem item) {
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        records.add(createRecord(item));

        for (Stamp stamp : item.getStamps())
            records.add(createRecord(stamp));

        return records;
    }

    /**
     * Returns an EIM record describing the entire state of the
     * collection (but not its child items).
     *
     * @param collection the collection to convert to EIM
     */
    public static EimRecord createRecord(CollectionItem collection) {
        return new CollectionRecord(collection);
    }

    /**
     * Returns an EIM record describing the entire state of the item
     * (but not its stamps).
     *
     * @param item the item to convert to EIM
     */
    public static EimRecord createRecord(ContentItem item) {
        return new ItemRecord(item);
    }

    /**
     * Returns an EIM record describing the entire state of the
     * stamp.
     *
     * @param stamp the stamp to convert to EIM
     * @throws IllegalArgumentException if the stamp does not map to a
     * known EIM record type
     */
    public static EimRecord createRecord(Stamp stamp) {
        if (stamp instanceof EventStamp)
            return new EventRecord((EventStamp) stamp);
        if (stamp instanceof NoteStamp)
            return new NoteRecord((NoteStamp) stamp);
        if (stamp instanceof TaskStamp)
            return new TaskRecord((TaskStamp) stamp);
        if (stamp instanceof MessageStamp)
            return new MailMessageRecord((MessageStamp) stamp);
        // XXX: "generic" EIM record type needed?
        throw new IllegalArgumentException("stamp type " + stamp.getClass() + " does not map to an EIM record type");
    }
}
