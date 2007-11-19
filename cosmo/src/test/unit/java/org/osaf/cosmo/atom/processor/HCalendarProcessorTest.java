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
package org.osaf.cosmo.atom.processor;

import java.io.Reader;

import junit.framework.TestCase;

import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.TestHelper;

/**
 * Test class for {@link HCalendarProcessor} tests.
 */
public class HCalendarProcessorTest extends TestCase {
    private static final Log log = LogFactory.getLog(HCalendarProcessorTest.class);

    protected TestHelper helper;

    public void testExample1() throws Exception {
        Reader content = helper.getReader("hcalendar/example1.xhtml");

        HCalendarProcessor processor = new HCalendarProcessor(helper.getEntityFactory());
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        assertEquals("Incorrect DTSTART", "DTSTART;VALUE=DATE:20071005", event.getStartDate().toString().trim());
        assertEquals("Incorrect DTEND", "DTEND;VALUE=DATE:20071020", event.getEndDate().toString().trim());
        assertEquals("Incorrect SUMMARY", "SUMMARY:Web 2.0 Conference", event.getSummary().toString().trim());
        assertEquals("Incorrect LOCATION", "LOCATION:Argent Hotel\\, San Francisco\\, CA", event.getLocation().toString().trim());
        assertEquals("Incorrect URL", "URL:http://www.web2con.com/", event.getUrl().toString().trim());
    }

    public void testExample2() throws Exception {
        Reader content = helper.getReader("hcalendar/example2.xhtml");

        HCalendarProcessor processor = new HCalendarProcessor(helper.getEntityFactory());
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        assertEquals("Incorrect DTSTART", "DTSTART:19980312T133000Z", event.getStartDate().toString().trim());
        assertEquals("Incorrect DTEND", "DTEND:19980312T143000Z", event.getEndDate().toString().trim());
        assertEquals("Incorrect SUMMARY", "SUMMARY:XYZ Project Review", event.getSummary().toString().trim());
        assertEquals("Incorrect UID", "UID:guid-1.host1.com", event.getUid().toString().trim());
        assertEquals("Incorrect DTSTAMP", "DTSTAMP:19980309T231000Z", event.getDateStamp().toString().trim());
        assertEquals("Incorrect LOCATION", "LOCATION:1CP Conference Room 4350", event.getLocation().toString().trim());
        assertEquals("Incorrect DESCRIPTION", "DESCRIPTION:Project XYZ Review Meeting", event.getDescription().toString().trim());
    }

    public void testSummaryInImgAlt() throws Exception {
        Reader content = helper.getReader("hcalendar/09-component-vevent-summary-in-img-alt.html");

        HCalendarProcessor processor = new HCalendarProcessor(helper.getEntityFactory());
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        assertEquals("Incorrect DTSTART", "DTSTART;VALUE=DATE:20060306", event.getStartDate().toString().trim());
        assertEquals("Incorrect DTEND", "DTEND;VALUE=DATE:20060310", event.getEndDate().toString().trim());
        assertEquals("Incorrect SUMMARY", "SUMMARY:O'Reilly Emerging Technology Conference", event.getSummary().toString().trim());
        assertEquals("Incorrect LOCATION", "LOCATION:Manchester Grand Hyatt in San Diego\\, CA", event.getLocation().toString().trim());
        assertEquals("Incorrect DESCRIPTION", "DESCRIPTION:3/6-9 @ Manchester Grand Hyatt in San Diego\\, CA", event.getDescription().toString().trim());
        assertEquals("Incorrect URL", "URL:http://conferences.oreillynet.com/et2006/", event.getUrl().toString().trim());
    }

    public void testEntity() throws Exception {
        Reader content = helper.getReader("hcalendar/10-component-vevent-entity.html");

        HCalendarProcessor processor = new HCalendarProcessor(helper.getEntityFactory());
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        assertEquals("Incorrect DTSTART", "DTSTART;VALUE=DATE:20060306", event.getStartDate().toString().trim());
        assertEquals("Incorrect SUMMARY", "SUMMARY:Cricket & Tennis Centre", event.getSummary().toString().trim());
        assertEquals("Incorrect DESCRIPTION", "DESCRIPTION:Melbourne's Cricket & Tennis Centres are in the heart of the city", event.getDescription().toString().trim());
    }

    public void testSummaryUrlInSameClass() throws Exception {
        Reader content = helper.getReader("hcalendar/12-component-vevent-summary-url-in-same-class.html");

        HCalendarProcessor processor = new HCalendarProcessor(helper.getEntityFactory());
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        assertEquals("Incorrect DTSTART", "DTSTART:20060125T000000", event.getStartDate().toString().trim());
        assertEquals("Incorrect SUMMARY", "SUMMARY:Art Reception for Tom Schultz and Felix Macnee", event.getSummary().toString().trim());
        assertEquals("Incorrect URL", "URL:http://www.laughingsquid.com/squidlist/calendar/12377/2006/1/25", event.getUrl().toString().trim());
    }

    public void testLang() throws Exception {
        Reader content = helper.getReader("hcalendar/hcalendar-calendar-lang-sub-lang.html");

        HCalendarProcessor processor = new HCalendarProcessor(helper.getEntityFactory());
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        assertEquals("Incorrect DTSTART", "DTSTART;VALUE=DATE:20051005", event.getStartDate().toString().trim());
        assertEquals("Incorrect DTEND", "DTEND;VALUE=DATE:20051008", event.getEndDate().toString().trim());
        assertEquals("Incorrect SUMMARY", "SUMMARY:Web 2.0 Conference", event.getSummary().toString().trim());
        assertEquals("Incorrect LOCATION", "LOCATION;LANGUAGE=de:Serrano Hotel\\, San Francisco\\, CA", event.getLocation().toString().trim());
        assertEquals("Incorrect URL", "URL:http://www.web2con.com/", event.getUrl().toString().trim());
    }

    public void testEventful() throws Exception {
        Reader content = helper.getReader("hcalendar/eventful-1.html");

        HCalendarProcessor processor = new HCalendarProcessor(helper.getEntityFactory());
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
    }

    protected void setUp() {
        helper = new TestHelper();
    }
}
