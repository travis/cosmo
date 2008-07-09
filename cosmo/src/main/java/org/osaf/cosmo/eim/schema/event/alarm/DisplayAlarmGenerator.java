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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;

/**
 * Generates EIM records from event stamps.
 *
 * @see BaseEventStamp
 */
public class DisplayAlarmGenerator extends BaseStampGenerator
    implements DisplayAlarmConstants {
    private static final Log log =
        LogFactory.getLog(DisplayAlarmGenerator.class);
    
    private static final HashSet<String> STAMP_TYPES = new HashSet<String>(2);
    
    public static final String DEFAULT_DESCRIPTION = "Event Reminder";
    
    static {
        STAMP_TYPES.add("event");
        STAMP_TYPES.add("eventexception");
    }
    
    /** */
    public DisplayAlarmGenerator(Item item) {
        super(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM, item);
        setStamp(StampUtils.getBaseEventStamp(item));
    }

    @Override
    protected Set<String> getStampTypes() {
        return STAMP_TYPES;
    }

    @Override
    public List<EimRecord> generateRecords(long timestamp) {
        BaseEventStamp stamp = (BaseEventStamp) getStamp();
        
        // use super class if item is an event
        if (stamp != null && stamp.getDisplayAlarm()!=null)
            return super.generateRecords(timestamp);
        
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
       
        // Otherwise overide for non-event records, where we
        // pull the alarm from NoteItem.reminderTime
        addRecordsNonEvent(records);
        return records;
    }
    
    /**
     * Adds records representing the event's display
     * alarm (if one exists).
     */
    protected void addRecords(List<EimRecord> records) {
        EimRecord record = new EimRecord(getPrefix(), getNamespace());
        addKeyFields(record);
        addFields(record);
        records.add(record);
    }
    
    protected void addRecordsNonEvent(List<EimRecord> records) {
        EimRecord record = new EimRecord(getPrefix(), getNamespace());
        addKeyFields(record);
        addFieldsNonEvent(record);
        records.add(record);
    }
    
    /**
     * Adds a key field for uuid.
     */
    protected void addKeyFields(EimRecord record) {
        record.addKeyField(new TextField(FIELD_UUID, getItem().getUid()));
    }

    private void addFields(EimRecord record) {
        BaseEventStamp stamp = (BaseEventStamp) getStamp();

        if(isMissingAttribute("displayAlarmDescription")) {
            record.addField(generateMissingField(new TextField(FIELD_DESCRIPTION, null)));
        } else {
            String value = stamp.getDisplayAlarmDescription(); 
            record.addField(new TextField(FIELD_DESCRIPTION, value));
        }
         
        if(isMissingAttribute("displayAlarmTrigger")) {
            record.addField(generateMissingField(new TextField(FIELD_TRIGGER, null)));
        } else {
            String value = EimValueConverter.fromIcalTrigger(stamp.getDisplayAlarmTrigger());
            record.addField(new TextField(FIELD_TRIGGER, value));
        }

        if(isMissingAttribute("displayAlarmDuration")) {
            record.addField(generateMissingField(new TextField(FIELD_DURATION, null)));
        } else {
            Dur dur = stamp.getDisplayAlarmDuration(); 
            record.addField(new TextField(FIELD_DURATION, (dur==null) ? null : dur.toString()));
        }
        
        if(isMissingAttribute("displayAlarmRepeat")) {
            record.addField(generateMissingField(new IntegerField(FIELD_REPEAT, 0)));
        } else {
            record.addField(new IntegerField(FIELD_REPEAT, stamp.getDisplayAlarmRepeat()));
        }
        
        record.addFields(generateUnknownFields());
    }
    
    private void addFieldsNonEvent(EimRecord record) {
        NoteItem noteItem = (NoteItem) getItem();
        boolean hasAlarm = noteItem.getReminderTime()!=null;
        
        if(hasAlarm)
            record.addField(new TextField(FIELD_DESCRIPTION, DEFAULT_DESCRIPTION));
        else
            record.addField(new TextField(FIELD_DESCRIPTION, null));
        
        // TODO: fix to generate isMissing
        if(!hasAlarm) {
            record.addField(new TextField(FIELD_TRIGGER, null));
        }
        else {
            DateTime dt = new DateTime(true);
            dt.setTime(noteItem.getReminderTime().getTime());
            record.addField(new TextField(FIELD_TRIGGER, EimValueConverter.formatTriggerFromDateTime(dt)));
        }
        
        record.addField(new TextField(FIELD_DURATION, null));
        record.addField(new IntegerField(FIELD_REPEAT, null));

        record.addFields(generateUnknownFields());
    }
}
