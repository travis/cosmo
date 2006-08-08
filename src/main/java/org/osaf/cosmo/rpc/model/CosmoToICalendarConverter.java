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
package org.osaf.cosmo.rpc.model;

import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.lang.StringUtils;
import org.osaf.cosmo.CosmoConstants;


import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterFactoryImpl;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;

/**
 * An instance of this class is used to convert Scooby Events into ical4j
 * events or ical4j events wrapped in ical4j Calendars.
 *
 *
 * @author bobbyrullo
 *
 */
public class CosmoToICalendarConverter {

    //TODO Have spring manage UUID generator
    public static VersionFourGenerator uidGenerator = new VersionFourGenerator();

    /**
     * Creates a Calendar object which wraps an ical4j VEvent
     *
     * All timezone information is ignored at this point, so all dates are
     * "floating" dates
     *
     * @param event
     * @return
     */
    public Calendar createWrappedVEvent(Event event){
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        VEvent vevent = createVEvent(event);
        calendar.getComponents().add(vevent);
        return calendar;
    }

    /**
     * Creates a VEvent from a scooby Event.
     *
     * All timezone information is
     * ignored at this point, so all dates are "floating" dates
     *
     * @param event
     * @return
     */
    public VEvent createVEvent(Event event){
        VEvent vevent = new VEvent();

        copyProperties(event, vevent);

        // New events need a new uid
        Uid uid = new Uid();
        uid.setValue(event.getId() != null ? event.getId() : getNextUID());
        vevent.getProperties().add(uid);


        return vevent;
    }

    /**
     * Updates an existing event within an ical4j calendar by finding the
     * VEvent within the calendar with the same UID and copying its properties
     * to that event. Any properties that Scooby doesn't support are preserved.
     *
     * Timezones of all dates will remain as they were originally.
     *
     * @param event the scooby event
     * @param calendar the calendar containing the VEvent to update
     * @return
     */
    public void updateEvent(Event event, Calendar calendar){
        String uid = event.getId();
        VEvent vevent = getMasterVEvent(uid, calendar);
        copyProperties(event, vevent);
    }

