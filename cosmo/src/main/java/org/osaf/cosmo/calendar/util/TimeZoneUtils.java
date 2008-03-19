/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import com.ibm.icu.util.TimeZone;

import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Contains utility methods for working with ical4j TimeZone and
 * VTimeZone objects, using icu4j built in timezone definitions.
 */
public class TimeZoneUtils {
    
    static HashSet<String> ALL_TIMEZONE_IDS = new HashSet<String>();
    
    // index all timezone ids from icu registry
    static
    {
        for(String id: TimeZone.getAvailableIDs())
            ALL_TIMEZONE_IDS.add(id);
    }
    
    /**
     * @return all available timezone ids
     */
    public static Set<String> getTimeZoneIds() {
        return ALL_TIMEZONE_IDS;
    }
    
    /**
     * Return ical4j TimeZone instance for timezone id.
     * @param id timezone id
     * @return ical4j TimeZone instance
     */
    public static net.fortuna.ical4j.model.TimeZone getTimeZone(String id) {
        if(!ALL_TIMEZONE_IDS.contains(id))
            return null;
        
        VTimeZone vtz = getVTimeZone(id);
        if(vtz==null)
            return null;
        
        return new net.fortuna.ical4j.model.TimeZone(vtz);
    }
    
    /**
     * Return ical4j VTimeZone instance for timezone id
     * @param id timezone id
     * @return ical4j VTimeZone instance
     */
    public static net.fortuna.ical4j.model.component.VTimeZone getVTimeZone(String id) {
        if(!ALL_TIMEZONE_IDS.contains(id))
            return null;
        
        com.ibm.icu.util.VTimeZone icuvtz = com.ibm.icu.util.VTimeZone.create(id);
        if(icuvtz==null)
            return null;
        
        try {
            StringWriter sw = new StringWriter();
            icuvtz.write(sw);
            VTimeZone vtz = (VTimeZone) CalendarUtils.parseComponent(sw.toString()); 
            return vtz;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Return simple ical4j VTimeZone instance (including only relevant
     * rules for given date.
     * @param id timezone id
     * @param time time in milliseconds to base rules off of
     * @return ical4j VTimeZone instance
     */
    public static net.fortuna.ical4j.model.component.VTimeZone getSimpleVTimeZone(String id, long time) {
        if(!ALL_TIMEZONE_IDS.contains(id))
            return null;
        
        com.ibm.icu.util.VTimeZone icuvtz = com.ibm.icu.util.VTimeZone.create(id);
        if(icuvtz==null)
            return null;
        
        StringWriter sw = new StringWriter();
        
        try {
            icuvtz.writeSimple(sw, time);
            VTimeZone vtz = (VTimeZone) CalendarUtils.parseComponent(sw.toString()); 
            return vtz;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Return equivalent Olson timezone id that corresponds to given 
     * VTIMEZONE definition
     * @param vtz VTIMEZONE
     * @return equivalent timezone id (null if no equivalent timezone found)
     */
    public static String getEquivalentTimeZoneId(VTimeZone vtz) {
        
        com.ibm.icu.util.VTimeZone icuvtz = com.ibm.icu.util.VTimeZone.create(new StringReader(vtz.toString()));
        for(String id: ALL_TIMEZONE_IDS) {
            com.ibm.icu.util.TimeZone tz = com.ibm.icu.util.TimeZone.getTimeZone(id);
            if(icuvtz.hasSameRules(tz))
                return id;
        }
        
        return null;
    }
}
