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
import java.util.ArrayList;
import java.util.List;

import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Translates item records to <code>NoteItem</code>s.
 * <p>
 * Implements the following schema:
 * <p>
 * TBD
 */
public class NoteTranslator extends BaseItemTranslator {
    private static final Log log = LogFactory.getLog(NoteTranslator.class);

    /** */
    public static final String FIELD_BODY = "body";
    /** */
    public static final String FIELD_ICALUID = "icalUid";
    /** */
    public static final int MAXLEN_ICALUID = 256;

    /** */
    public NoteTranslator() {
        super(PREFIX_NOTE, NS_NOTE);
    }

    /**
     * @throws EimSchemaException
     */
    protected void applyDeletion(EimRecord record,
                                 Item item)
        throws EimSchemaException {
        throw new EimSchemaException("Note-ness may not be deleted");
    }

    /**
     * Copies the data from the given record field into the note item.
     *
     * @throws IllegalArgumentException if the item is not a note
     * item
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected void applyField(EimRecordField field,
                              Item item)
        throws EimSchemaException {
        if (! (item instanceof NoteItem))
            throw new IllegalArgumentException("Item is not a note item");
        NoteItem ni = (NoteItem) item;

        if (field.getName().equals(FIELD_BODY)) {
            Reader value = validateClob(field);
            ni.setBody(value);
        } else if (field.getName().equals(FIELD_ICALUID)) {
            String value = validateText(field, MAXLEN_ICALUID);
            ni.setIcalUid(value);
        } else {
            applyUnknownField(field, item);
        }
    }

    /**
     * Copies the data from a note item into a note record.
     *
     * @throws IllegalArgumentException if the item is not a note item
     */
    public List<EimRecord> toRecords(Item item) {
        if (! (item instanceof NoteItem))
            throw new IllegalArgumentException("Item is not a note item");
        NoteItem ni = (NoteItem) item;

        EimRecord record = createRecord(item);

        record.addKeyField(new TextField(FIELD_UUID, item.getUid()));
        record.addField(new ClobField(FIELD_BODY,
                                      new StringReader(ni.getBody())));
        record.addField(new TextField(FIELD_ICALUID, ni.getIcalUid()));

        addUnknownFields(record, item);

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
