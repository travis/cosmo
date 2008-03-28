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

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.hcalendar.HCalendarParser;
import org.osaf.cosmo.model.EntityFactory;

/**
 * An {@link ICalendarProcessor} that reads calendar components from
 * hCalendar streams.
 *
 */
public class HCalendarProcessor extends BaseICalendarProcessor {
    private static final Log log = LogFactory.getLog(HCalendarProcessor.class);

    // BaseICalendarProcessor methods
    
    
    public HCalendarProcessor(EntityFactory entityFactory) {
        super(entityFactory);
    }

    /**
     * Converts the content body into a {@link VEvent}. The provided
     * content must be a valid hCalendar document containing a vevent. If more
     * than one vevent is present, only the first is processed.
     *
     * @throws ValidationException if the content does not represent a
     * valid hCalendar event
     * @throws ProcessorException
     */
    protected CalendarComponent readCalendarComponent(Reader content)
        throws ValidationException, ProcessorException {
        HCalendarParser parser = new HCalendarParser();
        CalendarBuilder builder = new CalendarBuilder(parser);

        try {
            Calendar calendar = builder.build(content);
            Iterator<CalendarComponent> events = (Iterator<CalendarComponent>)
                calendar.getComponents(Component.VEVENT).iterator();
            if (! events.hasNext())
                throw new ValidationException("hCalendar document must contain a vevent");
            return events.next();
        } catch (IOException e) {
            throw new ProcessorException("Unable to read hCalendar content", e);
        } catch (ParserException e) {
            throw new ValidationException("Unable to parse hCalendar content", e);
        }
    }
}
