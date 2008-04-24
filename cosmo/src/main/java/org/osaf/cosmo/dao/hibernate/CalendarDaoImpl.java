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

import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.CalendarFilterEvaluater;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.dao.hibernate.query.CalendarFilterConverter;
import org.osaf.cosmo.dao.hibernate.query.ItemFilterProcessor;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.ItemFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Implementation of CalendarDao using Hibernate persistence objects.
 */
public class CalendarDaoImpl extends HibernateDaoSupport implements CalendarDao {

    private static final Log log = LogFactory.getLog(CalendarDaoImpl.class);

    private ItemFilterProcessor itemFilterProcessor = null;
    private EntityConverter entityConverter = new EntityConverter(null);
   
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.CalendarDao#findCalendarItems(org.osaf.cosmo.model.CollectionItem, org.osaf.cosmo.calendar.query.CalendarFilter)
     */
    public Set<ICalendarItem> findCalendarItems(CollectionItem collection,
                                             CalendarFilter filter) {

        try {
            CalendarFilterConverter filterConverter = new CalendarFilterConverter();
            try {
                // translate CalendarFilter to ItemFilter and execute filter
                ItemFilter itemFilter = filterConverter.translateToItemFilter(collection, filter);
                Set results = itemFilterProcessor.processFilter(getSession(), itemFilter);
                return (Set<ICalendarItem>) results;
            } catch (IllegalArgumentException e) {
            }
            
            // Use brute-force method if CalendarFilter can't be translated
            // to an ItemFilter (slower but at least gets the job done).
            HashSet<ICalendarItem> results = new HashSet<ICalendarItem>();
            Set<Item> itemsToProcess = null;
            
            // Optimization:
            // Do a first pass query if possible to reduce the number
            // of items we have to examine.  Otherwise we have to examine
            // all items.
            ItemFilter firstPassItemFilter = filterConverter.getFirstPassFilter(collection, filter);
            if(firstPassItemFilter!=null)
                itemsToProcess = itemFilterProcessor.processFilter(getSession(), firstPassItemFilter);
            else
                itemsToProcess = collection.getChildren();
            
            CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
            
            // Evaluate filter against all calendar items
            for (Item child : itemsToProcess) {
                
                // only care about calendar items
                if (child instanceof ICalendarItem) {
                    
                    ICalendarItem content = (ICalendarItem) child;
                    Calendar calendar = entityConverter.convertContent(content);
                        
                    if(calendar!=null) {
                        if (evaluater.evaluate(calendar, filter) == true)
                            results.add(content);
                    }
                }
            }
            
            return results;
        } catch (HibernateException e) {
            getSession().clear();
            throw convertHibernateAccessException(e);
        }
    }
    
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.CalendarDao#findEvents(org.osaf.cosmo.model.CollectionItem, net.fortuna.ical4j.model.DateTime, net.fortuna.ical4j.model.DateTime, boolean)
     */
    public Set<ContentItem> findEvents(CollectionItem collection, DateTime rangeStart, DateTime rangeEnd, boolean expandRecurringEvents) {
        
        // Create a NoteItemFilter that filters by parent
        NoteItemFilter itemFilter = new NoteItemFilter();
        itemFilter.setParent(collection);
        
        // and EventStamp by timeRange
        EventStampFilter eventFilter = new EventStampFilter();
        Period period = new Period(rangeStart, rangeEnd);
        eventFilter.setPeriod(period);
        eventFilter.setExpandRecurringEvents(expandRecurringEvents);
        itemFilter.getStampFilters().add(eventFilter);
        
        try {
            Set results = itemFilterProcessor.processFilter(getSession(), itemFilter);
            return (Set<ContentItem>) results;
        } catch (HibernateException e) {
            getSession().clear();
            throw convertHibernateAccessException(e);
        }
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.CalendarDao#findEventByIcalUid(java.lang.String,
     *      org.osaf.cosmo.model.CollectionItem)
     */
    public ContentItem findEventByIcalUid(String uid,
            CollectionItem calendar) {
        try {
            Query hibQuery = getSession().getNamedQuery(
                    "event.by.calendar.icaluid");
            hibQuery.setParameter("calendar", calendar);
            hibQuery.setParameter("uid", uid);
            return (ContentItem) hibQuery.uniqueResult();
        } catch (HibernateException e) {
            getSession().clear();
            throw convertHibernateAccessException(e);
        }
    }
    
  
    public ItemFilterProcessor getItemFilterProcessor() {
        return itemFilterProcessor;
    }

    public void setItemFilterProcessor(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }


    /**
     * Initializes the DAO, sanity checking required properties and defaulting
     * optional properties.
     */
    public void init() {
        
        if (itemFilterProcessor == null) {
            throw new IllegalStateException("itemFilterProcessor is required");
        }

    }
    
}
