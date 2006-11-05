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
import java.util.Date;
import java.util.TimeZone;

/**
 * Provides utility methods for working with dates and times.
 */
public class DateUtil {

    /** */
    public static final String RFC_3339_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * @throws ParseException  */
    public static Date parseRfc3339Date(String date) 
    	throws ParseException{
    	return parseRfc3339Date(date, RFC_3339_DATE_FORMAT);
    }
    
    /** 
     * @throws ParseException  */
    public static Date parseRfc3339Date(String date, String format)
    	throws ParseException{
    	SimpleDateFormat formatter = new SimpleDateFormat(format);
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
