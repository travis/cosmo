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
package org.osaf.cosmo.eim.schema.note;

import java.io.Reader;

import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseItemApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.model.NoteItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Applies EIM records to note items.
 *
 * @see NoteItem
 */
public class NoteApplicator extends BaseItemApplicator
    implements NoteConstants {
    private static final Log log =
        LogFactory.getLog(NoteApplicator.class);

    private NoteItem note;

    /** */
    public NoteApplicator(NoteItem note) {
        super(PREFIX_NOTE, NS_NOTE, note);
        this.note = note;
    }

    /**
     * Copies record field values to note properties and
     * attributes.
     *
     * @throws EimValidationException if the field value is invalid
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the note 
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        if (field.getName().equals(FIELD_BODY)) {
            Reader value = EimFieldValidator.validateClob(field);
            note.setBody(value);
        } else if (field.getName().equals(FIELD_ICALUID)) {
            String value =
                EimFieldValidator.validateText(field, MAXLEN_ICALUID);
            note.setIcalUid(value);
        } else {
            applyUnknownField(field);
        }
    }
}
