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
 *
 * Copyright (c) 2005, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osaf.cosmo.calendar;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a <code>TimeZoneRegistry</code>. This implementation will 
 * search the classpath for applicable VTimeZone definitions used to back the 
 * provided TimeZone instances.  This code is based off of the 
 * <code>TimeZoneRegistryImpl</code> class in ical4j, with
 * the only differences being that the timezone alias file is different and 
 * that timezone aliases are searched only after the timezone could not be loaded 
 * from a resource file located in zoneinfo.  This allows aliases to be overridden
 * by adding a resource to zoneinfo.  Also, cosmo uses timezone definnitions 
 * generated from the vobject python library, which uses the timezone 
 * definitions from icu 3.6.
 */
public class CosmoTimeZoneRegistry implements TimeZoneRegistry {

    private static final String DEFAULT_RESOURCE_PREFIX = "/timezones/";

    private Log log = LogFactory.getLog(CosmoTimeZoneRegistry.class);

    private static final Map DEFAULT_TIMEZONES = new HashMap();

    private static final Properties ALIASES = new Properties();
    static {
        try {
            ALIASES.load(CosmoTimeZoneRegistry.class
                    .getResourceAsStream("/timezone.alias"));
        }
        catch (IOException ioe) {
            LogFactory.getLog(CosmoTimeZoneRegistry.class).warn(
                    "Error loading timezone aliases: " + ioe.getMessage());
        }
    }

    private Map timezones;

    private String resourcePrefix;

    /**
     * Default constructor.
     */
    public CosmoTimeZoneRegistry() {
        this(DEFAULT_RESOURCE_PREFIX);
    }

    /**
     * Creates a new instance using the specified resource prefix.
     * @param resourcePrefix a prefix prepended to classpath resource lookups for default timezones
     */
    public CosmoTimeZoneRegistry(final String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
        timezones = new HashMap();
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
                        VTimeZone vTimeZone = loadVTimeZone(id);
                        if (vTimeZone != null) {
                            // XXX: temporary kludge..
                            // ((TzId) vTimeZone.getProperties().getProperty(Property.TZID)).setValue(id);
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

    /**
     * Loads an existing VTimeZone from the classpath corresponding to the specified Java timezone.
     */
    private VTimeZone loadVTimeZone(final String id) throws IOException,
            ParserException {
        
        URL resource = CosmoTimeZoneRegistry.class.getResource(resourcePrefix
                + id + ".ics");
        if (resource != null) {
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(resource.openStream());
            return (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
        }
        return null;
    }
}
