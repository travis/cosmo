/*
 * Copyright (c) 2006 SimDesk Technologies, Inc.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * SimDesk Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with SimDesk Technologies.
 *
 * SIMDESK TECHNOLOGIES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
 * THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  SIMDESK TECHNOLOGIES
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
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
