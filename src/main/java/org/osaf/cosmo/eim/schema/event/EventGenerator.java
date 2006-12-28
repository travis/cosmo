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

import java.util.List;
import java.util.ArrayList;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates EIM records from event stamps.
 *
 * @see EventStamp
 */
public class EventGenerator extends BaseStampGenerator
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(EventGenerator.class);

    private EventStamp event;

    /** */
    public EventGenerator(EventStamp event) {
        super(PREFIX_EVENT, NS_EVENT, event);
        this.event = event;
    }

    /**
     * Copies event properties and attributes into a event record.
     */
    public List<EimRecord> generateRecords() {
        EimRecord master = new EimRecord(getPrefix(), getNamespace());
        String value = null;

        master.addKeyField(new TextField(FIELD_UUID, event.getItem().getUid()));

        value = EimValueConverter.fromICalDate(event.getStartDate());
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

        value = EimValueConverter.fromICalDate(event.getRecurrenceId());
        master.addField(new TextField(FIELD_RECURRENCE_ID, value));

        master.addField(new TextField(FIELD_STATUS, event.getStatus()));

        master.addFields(generateUnknownFields());

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(master);

        // XXX: exception instance records

        return records;
    }
}
