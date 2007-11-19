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

import java.io.StringReader;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseApplicatorTestCase;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.mock.MockMessageStamp;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test Case for {@link MessageApplicator}.
 */
public class MessageApplicatorTest extends BaseApplicatorTestCase
    implements MessageConstants {
    private static final Log log =
        LogFactory.getLog(MessageApplicatorTest.class);

    public void testApplyField() throws Exception {
        NoteItem noteItem = new MockNoteItem();
        MessageStamp messageStamp = new MockMessageStamp(noteItem);
        noteItem.addStamp(messageStamp);

        EimRecord record = makeTestRecord();

        MessageApplicator applicator =
            new MessageApplicator(noteItem);
        applicator.applyRecord(record);

        Assert.assertEquals(messageStamp.getMessageId(),"id");
        Assert.assertEquals(messageStamp.getHeaders(), "blah");
        Assert.assertEquals(messageStamp.getFrom(), "from");
        Assert.assertEquals(messageStamp.getCc(), "cc");
        Assert.assertEquals(messageStamp.getBcc(), "bcc");
        Assert.assertEquals(messageStamp.getOriginators(), "originators");
        Assert.assertEquals(messageStamp.getDateSent(), "dateSent");
        Assert.assertEquals(messageStamp.getInReplyTo(), "inReplyTo");
        Assert.assertEquals(messageStamp.getReferences(), "blah2");
    }
    
    public void testApplyMissingFields() throws Exception {
        NoteItem masterNote = new MockNoteItem();
        NoteItem modItem = new MockNoteItem();
        MessageStamp messageStamp = new MockMessageStamp(masterNote);
        messageStamp.setMessageId("test");
        messageStamp.setHeaders("test");
        messageStamp.setFrom("test");
        messageStamp.setCc("test");
        messageStamp.setBcc("test");
        messageStamp.setOriginators("test");
        messageStamp.setDateSent("test");
        messageStamp.setInReplyTo("test");
        messageStamp.setReferences("test");
        masterNote.addStamp(messageStamp);

        modItem.setModifies(masterNote);
        
        EimRecord record = makeTestMissingRecord();

        MessageApplicator applicator =
            new MessageApplicator(modItem);
        applicator.applyRecord(record);

        MessageStamp modStamp = StampUtils.getMessageStamp(modItem);
        
        Assert.assertNull(modStamp.getMessageId());
        Assert.assertNull(modStamp.getHeaders());
        Assert.assertNull(modStamp.getFrom());
        Assert.assertNull(modStamp.getCc());
        Assert.assertNull(modStamp.getBcc());
        Assert.assertNull(modStamp.getOriginators());
        Assert.assertNull(modStamp.getDateSent());
        Assert.assertNull(modStamp.getInReplyTo());
        Assert.assertNull(modStamp.getReferences());
    }
    
    private EimRecord makeTestRecord() {
        EimRecord record = new EimRecord(PREFIX_MESSAGE, NS_MESSAGE);

        record.addField(new TextField(FIELD_MESSAGE_ID, "id"));
        record.addField(new ClobField(FIELD_HEADERS, new StringReader("blah")));
        record.addField(new TextField(FIELD_FROM, "from"));
        record.addField(new TextField(FIELD_TO, "to"));
        record.addField(new TextField(FIELD_CC, "cc"));
        record.addField(new TextField(FIELD_BCC, "bcc"));
        record.addField(new TextField(FIELD_ORIGINATORS, "originators"));
        record.addField(new TextField(FIELD_DATE_SENT, "dateSent"));
        record.addField(new TextField(FIELD_IN_REPLY_TO, "inReplyTo"));
        record.addField(new ClobField(FIELD_REFERENCES, new StringReader("blah2")));

        return record;
    }
    
    private EimRecord makeTestMissingRecord() {
        EimRecord record = new EimRecord(PREFIX_MESSAGE, NS_MESSAGE);

        addMissingTextField(FIELD_MESSAGE_ID, record);
        addMissingClobField(FIELD_HEADERS, record);
        addMissingTextField(FIELD_FROM, record);
        addMissingTextField(FIELD_TO, record);
        addMissingTextField(FIELD_CC, record);
        addMissingTextField(FIELD_BCC, record);
        addMissingTextField(FIELD_ORIGINATORS, record);
        addMissingTextField(FIELD_DATE_SENT, record);
        addMissingTextField(FIELD_IN_REPLY_TO, record);
        addMissingClobField(FIELD_REFERENCES, record);

        return record;
    }
    
}
