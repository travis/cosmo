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
package org.osaf.cosmo.eim.schema;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Trigger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EimValueConverterTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(EimValueConverterTest.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    public void testToICalDatesAsDateTime() throws Exception {
        String str = ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";

        DateList dl = EimValueConverter.toICalDates(str);
        assertNotNull("null date list", dl);
        assertEquals("wrong number of dates", 1, dl.size());

        TimeZone tz = dl.getTimeZone();
        assertNotNull("null timezone", tz);
        assertEquals("incorrect timezone", "America/Los_Angeles", tz.getID());

        assertEquals("not datetime", Value.DATE_TIME, dl.getType());
        DateTime dt = (DateTime) dl.get(0);
        assertEquals("incorrect datetime", "20021010T120000", dt.toString());

        TimeZone dtTz = dt.getTimeZone();
        assertNotNull("null datetime timezone", dtTz);
        assertEquals("incorrect datetime timezone", "America/Los_Angeles",
                     dtTz.getID());
    }
    
    public void testToICalDatesAsDate() throws Exception {
        String str = ";VALUE=DATE:20021010";

        DateList dl = EimValueConverter.toICalDates(str);
        assertNotNull("null date list", dl);
        assertEquals("wrong number of dates", 1, dl.size());

        assertNull("not null timezone", dl.getTimeZone());
        assertEquals("not date", Value.DATE, dl.getType());

        assertEquals("incorrect date", "20021010", dl.get(0).toString());
    }
    
    public void testToICalDatesMultiple() throws Exception {
        String str = ";VALUE=DATE:20021010,20021011,20021012";

        DateList dl = EimValueConverter.toICalDates(str);
        assertNotNull("null date list", dl);
        assertEquals("wrong number of dates", 3, dl.size());

        assertNull("not null timezone", dl.getTimeZone());

        assertEquals("not date", Value.DATE, dl.getType());

        assertEquals("incorrect date", "20021010", dl.get(0).toString());
        assertEquals("incorrect date", "20021011", dl.get(1).toString());
        assertEquals("incorrect date", "20021012", dl.get(2).toString());
    }
    
    public void testToICalDateAsDateTime() throws Exception {
        String str = ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";

        Date d = EimValueConverter.toICalDate(str);
        assertNotNull("null date", d);
        assertTrue("not datetime", d instanceof DateTime);

        DateTime dt = (DateTime) d;
        assertEquals("incorrect datetime", "20021010T120000", dt.toString());

        TimeZone tz = dt.getTimeZone();
        assertNotNull("null timezone", tz);
        assertEquals("incorrect timezone", "America/Los_Angeles", tz.getID());
    }
    
    public void testToICalDateAsDate() throws Exception {
        String str = ";VALUE=DATE:20021010";

        Date d = EimValueConverter.toICalDate(str);
        assertNotNull("null date", d);
        assertFalse("not date", d instanceof DateTime);
        assertEquals("incorrect date", "20021010", d.toString());
    }
    
    public void testToICalDateNoParams() throws Exception {
        DateList dl = EimValueConverter.toICalDates("20021010T120000");
        assertEquals("not datetime", Value.DATE_TIME, dl.getType());
        assertNull("timezone where none should be", dl.getTimeZone());
    }

    public void testToICalDateQuotedParam() throws Exception {
        EimValueConverter.toICalDate(";VALUE=\"DATE-TIME\":20021010T120000");
    }

    public void testToICalDateParseErrors() throws Exception {
        try {
            EimValueConverter.toICalDate(";FOO=bar:20021010T120000");
            fail("converted with bad param FOO");
        } catch (EimConversionException e) {}

        try {
            EimValueConverter.toICalDate(";TZID=deadbeef:20021010T120000");
            fail("converted with bad TZID deadbeef");
        } catch (EimConversionException e) {}

        try {
            EimValueConverter.toICalDate(";VALUE=deadbeef:20021010T120000");
            fail("converted with bad VALUE deadbeef");
        } catch (EimConversionException e) {}

        try {
            EimValueConverter.toICalDate(";VALUE=\"DATE-TIME:20021010T120000");
            fail("converted with unclosed doublequotes");
        } catch (EimConversionException e) {}

        try {
            EimValueConverter.toICalDate("deadbeef");
            fail("converted with bad text deadbeef");
        } catch (EimConversionException e) {}
    }
    
    public void testFromICalDatesAsDateTime() throws Exception {
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        DateList dl = new DateList(Value.DATE_TIME, tz);
        dl.add(new DateTime("20021010T120000", tz));

        String test = ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";
        String result = EimValueConverter.fromICalDates(dl);
        assertNotNull("null result", result);
        assertEquals(test, result);
    }

    public void testFromICalDatesAsDate() throws Exception {
        DateList dl = new DateList(Value.DATE);
        dl.add(new Date("20021010"));

        String test = ";VALUE=DATE:20021010";
        String result = EimValueConverter.fromICalDates(dl);
        assertNotNull("null result", result);
        assertEquals(test, result);
    }

    public void testFromICalDatesMultiple() throws Exception {
        DateList dl = new DateList(Value.DATE);
        dl.add(new Date("20021010"));
        dl.add(new Date("20021011"));
        dl.add(new Date("20021012"));

        String test = ";VALUE=DATE:20021010,20021011,20021012";
        String result = EimValueConverter.fromICalDates(dl);
        assertNotNull("null result", result);
        assertEquals(test, result);
    }
    
    public void testFromICalDateAsDateTime() throws Exception {
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        DateTime dt = new DateTime("20021010T120000", tz);

        String test = ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";
        String result = EimValueConverter.fromICalDate(dt);
        assertNotNull("null result", result);
        assertEquals(test, result);
    }
    
    public void testFromICalDateAsDate() throws Exception {
        Date d = new Date("20021010");

        String test = ";VALUE=DATE:20021010";
        String result = EimValueConverter.fromICalDate(d);
        assertNotNull("null result", result);
        assertEquals(test, result);
    }
    
    public void testToIcalTrigger() throws Exception {
        String strTrigger = ";RELATED=END:P7W";
        Trigger trigger = EimValueConverter.toIcalTrigger(strTrigger);
        Assert.assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));
        
        strTrigger = "PT15M";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        Assert.assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));
        
        strTrigger = "-PT15M15S";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        Assert.assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));
    
        strTrigger = ";VALUE=DATE-TIME:19970317T133000Z";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        Assert.assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));
        
        strTrigger = "P15W";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        Assert.assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));
        
        strTrigger = "-P2D";
        trigger = EimValueConverter.toIcalTrigger(strTrigger);
        Assert.assertEquals(strTrigger, EimValueConverter.fromIcalTrigger(trigger));
        
        strTrigger = ";RELATED=END:19970317T133000Z";
        try {
            trigger = EimValueConverter.toIcalTrigger(strTrigger);
            Assert.fail("able to convert invalid trigger");
        } catch (EimConversionException e) {
        }
        
        strTrigger = "foo";
        try {
            trigger = EimValueConverter.toIcalTrigger(strTrigger);
            Assert.fail("able to convert invalid trigger");
        } catch (EimConversionException e) {
        }
        
    }
}
