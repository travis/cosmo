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
package org.osaf.cosmo.eim.schema.occurrenceitem;

import java.util.List;

import net.fortuna.ical4j.model.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.eim.schema.occurenceitem.OccurrenceItemGenerator;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.NoteOccurrenceUtil;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test Case for {@link OccurrenceItemGenerator}.
 */
public class OccurrenceItemGeneratorTest extends BaseGeneratorTestCase {
    private static final Log log =
        LogFactory.getLog(OccurrenceItemGeneratorTest.class);

    public void testGenerateRecord() throws Exception {
       
        NoteItem master = new MockNoteItem();
        master.setUid("uid");
        NoteOccurrence occurrenceItem = NoteOccurrenceUtil.createNoteOccurrence(new Date("20070101"), master);
        
        OccurrenceItemGenerator generator = new OccurrenceItemGenerator(occurrenceItem);

        List<EimRecord> records = generator.generateRecords();
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_OCCURRENCE_ITEM, NS_OCCURRENCE_ITEM);
        checkUuidKey(record.getKey(), "uid:20070101");

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 0, fields.size());
    }

   
}
