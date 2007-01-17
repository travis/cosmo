/*
 * Copyright 2007 Open Source Applications Foundation
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

import java.util.Iterator;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.EimRecordKey;
import org.osaf.cosmo.eim.schema.BaseStampApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.eim.schema.event.EventConstants;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;

/**
 * Applies display alarm EIM records to an EventStamp.
 *
 * @see EventStamp
 */
public class DisplayAlarmApplicator extends BaseStampApplicator
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(DisplayAlarmApplicator.class);

    /** */
    public DisplayAlarmApplicator(Item item) {
        super(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM, item);
    }
    
    @Override
    protected void applyDeletion(EimRecord record) throws EimSchemaException {
        VEvent event = getEvent(record);
        
        if(event==null)
            throw new EimSchemaException("no event found for alarm");
        
        VAlarm alarm = getDisplayAlarm(event);
        
        // remove alarm
        if(alarm != null)
            event.getAlarms().remove(alarm);
    }

    @Override
    public void applyRecord(EimRecord record) throws EimSchemaException {
        VEvent event = getEvent(record);
        
        if(event==null)
            throw new EimSchemaException("no event found for alarm");
        
        VAlarm alarm = getOrCreateDisplayAlarm(event);
            
        for (EimRecordField field : record.getFields()) {
            if(field.getName().equals(FIELD_DESCRIPTION))
                applyDescription(field, alarm);
            else if(field.getName().equals(FIELD_TRIGGER))
                applyTrigger(field,alarm);
            else if (field.getName().equals(FIELD_DURATION))
                applyDuration(field, alarm);
            else if(field.getName().equals(FIELD_REPEAT))
                applyRepeat(field, alarm);
            else
                log.warn("usupported eim field " + field.getName()
                        + " found in " + record.getNamespace());
        }
    }
    
    @Override
    protected void applyField(EimRecordField field) throws EimSchemaException {
        // do nothing beause we override applyRecord()
    }
    
    @Override
    protected Stamp createStamp() {
        // do nothing as the stamp should already be created
        return null;
    }

    /**
     * get the recurrenceId key field from the eim record
     */
    private Date getRecurrenceId(EimRecord record) throws EimSchemaException {
        // recurrenceId is a key field
        EimRecordKey key = record.getKey();
        for(EimRecordField keyField: key.getFields()) {
            if(keyField.getName().equals(FIELD_RECURRENCE_ID)) {
                String value = EimFieldValidator.validateText(keyField,
                        MAXLEN_RECURRENCE_ID);
                return EimValueConverter.toICalDate(value);
            }
        }
        return null;
    }
        
    /**
     * Apply a description field to the alarm
     */
    private void applyDescription(EimRecordField field, VAlarm alarm) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field, MAXLEN_DESCRIPTION);
        
        Description description = (Description) alarm.getProperties()
                .getProperty(Property.DESCRIPTION);
        if (value == null) {
            if (description != null)
                alarm.getProperties().remove(description);
        }
        if (description == null) {
            description = new Description();
            alarm.getProperties().add(description);
        }
        
        description.setValue(value);
    }
     
    /**
     * Apply a trigger field to the alarm
     */
    private void applyTrigger(EimRecordField field, VAlarm alarm) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field, MAXLEN_TRIGGER);
        Trigger newTrigger = EimValueConverter.toIcalTrigger(value);
        
        Trigger oldTrigger = (Trigger) alarm.getProperties()
                .getProperty(Property.TRIGGER);
        if (oldTrigger != null)
            alarm.getProperties().remove(oldTrigger);
        
        alarm.getProperties().add(newTrigger);
    }
    
    /**
     * Apply a duration field to the alarm
     */
    private void applyDuration(EimRecordField field, VAlarm alarm) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field, MAXLEN_DURATION);
       
        Duration duration = (Duration) alarm.getProperties()
                .getProperty(Property.DURATION);
        if (value == null) {
            if (duration != null)
                alarm.getProperties().remove(duration);
        }
        if (duration == null) {
            duration = new Duration();
            alarm.getProperties().add(duration);
        }
        
        duration.setDuration(EimValueConverter.toICalDur(value));
    }
    
    /**
     * Apply a repeat field to the alarm
     */
    private void applyRepeat(EimRecordField field, VAlarm alarm) throws EimSchemaException {
        Integer value = EimFieldValidator.validateInteger(field);
       
        Repeat repeat = (Repeat) alarm.getProperties()
                .getProperty(Property.REPEAT);
        if (value == null) {
            if (repeat != null)
                alarm.getProperties().remove(repeat);
        }
        if (repeat == null) {
            repeat = new Repeat();
            alarm.getProperties().add(repeat);
        }
     
        repeat.setCount(value.intValue());
    }
    
    /**
     * get the current display alarm, or create a new one
     */
    private VAlarm getOrCreateDisplayAlarm(VEvent event) {
        VAlarm alarm = getDisplayAlarm(event);
        if(alarm==null)
            alarm =  creatDisplayAlarm(event);
        return alarm;
    }
    
    /**
     * create new display alarm and add to event
     */
    private VAlarm creatDisplayAlarm(VEvent event) {
        VAlarm alarm = new VAlarm();
        alarm.getProperties().add(Action.DISPLAY);
        event.getAlarms().add(alarm);
        return alarm;
    }
    
    /**
     * Get the first display alarm from an event, or create one
     */
    private VAlarm getDisplayAlarm(VEvent event) {
        VAlarm alarm = null;
        
        // Find the first display alarm
        for(Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm currAlarm = (VAlarm) it.next();
            if (currAlarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY))
                alarm = currAlarm;
        }
        
        return alarm;
    }
    
    /**
     * Get the event associated with the displayAlarm record.
     * This is either the master event (no recurrenceId present)
     * or a modification (recurrenceId present).
     */
    private VEvent getEvent(EimRecord record) throws EimSchemaException {
        if(getEventStamp()==null)
            throw new EimSchemaException("EventStamp required");
        
        // retrieve key field (recurrenceId)
        Date recurrenceId = getRecurrenceId(record);
        
        if(recurrenceId==null)
            return getEventStamp().getMasterEvent();
        else
            return getEventStamp().getModification(recurrenceId);
        
    }
    
    private EventStamp getEventStamp() {
        return EventStamp.getStamp(getItem());
    }
}
