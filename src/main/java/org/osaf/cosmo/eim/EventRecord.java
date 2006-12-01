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
import java.util.List;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Recur;

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
    private List<Recur> rRules;
    private List<Recur> exRules;
    private DateList rDates;
    private DateList exDates;
    private Date recurrenceId;
    private String status;

    /** */
    public EventRecord() {
        rRules = new ArrayList<Recur>();
        exRules = new ArrayList<Recur>();
        rDates = new DateList();
        exDates = new DateList();
    }

    /** */
    public EventRecord(EventStamp stamp) {
        super(stamp.getItem());
        dtStart = stamp.getStartDate();
        dtEnd = stamp.getEndDate();
        location = stamp.getLocation();
        rRules = stamp.getRecurrenceRules();
        exRules = stamp.getExceptionRules();
        rDates = stamp.getRecurrenceDates();
        exDates = stamp.getExceptionDates();
        recurrenceId = stamp.getRecurrenceId();
        status = stamp.getStatus();
    }

    /** */
    public void applyTo(EventStamp stamp) {
        super.applyTo(stamp.getItem());
        stamp.setStartDate(dtStart);
        stamp.setEndDate(dtEnd);
        stamp.setLocation(location);
        stamp.setRecurrenceRules(rRules);
        stamp.setExceptionRules(exRules);
        stamp.setRecurrenceDates(rDates);
        stamp.setExceptionDates(exDates);
        stamp.setRecurrenceId(recurrenceId);
        stamp.setStatus(status);
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
    public List<Recur> getRRules() {
        return rRules;
    }

    /** */
    public void setRRules(List<Recur> rRules) {
        this.rRules = rRules;
    }

    /** */
    public List<Recur> getExRules() {
        return exRules;
    }

    /** */
    public void setExRules(List<Recur> exRules) {
        this.exRules = exRules;
    }

    /** */
    public DateList getRDates() {
        return rDates;
    }

    /** */
    public void setRDates(DateList rDates) {
        this.rDates = rDates;
    }

    /** */
    public DateList getExDates() {
        return exDates;
    }

    /** */
    public void setExDates(DateList exDates) {
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
