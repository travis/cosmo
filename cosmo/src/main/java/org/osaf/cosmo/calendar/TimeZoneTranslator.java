/*
 * Copyright 2005-2007 Open Source Applications Foundation
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Provides methods for translating an arbitrary timezone into
 * an equivalent Olson timezone.  Translation relies on a set of
 * known timezone aliases.
 */
public class TimeZoneTranslator {
    
    // mapping of known timezone aliases to Olson tzids
    private Properties ALIASES = new Properties();

    private TimeZoneRegistry REGISTRY = TimeZoneRegistryFactory.getInstance()
            .createRegistry();
    
    private Map<Integer, List<TimeZone>> tzMap = null;
    private List<TimeZone> tzs = null;
    
    
    private static TimeZoneTranslator instance = new TimeZoneTranslator();
    
    private TimeZoneTranslator() {
        try {
            ALIASES.load(TimeZoneTranslator.class
                    .getResourceAsStream("/timezone.alias"));
        } catch (IOException e) {
            throw new RuntimeException("Error parsing tz aliases");
        }
    }
    
    public static TimeZoneTranslator getInstance() {
        return instance;
    }
    
    /**
     * Given a timezone and date, return the equivalent Olson timezone.
     * @param timezone timezone
     * @return equivalent Olson timezone
     */
    public TimeZone translateToOlsonTz(TimeZone timezone) {
        
        // First use registry to find Olson tz
        TimeZone translatedTz = REGISTRY.getTimeZone(timezone.getID());
        if(translatedTz!=null)
            return translatedTz;
        
        // Next check for known aliases
        String aliasedTzId = ALIASES.getProperty(timezone.getID());
        
        // If an aliased id was found, return the Olson tz from the registry
        if(aliasedTzId!=null)
            return REGISTRY.getTimeZone(aliasedTzId);
        
        // Try to find a substring match
        translatedTz = findSubStringMatch(timezone.getID());
        if(translatedTz!=null)
            return translatedTz;
        
        // failed to find match
        return null;
    }
    
    /**
     * Given a timezone id, return the equivalent Olson timezone.
     * @param tzId timezone id to translate
     * @return equivalent Olson timezone
     */
    public TimeZone translateToOlsonTz(String tzId) {
        
        // First use registry to find Olson tz
        TimeZone translatedTz = REGISTRY.getTimeZone(tzId);
        if(translatedTz!=null)
            return translatedTz;
        
        // Next check for known aliases
        String aliasedTzId = ALIASES.getProperty(tzId);
        
        // If an aliased id was found, return the Olson tz from the registry
        if(aliasedTzId!=null)
            return REGISTRY.getTimeZone(aliasedTzId);
        
        // Try to find a substring match
        translatedTz = findSubStringMatch(tzId);
        if(translatedTz!=null)
            return translatedTz;
        
        // failed to find match
        return null;
    }
    
    /**
     * Find and parse all Olson timezone definitions.  All timezone definitions
     * are assumed to be located in /timezones.  This is very CPU and IO
     * intensive to run for the first time as it has to parse 400 .ics files.
     * @return list of timezones
     */
    protected synchronized List<TimeZone> getOlsonTimeZones() {
        
        if(tzs!=null)
            return tzs;
        
        try {
            PathMatchingResourcePatternResolver pm = new PathMatchingResourcePatternResolver();
            Resource[] resources = pm.getResources("classpath*:timezones/**/*.ics");
            CalendarBuilder builder = new CalendarBuilder();
            tzs = new ArrayList<TimeZone>();
            
            for(Resource r: resources) {
                Calendar calendar = builder.build(r.getInputStream());
                VTimeZone vtz = (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
                tzs.add(new TimeZone(vtz));
            }
            
            return tzs;
        } catch (IOException e) {
            throw new RuntimeException("Error reading timezone defs");
        } catch (ParserException e) {
            throw new RuntimeException("Error parsing timezone defs");
        }
    }
    
    /**
     * Find and parse all Olson timezone definitions and partition into
     * buckets according to the raw offset.
     * @return map of timezone lists keyed by timezone raw offset
     */
    protected synchronized Map<Integer, List<TimeZone>> getOlsonTimeZonesByOffset() {
        if(tzMap!=null)
            return tzMap;
        
        List<TimeZone> tzs = getOlsonTimeZones();
        tzMap = new HashMap<Integer, List<TimeZone>>();
        for(TimeZone tz : tzs) {
            int offset = tz.getRawOffset();
            if(!tzMap.containsKey(offset))
                tzMap.put(new Integer(offset), new ArrayList<TimeZone>());
            
            tzMap.get(offset).add(tz);
        }
        
        return tzMap;
    }
    
    /**
     * Attempt to find a matching Olson timezone by searching for
     * a valid Olson TZID in the specified string at the end.  This
     * happens to be what Lightning does as its timezones look 
     * like: TZID=/mozilla.org/20050126_1/America/Los_Angeles
     * @param tzname
     * @return matching Olson timezone, null if no match found
     */
    protected TimeZone findSubStringMatch(String tzname) {
        for(TimeZone tz: getOlsonTimeZones())
            if(tzname.endsWith(tz.getID()))
                return tz;
        
        return null;
    }
}
