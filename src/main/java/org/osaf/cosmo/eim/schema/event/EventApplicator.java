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

import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.EventStamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Applies EIM records to event stamps.
 *
 * @see EventStamp
 */
public class EventApplicator extends BaseStampApplicator
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(EventApplicator.class);

    private EventStamp event;

    /** */
    public EventApplicator(EventStamp event) {
        super(PREFIX_EVENT, NS_EVENT, event);
        this.event = event;
    }

    /**
     * Copies record field values to event properties and
     * attributes.
     *
     * @throws EimValidationException if the field value is invalid
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the event 
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        if (field.getName().equals(FIELD_DTSTART)) {
            String value =
                EimFieldValidator.validateText(field, MAXLEN_DTSTART);
            event.setStartDate(EimValueConverter.toICalDate(value));
        } else if (field.getName().equals(FIELD_DTEND)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_DTEND);
            event.setEndDate(EimValueConverter.toICalDate(value));
        } else if (field.getName().equals(FIELD_LOCATION)) {
            String value =
                EimFieldValidator.validateText(field, MAXLEN_LOCATION);
            event.setLocation(value);
        } else if (field.getName().equals(FIELD_RRULE)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_RRULE);
            event.setRecurrenceRules(EimValueConverter.toICalRecurs(value));
        } else if (field.getName().equals(FIELD_EXRULE)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_EXRULE);
            event.setExceptionRules(EimValueConverter.toICalRecurs(value));
        } else if (field.getName().equals(FIELD_RDATE)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_RDATE);
            event.setRecurrenceDates(EimValueConverter.toICalDates(value));
        } else if (field.getName().equals(FIELD_EXDATE)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_EXDATE);
            event.setExceptionDates(EimValueConverter.toICalDates(value));
        } else if (field.getName().equals(FIELD_RECURRENCE_ID)) {
            String value =
                EimFieldValidator.validateText(field, MAXLEN_RECURRENCE_ID);
            event.setRecurrenceId(EimValueConverter.toICalDate(value));
        } else if (field.getName().equals(FIELD_STATUS)) {
            String value = EimFieldValidator.validateText(field, MAXLEN_STATUS);
            event.setStatus(value);
        } else {
            applyUnknownField(field);
        }
    }
}
