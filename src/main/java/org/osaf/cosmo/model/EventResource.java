/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this calendar except in compliance with the License.
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
package org.osaf.cosmo.model;

import java.io.IOException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

/**
 * Extends {@link CalendarResource} to represent an event resource.
 */
public class EventResource extends CalendarResource {

    private Calendar calendar;

    /**
     */
    public EventResource() {
        super();
    }

    /**
     * Returns a {@link net.fortuna.ical4j.model.Calendar}
     * representing the content of this resource. This method will
     * only parse the content once, returning the same calendar
     * instance on subsequent invocations.
     */
    public Calendar getCalendar()
        throws IOException, ParserException {
        if (calendar == null) {
            calendar = new CalendarBuilder().build(getContent());
        }
        return calendar;
    }
}
