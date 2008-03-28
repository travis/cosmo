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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseStampApplicator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.TaskStamp;

/**
 * Applies EIM records to task stamps.
 *
 * @see TaskStamp
 */
public class TaskApplicator extends BaseStampApplicator {
    private static final Log log =
        LogFactory.getLog(TaskApplicator.class);

    /** */
    public TaskApplicator(Item item) {
        super(PREFIX_TASK, NS_TASK, item);
        setStamp(StampUtils.getTaskStamp(item));
    }

    /** */
    protected Stamp createStamp(EimRecord record) throws EimSchemaException {
        return getItem().getFactory().createTaskStamp();
    }

    /**
     * Copies record field values to task properties and
     * attributes.
     *
     * @throws EimValidationException if the field value is invalid
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the task 
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        // Update timestamp of stamp so that task record will be 
        // serialized next sync
        getStamp().updateTimestamp();
        applyUnknownField(field);
    }
}
