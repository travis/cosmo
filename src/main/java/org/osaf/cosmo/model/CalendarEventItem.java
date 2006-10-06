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
package org.osaf.cosmo.model;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Extends {@link CalendarItem} to represent a calendar item containing a single
 * calendar event (VEVENT).
 */
public class CalendarEventItem extends CalendarItem {
    /**
     * 
     */
    private static final long serialVersionUID = 4090512843371394571L;

    public CalendarEventItem() {
    }

    /**
     */
    public VEvent getMasterEvent() {
        return (VEvent) getCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }

    // XXX: need methods for returning all instances, including
    // master, recurrence and exception instances, with properties and
    // parameters inherited from the master instance except where
    // overridden by exception instances. should also be able to
    // return just the instances for a given time range.
}
