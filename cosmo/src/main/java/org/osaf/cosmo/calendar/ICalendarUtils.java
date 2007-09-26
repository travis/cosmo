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
package org.osaf.cosmo.calendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.calendar.util.Dates;

/**
 * Contains utility methods for creating/updating net.fortuna.ical4j
 * objects.
 */
public class ICalendarUtils {
    
    /**
     * Create a base Calendar containing a single component.
     * @param comp Component to add to the base Calendar
     * @param icalUid uid of component, if null no UID 
     *                property will be added to the component
     * @return base Calendar
     */
    public static Calendar createBaseCalendar(Component comp, String icalUid) {
        Uid uid = new Uid(icalUid);
        comp.getProperties().add(uid);
        return createBaseCalendar(comp);
    }
    
    /**
     * Create a base Calendar containing a single component.
     * @param comp Component to add to the base Calendar
     * @return base Calendar
     */
    public static Calendar createBaseCalendar(Component comp) {
        Calendar cal = new Calendar();
        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        
        cal.getComponents().add(comp);
        return cal;
    }
    
    /**
     * Update the SUMMARY property on a component.
     * @param text SUMMARY value to update.  If null, the SUMMARY property
     *        will be removed
     * @param comp component to update
     */
    public static void setSummary(String text, Component comp) {
        Summary summary = (Summary)
        comp.getProperties().getProperty(Property.SUMMARY);
        if (text == null) {
            if (summary != null)
                comp.getProperties().remove(summary);
            return;
        }                
        if (summary == null) {
            summary = new Summary();
            comp.getProperties().add(summary);
        }
        summary.setValue(text);
    }
    
    /**
     * Update the DESCRIPTION property on a component.
     * @param text DESCRIPTION value to update.  If null, the DESCRIPTION property
     *        will be removed
     * @param comp component to update
     */
    public static void setDescription(String text, Component comp) {
        Description description = (Description)
        comp.getProperties().getProperty(Property.DESCRIPTION);
   
        if (text == null) {
            if (description != null)
                comp.getProperties().remove(description);
            return;
        }                
        if (description == null) {
            description = new Description();
            comp.getProperties().add(description);
        }
        description.setValue(text);
    }
    
    /**
     * Update the UID property on a component.
     * @param text UID value to update.  If null, the UID property
     *        will be removed
     * @param comp component to update
     */
    public static void setUid(String text, Component comp) {
        Uid uid = (Uid)
        comp.getProperties().getProperty(Property.UID);
   
        if (text == null) {
            if (uid != null)
                comp.getProperties().remove(uid);
            return;
        }                
        if (uid == null) {
            uid = new Uid();
            comp.getProperties().add(uid);
        }
        uid.setValue(text);
    }
    
    /**
     * Construct a new DateTime instance for floating times (no timezone).
     * If the specified date is not floating, then the instance is returned. 
     * 
     * This allows a floating time to be converted to an instant in time
     * depending on the specified timezone.
     * 
     * @param date floating date
     * @param tz timezone
     * @return new DateTime instance representing floating time pinned to
     *         the specified timezone
     */
    public static DateTime pinFloatingTime(DateTime date, TimeZone tz) {
        try {
            return new DateTime(date.toString(), tz);
        } catch (ParseException e) {
            throw new RuntimeException("error parsing date");
        }
    }
    
    /**
     * Return list of subcomponents for a component.  Ica4j doesn't have
     * a generic way to do this.
     * @param component
     * @return list of subcomponents
     */
    public static ComponentList getSubComponents(Component component) {
        if(component instanceof VEvent)
            return ((VEvent) component).getAlarms();
        else if(component instanceof VTimeZone)
            return ((VTimeZone) component).getObservances();
        else if(component instanceof VToDo)
            return ((VToDo) component).getAlarms();
        
        return new ComponentList();
    }
    
    /**
     * Return the list of dates that an alarm will trigger.
     * @param alarm alarm component
     * @param parent parent compoennt (VEvent,VToDo)
     * @return dates that alarm is configured to trigger
     */
    public static List<Date> getTriggerDates(VAlarm alarm, Component parent) {
        ArrayList<Date> dates = new ArrayList<Date>();
        Trigger trigger = alarm.getTrigger();
        if(trigger==null)
            return dates;
        
        Date initialTriggerDate = getTriggerDate(trigger, parent);
        if(initialTriggerDate==null)
            return dates;
        
        dates.add(initialTriggerDate);
        
        Duration dur = alarm.getDuration();
        if(dur==null)
            return dates;
        Repeat repeat = alarm.getRepeat(); 
        if(repeat==null)
            return dates;
        
        Date nextTriggerDate = initialTriggerDate;
        for(int i=0;i<repeat.getCount();i++) {
            nextTriggerDate = Dates.getInstance(dur.getDuration().getTime(nextTriggerDate), nextTriggerDate);
            dates.add(nextTriggerDate);
        }
        
        return dates;
    }
    
    /**
     * Return the date that a trigger refers to, which can be an absolute
     * date or a date relative to the start or end time of a parent 
     * component (VEVENT/VTODO).
     * @param trigger
     * @param parent
     * @return date of trigger
     */
    public static Date getTriggerDate(Trigger trigger, Component parent) {
        
        if(trigger==null)
            return null;
        
        // if its absolute then we are done
        if(trigger.getDateTime()!=null)
            return trigger.getDateTime();
        
        // otherwise we need a start date if VEVENT
        DtStart start = (DtStart) parent.getProperty(Property.DTSTART);
        if(start==null && parent instanceof VEvent)
            return null;
        
        // is trigger relative to start or end
        Related related = (Related) trigger.getParameter(Parameter.RELATED);
        if(related==null || related.equals(Related.START)) {    
            // must have start date
            if(start==null)
                return null;
            
            // relative to start
            return Dates.getInstance(trigger.getDuration().getTime(start.getDate()), start.getDate());
        } else {
            // relative to end
            Date endDate = null;
            
            // need an end date or duration or due 
            DtEnd end = (DtEnd) parent.getProperty(Property.DTEND);
            if(end!=null)
                endDate = end.getDate();
           
            if(endDate==null) {
                Duration dur = (Duration) parent.getProperty(Property.DURATION);
                if(dur!=null && start!=null)
                    endDate= Dates.getInstance(dur.getDuration().getTime(start.getDate()), start.getDate());
            }
            
            if(endDate==null) {
                Due due = (Due) parent.getProperty(Property.DUE);
                if(due!=null)
                    endDate = due.getDate();
            }
            
            // require end date
            if(endDate==null)
                return null;
            
            return Dates.getInstance(trigger.getDuration().getTime(endDate), endDate);
        }
    }
    
    /**
     * Find and return the first DISPLAY VALARM in a comoponent
     * @param component VEVENT or VTODO
     * @return first DISPLAY VALARM, null if there is none
     */
    public static VAlarm getDisplayAlarm(Component component) {
        ComponentList alarms = null;
        
        if(component instanceof VEvent)
            alarms = ((VEvent) component).getAlarms();
        else if(component instanceof VToDo)
            alarms = ((VToDo) component).getAlarms();
        
        if(alarms==null || alarms.size()==0)
            return null;
        
        for(Iterator<VAlarm> it = alarms.iterator();it.hasNext();) {
            VAlarm alarm = it.next();
            if(Action.DISPLAY.equals(alarm.getAction()))
                return alarm;
        }
        
        return null;   
    }
}
