/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.icalendar;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.property.*;

/**
 * Utilities for working with iCalendar in Cosmo.
 */
public class ICalendarUtils {

    /**
     */
    public static Clazz getClazz(Component component) {
        return (Clazz) component.getProperties().getProperty(Property.CLASS);
    }

    /**
     */
    public static RecurrenceId getRecurrenceId(Component component) {
        return (RecurrenceId) component.getProperties().
            getProperty(Property.RECURRENCE_ID);
    }

    /**
     */
    public static RRule getRRule(Component component) {
        return (RRule) component.getProperties().getProperty(Property.RRULE);
    }

    /**
     */
    public static Uid getUid(Component component) {
        return (Uid) component.getProperties().getProperty(Property.UID);
    }
}
