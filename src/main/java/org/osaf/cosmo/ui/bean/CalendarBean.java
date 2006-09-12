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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.data.ParserException;

import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.CalendarCollectionItem;

/**
 * A simple bean that translates the information about a set of
 * calendar components from the iCalendar format represented by
 * iCal4j to one more accessible to JSP.
 */
public class CalendarBean {

    private CalendarCollectionItem item;
    private HashSet<EventBean> events;

    /**
     */
    public CalendarBean(CalendarCollectionItem item)
        throws IOException, ParserException {
        this.item = item;
        events = new HashSet<EventBean>();

        // XXX no way to get child items
//         for (Iterator<EventBean> i=item.getItems().iterator(); i.hasNext();) {
//             Item child = (Item) i.next();
//             if (child instanceof CalendarEventItem) {
//                 events.add(new EventBean((CalendarEventItem) child));
//             }
//        }
    }

    /**
     */
    public CalendarCollectionItem getItem() {
        return item;
    }

    /**
     */
    public Set getEvents() {
        return events;
    }

    /**
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Iterator<EventBean> i=events.iterator(); i.hasNext();) {
            EventBean event = i.next();
            buf.append(event.toString());
            if (i.hasNext()) {
                buf.append("\n");
            }
        }
        return buf.toString();
    }
}
