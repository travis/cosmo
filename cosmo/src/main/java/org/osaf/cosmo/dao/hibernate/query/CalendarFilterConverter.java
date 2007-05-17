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
package org.osaf.cosmo.dao.hibernate.query;

import java.util.Iterator;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.PropertyFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;

/**
 * Translates <code>CalendarFilter</code> into <code>ItemFilter</code>
 */
public class CalendarFilterConverter {
    
    public CalendarFilterConverter() {}
    
    /**
     * Tranlsate CalendarFilter to an equivalent ItemFilter.
     * For now, only the basic CalendarFilter is supported, which is
     * essentially a timerange filter.  The majority of CalendarFilters
     * will fall into this case.  More cases will be supported as they
     * are implemented.
     * @param calendar parent calendar
     * @param calendarFilter filter to translate
     * @return equivalent ItemFilter
     */
    public ItemFilter translateToItemFilter(CollectionItem calendar, CalendarFilter calendarFilter) {
        NoteItemFilter itemFilter = new NoteItemFilter();
        itemFilter.setParent(calendar);
        ComponentFilter rootFilter = calendarFilter.getFilter();
        if(!"VCALENDAR".equalsIgnoreCase(rootFilter.getName()))
            throw new IllegalArgumentException("unsupported component filter: " + rootFilter.getName());
        
        for(Iterator it = rootFilter.getComponentFilters().iterator(); it.hasNext();) {
            ComponentFilter compFilter = (ComponentFilter) it.next();
            handleCompFilter(compFilter, itemFilter);
        }
        
        return itemFilter;
    }
        
    private void handleCompFilter(ComponentFilter compFilter, NoteItemFilter itemFilter) {
        
        if(!"VEVENT".equalsIgnoreCase(compFilter.getName()))
            throw new IllegalArgumentException("unsupported component filter: " + compFilter.getName());
        
        // TODO: handle case of multiple VEVENT filters
        EventStampFilter eventFilter = new EventStampFilter();
        itemFilter.getStampFilters().add(eventFilter);
        
        // handle time-range filter
        if(compFilter.getTimeRangeFilter()!=null) {
            eventFilter.setPeriod(compFilter.getTimeRangeFilter().getPeriod());
            eventFilter.setTimezone(eventFilter.getTimezone());
        }
            
        for(Iterator it = compFilter.getComponentFilters().iterator(); it.hasNext();) {
            ComponentFilter subComp = (ComponentFilter) it.next();
            throw new IllegalArgumentException("unsupported sub component filter: " + subComp.getName());
        }
        
        for(Iterator it = compFilter.getPropFilters().iterator(); it.hasNext();) {
            PropertyFilter propFilter = (PropertyFilter) it.next();
            throw new IllegalArgumentException("unsupported prop filter: " + propFilter.getName());
        }
    }
        
}
