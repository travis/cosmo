/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

package org.osaf.cosmo.dav.caldav;

import java.io.IOException;
import java.util.Iterator;
import java.io.StringReader;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.jackrabbit.webdav.property.DavProperty;

/**
 * Provides utilities for processing data sent in CalDAV requests.
 */
public class CaldavRequestHelper {

    /**
     * Creates an instance of <code>VTimeZone</code> from a
     * <code>CALENDAR:timezone</code> DAV property.
     *
     * The property value must be an iCalendar string. The further
     * requirements for a valid time zone are as per 
     * {@link #extractTimeZone(String)}.
     *
     * @param prop the <code>DavProperty</code> containing the
     * timezone
     * @return a <code>VTimeZone</code> representing the submitted
     * timezone, or <code>null</code> if the property or its value is
     * <code>null</code>
     * @throws IOException if an error occurs reading the iCalendar
     * string
     * @throws ParserException if the iCalendar string cannot be
     * successfully parsed
     * @throws ValidationException if the parsed iCalendar object does
     * not conform to the requirements specified above
     */
    public static VTimeZone extractTimeZone(DavProperty prop)
        throws IOException, ParserException, ValidationException {
        if (prop == null) {
            return null;
        }

        return extractTimeZone((String) prop.getValue());
    }

    /**
     * Creates an instance of <code>VTimeZone</code> from an
     * iCalendar string.
     *
     * The iCalendar string must include an enclosing VCALENDAR object
     * and exactly one enclosed VTIMEZONE component. All components,
     * properties and parameters are validated according to RFC 2445.
     *
     * @param ical the iCalendar string to parse
     * @return a <code>VTimeZone</code> representing the submitted
     * timezone, or <code>null</code> if the iCalendar string is
     * <code>null</code>
     * @throws IOException if an error occurs reading the iCalendar
     * string
     * @throws ParserException if the iCalendar string cannot be
     * successfully parsed
     * @throws ValidationException if the parsed iCalendar object does
     * not conform to the requirements specified above
     */
    public static VTimeZone extractTimeZone(String ical)
        throws IOException, ParserException, ValidationException {
        if (ical == null) {
            return null;
        }

        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(new StringReader(ical));
        calendar.validate(true);

        if (calendar.getComponents().size() > 1) {
            throw new ValidationException("CALDAV:timezone must contain exactly one VTIMEZONE component - too many components enclosed");
        }

        VTimeZone vtz = (VTimeZone)
            calendar.getComponents().getComponent(Component.VTIMEZONE);
        if (vtz == null) {
            throw new ValidationException("CALDAV:timezone must contain exactly one VTIMEZONE component - enclosed component not VTIMEZONE");
        }
 
       return vtz;
    }
}
