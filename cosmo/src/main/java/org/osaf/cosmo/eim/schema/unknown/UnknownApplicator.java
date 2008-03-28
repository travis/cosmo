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
package org.osaf.cosmo.eim.schema.unknown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseApplicator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.model.Item;

/**
 * Applies EIM records of unknown type to item attributes.
 */
public class UnknownApplicator extends BaseApplicator {
    private static final Log log =
        LogFactory.getLog(UnknownApplicator.class);

    /** */
    public UnknownApplicator(Item item) {
        super(null, null, item);
    }

    /**
     * Copies the data from an EIM record into the item.
     * <p>
     * If the record is marked deleted, then
     * {@link #applyDeletion(EimRecord)} is called.
     * <p>
     * If the record is not marked deleted, then
     * {@link #applyField(EimRecordField)} is called for each
     * non-key record field.
     * 
     * @throws EimValidationException if the record contains an
     * invalid field value
     * @throws EimSchemaException if the record is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    public void applyRecord(EimRecord record)
        throws EimSchemaException {
        if (record.isDeleted()) {
            applyDeletion(record);
            return;
        }

        for (EimRecordField field : record.getFields()) {
            applyField(field);
        }
    }

    /**
     * Removes all attributes in the record's namespace from the
     * item.
     *
     * @throws EimSchemaException if deletion cannot be processed.
     */
    protected void applyDeletion(EimRecord record)
        throws EimSchemaException {
        getItem().removeAttributes(record.getNamespace());
    }

    /**
     * Copies the data from the given record field into the item as
     * attributes in the record's namespace.
     *
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        applyUnknownField(field);
    }
}
