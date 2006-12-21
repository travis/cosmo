/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Provides utility methods for working with dates and times.
 */
public class DateUtil {

    /**
     * Note that this is not a perfect match to the RFC 3339
     * format. In particular, it does not correctly parse timezone
     * offsets of the form <code>xx:yy</code>. It requires RFC 822
     * style timezone offsets, of the form <code>xxyy</code>.
     */
    public static final String RFC_3339_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    /** */
    public static final String RFC_3339_TIMESTAMP_FORMAT =
        "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    /**
     * @throws ParseException  */
    public static Date parseRfc3339Date(String date) 
    	throws ParseException {
    	return parseDate(date, RFC_3339_DATE_FORMAT);
    }
    
    /**
     * @throws ParseException  */
    public static Date parseRfc3339TimeStamp(String date) 
    	throws ParseException {
    	return parseDate(date, RFC_3339_TIMESTAMP_FORMAT, GMT);
    }
    
    /** 
     * @throws ParseException  */
    public static Date parseDate(String date,
                                 String format)
    	throws ParseException {
        return parseDate(date, format, null);
    }

    /** 
     * @throws ParseException  */
    public static Date parseDate(String date,
                                 String format,
                                 TimeZone tz)
    	throws ParseException {
    	SimpleDateFormat formatter = new SimpleDateFormat(format);
        if (tz != null)
            formatter.setTimeZone(tz);
    	return formatter.parse(date);
    }

    /** */
    public static String formatRfc3339Date(Date date) {
        return formatRfc3339Date(date, null);
    }

    /** */
    public static String formatRfc3339Date(Date date,
                                           TimeZone tz) {
        return formatDate(RFC_3339_DATE_FORMAT, date, tz);
    }

    /** */
    public static String formatRfc3339Date(Calendar cal) {
        return formatDate(RFC_3339_DATE_FORMAT,
                          cal.getTime(), cal.getTimeZone());
    }

    /** */
    public static String formatRfc3339TimeStamp(Date date) {
        return formatDate(RFC_3339_TIMESTAMP_FORMAT, date, GMT);
    }

    /** */
    public static String formatDate(String pattern,
                                    Date date) {
        return formatDate(pattern, date, null);
    }

    /** */
    public static String formatDate(String pattern,
                                    Date date,
                                    TimeZone tz) {
    	SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        if (tz != null)
            formatter.setTimeZone(tz);
        return formatter.format(date);
    }
}
