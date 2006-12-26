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
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for schema translators that translates stamp attributes.
 */
public abstract class BaseStampTranslator extends EimSchemaTranslator {
    private static final Log log =
        LogFactory.getLog(BaseStampTranslator.class);

    /**
     * This class should not be instantiated directly.
     */
    protected BaseStampTranslator(String prefix,
                                  String namespace) {
        super(prefix, namespace);
    }

    /**
     * Copies the data from an stamp into one or more EIM records.
     */
    public abstract List<EimRecord> toRecords(Stamp stamp);

    /**
     * Creates an empty EIM record that can subsequently be filled
     * with data.
     * <p>
     * Sets the record's prefix and namespace.
     * <p>
     * If the stamp is inactive, the record is marked deleted.
     */
    public EimRecord createRecord(Stamp stamp) {
        EimRecord record = new EimRecord(getPrefix(), getNamespace());

        // XXX:
//         if (! stamp.getIsActive())
//             record.setDeleted(true);

        return record;
    }
}
