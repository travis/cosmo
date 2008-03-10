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

import java.text.ParseException;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseStampApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.eim.schema.text.DurationFormat;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampUtils;

/**
 * Applies display alarm EIM records to an EventStamp/NoteItem.
 * A deleted displayAlarm record is the same as sending a
 * trigger value of 'None'.
 *
 * @see BaseEventStamp
 */
public class DisplayAlarmApplicator extends BaseStampApplicator
    implements DisplayAlarmConstants {
    private static final Log log =
        LogFactory.getLog(DisplayAlarmApplicator.class);

    /** */
    public DisplayAlarmApplicator(Item item) {
        super(PREFIX_DISPLAY_ALARM, NS_DISPLAY_ALARM, item);
        setStamp(StampUtils.getBaseEventStamp(item));
    }
    
    @Override
    protected void applyDeletion(EimRecord record) throws EimSchemaException {
        BaseEventStamp eventStamp = getEventStamp();    
        
        // Require event to continue
        if(eventStamp==null)
            throw new EimSchemaException("No alarm to delete");
        
        eventStamp.removeDisplayAlarm();
        
        // remove reminder on NoteItem
        applyDeletionNonEvent(record);
    }
    
    protected void applyDeletionNonEvent(EimRecord record) throws EimSchemaException {
        NoteItem note = (NoteItem) getItem();
        note.setReminderTime(null);
    }

    @Override
    public void applyRecord(EimRecord record) throws EimSchemaException {
        // stamp could have been added before record is processed
        setStamp(StampUtils.getBaseEventStamp(getItem()));
        
        // If item is not an event, then process differently
        if(getEventStamp()==null) {
            applyRecordNonEvent(record);
            return;
        }
        
        // handle deletion
        if (record.isDeleted()) {
            applyDeletion(record);
            return;
        }
        
        BaseEventStamp eventStamp = getEventStamp();
        NoteItem note = (NoteItem) getItem();
        
        // keep track of whether alarm existed before
        boolean newAlarm = false;
        // keep track of whether trigger field is present
        boolean triggerPresent = false;
        
        // create display alarm if it doesn't exist
        if(eventStamp.getDisplayAlarm()==null) {
            eventStamp.creatDisplayAlarm();
            newAlarm = true;
        }
            
        for (EimRecordField field : record.getFields()) {
            if(field.getName().equals(FIELD_DESCRIPTION)) {
                if(field.isMissing()) {
                    handleMissingAttribute("displayAlarmDescription");
                }
                else {
                    String value = EimFieldValidator.validateText(field, MAXLEN_DESCRIPTION);
                    eventStamp.setDisplayAlarmDescription(value);
                }
            }
            else if(field.getName().equals(FIELD_TRIGGER)) {
                triggerPresent = true;
                if(field.isMissing()) {
                    handleMissingAttribute("displayAlarmTrigger");
                }
                else {
                    String value = EimFieldValidator.validateText(field, MAXLEN_TRIGGER);
                    // Trigger=None = no alarm
                    if(value==null) {
                        applyDeletion(record);
                        return;
                    }
                    
                    Trigger newTrigger = EimValueConverter.toIcalTrigger(value);
                    
                    eventStamp.setDisplayAlarmTrigger(newTrigger);
                    setReminderTime(note, getEventStamp(), newTrigger);
                }
            }
            else if (field.getName().equals(FIELD_DURATION)) {
                if(field.isMissing()) {
                    handleMissingAttribute("displayAlarmDuration");
                }
                else {
                    String value = EimFieldValidator.validateText(field, MAXLEN_DURATION);
                    try {
                        Dur dur = DurationFormat.getInstance().parse(value);
                        eventStamp.setDisplayAlarmDuration(dur);
                    } catch (ParseException e) {
                        throw new EimValidationException(
                                "Illegal duration for item "
                                        + record.getRecordSet().getUuid(), e);
                    }
                }
            }
            else if(field.getName().equals(FIELD_REPEAT)) {
                if(field.isMissing()) {
                    handleMissingAttribute("displayAlarmRepeat");
                }
                else {
                    Integer value = EimFieldValidator.validateInteger(field);
                    if(value!=null && value.intValue()==0)
                        value = null;
                    eventStamp.setDisplayAlarmRepeat(value);
                }
            }
            else
                log.warn("usupported eim field " + field.getName()
                        + " found in " + record.getNamespace());
        }
        
        // Make sure trigger is present for new alarms
        if (newAlarm && !triggerPresent)
            throw new EimValidationException(
                    "Trigger must be specified for item "
                            + record.getRecordSet().getUuid());

    }
    
    public void applyRecordNonEvent(EimRecord record) throws EimSchemaException {
        
        // handle deletion
        if (record.isDeleted()) {
            applyDeletionNonEvent(record);
            return;
        }
        
        NoteItem note = (NoteItem) getItem();
            
        for (EimRecordField field : record.getFields()) {
            if(field.getName().equals(FIELD_DESCRIPTION)) {
                // ignore, don't support
            }
            else if(field.getName().equals(FIELD_TRIGGER)) {
                
                if(field.isMissing()) {
                    // NOTE: We dont' really support missing trigger for non-events
                    // So we should never get here.  For now treat "missing" as delete.
                    log.debug("tried to apply missing Trigger on non-event");
                    applyDeletionNonEvent(record);
                }
                else {
                    String value = EimFieldValidator.validateText(field, MAXLEN_TRIGGER);
                    // Trigger=None means no alarm
                    if(value==null) {
                        applyDeletionNonEvent(record);
                        return;
                    }
                    
                    Trigger trigger = EimValueConverter.toIcalTrigger(value);
                        
                    setReminderTime(note, trigger);
                }
            }
            else if (field.getName().equals(FIELD_DURATION)) {
                // ignore, don't support
            }
            else if(field.getName().equals(FIELD_REPEAT)) {
                // ignore, don't support
            }
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
    protected Stamp createStamp(EimRecord record) throws EimSchemaException {
        // do nothing as the stamp should already be created
        return null;
    }
        
    
    private BaseEventStamp getEventStamp() {
        return StampUtils.getBaseEventStamp(getItem());
    }
    
    private void setReminderTime(NoteItem note, BaseEventStamp eventStamp, Trigger trigger) {
        
        // If there is no duration, then reminderTime will be the
        // trigger datetime
        if(trigger.getDuration()==null) {
            note.setReminderTime(trigger.getDateTime());
            return;
        }
        
        // Calculate reminderTime based on event start and trigger duration
        Date start = eventStamp.getStartDate();
        if(!(start instanceof DateTime))
            start = new DateTime(start);
            
        Period period = new Period((DateTime) start, trigger.getDuration());
        note.setReminderTime(period.getEnd());        
    }
    
    private void setReminderTime(NoteItem note, Trigger trigger)
            throws EimSchemaException {
        // non events only support absolute triggers
        if (trigger.getDateTime() != null) {
            note.setReminderTime(trigger.getDateTime());
        } else {
            throw new EimSchemaException(
                    "trigger for non event must be absolute");
        }
    }
}
