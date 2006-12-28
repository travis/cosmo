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

import java.util.List;
import java.util.ArrayList;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates EIM records from message stamps.
 *
 * @see MessageStamp
 */
public class MessageGenerator extends BaseStampGenerator
    implements MessageConstants {
    private static final Log log =
        LogFactory.getLog(MessageGenerator.class);

    private MessageStamp message;

    /** */
    public MessageGenerator(MessageStamp message) {
        super(PREFIX_MESSAGE, NS_MESSAGE, message);
        this.message = message;
    }

    /**
     * Copies message properties and attributes into a message record.
     */
    public List<EimRecord> generateRecords() {
        EimRecord record = new EimRecord(getPrefix(), getNamespace());

        record.addKeyField(new TextField(FIELD_UUID,
                                         message.getItem().getUid()));

        record.addField(new TextField(FIELD_SUBJECT, message.getSubject()));
        record.addField(new TextField(FIELD_TO, message.getTo()));
        record.addField(new TextField(FIELD_CC, message.getCc()));
        record.addField(new TextField(FIELD_BCC, message.getBcc()));

        record.addFields(generateUnknownFields());

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
