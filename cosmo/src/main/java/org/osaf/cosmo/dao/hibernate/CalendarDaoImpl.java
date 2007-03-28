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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Implemtation of CalendarDao using Hibernate persistence objects.
 */
public class CalendarDaoImpl extends HibernateDaoSupport implements CalendarDao {

    private static final Log log = LogFactory.getLog(CalendarDaoImpl.class);

    private CalendarFilterTranslator calendarFilterTranslator = null;
    private CalendarIndexer calendarIndexer = null;
  
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.CalendarDao#findEvents(org.osaf.cosmo.model.CollectionItem, org.osaf.cosmo.calendar.query.CalendarFilter)
     */
    public Set<ContentItem> findEvents(CollectionItem collection,
                                             CalendarFilter filter) {

        try {
            return calendarFilterTranslator.
                getCalendarItems(getSession(), collection, filter);
        } catch (HibernateException e) {
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
            throw convertHibernateAccessException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.CalendarDao#indexEvent(org.osaf.cosmo.model.EventStamp)
     */
    public void indexEvent(EventStamp eventStamp) {
        try {
            getCalendarIndexer().indexCalendarEvent(getSession(), eventStamp);
            getSession().update(eventStamp);
            getSession().flush();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } 
    }

    public CalendarFilterTranslator getCalendarFilterTranslator() {
        return calendarFilterTranslator;
    }

    public void setCalendarFilterTranslator(
            CalendarFilterTranslator calendarFilterTranslator) {
        this.calendarFilterTranslator = calendarFilterTranslator;
    }

    public CalendarIndexer getCalendarIndexer() {
        return calendarIndexer;
    }

    public void setCalendarIndexer(CalendarIndexer calendarIndexer) {
        this.calendarIndexer = calendarIndexer;
    }
    
    /**
     * Initializes the DAO, sanity checking required properties and defaulting
     * optional properties.
     */
    public void init() {
        
        if (calendarFilterTranslator == null) {
            throw new IllegalStateException(
                    "calendarFilterTranslatorClass is required");
        }
        
        if (calendarIndexer == null)
            throw new IllegalStateException("calendarIndexer is required");

    }
    
}
