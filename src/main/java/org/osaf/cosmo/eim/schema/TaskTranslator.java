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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TaskStamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Translates item records to <code>TaskStamp</code>s.
 * <p>
 * Implements the following schema:
 * <p>
 * TBD
 */
public class TaskTranslator extends BaseStampTranslator {
    private static final Log log = LogFactory.getLog(TaskTranslator.class);

    /** */
    public TaskTranslator() {
        super(PREFIX_TASK, NS_TASK);
    }

    /**
     * Removes the task stamp associated with the record.
     */
    protected void applyDeletion(Item item)
        throws EimSchemaException {
        TaskStamp stamp = TaskStamp.getStamp(item);
        if (stamp == null)
            throw new IllegalArgumentException("Item does not have an task stamp");

        item.removeStamp(stamp);
    }

    /**
     * Copies the data from the given record field into the task
     * stamp.
     *
     * @throws IllegalArgumentException if the item does not have an
     * task stamp
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected void applyField(EimRecordField field,
                              Item item)
        throws EimSchemaException {
        TaskStamp stamp = TaskStamp.getStamp(item);
        if (stamp == null)
            throw new IllegalArgumentException("Item does not have an task stamp");

        applyUnknownField(field, stamp.getItem());
    }

    /**
     * Copies message properties into a task record.
     *
     * @throws IllegalArgumentException if the stamp is not an task stamp
     */
    public List<EimRecord> toRecords(Stamp stamp) {
        if (! (stamp instanceof TaskStamp))
            throw new IllegalArgumentException("Stamp is not an task stamp");

        EimRecord record = createRecord(stamp);

        record.addKeyField(new TextField(FIELD_UUID,
                                         stamp.getItem().getUid()));

        addUnknownFields(record, stamp.getItem());

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
