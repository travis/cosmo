/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.osaf.cosmo.icalendar.ICalendarConstants;

/**
 * Utility methods for working with icalendar data.
 */
public class CalendarUtils implements ICalendarConstants {
    
    
    /**
     * Convert Calendar object to String
     * @param calendar
     * @return string representation of calendar
     */
    public static String outputCalendar(Calendar calendar) 
        throws ValidationException, IOException {
        if (calendar == null)
            return null;
        CalendarOutputter outputter = new CalendarOutputter();
        StringWriter sw = new StringWriter();
        outputter.output(calendar, sw);
        return sw.toString();
    }

    /**
     * Parse icalendar string into Calendar object.
     * @param calendar icalendar string
     * @return Calendar object
     */
    public static Calendar parseCalendar(String calendar) 
        throws ParserException, IOException {
        if (calendar == null)
            return null;
        CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
        clearTZRegistry(builder);
        
        StringReader sr = new StringReader(calendar);
        return builder.build(sr);
    }
    
    /**
     * Parse icalendar string into calendar component
     * @param calendar icalendar string
     * @return Component object
     */
    public static Component parseComponent(String component) 
        throws ParserException, IOException {
        if (component == null)
            return null;
        // Don't use dispenser as this method may be called from within
        // a build() as in the case of the custom timezone registry
        // parsing a timezone
        CalendarBuilder builder = new CalendarBuilder();
        StringReader sr = new StringReader("BEGIN:VCALENDAR\n" + component + "END:VCALENDAR");
        
        return (Component) builder.build(sr).getComponents().get(0);
    }

    /**
     * Parse icalendar data from Reader into Calendar object.
     * @param reader icalendar data reader
     * @return Calendar object
     */
    public static Calendar parseCalendar(Reader reader)
        throws ParserException, IOException {
        if (reader == null)
            return null;
        CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
        clearTZRegistry(builder);
        return builder.build(reader);
    }

    /**
     * Parse icalendar data from byte[] into Calendar object.
     * @param content icalendar data
     * @return Calendar object
     * @throws Exception
     */
    public static Calendar parseCalendar(byte[] content) 
        throws ParserException, IOException {
        CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
        clearTZRegistry(builder);
        return builder.build(new ByteArrayInputStream(content));
    }

    /**
     * Parse icalendar data from InputStream
     * @param is icalendar data inputstream
     * @return Calendar object
     * @throws Exception
     */
    public static Calendar parseCalendar(InputStream is) 
        throws ParserException, IOException {
        CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
        clearTZRegistry(builder);
        return builder.build(is);
    }
    
    public static Calendar copyCalendar(Calendar calendar) {
        if (calendar == null)
            return null;
        try {
            return new Calendar(calendar);
        } catch (Exception e) {
           throw new RuntimeException("error copying calendar: " + calendar, e);
        } 
    }
    
    public static Component copyComponent(Component comp) {
        try {
            return comp.copy();
        } catch (Exception e) {
           throw new RuntimeException("error copying component: " + comp, e);
        } 
    }

    public static boolean isSupportedComponent(String type) {
        for (String s : SUPPORTED_COMPONENT_TYPES)
            if (s.equalsIgnoreCase(type)) return true;
        return false;
    }
    
    public static boolean isSupportedCollation(String collation) {
        for (String s : SUPPORTED_COLLATIONS)
            if (s.equalsIgnoreCase(collation)) return true;
        return false;
    }

    public static boolean hasMultipleComponentTypes(Calendar calendar) {
        String found = null;
        for (Iterator<Component> i=calendar.getComponents().iterator();
             i.hasNext();) {
            Component component = i.next();
            if (component instanceof VTimeZone)
                continue;
            if (found == null) {
                found = component.getName();
                continue;
            }
            if (! found.equals(component.getName()))
                return true;
        }
        return false;
    }

    public static boolean hasSupportedComponent(Calendar calendar) {
        for (Iterator<Component> i=calendar.getComponents().iterator();
             i.hasNext();) {
            if (isSupportedComponent(i.next().getName()))
                 return true;
         }
         return false;
    }
    
    private static void clearTZRegistry(CalendarBuilder cb) {
        // clear timezone registry if present
        TimeZoneRegistry tzr = cb.getRegistry();
        if(tzr!=null)
            tzr.clear();
    }
}
