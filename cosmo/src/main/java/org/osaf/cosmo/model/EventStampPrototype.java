/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtStart;

import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.hibernate.HibEventExceptionStamp;

/**
 * Contains methods common to EventStamp.
 */
public class EventStampPrototype {
    
    protected static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    private EventStamp stamp = null;
    
    public EventStampPrototype(EventStamp stamp) {
        this.stamp = stamp;
    }
    
    public Calendar getCalendar() {
        Calendar masterCal = CalendarUtils.copyCalendar(stamp.getEventCalendar());
        if (masterCal == null)
            return null;
       
        // the master calendar might not have any events; for
        // instance, a client might be trying to save a VTODO
        if (masterCal.getComponents(Component.VEVENT).isEmpty())
            return masterCal;

        VEvent masterEvent = (VEvent) masterCal.getComponents(Component.VEVENT).get(0);
        VAlarm masterAlarm = getDisplayAlarm(masterEvent);
        
        // build timezone map that includes all timezones in master calendar
        ComponentList timezones = masterCal.getComponents(Component.VTIMEZONE);
        HashMap<String, VTimeZone> tzMap = new HashMap<String, VTimeZone>();
        for(Iterator it = timezones.iterator(); it.hasNext();) {
            VTimeZone vtz = (VTimeZone) it.next();
            tzMap.put(vtz.getTimeZoneId().getValue(), vtz);
        }
        
        // check start/end date tz is included, and add if it isn't
        String tzid = getTzId(stamp. getStartDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
            masterCal.getComponents().add(vtz);
            tzMap.put(tzid, vtz);
        }
        
        tzid = getTzId(stamp.getEndDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
            masterCal.getComponents().add(vtz);
            tzMap.put(tzid, vtz);
        }
        
        // merge item properties to icalendar props
        mergeCalendarProperties(masterEvent, (NoteItem) stamp.getItem());
        
        // bug 10558: remove redundant VALUE=DATE-TIME params because
        // some clients don't like them
        fixDateTimeProperties(masterEvent);
        
        // bug 9606: handle displayAlarm with no trigger by not including
        // in exported icalendar
        if(masterAlarm!=null) {
            if(stamp.getDisplayAlarmTrigger()==null) {
                masterEvent.getAlarms().remove(masterAlarm);
                masterAlarm = null;
            }
        }
        
        // If event is not recurring, skip all the event modification
        // processing
        if(!stamp.isRecurring())
            return masterCal;
        
        // add all exception events
        NoteItem note = (NoteItem) stamp.getItem();
        TreeMap<String, VEvent> sortedMap = new TreeMap<String, VEvent>();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = HibEventExceptionStamp.getStamp(exception);
            if(exceptionStamp==null)
                continue;
            
            // Get exception event copy
            VEvent exceptionEvent = (VEvent) CalendarUtils
                    .copyComponent(exceptionStamp.getExceptionEvent());

            // ensure DURATION or DTEND exists on modfication
            if (ICalendarUtils.getDuration(exceptionEvent) == null) {
                ICalendarUtils.setDuration(exceptionEvent, ICalendarUtils
                        .getDuration(masterEvent));
            }
            
            // merge item properties to icalendar props
            mergeCalendarProperties(exceptionEvent, exception);
            
            // bug 10558: remove redundant VALUE=DATE-TIME params because
            // some clients don't like them
            fixDateTimeProperties(masterEvent);
            
            // check for inherited anyTime
            if(exceptionStamp.isAnyTime()==null) {
                DtStart modDtStart = exceptionEvent.getStartDate();
                // remove "missing" value
                modDtStart.getParameters().remove(modDtStart.getParameter(ICalendarConstants.PARAM_X_OSAF_ANYTIME));
                // add inherited value
                if(stamp.isAnyTime()) {
                    modDtStart.getParameters().add(getAnyTimeXParam());
                }
            }
                
            // Check for inherited displayAlarm, which is represented
            // by a valarm with no TRIGGER
            VAlarm displayAlarm = getDisplayAlarm(exceptionEvent);
            if(displayAlarm !=null && exceptionStamp.getDisplayAlarmTrigger()==null) {
                exceptionEvent.getAlarms().remove(displayAlarm);
                if(masterAlarm!=null)
                    exceptionEvent.getAlarms().add(masterAlarm);
            }
            
            sortedMap.put(exceptionStamp.getRecurrenceId().toString(), exceptionEvent);
            
            // verify that timezones are present for exceptions, and add if not
            tzid = getTzId(exceptionStamp.getStartDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
                masterCal.getComponents().add(vtz);
                tzMap.put(tzid, vtz);
            }
            
            tzid = getTzId(exceptionStamp.getEndDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                VTimeZone vtz = TIMEZONE_REGISTRY.getTimeZone(tzid).getVTimeZone();
                masterCal.getComponents().add(vtz);
                tzMap.put(tzid, vtz);
            }
        }
        
        masterCal.getComponents().addAll(sortedMap.values());
        
        return masterCal;
    }
    
    private void mergeCalendarProperties(VEvent event, NoteItem note) {
        //summary = displayName
        //description = body
        //uid = icalUid
        //dtstamp = clientModifiedDate/modifiedDate
        if(note.getModifies()!=null)
            ICalendarUtils.setUid(note.getModifies().getIcalUid(), event);
        else
            ICalendarUtils.setUid(note.getIcalUid(), event);
        
        ICalendarUtils.setSummary(note.getDisplayName(), event);
        ICalendarUtils.setDescription(note.getBody(), event);
        
        if(note.getClientModifiedDate()!=null)
            ICalendarUtils.setDtStamp(note.getClientModifiedDate(), event);
        else
            ICalendarUtils.setDtStamp(note.getModifiedDate(), event);
    }
    
    // Remove VALUE=DATE-TIME because it is redundant and some clients
    // don't like when its present.
    private void fixDateTimeProperties(VEvent event) {
        PropertyList props = event.getProperties();
        for(Iterator<Property> it = props.iterator(); it.hasNext();) {
            Property prop = it.next();
            if(prop instanceof DateProperty || prop instanceof DateListProperty) {
                Value v = (Value) prop.getParameter(Parameter.VALUE);
                if(v!=null && Value.DATE_TIME.equals(v))
                    prop.getParameters().remove(v);
            }
        }
    }
    
    private VAlarm getDisplayAlarm(VEvent event) {
        for(Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY))
                return alarm;
        }
        
        return null;
    }
    
    private String getTzId(Date date) {
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            if(dt.getTimeZone()!=null)
                return dt.getTimeZone().getID();
        }
        
        return null;
    }
    
    private Parameter getAnyTimeXParam() {
        return new XParameter(ICalendarConstants.PARAM_X_OSAF_ANYTIME, ICalendarConstants.VALUE_TRUE);
    }
}
