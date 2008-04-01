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

import java.text.ParseException;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.ICalDate;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseStampApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.eim.schema.text.DurationFormat;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampUtils;

/**
 * Applies EIM records to event stamps.
 *
 * @see EventStamp
 */
public class EventApplicator extends BaseStampApplicator
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(EventApplicator.class);

    /** */
    public EventApplicator(Item item) {
        super(PREFIX_EVENT, NS_EVENT, item);
        setStamp(StampUtils.getBaseEventStamp(item));
    }

    /**
     * Creates and returns a stamp instance that can be added by
     * <code>BaseStampApplicator</code> to the item. Used when a
     * stamp record is applied to an item that does not already have
     * that stamp.
     */
    protected Stamp createStamp(EimRecord record) throws EimSchemaException {
        BaseEventStamp eventStamp = null;
        NoteItem note = (NoteItem) getItem();
        
        // Create master event stamp, or event exception stamp
        if(note.getModifies()==null) {
            eventStamp = getItem().getFactory().createEventStamp(note);
            getItem().addStamp(eventStamp);
            eventStamp.createCalendar();
        }
        else {
            eventStamp = getItem().getFactory().createEventExceptionStamp(note);
            getItem().addStamp(eventStamp);
            eventStamp.createCalendar();
            
            EventStamp masterEventStamp = ((EventExceptionStamp) eventStamp).getMasterStamp();
            
            ModificationUid modUid = new ModificationUid(note.getUid());
            Date recurrenceId = modUid.getRecurrenceId();
            
            // If recurrenceId is a DateTime with a timezone (will be UTC)
            // store the recurrenceId in same timezone as master event's 
            // date properties.
            if(recurrenceId instanceof DateTime) {
                // Get master's start date
                Date masterStart = masterEventStamp != null ? masterEventStamp
                        .getStartDate() : null;
                
                // Master's start date must be a DateTime also
                if((masterStart != null && masterStart instanceof DateTime) ) {
                    DateTime modDt = (DateTime) recurrenceId;
                    DateTime masterDt = (DateTime) masterStart;
                    
                    // If the modification's RECURRENCE-ID is in UTC and the master's 
                    // isn't, then update the modification's timezone to be the same
                    // as the master.  Although its technically legal to specify date
                    // properties in different timezones, some clients don't like this.
                    if(modDt.isUtc() && !masterDt.isUtc()) 
                        modDt.setTimeZone(masterDt.getTimeZone());
                }
            }
            eventStamp.setRecurrenceId(modUid.getRecurrenceId());
            
            // default dtstart to recurrenceId
            eventStamp.setStartDate(modUid.getRecurrenceId());
        }
        
        // need to copy reminderTime to alarm in event
        if(note.getReminderTime()!=null) {
            eventStamp.creatDisplayAlarm();
            eventStamp.setDisplayAlarmDescription("display alarm");
            DateTime dt = new DateTime(true);
            dt.setTime(note.getReminderTime().getTime());
            eventStamp.setDisplayAlarmTriggerDate(dt);
        }
        
        return eventStamp;
    }

    @Override
    public void applyRecord(EimRecord record) throws EimSchemaException {
        super.applyRecord(record);
        
        // Ensure that a startDate, duration are present
        if(!record.isDeleted()) {
            BaseEventStamp event = (BaseEventStamp) getStamp();
            if(event.getStartDate()==null)
                throw new EimValidationException("field " + FIELD_DTSTART + " is required");
            
            if(event instanceof EventStamp) {
                if(event.getDuration()==null)
                    throw new EimValidationException("field " + FIELD_DURATION + " is required");
            }
        }
    }

    /**
     * Copies record field values to stamp properties and
     * attributes.
     *
     * @throws EimValidationException if the field value is invalid
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the event 
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        BaseEventStamp event = (BaseEventStamp) getStamp();

        if (field.getName().equals(FIELD_DTSTART)) {
            if(field.isMissing()) {
                handleMissingDtStart();
            }
            else {
                // Handle the case where there is an existing dtend (no duration)
                // and only dtstart is present in the record.  Calculate old
                // duration and reset it on event to avoid conflicts.
                if(event.getEndDate() != null && !hasDurationField(field.getRecord()))    
                    event.setDuration(new Dur(event.getStartDate(), event.getEndDate())); 
                
                String value =
                    EimFieldValidator.validateText(field, MAXLEN_DTSTART);
                ICalDate icd = EimValueConverter.toICalDate(value);
                event.setStartDate(icd.getDate());
                event.setAnyTime(icd.isAnyTime());
            }
        } else if (field.getName().equals(FIELD_DURATION)) {
            if(field.isMissing()) {
                handleMissingAttribute("duration");
            } else {
                String value =
                    EimFieldValidator.validateText(field, MAXLEN_DURATION);
                try {
                    Dur dur = DurationFormat.getInstance().parse(value);
                    
                    // Duration must be positive
                    if(dur!=null && dur.isNegative())
                        throw new EimValidationException("Illegal duration " + value);
                    
                    event.setDuration(dur);
                } catch (ParseException e) {
                    throw new EimValidationException("Illegal duration " + value, e);
                }
            }
        } else if (field.getName().equals(FIELD_LOCATION)) {
            if(field.isMissing()) {
                handleMissingAttribute("location");
            }
            else {
                String value =
                    EimFieldValidator.validateText(field, MAXLEN_LOCATION);
                event.setLocation(value);
            }
        } else if (field.getName().equals(FIELD_RRULE)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_RRULE);
            event.setRecurrenceRules(EimValueConverter.toICalRecurs(value));
        } else if (field.getName().equals(FIELD_EXRULE)) {
            String value =
                EimFieldValidator.validateText(field, MAXLEN_EXRULE);
            event.setExceptionRules(EimValueConverter.toICalRecurs(value));
        } else if (field.getName().equals(FIELD_RDATE)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_RDATE);
            ICalDate icd = EimValueConverter.toICalDate(value);
            event.setRecurrenceDates(icd != null ? icd.getDateList() : null);
        } else if (field.getName().equals(FIELD_EXDATE)) {
            String value =
                EimFieldValidator.validateText(field, MAXLEN_EXDATE);
            ICalDate icd = EimValueConverter.toICalDate(value);
            event.setExceptionDates(icd != null ? icd.getDateList() : null);
        } else if (field.getName().equals(FIELD_STATUS)) {
            if(field.isMissing()) {
                handleMissingAttribute("status");
            }
            else {
                String value =
                    EimFieldValidator.validateText(field, MAXLEN_STATUS);
                event.setStatus(value);
            }
        } else {
            // Update timestamp of stamp so that event record will be 
            // serialized next sync
            getStamp().updateTimestamp();
            applyUnknownField(field);
        }
    }
    
    private boolean hasDurationField(EimRecord record) {
        for(EimRecordField field : record.getFields()) {
            if(field.getName().equals(FIELD_DURATION))
                return true;
        }
        return false;
    }
    
    private void handleMissingDtStart() throws EimSchemaException {
        checkIsModification();
        // A missing dtstart on a modification means that the start date
        // is equal to the recurrenceId
        BaseEventStamp event = (BaseEventStamp) getStamp();
        event.setStartDate(event.getRecurrenceId());
        
        // A missing anyTime is set using null
        event.setAnyTime(null);
    }
}
