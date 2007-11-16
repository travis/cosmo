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

        HCalendarProcessor processor = new HCalendarProcessor();
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        System.out.println("Example 1:\n" + event.toString());
    }

    public void testExample2() throws Exception {
        Reader content = helper.getReader("hcalendar/example2.xhtml");

        HCalendarProcessor processor = new HCalendarProcessor();
        CalendarComponent component = processor.readCalendarComponent(content);
        assertNotNull("Null component", component);
        assertTrue("Component not a VEVENT", component instanceof VEvent);

        VEvent event = (VEvent) component;
        System.out.println("Example 2:\n" + event.toString());
    }

    protected void setUp() {
        helper = new TestHelper();
    }
}
