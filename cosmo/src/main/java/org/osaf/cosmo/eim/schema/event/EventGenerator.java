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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;

/**
 * Generates EIM records from event stamps.
 *
 * @see EventStamp
 */
public class EventGenerator extends BaseStampGenerator
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(EventGenerator.class);

    /** */
    public EventGenerator(Item item) {
        super(PREFIX_EVENT, NS_EVENT, item);
        setStamp(EventStamp.getStamp(item));
    }

    /**
     * Copies event properties and attributes into a event record.
     */
    public List<EimRecord> generateRecords() {
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        EventStamp event = (EventStamp) getStamp();
        if (event == null)
            return records;

        EimRecord master = new EimRecord(getPrefix(), getNamespace());
        String value = null;

        master.addKeyField(new TextField(FIELD_UUID, event.getItem().getUid()));

        value = EimValueConverter.fromICalDate(event.getStartDate(),
                                               event.isAnyTime());
        master.addField(new TextField(FIELD_DTSTART, value));
                                      
        value = EimValueConverter.fromICalDate(event.getEndDate());
        master.addField(new TextField(FIELD_DTEND, value));

        master.addField(new TextField(FIELD_LOCATION, event.getLocation()));

        value = EimValueConverter.fromICalRecurs(event.getRecurrenceRules());
        master.addField(new TextField(FIELD_RRULE, value));

        value = EimValueConverter.fromICalRecurs(event.getExceptionRules());
        master.addField(new TextField(FIELD_EXRULE, value));

        value = EimValueConverter.fromICalDates(event.getRecurrenceDates());
        master.addField(new TextField(FIELD_RDATE, value));

        value = EimValueConverter.fromICalDates(event.getExceptionDates());
        master.addField(new TextField(FIELD_EXDATE, value));

        master.addField(new TextField(FIELD_STATUS, event.getStatus()));

        master.addFields(generateUnknownFields());

        records.add(master);
        
        // generate alarm record;
        VAlarm alarm = event.getDisplayAlarm(null);
        if(alarm != null)
            records.add(generateAlarmRecord(alarm, null));

        // generate modification records
        for (VEvent override : event.getModifications()) {
            EimRecord overrideRec = new EimRecord(PREFIX_EVENT_MODIFICATION,
                    NS_EVENT_MODIFICATION);
            overrideRec.addKeyField(new TextField(FIELD_UUID, event.getItem()
                    .getUid()));

            value = EimValueConverter.fromICalDate(override.getReccurrenceId()
                    .getDate());
            overrideRec.addKeyField(new TextField(FIELD_RECURRENCE_ID, value));

            value = EimValueConverter.fromICalDate(override.getStartDate()
                    .getDate());
            overrideRec.addField(new TextField(FIELD_DTSTART, value));

            value = EimValueConverter.fromICalDate(override.getEndDate()
                    .getDate());
            overrideRec.addField(new TextField(FIELD_DTEND, value));

            Property prop = override.getProperties().getProperty(
                    Property.LOCATION);
            value = (prop == null) ? null : prop.getValue();
            overrideRec.addField(new TextField(FIELD_LOCATION, value));

            prop = override.getProperties().getProperty(Property.STATUS);
            value = (prop == null) ? null : prop.getValue();
            overrideRec.addField(new TextField(FIELD_STATUS, value));

            prop = override.getProperties().getProperty(Property.SUMMARY);
            value = (prop == null) ? null : prop.getValue();
            overrideRec.addField(new TextField(FIELD_DISPLAY_NAME, value));

            prop = override.getProperties().getProperty(Property.DESCRIPTION);
            value = (prop == null) ? null : prop.getValue();
            overrideRec.addField(new ClobField(FIELD_BODY, new StringReader(
                    value)));

            records.add(overrideRec);
            Date recurrenceId = override.getReccurrenceId().getDate();
            alarm = event.getDisplayAlarm(recurrenceId);
            
            if (alarm != null)
                records.add(generateAlarmRecord(alarm, recurrenceId));
        }

        return records;
    }
    
    /**
     * Create display alarm eim record.
     */
    private EimRecord generateAlarmRecord(VAlarm alarm, Date recurrenceId) {
        EventStamp event = (EventStamp) getStamp();
        
        EimRecord alarmRec = new EimRecord(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM);
        alarmRec.addKeyField(new TextField(FIELD_UUID, event.getItem().getUid()));

        if(recurrenceId!=null)
            alarmRec.addKeyField(new TextField(FIELD_RECURRENCE_ID, EimValueConverter.fromICalDate(recurrenceId)));
        
        Property prop = alarm.getProperties().getProperty(Property.DESCRIPTION);
        String value = (prop==null) ? null : prop.getValue();
        alarmRec.addField(new TextField(FIELD_DESCRIPTION, value));
        
        value = EimValueConverter.fromIcalTrigger((Trigger) alarm.getProperties().getProperty(Property.TRIGGER));
        alarmRec.addField(new TextField(FIELD_TRIGGER, value));
        
        prop = alarm.getProperties().getProperty(Property.DURATION);
        value = (prop==null) ? null : prop.getValue();
        alarmRec.addField(new TextField(FIELD_DURATION, value));
        
        prop = alarm.getProperties().getProperty(Property.REPEAT);
        value = (prop==null) ? null : prop.getValue();
        alarmRec.addField(new IntegerField(FIELD_REPEAT, new Integer(value))); 
        
        return alarmRec;
    }
    
}
