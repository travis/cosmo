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
package org.osaf.cosmo.calendar;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.util.TimeZoneUtils;

/**
 * Implementation of a <code>TimeZoneRegistry</code>. This implementation will 
 * use VTIMEZONE definitions provided by icu 3.8
 */
public class CosmoICUTimeZoneRegistry implements TimeZoneRegistry {

    private Log log = LogFactory.getLog(CosmoICUTimeZoneRegistry.class);

    private static final Map<String, TimeZone> DEFAULT_TIMEZONES = new HashMap<String, TimeZone>();

    private static final Properties ALIASES = new Properties();
    static {
        try {
            ALIASES.load(CosmoICUTimeZoneRegistry.class
                    .getResourceAsStream("/timezone.alias"));
        }
        catch (IOException ioe) {
            LogFactory.getLog(CosmoICUTimeZoneRegistry.class).warn(
                    "Error loading timezone aliases: " + ioe.getMessage());
        }
    }

    private Map<String, TimeZone> timezones = new HashMap<String, TimeZone>();

    /**
     * Default constructor.
     */
    public CosmoICUTimeZoneRegistry() {
    }

    
    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.model.TimeZoneRegistry#register(net.fortuna.ical4j.model.TimeZone)
     */
    public final void register(final TimeZone timezone) {
        timezones.put(timezone.getID(), timezone);
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.model.TimeZoneRegistry#clear()
     */
    public final void clear() {
        timezones.clear();
    }

    /*
     * (non-Javadoc)
     * @see net.fortuna.ical4j.model.TimeZoneRegistry#getTimeZone(java.lang.String)
     */
    public final TimeZone getTimeZone(final String id) {
        TimeZone timezone = (TimeZone) timezones.get(id);
        if (timezone == null) {
            timezone = (TimeZone) DEFAULT_TIMEZONES.get(id);
            if (timezone == null) {
                synchronized (DEFAULT_TIMEZONES) {
                    try {
                        VTimeZone vTimeZone = TimeZoneUtils.getVTimeZone(id);
                        if (vTimeZone != null) {
                            timezone = new TimeZone(vTimeZone);
                            DEFAULT_TIMEZONES.put(timezone.getID(), timezone);
                        }
                    }
                    catch (Exception e) {
                        log.warn("Error occurred loading VTimeZone", e);
                    }
                }
                if(timezone==null) {
                    // if timezone not found with identifier, try loading an alias..
                    String alias = ALIASES.getProperty(id);
                    if (alias != null) {
                        return getTimeZone(alias);
                    }
                }
            }
        }
        return timezone;
    }
}
