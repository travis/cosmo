/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.eim.schema.modifiedby;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test Case for {@link ModifiedByGenerator}.
 */
public class ModifiedByGeneratorTest extends BaseGeneratorTestCase
    implements ModifiedByConstants {
    private static final Log log =
        LogFactory.getLog(ModifiedByGeneratorTest.class);

    public void testGenerateRecord() throws Exception {
        String uuid = "deadbeef";
        String lastModifiedBy = "bcm@osafoundation.org";
        Integer lastModification = ContentItem.Action.CREATED;
        Date modifiedDate = Calendar.getInstance().getTime();

        ContentItem contentItem = new MockNoteItem();
        contentItem.setUid(uuid);
        contentItem.setLastModifiedBy(lastModifiedBy);
        contentItem.setLastModification(lastModification);
        contentItem.setClientModifiedDate(modifiedDate);

        ModifiedByGenerator generator = new ModifiedByGenerator(contentItem);

        List<EimRecord> records = generator.generateRecords();
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_MODIFIEDBY, NS_MODIFIEDBY);

        List<EimRecordField> fields = record.getKey().getFields();
        assertEquals("unexpected number of fields", 4, fields.size());

        EimRecordField uuidField = fields.get(0);
        checkTextField(uuidField, FIELD_UUID, uuid);

        EimRecordField useridField = fields.get(1);
        checkTextField(useridField, FIELD_USERID, lastModifiedBy);

        EimRecordField modifiedDateField = fields.get(2);
        checkTimeStampField(modifiedDateField, FIELD_TIMESTAMP,
                            modifiedDate);

        EimRecordField actionField = fields.get(3);
        checkIntegerField(actionField, FIELD_ACTION, lastModification);

        assertEquals("unexpected number of fields", 0,
                     record.getFields().size());
    }
}
