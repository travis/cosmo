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
package org.osaf.cosmo.eim.schema.event.modification;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;

import org.apache.commons.io.IOUtils;
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
 * Applies event modification EIM records to and EventStamp.
 *
 * @see EventStamp
 */
public class EventModificationApplicator extends BaseStampApplicator
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(EventModificationApplicator.class);

    /** */
    public EventModificationApplicator(Item item) {
        super(PREFIX_EVENT_MODIFICATION, NS_EVENT_MODIFICATION, item);
    }
    
    @Override
    protected void applyDeletion(EimRecord record) throws EimSchemaException {
        if(getEventStamp()==null)
            throw new EimSchemaException("EventStamp required");
        
        getEventStamp().removeModification(getRecurrenceId(record));
    }

    @Override
    public void applyRecord(EimRecord record) throws EimSchemaException {
        if(getEventStamp()==null)
            throw new EimSchemaException("EventStamp required");
        
        // retrieve key field (recurrenceId)
        Date recurrenceId = getRecurrenceId(record);
        
        // retrieve modification (one will be created if it doesn't exist)
        VEvent modification = getModification(recurrenceId);
        
        for (EimRecordField field : record.getFields()) {
            if(field.getName().equals(FIELD_DTSTART))
                applyStartDate(field, modification);
            else if(field.getName().equals(FIELD_DTEND))
                applyEndDate(field,modification);
            else if (field.getName().equals(FIELD_LOCATION))
                applyLocation(field, modification);
            else if(field.getName().equals(FIELD_STATUS))
                applyStatus(field, modification);
            else if(field.getName().equals(FIELD_BODY))
                applyBody(field, modification);
            else if(field.getName().equals(FIELD_DISPLAY_NAME))
                applyDisplayName(field, modification);
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
        return null;
    }

    private Date getRecurrenceId(EimRecord record) throws EimSchemaException {
        // recurrenceId is a key field
        EimRecordKey key = record.getKey();
        for(EimRecordField keyField: key.getFields()) {
            if(keyField.getName().equals(FIELD_RECURRENCE_ID)) {
                String value = EimFieldValidator.validateText(keyField,
                        MAXLEN_RECURRENCE_ID);
                return EimValueConverter.toICalDate(value).getDate();
            }
        }
        
        throw new EimSchemaException("key field " + FIELD_RECURRENCE_ID
                + " required");
    }
    
    //  Apply a startDate field to the event stamp
    private void applyStartDate(EimRecordField field, VEvent modification) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field, MAXLEN_DTSTART);
        modification.getStartDate().setDate(EimValueConverter.toICalDate(value).getDate());
    }
    
    //  Apply an endDate field to the event stamp
    private void applyEndDate(EimRecordField field, VEvent modification) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field, MAXLEN_DTEND);
        modification.getEndDate().setDate(EimValueConverter.toICalDate(value).getDate());
    }
    
    //  Apply a status field to the event stamp
    private void applyStatus(EimRecordField field, VEvent modification) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field, MAXLEN_STATUS);
        Status status = (Status) modification.getProperties().getProperty(
                Property.STATUS);
        if (value == null) {
            if (status != null)
                modification.getProperties().remove(status);
            return;
        }
        if (status == null) {
            status = new Status();
            modification.getProperties().add(status);
        }
        status.setValue(value);
    }
    
    //  Apply a location field to the event stamp
    private void applyLocation(EimRecordField field, VEvent modification) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field,
                MAXLEN_LOCATION);
        Location location = (Location) modification.getProperties()
                .getProperty(Property.LOCATION);
        if (value == null) {
            if (location != null)
                modification.getProperties().remove(location);
        }
        if (location == null) {
            location = new Location();
            modification.getProperties().add(location);
        }
        location.setValue(value);
    }
    
    // Apply a displayName field to the event stamp
    private void applyDisplayName(EimRecordField field, VEvent modification) throws EimSchemaException {
        String value = EimFieldValidator.validateText(field, MAXLEN_DISPLAY_NAME);
        Summary summary = (Summary) modification.getProperties()
                .getProperty(Property.SUMMARY);
        if (value == null) {
            if (summary != null)
                modification.getProperties().remove(summary);
        }
        if (summary == null) {
            summary = new Summary();
            modification.getProperties().add(summary);
        }
        
        summary.setValue(value);
    }
    
    // Apply a body field to the event stamp
    private void applyBody(EimRecordField field, VEvent modification) throws EimSchemaException {
        Reader value = EimFieldValidator.validateClob(field);
        Description description = (Description) modification.getProperties()
                .getProperty(Property.DESCRIPTION);
        if (value == null) {
            if (description != null)
                modification.getProperties().remove(description);
        }
        if (description == null) {
            description = new Description();
            modification.getProperties().add(description);
        }
        
        // Read body
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(value, writer);
        } catch (IOException e) {
            log.error("error reading string value", e);
            throw new EimSchemaException("error reading clob");
        }
        description.setValue(writer.toString()); 
    }
    
    // Retrieve event modification if it exists in the event stamp,
    // or create one
    private VEvent getModification(Date date) {
        EventStamp event = getEventStamp();
        VEvent modification = event.getModification(date);
        if (modification != null)
            return modification;

        // create new VEVENT, and initialize UID, RECURRENCE-ID
        modification = new VEvent();
        RecurrenceId recurrenceId = new RecurrenceId();
        recurrenceId.setDate(date);

        modification.getProperties()
                .add(event.getMasterEvent().getUid().copy());
        modification.getProperties().add(recurrenceId);

        // copy SUMMARY, DESCRIPTION from master event
        Property prop = event.getMasterEvent().getProperties().getProperty(
                Property.SUMMARY);
        if (prop != null)
            modification.getProperties().add(prop);
        prop = event.getMasterEvent().getProperties().getProperty(
                Property.DESCRIPTION);
        if (prop != null)
            modification.getProperties().add(prop);

        getEventStamp().addModification(modification);
        return modification;
    }
    
    private EventStamp getEventStamp() {
        return EventStamp.getStamp(getItem());
    }
}
