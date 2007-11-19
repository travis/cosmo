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

import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseStampApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampUtils;

/**
 * Applies EIM records to message stamps.
 *
 * @see MessageStamp
 */
public class MessageApplicator extends BaseStampApplicator
    implements MessageConstants {
    private static final Log log =
        LogFactory.getLog(MessageApplicator.class);

    /** */
    public MessageApplicator(Item item) {
        super(PREFIX_MESSAGE, NS_MESSAGE, item);
        setStamp(StampUtils.getMessageStamp(item));
    }

    /**
     * Creates and returns a stamp instance that can be added by
     * <code>BaseStampApplicator</code> to the item. Used when a
     * stamp record is applied to an item that does not already have
     * that stamp.
     */
    protected Stamp createStamp(EimRecord record) throws EimSchemaException {
        return getItem().getFactory().createMessageStamp();
    }

    /**
     * Copies record field values to message properties and
     * attributes.
     *
     * @throws EimValidationException if the field value is invalid
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the message 
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        MessageStamp message = (MessageStamp) getStamp();

        if (field.getName().equals(FIELD_MESSAGE_ID)) {
            if(field.isMissing()) {
                handleMissingAttribute("messageId");
            }
            else {
                String value =
                    EimFieldValidator.validateText(field, MAXLEN_MESSAGE_ID);
                message.setMessageId(value);
            }
        } else if (field.getName().equals(FIELD_HEADERS)) {
            if(field.isMissing()) {
                handleMissingAttribute("headers");
            }
            else {
                Reader value = EimFieldValidator.validateClob(field);
                message.setHeaders(value);
            }
        } else if (field.getName().equals(FIELD_FROM)) {
            if(field.isMissing()) {
                handleMissingAttribute("from");
            }
            else {
                String value = EimFieldValidator.validateText(field, MAXLEN_FROM);
                message.setFrom(value);
            }
        } else if (field.getName().equals(FIELD_TO)) {
            if(field.isMissing()) {
                handleMissingAttribute("to");
            }
            else {
                String value = EimFieldValidator.validateText(field, MAXLEN_TO);
                message.setTo(value);
            }
        } else if (field.getName().equals(FIELD_CC)) {
            if(field.isMissing()) {
                handleMissingAttribute("cc");
            }
            else {
                String value = EimFieldValidator.validateText(field, MAXLEN_CC);
                message.setCc(value);
            }
        } else if (field.getName().equals(FIELD_BCC)) {
            if(field.isMissing()) {
                handleMissingAttribute("bcc");
            }
            else {
                String value = EimFieldValidator.validateText(field, MAXLEN_BCC);
                message.setBcc(value);
            }
        } else if (field.getName().equals(FIELD_ORIGINATORS)) {
            if(field.isMissing()) {
                handleMissingAttribute("originators");
            }
            else {
                String value = EimFieldValidator.validateText(field, MAXLEN_ORIGINATORS);
                message.setOriginators(value);
            }
        } else if (field.getName().equals(FIELD_DATE_SENT)) {
            if(field.isMissing()) {
                handleMissingAttribute("dateSent");
            }
            else {
                String value = EimFieldValidator.validateText(field, MAXLEN_DATE_SENT);
                message.setDateSent(value);
            }
        } else if (field.getName().equals(FIELD_IN_REPLY_TO)) {
            if(field.isMissing()) {
                handleMissingAttribute("inReplyTo");
            }
            else {
                String value = EimFieldValidator.validateText(field, MAXLEN_IN_REPLY_TO);
                message.setInReplyTo(value);
            }
        } else if (field.getName().equals(FIELD_REFERENCES)) {
            if(field.isMissing()) {
                handleMissingAttribute("references");
            }
            else {
                Reader value = EimFieldValidator.validateClob(field);
                message.setReferences(value);
            }
        } else {
            // Update timestamp of stamp so that message record will be 
            // serialized next sync
            getStamp().updateTimestamp();
            applyUnknownField(field);
        }
    }
}
