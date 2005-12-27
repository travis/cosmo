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
package org.osaf.cosmo.ui.bean;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

/**
 * A simple bean that translates the information about a set of
 * calendar components from the iCalendar format represented by
 * iCal4j to one more accessible to JSP.
 */
public class CalendarBean {

    private Calendar vcalendar;

    /**
     */
    public CalendarBean(Calendar calendar) {
        this.vcalendar = calendar;
    }

    /**
     */
    public String toString() {
        return vcalendar.toString();
    }
}
