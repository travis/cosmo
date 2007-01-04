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
package org.osaf.cosmo.eim.schema.contentitem;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.EimRecordKey;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.StringAttribute;

/**
 * Test Case for {@link ContentItemGenerator}.
 */
public class ContentItemGeneratorTest extends BaseGeneratorTestCase
    implements ContentItemConstants {
    private static final Log log =
        LogFactory.getLog(ContentItemGeneratorTest.class);

    public void testGenerateRecord() throws Exception {
        ContentItem contentItem = new ContentItem();
        contentItem.setUid("deadbeef");
        contentItem.setName("3inchesofblood");
        contentItem.setDisplayName("3 Inches of Blood");
        contentItem.setTriageStatus("DONE");
        contentItem.setTriageStatusUpdated(new BigDecimal("666.66"));
        contentItem.setLastModifiedBy("bcm@osafoundation.org");
        contentItem.setClientCreationDate(Calendar.getInstance().getTime());

        StringAttribute unknownAttr = makeStringAttribute();
        contentItem.addAttribute(unknownAttr);

        ContentItemGenerator generator = new ContentItemGenerator(contentItem);

        List<EimRecord> records = generator.generateRecords();
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_ITEM, NS_ITEM);
        checkUuidKey(record.getKey(), contentItem.getUid());

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 6, fields.size());

        EimRecordField title = fields.get(0);
        checkTextField(title, FIELD_TITLE, contentItem.getDisplayName());

        EimRecordField triageStatus = fields.get(1);
        checkTextField(triageStatus, FIELD_TRIAGE_STATUS,
                       contentItem.getTriageStatus());

        EimRecordField triageStatusChanged = fields.get(2);
        checkDecimalField(triageStatusChanged, FIELD_TRIAGE_STATUS_CHANGED,
                          contentItem.getTriageStatusUpdated(),
                          DIGITS_TRIAGE_STATUS_CHANGED,
                          DEC_TRIAGE_STATUS_CHANGED);

        EimRecordField lastModifiedBy = fields.get(3);
        checkTextField(lastModifiedBy, FIELD_LAST_MODIFIED_BY,
                       contentItem.getLastModifiedBy());

        EimRecordField createdOn = fields.get(4);
        checkTimeStampField(createdOn, FIELD_CREATED_ON,
                            contentItem.getClientCreationDate());

        EimRecordField unknown = fields.get(5);
        checkTextField(unknown, unknownAttr.getName(), unknownAttr.getValue());
    }

    public void testInactiveNotDeleted() throws Exception {
        // inactive items are not deleted via item records but via
        // recordset
        ContentItem contentItem = new ContentItem();
        contentItem.setIsActive(false);

        ContentItemGenerator generator = new ContentItemGenerator(contentItem);

        checkNotDeleted(generator.generateRecords().get(0));
    }

    private StringAttribute makeStringAttribute() {
        StringAttribute attr = new StringAttribute();
        attr.setQName(new QName(NS_ITEM, "Blues Traveler"));
        attr.setValue("Sweet talkin' hippie");
        return attr;
    }
}
