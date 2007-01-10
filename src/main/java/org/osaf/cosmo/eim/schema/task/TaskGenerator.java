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
package org.osaf.cosmo.eim.schema.task;

import java.util.List;
import java.util.ArrayList;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TaskStamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates EIM records from task stamps.
 *
 * @see TaskStamp
 */
public class TaskGenerator extends BaseStampGenerator {
    private static final Log log =
        LogFactory.getLog(TaskGenerator.class);

    /** */
    public TaskGenerator(Item item) {
        super(PREFIX_TASK, NS_TASK, item);
        setStamp(TaskStamp.getStamp(item));
    }

    /**
     * Copies task properties and attributes into a task record.
     */
    public List<EimRecord> generateRecords() {
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        TaskStamp task = (TaskStamp) getStamp();
        if (task == null)
            return records;

        EimRecord record = new EimRecord(getPrefix(), getNamespace());

        record.addKeyField(new TextField(FIELD_UUID, task.getItem().getUid()));

        record.addFields(generateUnknownFields());

        records.add(record);

        return records;
    }
}
