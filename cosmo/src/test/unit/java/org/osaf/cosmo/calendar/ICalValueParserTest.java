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
package org.osaf.cosmo.calendar;

import java.text.ParseException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ICalValueParserTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(ICalValueParserTest.class);

    public void testParseWithParams() throws Exception {
        String str =
            ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        parser.parse();

        assertNotNull("null value", parser.getValue());
        assertEquals("incorrect value", "20021010T120000", parser.getValue());

        assertEquals("wrong number of params", 2,
                     parser.getParams().keySet().size());
        assertEquals("wrong VALUE value", "DATE-TIME",
                     parser.getParams().get("VALUE"));
        assertEquals("wrong TZID value", "America/Los_Angeles",
                     parser.getParams().get("TZID"));
    }
    
    public void testParseWithoutParams() throws Exception {
        String str = "20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        parser.parse();

        assertNotNull("null value", parser.getValue());
        assertEquals("incorrect value", "20021010T120000", parser.getValue());

        assertEquals("wrong number of params", 0,
                     parser.getParams().keySet().size());
    }

    public void testParseQuotedParam() throws Exception {
        String str =";VALUE=\"DATE-TIME\":20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        parser.parse();

        assertNotNull("null value", parser.getValue());
        assertEquals("incorrect value", "20021010T120000", parser.getValue());

        assertEquals("wrong number of params", 1,
                     parser.getParams().keySet().size());
        assertEquals("wrong VALUE value", "DATE-TIME",
                     parser.getParams().get("VALUE"));
    }

    public void testParseUnclosedQuotes() throws Exception {
        String str = ";VALUE=\"DATE-TIME:20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        try {
            parser.parse();
            fail("parsed param value with unclosed quotes");
        } catch (ParseException e) {}
    }
}
