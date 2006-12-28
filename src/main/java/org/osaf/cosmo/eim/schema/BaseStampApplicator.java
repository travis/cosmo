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

import java.util.List;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for schema applicators that map to <code>Stamp</code>s.
 *
 * @see Stamp
 */
public abstract class BaseStampApplicator extends BaseApplicator {
    private static final Log log =
        LogFactory.getLog(BaseStampApplicator.class);

    private Stamp stamp;

    /**
     * This class should not be instantiated directly.
     */
    protected BaseStampApplicator(String prefix,
                                  String namespace,
                                  Stamp stamp) {
        super(prefix, namespace, stamp.getItem());
        this.stamp = stamp;
    }

    /**
     * Copies the data from an EIM record into the stamp.
     * <p>
     * If the record is marked deleted, then
     * {@link #applyDeletion(EimRecord)} is called.
     * <p>
     * If the record is not marked deleted, then
     * {@link #applyField(EimRecordField)} is called for each
     * non-key record field.
     * 
     * @throws IllegalArgumentException if the record's namespace does
     * not match this translator's namespace
     * @throws EimValidationException if the record contains a field
     * with an invalid value
     * @throws EimSchemaException if the record is improperly
     * constructed or cannot otherwise be applied to the stamp 
     */
    public void applyRecord(EimRecord record)
        throws EimSchemaException {
        if (getNamespace() != null &&
            ! record.getNamespace().equals(getNamespace()))
            throw new IllegalArgumentException("Record namespace " + record.getNamespace() + " does not match " + getNamespace());
        
        if (record.isDeleted()) {
            applyDeletion(record);
            return;
        }

        for (EimRecordField field : record.getFields()) {
            applyField(field);
        }
    }

    /**
     * Deletes the stamp.
     *
     * @throws EimSchemaException if deletion is not allowed for this
     * record type or if deletion cannot otherwise be processed.
     */
    protected void applyDeletion(EimRecord record)
        throws EimSchemaException {
        getItem().removeStamp(stamp);
    }

    /**
     * Copies the data from the given record field into the stamp.
     *
     * If the field is not part of the subclass' schema, the field
     * should be handled with
     * {@link applyUnknownField(EimRecordField)}.
     *
     * @throws EimValidationException if the field value is invalid
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the stamp 
     */
    protected abstract void applyField(EimRecordField field)
        throws EimSchemaException;

    /** */
    public Stamp getStamp() {
        return stamp;
    }
}
