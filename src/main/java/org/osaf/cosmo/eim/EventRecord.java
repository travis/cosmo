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

import java.util.Date;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.RecurrenceId;

import org.apache.commons.lang.StringUtils;
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
    private String rRule;
    private String exRule;
    private String rDate;
    private String exDate;
    private Date recurrenceId;
    private String status;

    /** */
    public EventRecord() {
    }

    /** */
    public EventRecord(EventStamp stamp) {
        setUuid(stamp.getItem().getUid());

        VEvent event = stamp.getMasterEvent();
        Property p = null;
        PropertyList pl = null;

        p = event.getProperties().getProperty(Property.DTSTART);
        dtStart = ((DtStart)p).getDate();

        p = event.getProperties().getProperty(Property.DTEND);
        if (p != null) {
            dtEnd = ((DtEnd)p).getDate();
        } else {
            p = event.getProperties().getProperty(Property.DURATION);
            dtEnd = ((Duration)p).getDuration().getTime(dtStart);
        }

        p = event.getProperties().getProperty(Property.LOCATION);
        if (p != null)
            location = p.getValue();

        pl = event.getProperties().getProperties(Property.RRULE);
        if (pl.iterator().hasNext())
            rRule = StringUtils.join(pl.iterator(), ",");

        pl = event.getProperties().getProperties(Property.EXRULE);
        if (pl.iterator().hasNext())
            exRule = StringUtils.join(pl.iterator(), ",");

        pl = event.getProperties().getProperties(Property.RDATE);
        if (pl.iterator().hasNext())
            rDate = StringUtils.join(pl.iterator(), ",");

        pl = event.getProperties().getProperties(Property.EXDATE);
        if (pl.iterator().hasNext())
            exDate = StringUtils.join(pl.iterator(), ",");

        p = event.getProperties().getProperty(Property.RECURRENCE_ID);
        if (p != null)
            recurrenceId = ((RecurrenceId)p).getDate();

        p = event.getProperties().getProperty(Property.STATUS);
        if (p != null)
            status = p.getValue();
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
    public String getRRule() {
        return rRule;
    }

    /** */
    public void setRRule(String rRule) {
        this.rRule = rRule;
    }

    /** */
    public String getExRule() {
        return exRule;
    }

    /** */
    public void setExRule(String exRule) {
        this.exRule = exRule;
    }

    /** */
    public String getRDate() {
        return rDate;
    }

    /** */
    public void setRDate(String rDate) {
        this.rDate = rDate;
    }

    /** */
    public String getExDate() {
        return exDate;
    }

    /** */
    public void setExDate(String exDate) {
        this.exDate = exDate;
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
