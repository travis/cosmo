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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
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
}
