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

import java.io.Reader;
import java.io.StringReader;

import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Translates item records to <code>MessageStamp</code>s.
 * <p>
 * Implements the following schema:
 * <p>
 * TBD
 */
public class MessageTranslator extends EimSchemaTranslator {
    private static final Log log = LogFactory.getLog(MessageTranslator.class);

    /** */
    public static final String FIELD_SUBJECT = "subject";
    /** */
    public static final int MAXLEN_SUBJECT = 256;
    /** */
    public static final String FIELD_TO = "to";
    /** */
    public static final int MAXLEN_TO = 256;
    /** */
    public static final String FIELD_CC = "cc";
    /** */
    public static final int MAXLEN_CC = 256;
    /** */
    public static final String FIELD_BCC = "bcc";
    /** */
    public static final int MAXLEN_BCC = 256;

    /** */
    public MessageTranslator() {
        super(PREFIX_MESSAGE, NS_MESSAGE);
    }

    /**
     * Removes the message stamp associated with the record.
     */
    protected void applyDeletion(Item item)
        throws EimSchemaException {
        MessageStamp stamp = MessageStamp.getStamp(item);
        if (stamp == null)
            throw new IllegalArgumentException("Item does not have an message stamp");

        item.removeStamp(stamp);
    }

    /**
     * Copies the data from the given record field into the message
     * stamp.
     *
     * @throws IllegalArgumentException if the item does not have an
     * message stamp
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected void applyField(EimRecordField field,
                              Item item)
        throws EimSchemaException {
        MessageStamp stamp = MessageStamp.getStamp(item);
        if (stamp == null)
            throw new IllegalArgumentException("Item does not have a message stamp");

        if (field.getName().equals(FIELD_SUBJECT)) {
            String value = validateText(field, MAXLEN_SUBJECT);
            stamp.setSubject(value);
        } else if (field.getName().equals(FIELD_TO)) {
            String value = validateText(field, MAXLEN_TO);
            stamp.setTo(value);
        } else if (field.getName().equals(FIELD_CC)) {
            String value = validateText(field, MAXLEN_CC);
            stamp.setCc(value);
        } else if (field.getName().equals(FIELD_BCC)) {
            String value = validateText(field, MAXLEN_BCC);
            stamp.setBcc(value);
        }

        applyUnknownField(field, stamp.getItem());
    }

    /**
     * Adds record fields for each applicable message property.
     */
    protected void addFields(EimRecord record,
                             Item item) {
        addFields(record, MessageStamp.getStamp(item));
    }

    /**
     * Adds record fields for each applicable message property.
     *
     * @throws IllegalArgumentException if the stamp is not a message stamp
     */
    protected void addFields(EimRecord record,
                             Stamp stamp) {
        if (! (stamp instanceof MessageStamp))
            throw new IllegalArgumentException("Stamp is not a message stamp");
        MessageStamp ms = (MessageStamp) stamp;

        record.addKeyField(new TextField(FIELD_UUID,
                                         stamp.getItem().getUid()));
        record.addField(new TextField(FIELD_SUBJECT, ms.getSubject()));
        record.addField(new TextField(FIELD_TO, ms.getTo()));
        record.addField(new TextField(FIELD_CC, ms.getCc()));
        record.addField(new TextField(FIELD_BCC, ms.getBcc()));
        addUnknownFields(record, stamp.getItem());
    }
}
