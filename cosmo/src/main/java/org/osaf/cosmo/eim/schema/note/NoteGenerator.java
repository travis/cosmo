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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseItemGenerator;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;

/**
 * Generates EIM records from note items.
 *
 * @see NoteItem
 */
public class NoteGenerator extends BaseItemGenerator
    implements NoteConstants {
    private static final Log log =
        LogFactory.getLog(NoteGenerator.class);

    /** */
    public NoteGenerator(Item item) {
        super(PREFIX_NOTE, NS_NOTE, item);
        if (! (item instanceof NoteItem))
            throw new IllegalArgumentException("item " + item.getUid() + " not a note item");
    }

    /**
     * Copies note properties and attributes into a note record.
     */
    public List<EimRecord> generateRecords() {
        NoteItem note = (NoteItem) getItem();

        EimRecord record = new EimRecord(getPrefix(), getNamespace());

        record.addKeyField(new TextField(FIELD_UUID, note.getUid()));

        if(isMissingAttribute("body")) {
            record.addField(generateMissingField(new ClobField(FIELD_BODY, null)));
        } else {
            StringReader body = note.getBody() != null ?
                    new StringReader(note.getBody()) :
                    null;
            record.addField(new ClobField(FIELD_BODY, body));
        }
        
        if(isMissingAttribute("icalUid")) {
            record.addField(generateMissingField(new TextField(FIELD_ICALUID, null)));
        } else {
            record.addField(new TextField(FIELD_ICALUID, note.getIcalUid()));
        }
        
        record.addFields(generateUnknownFields());

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
