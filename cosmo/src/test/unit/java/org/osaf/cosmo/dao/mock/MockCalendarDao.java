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
package org.osaf.cosmo.dao.mock;

import java.util.HashSet;
import java.util.Set;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;

/**
 * Mock implementation of <code>CalendarDao</code> useful for testing.
 *
 * @see CalendarDao
 * @see CalendarItem
 * @see CalendarEventItem
 * @see CalendarCollectionItem
 */
public class MockCalendarDao extends MockItemDao implements CalendarDao {

    private CalendarFilter lastCalendarFilter;
    
    /**
     * Useful for unit tests.
     * @return
     */
    public CalendarFilter getLastCalendarFilter() {
        return lastCalendarFilter;
    }

    /**
     */
    public MockCalendarDao(MockDaoStorage storage) {
        super(storage);
    }

    // CalendarDao methods

  
    /**
     * Find calendar events by filter.
     * NOTE: This impl always returns an empty set, but has the side effect 
     * of setting the last 
     * @param calendar
     *            calendar collection to search
     * @param filter
     *            filter to use in search
     * @return set CalendarEventItem objects matching specified
     *         filter.
     */
    public Set<ContentItem> findEvents(CollectionItem calendar,
                                             CalendarFilter filter) {
        lastCalendarFilter = filter;
        return new HashSet<ContentItem>();

    }

    

    public ContentItem findEventByIcalUid(String uid, CollectionItem calendar) {
        // TODO implement
        return null;
    }

    public void indexEvent(EventStamp eventStamp) {
        // TODO implement
    }
    
    
    
}
