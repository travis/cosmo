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
package org.osaf.cosmo.calendar.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

public class CalendarUtils {
	public static String outputCalendar(Calendar calendar)
	{
		if(calendar==null)
			return null;
		CalendarOutputter outputter = new CalendarOutputter();
		StringWriter sw = new StringWriter();
		try {
			outputter.output(calendar, sw);
		} catch (IOException e) {
			return null;
		} catch (ValidationException e) {
			return null;
		}
		return sw.toString();
	}
	
	public static Calendar parseCalendar(String calendar)
	{
		if(calendar==null)
			return null;
		CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
		StringReader sr = new StringReader(calendar);
		try {
			return builder.build(sr);
		} catch (IOException e) {
			return null;
		} catch (ParserException e) {
			return null;
		}
	}
    
    public static Calendar parseCalendar(byte[] content) throws Exception {
        Calendar calendar = new CalendarBuilder()
                .build(new ByteArrayInputStream(content));
        return calendar;
    }
}
