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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.StampUtils;

/**
 * Generates EIM records from message stamps.
 *
 * @see MessageStamp
 */
public class MessageGenerator extends BaseStampGenerator
    implements MessageConstants {
    private static final Log log =
        LogFactory.getLog(MessageGenerator.class);

    private static final HashSet<String> STAMP_TYPES = new HashSet<String>(2);
    
    static {
        STAMP_TYPES.add("message");
    }

    /** */
    public MessageGenerator(Item item) {
        super(PREFIX_MESSAGE, NS_MESSAGE, item);
        setStamp(StampUtils.getMessageStamp(item));
    }

    @Override
    protected Set<String> getStampTypes() {
        return STAMP_TYPES;
    }

    /**
     * Adds a record for the message.
     */
    protected void addRecords(List<EimRecord> records) {
        MessageStamp stamp = (MessageStamp) getStamp();
        if (stamp == null)
            return;

        EimRecord record = new EimRecord(getPrefix(), getNamespace());
        addKeyFields(record);
        addFields(record);
        records.add(record);
    }

    /**
     * Adds key field for uuid.
     */
    protected void addKeyFields(EimRecord record) {
        record.addKeyField(new TextField(FIELD_UUID, getItem().getUid()));
    }

    private void addFields(EimRecord record) {
        MessageStamp stamp = (MessageStamp) getStamp();
        record.addField(new TextField(FIELD_MESSAGE_ID, stamp.getMessageId()));
        StringReader headers = stamp.getHeaders() != null ?
                new StringReader(stamp.getHeaders()) :
                null;
        record.addField(new ClobField(FIELD_HEADERS, headers));
        record.addField(new TextField(FIELD_FROM, stamp.getFrom()));
        record.addField(new TextField(FIELD_TO, stamp.getTo()));
        record.addField(new TextField(FIELD_CC, stamp.getCc()));
        record.addField(new TextField(FIELD_BCC, stamp.getBcc()));
        record.addField(new TextField(FIELD_ORIGINATORS, stamp.getOriginators()));
        record.addField(new TextField(FIELD_DATE_SENT, stamp.getDateSent()));
        record.addField(new TextField(FIELD_IN_REPLY_TO, stamp.getInReplyTo()));
        
        StringReader refs = stamp.getReferences() != null ?
                new StringReader(stamp.getReferences()) :
                null;
        record.addField(new ClobField(FIELD_REFERENCES, refs));
        record.addFields(generateUnknownFields());
    }
}
