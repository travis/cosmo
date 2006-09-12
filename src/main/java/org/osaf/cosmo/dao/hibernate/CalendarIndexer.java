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
package org.osaf.cosmo.dao.hibernate;

import net.fortuna.ical4j.model.Calendar;

import org.hibernate.Session;
import org.osaf.cosmo.model.CalendarItem;
import org.osaf.cosmo.model.Item;

/**
 * Interface for a Calendar Item indexer. A CalendarIndexer is responsible for
 * indexing the content of a calendar item. Since an item only stores basic
 * attributes, advanced indexing techniques will require data to be stored
 * elsewhere.
 * 
 * An indexer indexes the calendar content so that the DbItem can be found
 * quickly. The major usecase for an indexer will be for timerange indexes.
 * 
 */
public interface CalendarIndexer {

    public abstract void indexCalendarEvent(Session session, CalendarItem item,
            Calendar calendar);

}
