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
package org.osaf.cosmo.dao;

import java.util.Set;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;

/**
 * Interface for DAO that provides query apis for finding 
 * ContentItems with EventStamps matching certain criteria.
 * 
 */
public interface CalendarDao extends ItemDao {

    /**
     * Find calendar event with a specified icalendar uid. The icalendar format
     * requires that an event's uid is unique within a calendar.
     * 
     * @param uid
     *            icalendar uid of calendar event
     * @param collection
     *            collection to search
     * @return calendar event represented by uid and calendar
     */
    public ContentItem findEventByIcalUid(String uid,
            CollectionItem collection);
    

    /**
     * Find calendar events by filter.
     *
     * @param collection
     *            collection to search
     * @param filter
     *            filter to use in search
     * @return set ContentItem objects that contain EventStamps matching specified
     *         filter.
     */
    public Set<ContentItem> findEvents(CollectionItem collection,
                                             CalendarFilter filter);

}