    /**
     * Sets a DateProperty with the Date (no time)  represented by the ScoobyDate.
     *
     * @param dateProp
     * @param scoobyDate
     */
    protected void setDate(DateProperty dateProp, CosmoDate scoobyDate){
        String dateString = getDateTimeString(scoobyDate, false, false);
        Date date = null;
        try {
            date = new Date(dateString);
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
        dateProp.setDate(date);
        dateProp.getParameters().add(Value.DATE);
    }

    /**
     * Sets a DateProperty with the DateTime represented by the ScoobyDate.
     *
     * @param dateProp
     * @param scoobyDate
     */
    protected void setDateTime(DateProperty dateProp, CosmoDate scoobyDate){
        String dateString = getDateTimeString(scoobyDate, true, scoobyDate.isUtc());
        DateTime dateTime = null;
        try {
            dateTime = new DateTime(dateString);
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
        dateProp.setDate(dateTime);
    }

    protected void copyProperties(Event event, VEvent vevent){
        //if dtStart and end are dates with times, this is true
        boolean dateTime = true;
        DtStart dtStart = null;
        DtEnd dtEnd = null;

        DtStart dtStartOld = vevent.getStartDate();
        DtEnd dtEndOld = null;

        boolean dtEndHasDuration = hasProperty(vevent, Property.DURATION);
        if (hasProperty(vevent, Property.DTEND)
                || dtEndHasDuration ) {
            dtEndOld = vevent.getEndDate();
        }

        if (event.isAllDay()) {
            dateTime = false;
            dtStart = new DtStart();
            setDate(dtStart, event.getStart());
            if (event.getEnd() != null){
                dtEnd = new DtEnd();
                java.util.Calendar endCalendar = createCalendar(event.getEnd());
                endCalendar.add(java.util.Calendar.DATE, 1);
                Date endDate = null;
                try {
                    endDate = new Date(getDateTimeString(endCalendar, false, false));
                } catch (ParseException pe){
                   throw new RuntimeException(pe);
                }
                dtEnd.setDate(endDate);
                dtEnd.getParameters().add(Value.DATE);
            }

        } else if (event.isAnyTime()) {
            dateTime = false;
            Parameter anyTimeParam = null;
            try {
                anyTimeParam = ParameterFactoryImpl.getInstance()
                        .createParameter(CosmoConstants.PARAM_X_OSAF_ANYTIME,
                                CosmoConstants.VALUE_TRUE);
            } catch (URISyntaxException urise) {
                throw new RuntimeException(urise);
            }
            dtStart = new DtStart();
            setDate(dtStart, event.getStart());
            dtStart.getParameters().add(anyTimeParam);
            removeProperty(vevent, Property.DTEND);

        } else if (event.isPointInTime()) {
            dtStart = new DtStart();
            setDateTime(dtStart, event.getStart());
            removeProperty(vevent, Property.DTEND);
        } else {
            dtStart = new DtStart();
            setDateTime(dtStart, event.getStart());
            dtEnd = new DtEnd();
            setDateTime(dtEnd, event.getEnd());
        }

        Summary summary = new Summary();
        summary.setValue(event.getTitle());

        Description description = new Description();
        description.setValue(event.getDescription());

        Status status = null;
        if(!StringUtils.isEmpty(event.getStatus())) {
            status = new Status();
            status.setValue(event.getStatus());
        }

        if (dateTime) {
            if (dtStartOld != null && dtStart != null){
                copyTimeZone(dtStartOld,  dtStart);
            }
            if (dtEndOld != null && dtEnd!= null){
                copyTimeZone(dtEndHasDuration ? dtStartOld : dtEndOld, dtEnd);
            }
        }

        addOrReplaceProperty(vevent, description);
        addOrReplaceProperty(vevent, summary);
        if(status != null) {
            addOrReplaceProperty(vevent, status);
        } else {
            removeProperty(vevent, Property.STATUS);
        }
        addOrReplaceProperty(vevent, dtStart);
        if (dtEnd != null){
            addOrReplaceProperty(vevent, dtEnd);
        }
        removeProperty(vevent, Property.DURATION);

    }

    protected static void addOrReplaceProperty(VEvent destination,
                                               Property property) {

        Property oldProp = destination.getProperties().getProperty(
                property.getName());
        if (oldProp != null) {
            destination.getProperties().remove(oldProp);
        }
        destination.getProperties().add(property);

    }

    protected static void removeProperty(Component component, String propName) {
        Property property = component.getProperties().getProperty(propName);
        component.getProperties().remove(property);
    }

    protected static String getDateTimeString(CosmoDate scoobyDate,
                                              boolean includeTime, boolean utc) {
        StringBuffer buf = new StringBuffer();
        buf.append(StringUtils.leftPad(""+scoobyDate.getYear(),  4, "0"));
        buf.append(StringUtils.leftPad(""+(scoobyDate.getMonth()+1), 2, "0"));
        buf.append(StringUtils.leftPad(""+scoobyDate.getDate(), 2, "0"));
        if (includeTime) {
            buf.append("T");
            buf.append(StringUtils.leftPad("" + scoobyDate.getHours(), 2, "0"));
            buf.append(StringUtils
                    .leftPad("" + scoobyDate.getMinutes(), 2, "0"));
            buf.append(StringUtils
                    .leftPad("" + scoobyDate.getSeconds(), 2, "0"));
            if (utc){
                buf.append("Z");
            }
        }
        return buf.toString();
    }

    protected static String getDateTimeString(java.util.Calendar calendar,
                                              boolean includeTime, boolean utc) {
        StringBuffer buf = new StringBuffer();
        buf.append(StringUtils.leftPad(""
                + calendar.get(java.util.Calendar.YEAR), 4, "0"));
        buf.append(StringUtils.leftPad(""
                + (calendar.get(java.util.Calendar.MONTH) + 1), 2, "0"));
        buf.append(StringUtils.leftPad(""
                + calendar.get(java.util.Calendar.DAY_OF_MONTH), 2, "0"));
        if (includeTime) {
            buf.append("T");
            buf.append(StringUtils.leftPad("" + calendar.get(java.util.Calendar.HOUR_OF_DAY), 2, "0"));
            buf.append(StringUtils
                    .leftPad("" + calendar.get(java.util.Calendar.MINUTE), 2, "0"));
            buf.append(StringUtils
                    .leftPad("" + calendar.get(java.util.Calendar.SECOND), 2, "0"));
            if (utc){
                buf.append("Z");
            }
        }
        return buf.toString();
    }

    protected static java.util.Calendar createCalendar(CosmoDate scoobyDate){
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.clear();
        calendar.set(java.util.Calendar.YEAR, scoobyDate.getYear());
        calendar.set(java.util.Calendar.MONTH, scoobyDate.getMonth());
        calendar.set(java.util.Calendar.DAY_OF_MONTH, scoobyDate.getDate());
        calendar.set(java.util.Calendar.HOUR_OF_DAY, scoobyDate.getHours());
        calendar.set(java.util.Calendar.MINUTE, scoobyDate.getMinutes());
        calendar.set(java.util.Calendar.SECOND, scoobyDate.getDate());
        return calendar;
     }

    protected static String getNextUID() {
        return uidGenerator.nextIdentifier().toString();
    }

    /**
     * Copies the TZID from the source to the dest
     * @param source
     * @param dest
     */
    protected void copyTimeZone(DateProperty source, DateProperty dest) {
        TzId sourceTz = (TzId) source.getParameters().getParameter(
                Parameter.TZID);
        if (sourceTz != null) {
            dest.getParameters().add(sourceTz);
        }
    }

    protected VEvent getMasterVEvent(String uid, Calendar calendar){
        ComponentList list = calendar.getComponents().getComponents(Component.VEVENT);
        for (Object obj: list){
            VEvent vevent = (VEvent) obj;
            if (uid.equals(getUID(vevent))){
                if (!hasProperty(vevent, Property.RECURRENCE_ID) &&
                    !hasProperty(vevent, Property.EXRULE) &&
                    !hasProperty(vevent, Property.EXDATE)){
                    return vevent;
                }
            }
        }

        return null;
    }

    private String getUID(VEvent vevent){
        Uid uid = (Uid) vevent.getProperties().getProperty(Property.UID);
        return uid == null ? null : uid.getValue();
    }

    private boolean hasProperty(Component c, String propName){
        PropertyList l = c.getProperties().getProperties(propName);
        return l != null && l.size() > 0;
    }
}