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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.EimRecordKey;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.StringAttribute;

/**
 * Test Case for {@link CollectionGenerator}.
 */
public class CollectionGeneratorTest extends BaseGeneratorTestCase {
    private static final Log log =
        LogFactory.getLog(CollectionGeneratorTest.class);

    public void testGenerateRecord() throws Exception {
        CollectionItem collection = new CollectionItem();
        collection.setUid("deadbeef");

        StringAttribute unknownAttr = makeStringAttribute();
        collection.addAttribute(unknownAttr);

        CollectionGenerator generator = new CollectionGenerator(collection);

        List<EimRecord> records = generator.generateRecords();
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_COLLECTION, NS_COLLECTION);
        checkUuidKey(record.getKey(), collection.getUid());

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 1, fields.size());

        EimRecordField field = fields.get(0);
        checkTextField(field, unknownAttr.getName(), unknownAttr.getValue());
    }

    public void testInactiveNotDeleted() throws Exception {
        // inactive items are not deleted via item records but via
        // recordset
        CollectionItem collection = new CollectionItem();
        collection.setIsActive(false);

        CollectionGenerator generator = new CollectionGenerator(collection);

        checkNotDeleted(generator.generateRecords().get(0));
    }

    private StringAttribute makeStringAttribute() {
        StringAttribute attr = new StringAttribute();
        attr.setQName(new QName(NS_COLLECTION, "Pentagram"));
        attr.setValue("Forever my queen");
        return attr;
    }
}
