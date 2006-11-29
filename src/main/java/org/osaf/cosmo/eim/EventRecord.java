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
package org.osaf.cosmo.eim;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.EventStamp;

/**
 * Models an EIM event record.
 */
public class EventRecord extends EimRecord {
    private static final Log log = LogFactory.getLog(EventRecord.class);

    private Date dtStart;
    private Date dtEnd;
    private String location;
    private List<String> rRules;
    private List<String> exRules;
    private List<String> rDates;
    private List<String> exDates;
    private Date recurrenceId;
    private String status;

    /** */
    public EventRecord() {
        rRules = new ArrayList<String>();
        exRules = new ArrayList<String>();
        rDates = new ArrayList<String>();
        exDates = new ArrayList<String>();
    }

    /** */
    public EventRecord(EventStamp stamp) {
        setUuid(stamp.getItem().getUid());
        dtStart = stamp.getStartDate();
        dtEnd = stamp.getEndDate();
        location = stamp.getLocation();
        rRules = stamp.getRecurrenceRules();
        exRules = stamp.getExceptionRules();
        rDates = stamp.getRecurrenceDates();
        exDates = stamp.getExceptionDates();
        recurrenceId = stamp.getRecurrenceId();
    }

    /** */
    public void applyTo(EventStamp stamp) {
        if (! stamp.getItem().getUid().equals(getUuid()))
            throw new IllegalArgumentException("cannot apply record to item with non-matching uuid");

        // XXX
    }

    /** */
    public Date getDtStart() {
        return dtStart;
    }

    /** */
    public void setDtStart(Date dtStart) {
        this.dtStart = dtStart;
    }

    /** */
    public Date getDtEnd() {
        return dtEnd;
    }

    /** */
    public void setDtEnd(Date dtEnd) {
        this.dtEnd = dtEnd;
    }

    /** */
    public String getLocation() {
        return location;
    }

    /** */
    public void setLocation(String location) {
        this.location = location;
    }

    /** */
    public List<String> getRRules() {
        return rRules;
    }

    /** */
    public void setRRules(List<String> rRules) {
        this.rRules = rRules;
    }

    /** */
    public List<String> getExRules() {
        return exRules;
    }

    /** */
    public void setExRules(List<String> exRules) {
        this.exRules = exRules;
    }

    /** */
    public List<String> getRDates() {
        return rDates;
    }

    /** */
    public void setRDates(List<String> rDates) {
        this.rDates = rDates;
    }

    /** */
    public List<String> getExDates() {
        return exDates;
    }

    /** */
    public void setExDates(List<String> exDates) {
        this.exDates = exDates;
    }

    /** */
    public Date getRecurrenceId() {
        return recurrenceId;
    }

    /** */
    public void setRecurrenceId(Date recurrenceId) {
        this.recurrenceId = recurrenceId;
    }

    /** */
    public String getStatus() {
        return status;
    }

    /** */
    public void setStatus(String status) {
        this.status = status;
    }
}
