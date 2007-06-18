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

import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseStampGenerator;
import org.osaf.cosmo.eim.schema.EimValueConverter;
import org.osaf.cosmo.eim.schema.text.DurationFormat;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.Item;

/**
 * Generates EIM records from event stamps.
 *
 * @see BaseEventStamp
 */
public class EventGenerator extends BaseStampGenerator
    implements EventConstants {
    private static final Log log =
        LogFactory.getLog(EventGenerator.class);

    private static final HashSet<String> STAMP_TYPES = new HashSet<String>(2);
    
    static {
        STAMP_TYPES.add("event");
        STAMP_TYPES.add("eventexception");
    }
   
    /** */
    public EventGenerator(Item item) {
        super(PREFIX_EVENT, NS_EVENT, item);
        setStamp(BaseEventStamp.getStamp(item));
    }
    
    @Override
    protected Set<String> getStampTypes() {
        return STAMP_TYPES;
    }

    /**
     * Adds records representing the master event and its display
     * alarm (if one exists).
     */
    protected void addRecords(List<EimRecord> records) {
        BaseEventStamp stamp = (BaseEventStamp) getStamp();
        if (stamp == null)
            return;

        EimRecord record = new EimRecord(getPrefix(), getNamespace());
        addKeyFields(record);
        addFields(record);
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

        String value = null;

        if(isDtStartMissing(stamp)) {
            record.addField(generateMissingField(new TextField(FIELD_DTSTART, null)));
        } else {
            value = EimValueConverter.fromICalDate(stamp.getStartDate(),
                                                   stamp.isAnyTime());
            record.addField(new TextField(FIELD_DTSTART, value));
        }
         
        if(isDurationMissing(stamp)) {
            record.addField(generateMissingField(new TextField(FIELD_DURATION, null)));
        } else {
            value = DurationFormat.getInstance().format(stamp.getDuration());
            // Empty duration translates to None
            if("".equals(value))
                value = null;
            record.addField(new TextField(FIELD_DURATION, value));
        }
        
        if(isMissingAttribute("location")) {
            record.addField(generateMissingField(new TextField(FIELD_LOCATION, null)));
        } else {
            record.addField(new TextField(FIELD_LOCATION, stamp.getLocation()));
        }

        value = EimValueConverter.fromICalRecurs(stamp.getRecurrenceRules());
        record.addField(new ClobField(FIELD_RRULE, toReader(value)));

        value = EimValueConverter.fromICalRecurs(stamp.getExceptionRules());
        record.addField(new ClobField(FIELD_EXRULE, toReader(value)));

        value = EimValueConverter.fromICalDates(stamp.getRecurrenceDates());
        record.addField(new ClobField(FIELD_RDATE, toReader(value)));

        value = EimValueConverter.fromICalDates(stamp.getExceptionDates());
        record.addField(new ClobField(FIELD_EXDATE, toReader(value)));

        if(isMissingAttribute("status")) {
            record.addField(generateMissingField(new TextField(FIELD_STATUS, null)));
        } else {
            record.addField(new TextField(FIELD_STATUS, stamp.getStatus()));
        }
        
        record.addFields(generateUnknownFields());
    }

    private Reader toReader(String value) {
        return value != null ? new StringReader(value) : null;
    }

    /**
     * Determine if startDate is missing.  The startDate is missing
     * if the startDate is equal to the reucurreceId and the anyTime
     * parameter is inherited.
     * @param stamp BaseEventStamp to test
     * @return
     */
    private boolean isDtStartMissing(BaseEventStamp stamp) {
        if(!isModification())
            return false;
        
        if(stamp.getStartDate()==null || stamp.getRecurrenceId()==null)
            return false;
        
        // "missing" startDate is represented as startDate==recurrenceId
        if(!stamp.getStartDate().equals(stamp.getRecurrenceId()))
            return false;
        
        // "missing" anyTime is represented as null
        if(stamp.isAnyTime()!=null)
            return false;
        
        return true;
    }
    
    /**
     * Determine if duration is missing.  The duration is missing
     * if there is no duration and the event is a modification, or 
     * if the duration is equal to the parent duration.
     * @param stamp BaseEventStamp to test
     * @return
     */
    private boolean isDurationMissing(BaseEventStamp stamp) {
        if(!isModification())
            return false;
        
        return (stamp.getDuration()==null);
    }
}
