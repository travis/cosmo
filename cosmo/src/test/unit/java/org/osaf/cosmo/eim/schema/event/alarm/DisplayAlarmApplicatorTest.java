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

import junit.framework.Assert;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseApplicatorTestCase;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.mock.MockEventExceptionStamp;
import org.osaf.cosmo.model.mock.MockEventStamp;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test Case for {@link DisplayAlarmApplicator}.
 */
public class DisplayAlarmApplicatorTest extends BaseApplicatorTestCase
    implements DisplayAlarmConstants {
    private static final Log log =
        LogFactory.getLog(DisplayAlarmApplicatorTest.class);

    public void testApplyField() throws Exception {
        NoteItem noteItem = new MockNoteItem();
        EventStamp eventStamp = new MockEventStamp(noteItem);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime(true));
        noteItem.addStamp(eventStamp);
        
        EimRecord record = makeTestRecord();

        DisplayAlarmApplicator applicator =
            new DisplayAlarmApplicator(noteItem);
        applicator.applyRecord(record);

        Period period = new Period((DateTime) eventStamp.getStartDate(), new Dur("PT15M"));
        
        Assert.assertEquals(eventStamp.getDisplayAlarmDescription(), "My alarm");
        Assert.assertEquals(eventStamp.getDisplayAlarmTrigger().getValue(), "PT15M");
        Assert.assertEquals(eventStamp.getDisplayAlarmDuration().toString(), "P1W");
        Assert.assertEquals(eventStamp.getDisplayAlarmRepeat(), new Integer(1));
        
        // verify that NoteItem.reminderTime was updated
        Assert.assertNotNull(noteItem.getReminderTime());
        // it should be the end date of the period of eventStart,trigger duration
        Assert.assertEquals(period.getEnd().getTime(), noteItem.getReminderTime().getTime());
    
        // test removing alarm from event
        record = makeTestNoneRecord();

        applicator.applyRecord(record);

        Assert.assertNull(noteItem.getReminderTime());
        Assert.assertNull(eventStamp.getDisplayAlarm());
    }
    
    public void testApplyBogusRecord() throws Exception {
        NoteItem noteItem = new MockNoteItem();
        EventStamp eventStamp = new MockEventStamp(noteItem);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime(true));
        noteItem.addStamp(eventStamp);
        
        EimRecord record = makeTestRecordNoTrigger();

        DisplayAlarmApplicator applicator =
            new DisplayAlarmApplicator(noteItem);
        try {
            applicator.applyRecord(record);
            Assert.fail("able to apply bogus record");
        } catch (EimValidationException e) {}
    }
    
    public void testApplyFieldNonEvent() throws Exception {
        NoteItem noteItem = new MockNoteItem();
        EimRecord record = makeTestRecordNonEvent();

        DisplayAlarmApplicator applicator =
            new DisplayAlarmApplicator(noteItem);
        applicator.applyRecord(record);

        Assert.assertNotNull(noteItem.getReminderTime());
        Assert.assertEquals(noteItem.getReminderTime(), new DateTime("20080101T075900Z"));
    
        record = makeTestNoneRecord();

        applicator.applyRecord(record);

        Assert.assertNull(noteItem.getReminderTime());
    }
    
    public void testApplyMissingField() throws Exception {
        NoteItem masterNote = new MockNoteItem();
        EventStamp masterEvent = new MockEventStamp(masterNote);
        masterEvent.createCalendar();
        masterEvent.creatDisplayAlarm();
        masterEvent.setDisplayAlarmDescription("My alarm");
        masterEvent.setDisplayAlarmDuration(new Dur("P1W"));
        masterEvent.setDisplayAlarmTrigger(EimValueConverter.toIcalTrigger("-PT15M"));
        masterEvent.setDisplayAlarmRepeat(1);
        
        masterNote.addStamp(masterEvent);
        
        NoteItem modNote = new MockNoteItem();
        EventExceptionStamp modEvent = new MockEventExceptionStamp(modNote);
        modEvent.createCalendar();
        modNote.setModifies(masterNote);
        modNote.addStamp(modEvent);
        
        EimRecord record = makeTestMissingRecord();

        DisplayAlarmApplicator applicator =
            new DisplayAlarmApplicator(modNote);
        applicator.applyRecord(record);

        modEvent.getEventCalendar().validate(true);
        
        Assert.assertNull(modEvent.getDisplayAlarmDescription());
        Assert.assertNull(modEvent.getDisplayAlarmTrigger());
        Assert.assertNull(modEvent.getDisplayAlarmDuration());
        Assert.assertNull(modEvent.getDisplayAlarmRepeat());
    }
    
    private EimRecord makeTestRecord() {
        EimRecord record = new EimRecord(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);

        record.addField(new TextField(FIELD_DESCRIPTION, "My alarm"));
        record.addField(new TextField(FIELD_TRIGGER, "PT15M"));
        record.addField(new TextField(FIELD_DURATION, "P1W"));
        record.addField(new IntegerField(FIELD_REPEAT, 1));

        return record;
    }
    
    private EimRecord makeTestRecordNoTrigger() {
        EimRecordSet set = new EimRecordSet();
        set.setUuid("bogus");
        EimRecord record = new EimRecord(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);

        record.addField(new TextField(FIELD_DESCRIPTION, "My alarm"));
        record.addField(new TextField(FIELD_DURATION, "P1W"));
        record.addField(new IntegerField(FIELD_REPEAT, 1));

        record.setRecordSet(set);
        
        return record;
    }
    
    
    private EimRecord makeTestNoneRecord() {
        EimRecord record = new EimRecord(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);

        record.addField(new TextField(FIELD_DESCRIPTION, null));
        record.addField(new TextField(FIELD_TRIGGER, null));
        record.addField(new TextField(FIELD_DURATION, null));
        record.addField(new IntegerField(FIELD_REPEAT, null));

        return record;
    }
    
    private EimRecord makeTestRecordNonEvent() {
        EimRecord record = new EimRecord(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);

        record.addField(new TextField(FIELD_DESCRIPTION, null));
        record.addField(new TextField(FIELD_TRIGGER, ";VALUE=DATE-TIME:20080101T075900Z"));
        record.addField(new TextField(FIELD_DURATION, null));
        record.addField(new IntegerField(FIELD_REPEAT, 1));

        return record;
    }
    
    private EimRecord makeTestMissingRecord() {
        EimRecord record = new EimRecord(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);
        addMissingTextField(FIELD_DESCRIPTION, record);
        addMissingTextField(FIELD_TRIGGER, record);
        addMissingTextField(FIELD_DURATION, record);
        addMissingIntegerField(FIELD_REPEAT, record);
        return record;
    }
    
    
}
