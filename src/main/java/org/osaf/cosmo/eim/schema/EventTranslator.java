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
package org.osaf.cosmo.eim.schema;

import java.io.Reader;
import java.io.StringReader;

import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Translates item records to <code>EventStamp</code>s.
 * <p>
 * Implements the following schema:
 * <p>
 * TBD
 */
public class EventTranslator extends EimSchemaTranslator {
    private static final Log log = LogFactory.getLog(EventTranslator.class);

    /** */
    public static final String FIELD_DTSTART = "dtstart";
    /** */
    public static final int MAXLEN_DTSTART = 20;
    /** */
    public static final String FIELD_DTEND = "dtend";
    /** */
    public static final int MAXLEN_DTEND = 20;
    /** */
    public static final String FIELD_LOCATION = "location";
    /** */
    public static final int MAXLEN_LOCATION = 256;
    /** */
    public static final String FIELD_RRULE = "rrule";
    /** */
    public static final int MAXLEN_RRULE = 1024;
    /** */
    public static final String FIELD_EXRULE = "exrule";
    /** */
    public static final int MAXLEN_EXRULE = 1024;
    /** */
    public static final String FIELD_RDATE = "rdate";
    /** */
    public static final int MAXLEN_RDATE = 1024;
    /** */
    public static final String FIELD_EXDATE = "exdate";
    /** */
    public static final int MAXLEN_EXDATE = 1024;
    /** */
    public static final String FIELD_RECURRENCE_ID = "recurrenceId";
    /** */
    public static final int MAXLEN_RECURRENCE_ID = 20;
    /** */
    public static final String FIELD_STATUS = "status";
    /** */
    public static final int MAXLEN_STATUS = 256;

    /** */
    public EventTranslator() {
        super(PREFIX_EVENT, NS_EVENT);
    }

    /**
     * Copies the data from the given record field into the event
     * stamp.
     *
     * @throws IllegalArgumentException if the item does not have an
     * event stamp
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected void applyField(EimRecordField field,
                              Item item)
        throws EimSchemaException {
        EventStamp stamp = EventStamp.getStamp(item);
        if (stamp == null)
            throw new IllegalArgumentException("Item does not have an event stamp");

        if (field.getName().equals(FIELD_DTSTART)) {
            String value = validateText(field, MAXLEN_DTSTART);
            stamp.setStartDate(parseICalDate(value));
        } else if (field.getName().equals(FIELD_DTEND)) {
            String value = validateText(field, MAXLEN_DTEND);
            stamp.setEndDate(parseICalDate(value));
        } else if (field.getName().equals(FIELD_LOCATION)) {
            String value = validateText(field, MAXLEN_LOCATION);
            stamp.setLocation(value);
        } else if (field.getName().equals(FIELD_RRULE)) {
            String value = validateText(field, MAXLEN_RRULE);
            stamp.setRecurrenceRules(parseICalRecurs(value));
        } else if (field.getName().equals(FIELD_EXRULE)) {
            String value = validateText(field, MAXLEN_EXRULE);
            stamp.setExceptionRules(parseICalRecurs(value));
        } else if (field.getName().equals(FIELD_RDATE)) {
            String value = validateText(field, MAXLEN_RDATE);
            stamp.setRecurrenceDates(parseICalDates(value));
        } else if (field.getName().equals(FIELD_EXDATE)) {
            String value = validateText(field, MAXLEN_EXDATE);
            stamp.setExceptionDates(parseICalDates(value));
        } else if (field.getName().equals(FIELD_RECURRENCE_ID)) {
            String value = validateText(field, MAXLEN_RECURRENCE_ID);
            stamp.setRecurrenceId(parseICalDate(value));
        } else if (field.getName().equals(FIELD_STATUS)) {
            String value = validateText(field, MAXLEN_STATUS);
            stamp.setStatus(value);
        } else {
            applyUnknownField(field, stamp.getItem());
        }
    }

    /**
     * Adds record fields for each applicable event property.
     */
    protected void addFields(EimRecord record,
                             Item item) {
        addFields(record, EventStamp.getStamp(item));
    }

    /**
     * Adds record fields for each applicable event property.
     *
     * @throws IllegalArgumentException if the stamp is not an event stamp
     */
    protected void addFields(EimRecord record,
                             Stamp stamp) {
        if (! (stamp instanceof EventStamp))
            throw new IllegalArgumentException("Stamp is not an event stamp");
        EventStamp es = (EventStamp) stamp;
        String value = null;

        record.addField(new TextField(FIELD_DTSTART, 
                                      formatICalDate(es.getStartDate())));

        record.addField(new TextField(FIELD_DTEND, 
                                      formatICalDate(es.getEndDate())));

        record.addField(new TextField(FIELD_LOCATION, es.getLocation()));

        value = formatRecurs(es.getRecurrenceRules());
        record.addField(new TextField(FIELD_RRULE, value));

        value = formatRecurs(es.getExceptionRules());
        record.addField(new TextField(FIELD_EXRULE, value));

        value = formatICalDates(es.getRecurrenceDates());
        record.addField(new TextField(FIELD_RDATE, value));

        value = formatICalDates(es.getExceptionDates());
        record.addField(new TextField(FIELD_EXDATE, value));

        value = formatICalDate(es.getRecurrenceId());
        record.addField(new TextField(FIELD_RECURRENCE_ID, value));

        record.addField(new TextField(FIELD_STATUS, es.getStatus()));

        addUnknownFields(record, stamp.getItem());
    }
}
