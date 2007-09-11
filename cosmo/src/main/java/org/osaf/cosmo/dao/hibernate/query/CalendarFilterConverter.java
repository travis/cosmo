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

import net.fortuna.ical4j.model.TimeZone;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.ParamFilter;
import org.osaf.cosmo.calendar.query.PropertyFilter;
import org.osaf.cosmo.calendar.query.TextMatchFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;

/**
 * Translates <code>CalendarFilter</code> into <code>ItemFilter</code>
 */
public class CalendarFilterConverter {
    
    private static final String COMP_VCALENDAR = "VCALENDAR";
    private static final String COMP_VEVENT = "VEVENT";
    private static final String PROP_UID = "UID";
    private static final String PROP_DESCRIPTION = "DESCRIPTION";
    private static final String PROP_SUMMARY = "SUMMARY";
    
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
        if(!COMP_VCALENDAR.equalsIgnoreCase(rootFilter.getName()))
            throw new IllegalArgumentException("unsupported component filter: " + rootFilter.getName());
        
        for(Iterator it = rootFilter.getComponentFilters().iterator(); it.hasNext();) {
            ComponentFilter compFilter = (ComponentFilter) it.next();
            handleCompFilter(compFilter, itemFilter);
        }
        
        return itemFilter;
    }
        
    private void handleCompFilter(ComponentFilter compFilter, NoteItemFilter itemFilter) {
        
        if(COMP_VEVENT.equalsIgnoreCase(compFilter.getName()))
            handleEventCompFilter(compFilter, itemFilter);
        else
            throw new IllegalArgumentException("unsupported component filter: " + compFilter.getName());
    }
    
    private void handleEventCompFilter(ComponentFilter compFilter, NoteItemFilter itemFilter) {
        // TODO: handle case of multiple VEVENT filters
        EventStampFilter eventFilter = new EventStampFilter();
        itemFilter.getStampFilters().add(eventFilter);
        
        TimeRangeFilter trf = compFilter.getTimeRangeFilter();
        
        // handle time-range filter
        if(trf!=null) {
            eventFilter.setPeriod(trf.getPeriod());
            if(trf.getTimezone()!=null)
            eventFilter.setTimezone(new TimeZone(trf.getTimezone()));
        }
            
        for(Iterator it = compFilter.getComponentFilters().iterator(); it.hasNext();) {
            ComponentFilter subComp = (ComponentFilter) it.next();
            throw new IllegalArgumentException("unsupported sub component filter: " + subComp.getName());
        }
        
        for(Iterator it = compFilter.getPropFilters().iterator(); it.hasNext();) {
            PropertyFilter propFilter = (PropertyFilter) it.next();
            handleEventPropFilter(propFilter, itemFilter);
        }
    }
    
    private void handleEventPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {
       
        if(PROP_UID.equalsIgnoreCase(propFilter.getName()))
            handleUidPropFilter(propFilter, itemFilter);
        else if(PROP_SUMMARY.equalsIgnoreCase(propFilter.getName()))
            handleSummaryPropFilter(propFilter, itemFilter);
        else if(PROP_DESCRIPTION.equalsIgnoreCase(propFilter.getName()))
            handleDescriptionPropFilter(propFilter, itemFilter);
        else
            throw new IllegalArgumentException("unsupported prop filter: " + propFilter.getName());
    }
    
    private void handleUidPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {
        
        for(Iterator it = propFilter.getParamFilters().iterator(); it.hasNext();) {
            ParamFilter paramFilter = (ParamFilter) it.next();
            throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
        }
        
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        if(textMatch==null)
            throw new IllegalArgumentException("unsupported filter: must contain text match filter");
        
        if(textMatch.isNegateCondition())
            throw new IllegalArgumentException("unsupported negate condition for prop-filter");
        
        if(textMatch.isCaseless())
            throw new IllegalArgumentException("unsupported isCaseless condition for prop-filter");
        
        itemFilter.setIcalUid(textMatch.getValue());
    }
    
    private void handleDescriptionPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {
        
        for(Iterator it = propFilter.getParamFilters().iterator(); it.hasNext();) {
            ParamFilter paramFilter = (ParamFilter) it.next();
            throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
        }
        
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        if(textMatch==null)
            throw new IllegalArgumentException("unsupported filter: must contain text match filter");
        
        if(textMatch.isNegateCondition())
            throw new IllegalArgumentException("unsupported negate condition for prop-filter");
        
        if(textMatch.isCaseless())
            throw new IllegalArgumentException("unsupported isCaseless condition for prop-filter");
        
        itemFilter.setBody(textMatch.getValue());
    }
    
    private void handleSummaryPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {
        
        for(Iterator it = propFilter.getParamFilters().iterator(); it.hasNext();) {
            ParamFilter paramFilter = (ParamFilter) it.next();
            throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
        }
        
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        if(textMatch==null)
            throw new IllegalArgumentException("unsupported filter: must contain text match filter");
        
        if(textMatch.isNegateCondition())
            throw new IllegalArgumentException("unsupported negate condition for prop-filter");
        
        if(textMatch.isCaseless())
            throw new IllegalArgumentException("unsupported isCaseless condition for prop-filter");
        
        itemFilter.setDisplayName(textMatch.getValue());
    }
        
}
