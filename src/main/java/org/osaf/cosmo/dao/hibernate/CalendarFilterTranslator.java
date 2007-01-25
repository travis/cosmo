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

import java.util.Set;

import org.hibernate.Session;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;

/**
 * Iterface that defines api for retrieving ContentItems
 * that contain EventStamps that match a given
 * CalendarFilter.
 * 
 * Used by CalendarDaoImpl.
 *
 */
public interface CalendarFilterTranslator {
    
    /**
     * Retrieve ContentItems that contain data that
     * matches the given CalendarFilter.
     * 
     * @param session Hibernate session obejct
     * @param collection parent collection to search
     * @param filter query filter
     * @return set of matching ContentItems
     */
    public Set<ContentItem> getCalendarItems(Session session,
            CollectionItem collection, CalendarFilter filter);
}
