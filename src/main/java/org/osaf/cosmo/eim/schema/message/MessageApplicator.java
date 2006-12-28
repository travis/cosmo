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

import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.model.MessageStamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Applies EIM records to message stamps.
 *
 * @see MessageStamp
 */
public class MessageApplicator extends BaseStampApplicator
    implements MessageConstants {
    private static final Log log =
        LogFactory.getLog(MessageApplicator.class);

    private MessageStamp message;

    /** */
    public MessageApplicator(MessageStamp message) {
        super(PREFIX_MESSAGE, NS_MESSAGE, message);
        this.message = message;
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
        if (field.getName().equals(FIELD_SUBJECT)) {
            String value =
                EimFieldValidator.validateText(field, MAXLEN_SUBJECT);
            message.setSubject(value);
        } else if (field.getName().equals(FIELD_TO)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_TO);
            message.setTo(value);
        } else if (field.getName().equals(FIELD_CC)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_CC);
            message.setCc(value);
        } else if (field.getName().equals(FIELD_BCC)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_BCC);
            message.setBcc(value);
        } else {
            applyUnknownField(field);
        }
    }
}
