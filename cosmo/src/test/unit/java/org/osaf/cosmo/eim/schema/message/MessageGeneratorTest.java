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
package org.osaf.cosmo.eim.schema.message;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.mock.MockMessageStamp;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test Case for {@link MessageGenerator}.
 */
public class MessageGeneratorTest extends BaseGeneratorTestCase
    implements MessageConstants {
    private static final Log log =
        LogFactory.getLog(MessageGeneratorTest.class);

    public void testGenerateRecord() throws Exception {
        
        MockNoteItem noteItem = new MockNoteItem();
        noteItem.setModifiedDate(new Date());
        noteItem.setUid("1");
        
        MockMessageStamp messageStamp = new MockMessageStamp(noteItem);
        messageStamp.setModifiedDate(noteItem.getModifiedDate());
        messageStamp.setMessageId("id");
        messageStamp.setHeaders("headers");
        messageStamp.setFrom("from");
        messageStamp.setTo("to");
        messageStamp.setCc("cc");
        messageStamp.setBcc("bcc");
        messageStamp.setOriginators("originators");
        messageStamp.setDateSent("dateSent");
        messageStamp.setInReplyTo("inReplyTo");
        messageStamp.setReferences("references");
        noteItem.addStamp(messageStamp);

        MessageGenerator generator = new MessageGenerator(noteItem);

        List<EimRecord> records = generator.generateRecords(-1);
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_MESSAGE, NS_MESSAGE);
        checkUuidKey(record.getKey(), "1");

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 10, fields.size());

        EimRecordField idField = fields.get(0);
        checkTextField(idField, FIELD_MESSAGE_ID, "id");

        EimRecordField headersField = fields.get(1);
        checkClobField(headersField, FIELD_HEADERS, "headers");
        
        EimRecordField fromField = fields.get(2);
        checkTextField(fromField, FIELD_FROM, "from");
        
        EimRecordField toField = fields.get(3);
        checkTextField(toField, FIELD_TO, "to");
        
        EimRecordField ccField = fields.get(4);
        checkTextField(ccField, FIELD_CC, "cc");
        
        EimRecordField bccField = fields.get(5);
        checkTextField(bccField, FIELD_BCC, "bcc");
        
        EimRecordField originatorsField = fields.get(6);
        checkTextField(originatorsField, FIELD_ORIGINATORS, "originators");
        
        EimRecordField dateSentField = fields.get(7);
        checkTextField(dateSentField, FIELD_DATE_SENT, "dateSent");
        
        EimRecordField inReplyToField = fields.get(8);
        checkTextField(inReplyToField, FIELD_IN_REPLY_TO, "inReplyTo");
        
        EimRecordField referencesField = fields.get(9);
        checkClobField(referencesField, FIELD_REFERENCES, "references");
    }   
}