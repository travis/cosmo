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
package org.osaf.cosmo.eim.schema.event;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test Case for {@link EventGenerator}.
 */
public class EventGeneratorTest extends BaseGeneratorTestCase
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(EventGeneratorTest.class);

    public void testGenerateRecord() throws Exception {
        
        NoteItem noteItem = new NoteItem();
        noteItem.setModifiedDate(new Date());
        noteItem.setUid("1");
        
        EventStamp eventStamp = new EventStamp(noteItem);
        eventStamp.setModifiedDate(noteItem.getModifiedDate());
        eventStamp.createCalendar();
        eventStamp.setLocation("here");
        eventStamp.setStatus("CONFIRMED");
        eventStamp.setStartDate(EimValueConverter.toICalDate(";VALUE=DATE-TIME:20070212T074500").getDate());
        eventStamp.setEndDate(EimValueConverter.toICalDate(";VALUE=DATE-TIME:20070212T084500").getDate());
        
        noteItem.addStamp(eventStamp);

        EventGenerator generator = new EventGenerator(noteItem);

        List<EimRecord> records = generator.generateRecords(-1);
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_EVENT, NS_EVENT);
        checkUuidKey(record.getKey(), "1");

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 8, fields.size());

        EimRecordField dtStartField = fields.get(0);
        checkTextField(dtStartField, FIELD_DTSTART, ";VALUE=DATE-TIME:20070212T074500");

        EimRecordField durationField = fields.get(1);
        checkTextField(durationField, FIELD_DURATION, "PT1H");

        EimRecordField locationField = fields.get(2);
        checkTextField(locationField, FIELD_LOCATION, "here");

        EimRecordField rruleField = fields.get(3);
        checkTextField(rruleField, FIELD_RRULE, null);
        
        EimRecordField exruleField = fields.get(4);
        checkTextField(exruleField, FIELD_EXRULE, null);
        
        EimRecordField rdateField = fields.get(5);
        checkTextField(rdateField, FIELD_RDATE, null);
        
        EimRecordField exdateField = fields.get(6);
        checkTextField(exdateField, FIELD_EXDATE, null);
        
        EimRecordField statusField = fields.get(7);
        checkTextField(statusField, FIELD_STATUS, "CONFIRMED");
    }

}
