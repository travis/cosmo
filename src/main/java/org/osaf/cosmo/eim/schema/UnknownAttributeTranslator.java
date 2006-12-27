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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Translates unknown records to <code>Attribute</code>s.
 */
public class UnknownAttributeTranslator extends BaseItemTranslator {
    private static final Log log =
        LogFactory.getLog(UnknownAttributeTranslator.class);

    /** */
    public UnknownAttributeTranslator() {
        super(null, null);
    }

    /**
     * Removes all attributes in the record's namespace from the
     * item.
     */
    protected void applyDeletion(EimRecord record,
                                 Item item)
        throws EimSchemaException {
        item.removeAttributes(record.getNamespace());
    }

    /**
     * Copies the data from the given record field into the item as
     * a string attribute in the record's namespace.
     *
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected void applyField(EimRecordField field,
                              Item item)
        throws EimSchemaException {
        applyUnknownField(field, item);
    }

    /**
     * Creates records for all item attributes in unknown namespaces.
     */
    public List<EimRecord> toRecords(Item item) {
        // index unknown namespaces
        HashSet<String> idx = new HashSet<String>();
        for (QName qn : item.getAttributes().keySet()) {
            if (isKnownNamespace(qn.getNamespace()))
                continue;
            idx.add(qn.getNamespace());
        }

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        int prefix = 1;
        for (String ns : idx) {
            EimRecord record = new EimRecord("pre" + prefix++, ns);
            addUnknownFields(record, item);
            records.add(record);
        }

        return records;
    }

    private static boolean isKnownNamespace(String namespace) {
        return (namespace.equals(NS_COLLECTION) ||
                namespace.equals(NS_ITEM) ||
                namespace.equals(NS_EVENT) ||
                namespace.equals(NS_TASK) ||
                namespace.equals(NS_MESSAGE) ||
                namespace.equals(NS_NOTE) ||
                namespace.equals(NS_ICALEXT));
    }
}
