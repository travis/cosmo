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
import org.osaf.cosmo.model.Item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for schema applicators that map to <code>Item</code>s.
 *
 * @see Item
 */
public abstract class BaseItemApplicator extends BaseApplicator {
    private static final Log log =
        LogFactory.getLog(BaseItemApplicator.class);

    /**
     * This class should not be instantiated directly.
     */
    protected BaseItemApplicator(String prefix,
                                 String namespace,
                                 Item item) {
        super(prefix, namespace, item);
    }

    /**
     * Copies the data from an EIM record into the item.
     * <p>
     * {@link #applyField(EimRecordField)} is called for each
     * non-key record field.
     * 
     * @throws IllegalArgumentException if the record's namespace does
     * not match this translator's namespace
     * @throws EimValidationException if the record contains an
     * invalid field value
     * @throws EimSchemaException if the record is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    public void applyRecord(EimRecord record)
        throws EimSchemaException {
        if (getNamespace() != null &&
            ! record.getNamespace().equals(getNamespace()))
            throw new IllegalArgumentException("Record namespace " + record.getNamespace() + " does not match " + getNamespace());
        
        if (record.isDeleted())
            throw new EimSchemaException("Item-based records cannot be marked deleted");

        for (EimRecordField field : record.getFields()) {
            applyField(field);
        }
    }

    /**
     * Copies the data from the given record field into the item.
     *
     * If the field is not part of the subclass' schema, the field
     * should be handled with
     * {@link applyUnknownField(EimRecordField)}.
     *
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected abstract void applyField(EimRecordField field)
        throws EimSchemaException;
}
