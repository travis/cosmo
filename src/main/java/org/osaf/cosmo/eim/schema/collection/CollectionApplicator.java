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
package org.osaf.cosmo.eim.schema.collection;

import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseItemApplicator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.model.CollectionItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Applies EIM records to collections.
 *
 * @see CollectionItem
 */
public class CollectionApplicator extends BaseItemApplicator {
    private static final Log log =
        LogFactory.getLog(CollectionApplicator.class);

    private CollectionItem collection;

    /** */
    public CollectionApplicator(CollectionItem collection) {
        super(PREFIX_COLLECTION, NS_COLLECTION, collection);
        this.collection = collection;
    }

    /**
     * Copies record field values to collection properties and
     * attributes.
     *
     * @throws EimValidationException if the field value is invalid
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the collection 
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        applyUnknownField(field);
    }
}
