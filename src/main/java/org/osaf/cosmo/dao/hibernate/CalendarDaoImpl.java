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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * Implemtation of CalendarDao using Hibernate persistence objects.
 */
public class CalendarDaoImpl extends ItemDaoImpl implements CalendarDao {

    private static final Log log = LogFactory.getLog(CalendarDaoImpl.class);

    private String calendarFilterTranslatorClass = null;

    private Class calendarFilterTranslator = null;


  
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.CalendarDao#findEvents(org.osaf.cosmo.model.CollectionItem, org.osaf.cosmo.calendar.query.CalendarFilter)
     */
    public Set<ContentItem> findEvents(CollectionItem collection,
                                             CalendarFilter filter) {

        try {
            List calendarItems = getCalendarFilterTranslater().
                getCalendarItems(getSession(), collection, filter);
            HashSet<ContentItem> events =
                new HashSet<ContentItem>();
            for (Iterator<ContentItem> i=calendarItems.iterator();
                 i.hasNext();) {
                events.add(i.next());
            }
            return events;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
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
            List results = hibQuery.list();
            if (results.size() > 0)
                return (ContentItem) results.get(0);
            else
                return null;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public String getCalendarFilterTranslatorClass() {
        return calendarFilterTranslatorClass;
    }

    public void setCalendarFilterTranslatorClass(
            String calendarFilterTranslatorClass) {
        if(calendarFilterTranslatorClass != null)
            calendarFilterTranslatorClass = calendarFilterTranslatorClass.trim();
        this.calendarFilterTranslatorClass = calendarFilterTranslatorClass;
    }

    /**
     * Initializes the DAO, sanity checking required properties and defaulting
     * optional properties.
     */
    public void init() {
        super.init();
        if (calendarFilterTranslatorClass == null) {
            throw new IllegalStateException(
                    "calendarFilterTranslatorClass is required");
        }

        try {
            calendarFilterTranslator = Class.forName(calendarFilterTranslatorClass);
            calendarFilterTranslator.newInstance();
        } catch (Exception e) {
            log.error(e);
            throw new IllegalStateException(
                    "calendarFilterTranslatorClass must be of correct type");
        }

    }
    
  
    /**
     * Get instance of the CalendarTranslatorFilter.  Need to return new
     * instance each time.  
     * TODO: figure out way to move this into implementation class
     */
    protected CalendarFilterTranslator getCalendarFilterTranslater() {
        try {
            return (CalendarFilterTranslator) calendarFilterTranslator.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
    
}
