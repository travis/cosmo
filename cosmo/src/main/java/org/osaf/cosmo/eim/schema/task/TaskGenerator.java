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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.TaskStamp;

/**
 * Generates EIM records from task stamps.
 *
 * @see TaskStamp
 */
public class TaskGenerator extends BaseStampGenerator {
    private static final Log log =
        LogFactory.getLog(TaskGenerator.class);

    private static final HashSet<String> STAMP_TYPES = new HashSet<String>(2);
    
    static {
        STAMP_TYPES.add("task");
    }
    
    /** */
    public TaskGenerator(Item item) {
        super(PREFIX_TASK, NS_TASK, item);
        setStamp(StampUtils.getTaskStamp(item));
    }

    @Override
    protected Set<String> getStampTypes() {
        return STAMP_TYPES;
    }

    /**
     * Adds a record for the task.
     */
    protected void addRecords(List<EimRecord> records) {
        TaskStamp stamp = (TaskStamp) getStamp();
        if (stamp == null)
            return;

        EimRecord record = new EimRecord(getPrefix(), getNamespace());
        addKeyFields(record);
        addFields(record);
        records.add(record);
    }

    /**
     * Adds a key field for uuid.
     */
    protected void addKeyFields(EimRecord record) {
        record.addKeyField(new TextField(FIELD_UUID, getItem().getUid()));
    }

    private void addFields(EimRecord record) {
        record.addFields(generateUnknownFields());
    }
}
