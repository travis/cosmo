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
package org.osaf.cosmo.calendar.util;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.TimeZoneTranslator;

/**
 * Utility methods for dealing with dates.
 *
 */
public class Dates {
    
    private static final Log log =
        LogFactory.getLog(Dates.class);
    
    
    /**
     * Returns a new date instance matching the type and timezone of the other
     * date. If no type is specified a DateTime instance is returned.
     * 
     * @param date
     *            a seed Java date instance
     * @param type
     *            the type of date instance
     * @return an instance of <code>net.fortuna.ical4j.model.Date</code>
     */
    public static Date getInstance(final java.util.Date date, final Date type) {
        if ((type == null) || (type instanceof DateTime)) {
            DateTime dt = new DateTime(date);
            if (((DateTime) type).isUtc()) {
                dt.setUtc(true);
            } else {
                dt.setTimeZone(((DateTime) type).getTimeZone());
            }
            return dt;
        } else
            return new Date(date);
    }
    
    /**
     * Serialize an ical4j Date to a String in the format:
     * <p>
     * <code>;VALUE=DATE-TIME;TZID=America/Los_Angeless:20070101T073000</code>
     * <p>
     * If the date/time contains a non-Olson timezone, the timezone will
     * try to be converted to an Olson timezone.  If an equivalent Olson
     * timezone is not found, then the date is serialized as a floating
     * date/time.
     * 
     * @param date date to serialize
     * @return String representation of date
     */
    public static String fromDateToString(Date date) {
        Value value = null;
        TimeZone tz = null;
        Parameter tzid = null;
        
        if (date instanceof DateTime) {
            value = Value.DATE_TIME;
            tz = ((DateTime) date).getTimeZone();
            
            // Make sure timezone is Olson.  If the translator can't
            // translate the timezon to an Olson timezone, then
            // the event will essentially be floating.
            if (tz != null) {
                String origId = tz.getID();
                tz = TimeZoneTranslator.getInstance().translateToOlsonTz(tz);
                if(tz==null)
                    log.warn("no Olson timezone found for " + origId);
            }
            
            if(tz != null) {
                String id = tz.getVTimeZone().getProperties().
                    getProperty(Property.TZID).getValue();
                tzid = new TzId(id);
            }
        } else {
            value = Value.DATE;
        }
       
        StringBuffer buf = new StringBuffer(";");
        buf.append(value.toString());
        if (tzid != null)
            buf.append(";").append("TZID=").append(tzid.getValue());
       
        buf.append(":").append(date.toString());
        return buf.toString();
    }
}
