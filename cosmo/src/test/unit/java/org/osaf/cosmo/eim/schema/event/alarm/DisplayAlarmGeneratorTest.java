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
package org.osaf.cosmo.eim.schema.event.alarm;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseGeneratorTestCase;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test Case for {@link DisplayAlarmGenerator}.
 */
public class DisplayAlarmGeneratorTest extends BaseGeneratorTestCase
    implements DisplayAlarmConstants {
    private static final Log log =
        LogFactory.getLog(DisplayAlarmGeneratorTest.class);

    public void testGenerateRecord() throws Exception {
        
        NoteItem noteItem = new NoteItem();
        noteItem.setModifiedDate(new Date());
        noteItem.setUid("1");
        
        EventStamp eventStamp = new EventStamp(noteItem);
        eventStamp.setModifiedDate(noteItem.getModifiedDate());
        eventStamp.createCalendar();
        eventStamp.creatDisplayAlarm();
        eventStamp.setDisplayAlarmDescription("description");
        eventStamp.setDisplayAlarmDuration(new Dur("P1W"));
        eventStamp.setDisplayAlarmTrigger(EimValueConverter.toIcalTrigger("-PT15M"));
        eventStamp.setDisplayAlarmRepeat(1);
        noteItem.addStamp(eventStamp);

        DisplayAlarmGenerator generator = new DisplayAlarmGenerator(noteItem);

        List<EimRecord> records = generator.generateRecords(-1);
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);
        checkUuidKey(record.getKey(), "1");

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 4, fields.size());

        EimRecordField descriptionField = fields.get(0);
        checkTextField(descriptionField, FIELD_DESCRIPTION, "description");

        EimRecordField triggerField = fields.get(1);
        checkTextField(triggerField, FIELD_TRIGGER,
                       eventStamp.getDisplayAlarmTrigger().getValue());

        EimRecordField durationField = fields.get(2);
        checkTextField(durationField, FIELD_DURATION,
                            eventStamp.getDisplayAlarmDuration().toString());

        EimRecordField repeatField = fields.get(3);
        checkIntegerField(repeatField, FIELD_REPEAT,
                       eventStamp.getDisplayAlarmRepeat());
    }
    
    public void testGenerateRecordNonEvent() throws Exception {
        
        NoteItem noteItem = new NoteItem();
        noteItem.setModifiedDate(new Date());
        noteItem.setReminderTime(new Date());
        noteItem.setUid("1");
        
        DisplayAlarmGenerator generator = new DisplayAlarmGenerator(noteItem);

        List<EimRecord> records = generator.generateRecords(-1);
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);
        checkUuidKey(record.getKey(), "1");

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 4, fields.size());

        EimRecordField descriptionField = fields.get(0);
        checkTextField(descriptionField, FIELD_DESCRIPTION, null);

        EimRecordField triggerField = fields.get(1);
        DateTime dt = new DateTime(true);
        dt.setTime(noteItem.getReminderTime().getTime());
        checkTextField(triggerField, FIELD_TRIGGER,
                       EimValueConverter.formatTriggerFromDateTime(dt));

        EimRecordField durationField = fields.get(2);
        checkTextField(durationField, FIELD_DURATION, null);

        EimRecordField repeatField = fields.get(3);
        checkIntegerField(repeatField, FIELD_REPEAT, 1);
    }
    
    public void testGenerateNoRecord() throws Exception {
        
        NoteItem noteItem = new NoteItem();
        noteItem.setModifiedDate(new Date());
        noteItem.setUid("1");
        
        DisplayAlarmGenerator generator = new DisplayAlarmGenerator(noteItem);

        List<EimRecord> records = generator.generateRecords(-1);
        assertEquals("unexpected number of records generated", 0,
                     records.size());
    }
    
    public void testGenerateDeleteRecord() throws Exception {
        
        NoteItem noteItem = new NoteItem();
        noteItem.setModifiedDate(new Date());
        noteItem.setReminderTime(new Date());
        noteItem.setReminderTime(null);
        noteItem.setUid("1");
        
        DisplayAlarmGenerator generator = new DisplayAlarmGenerator(noteItem);

        List<EimRecord> records = generator.generateRecords(1);
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);
        checkUuidKey(record.getKey(), "1");
        Assert.assertTrue(record.isDeleted());
    }
    
    public void testGenerateMissingRecord() throws Exception {
        
        NoteItem masterNote = new NoteItem();
        EventStamp masterEvent = new EventStamp(masterNote);
        masterEvent.createCalendar();
        masterEvent.creatDisplayAlarm();
        masterEvent.setDisplayAlarmDescription("My alarm");
        masterEvent.setDisplayAlarmDuration(new Dur("P1W"));
        masterEvent.setDisplayAlarmTrigger(EimValueConverter.toIcalTrigger("-PT15M"));
        masterEvent.setDisplayAlarmRepeat(1);
        
        masterNote.addStamp(masterEvent);
        
        NoteItem modNote = new NoteItem();
        modNote.setUid("1");
        EventExceptionStamp modEvent = new EventExceptionStamp(modNote);
        modEvent.createCalendar();
        modEvent.creatDisplayAlarm();
        modNote.setModifies(masterNote);
        modNote.addStamp(modEvent);
        modEvent.setModifiedDate(new Date());
        modEvent.setDisplayAlarmDescription("My alarm");
        modEvent.setDisplayAlarmDuration(new Dur("P1W"));
        modEvent.setDisplayAlarmTrigger(EimValueConverter.toIcalTrigger("-PT15M"));
        modEvent.setDisplayAlarmRepeat(1);

        DisplayAlarmGenerator generator = new DisplayAlarmGenerator(modNote);

        List<EimRecord> records = generator.generateRecords(-1);
        assertEquals("unexpected number of records generated", 1,
                     records.size());

        EimRecord record = records.get(0);
        checkNamespace(record, PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);
        checkUuidKey(record.getKey(), "1");

        List<EimRecordField> fields = record.getFields();
        assertEquals("unexpected number of fields", 4, fields.size());

        EimRecordField descriptionField = fields.get(0);
        Assert.assertTrue(descriptionField.isMissing());

        EimRecordField triggerField = fields.get(1);
        Assert.assertTrue(triggerField.isMissing());

        EimRecordField durationField = fields.get(2);
        Assert.assertTrue(durationField.isMissing());

        EimRecordField repeatField = fields.get(3);
        Assert.assertTrue(repeatField.isMissing());
    }

}
