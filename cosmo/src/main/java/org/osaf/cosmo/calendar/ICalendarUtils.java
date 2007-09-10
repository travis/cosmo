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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.osaf.cosmo.CosmoConstants;

/**
 * Contains utility methods for creating/updating net.fortuna.ical4j
 * objects.
 */
public class ICalendarUtils {
    
    /**
     * Create a base Calendar containing a single component.
     * @param comp Component to add to the base Calendar
     * @param icalUid uid of component
     * @return base Calendar
     */
    public static Calendar createBaseCalendar(Component comp, String icalUid) {
        Calendar cal = new Calendar();
        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        
        Uid uid = new Uid(icalUid);
        comp.getProperties().add(uid);
        
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
}
