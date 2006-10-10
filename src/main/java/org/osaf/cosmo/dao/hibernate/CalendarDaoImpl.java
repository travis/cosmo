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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DuplicateEventUidException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelConversionException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.User;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * Implemtation of CalendarDao using Hibernate persistence objects.
 */
public class CalendarDaoImpl extends ItemDaoImpl implements CalendarDao {

    private static final Log log = LogFactory.getLog(CalendarDaoImpl.class);

    private String calendarFilterTranslatorClass = null;

    private Class calendarFilterTranslator = null;

    private CalendarIndexer calendarIndexer = null;

    public CalendarIndexer getCalendarIndexer() {
        return calendarIndexer;
    }

    public void setCalendarIndexer(CalendarIndexer calendarIndexer) {
        this.calendarIndexer = calendarIndexer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#addEvent(org.osaf.cosmo.model.CalendarCollectionItem,
     *      org.osaf.cosmo.model.CalendarEventItem)
     */
    public CalendarEventItem addEvent(CalendarCollectionItem calendar,
            CalendarEventItem event) {

        if (calendar == null)
            throw new IllegalArgumentException("calendar cannot be null");

        if (event == null)
            throw new IllegalArgumentException("event cannot be null");

        if (event.getId()!=-1)
            throw new IllegalArgumentException("invalid event id (expected -1)");

        if (event.getOwner() == null)
            throw new IllegalArgumentException("event must have owner");

        try {
            
            User owner = event.getOwner();

            Long parentDbId = calendar.getId();

            // In a hierarchy, can't have two items with same name with
            // same parent
            checkForDuplicateItemName(owner.getId(), parentDbId, event.getName());

            // A calendar can't have two events with the same ical uid property
            verifyEventUidIsUnique(calendar, event);
            
            setBaseItemProps(event);
            event.setParent(calendar);
            
            // validate content
            event.validate();
            
            getSession().save(event);

            // index event
            calendarIndexer.indexCalendarEvent(getSession(), event, event
                    .getCalendar());

            return event;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch(ModelConversionException mce) {
            throw new ModelValidationException("error parsing .ics data");
        }
    }

    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.CalendarDao#createCalendar(org.osaf.cosmo.model.CalendarCollectionItem)
     */
    public CalendarCollectionItem createCalendar(CalendarCollectionItem calendar) {
        User owner = calendar.getOwner();

        if (owner == null)
            throw new IllegalArgumentException("calendar must have owner");

        CollectionItem root = getRootItem(owner);

        return createCalendar(root, calendar);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#createCalendar(org.osaf.cosmo.model.CalendarCollectionItem)
     */
    public CalendarCollectionItem createCalendar(CollectionItem collection, CalendarCollectionItem calendar) {

        // check the obvious
        if(collection == null)
            throw new IllegalArgumentException("collection cannot be null");

        if (calendar == null)
            throw new IllegalArgumentException("calendar cannot be null");

        if (calendar.getId()!=-1)
            throw new IllegalArgumentException("invalid calendar id (expected -1)");
        
        try {
            User owner = calendar.getOwner();

            if (owner == null)
                throw new IllegalArgumentException("calendar must have owner");

            // Can't have two calendars with same name and owner
            checkForDuplicateItemName(owner.getId(), collection.getId(), calendar
                    .getName());

            calendar.setParent(collection);
            setBaseItemProps(calendar);

            // validate item
            calendar.validate();
            
            getSession().save(calendar);
            return calendar;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#findCalendarByPath(java.lang.String)
     */
    public CalendarCollectionItem findCalendarByPath(String path) {

        try {
            Item item = getItemPathTranslator().findItemByPath(path);
            if (item == null || !(item instanceof CalendarCollectionItem))
                return null;

            return (CalendarCollectionItem) item;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#findCalendarByUid(java.lang.String)
     */
    public CalendarCollectionItem findCalendarByUid(String uid) {
        try {
            List results = getSession().getNamedQuery("calendarCollectionItem.by.uid").setParameter("uid", uid).list();
            if(results.size()>0)
                return (CalendarCollectionItem) results.get(0);
            else
                return null;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#findEventByPath(java.lang.String)
     */
    public CalendarEventItem findEventByPath(String path) {

        try {
            Item item = getItemPathTranslator().findItemByPath(path);
            if (item == null || !(item instanceof CalendarEventItem))
                return null;

            return (CalendarEventItem) item;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#findEventByUid(java.lang.String)
     */
    public CalendarEventItem findEventByUid(String uid) {
        try {
            List results = getSession().getNamedQuery("calendarEventItem.by.uid").setParameter("uid", uid).list();
            if(results.size()>0)
                return (CalendarEventItem) results.get(0);
            else
                return null;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#findEvents(org.osaf.cosmo.model.CalendarCollectionItem,
     *      java.util.Map)
     */
    public Set<CalendarEventItem> findEvents(CalendarCollectionItem calendar,
                                             Map criteria) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("select i from CalendarEventItem as i");
            Iterator keys = criteria.keySet().iterator();
            int count = 0;
            while (keys.hasNext()) {
                sb.append(",StringAttribute sa" + count++);
                keys.next();
            }
            sb.append(" where i.parent.uid=:parentuid");

            count = 0;
            keys = criteria.keySet().iterator();
            while (keys.hasNext()) {
                sb.append(" and sa" + count++ + ".item=i");
                keys.next();
            }
            count = 0;
            keys = criteria.keySet().iterator();
            while (keys.hasNext()) {
                sb.append(" and sa" + count + ".name=:name" + count);
                sb.append(" and sa" + count + ".value=:value" + count);
                keys.next();
                count++;
            }

            Query hibQuery = getSession().createQuery(sb.toString());
            hibQuery.setParameter("parentuid", calendar.getUid());

            count = 0;
            keys = criteria.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = (String) criteria.get(key);
                hibQuery.setParameter("name" + count, key);
                hibQuery.setParameter("value" + count, value);
                count++;
            }

            HashSet<CalendarEventItem> events =
                new HashSet<CalendarEventItem>();
            for (Iterator<CalendarEventItem> i=hibQuery.list().iterator();
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
     * @see org.osaf.cosmo.dao.CalendarDao#updateCalendar(org.osaf.cosmo.model.CalendarCollectionItem)
     */
    public CalendarCollectionItem updateCalendar(CalendarCollectionItem calendar) {
        try {
            
            if (calendar == null)
                throw new IllegalArgumentException("calendar cannot be null");

            // In a hierarchy, can't have two items with same name with
            // same parent
            checkForDuplicateItemNameMinusItem(calendar.getOwner().getId(), 
                    calendar.getParent().getId(), calendar.getName(), calendar.getId());
            
            updateBaseItemProps(calendar);
            
            // validate item
            calendar.validate();
            getSession().update(calendar);
            return calendar;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#updateEvent(org.osaf.cosmo.model.CalendarEventItem)
     */
    public CalendarEventItem updateEvent(CalendarEventItem event) {

        try {

            if (event == null)
                throw new IllegalArgumentException("event cannot be null");

            if (event.getOwner() == null)
                throw new IllegalArgumentException("event must have owner");
            
            if (event.getParent() == null)
                throw new IllegalArgumentException("event must have parent");
            
            // In a hierarchy, can't have two items with same name with
            // same parent
            checkForDuplicateItemNameMinusItem(event.getOwner().getId(), 
                    event.getParent().getId(), event.getName(), event.getId());
            
            // Index all the properties
            calendarIndexer.indexCalendarEvent(getSession(), event, event
                    .getCalendar());

            updateBaseItemProps(event);
            
            // validate content
            event.validate();
            
            getSession().update(event);
            return event;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch(ModelConversionException mce) {
            throw new ModelValidationException("error parsing .ics data");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#removeCalendar(org.osaf.cosmo.model.CalendarCollectionItem)
     */
    public void removeCalendar(CalendarCollectionItem calendar) {
        removeItem(calendar);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#removeEvent(org.osaf.cosmo.model.CalendarEventItem)
     */
    public void removeEvent(CalendarEventItem event) {
        removeItem(event);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.CalendarDao#findEvents(org.osaf.cosmo.model.CalendarCollectionItem,
     *      org.osaf.cosmo.calendar.query.CalendarFilter)
     */
    public Set<CalendarEventItem> findEvents(CalendarCollectionItem calendar,
                                             CalendarFilter filter) {

        try {
            List calendarItems = getCalendarFilterTranslater().
                getCalendarItems(getSession(), calendar.getId(), filter);
            HashSet<CalendarEventItem> events =
                new HashSet<CalendarEventItem>();
            for (Iterator<CalendarEventItem> i=calendarItems.iterator();
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
     *      org.osaf.cosmo.model.CalendarCollectionItem)
     */
    public CalendarEventItem findEventByIcalUid(String uid,
            CalendarCollectionItem calendar) {
        try {
            Query hibQuery = getSession().getNamedQuery(
                    "event.by.calendar.icaluid");
            hibQuery.setParameter("calendar", calendar);
            hibQuery.setParameter("uid", uid);
            List results = hibQuery.list();
            if (results.size() > 0)
                return (CalendarEventItem) results.get(0);
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

        if (calendarIndexer == null)
            throw new IllegalStateException("calendarIndexer is required");
    }
    
  
    /**
     * Get instance of the CalendarTranslatorFilter.  Need to return new
     * instance each time.  
     * TODO: figure out way to move this into implementation class
     * @return
     */
    protected CalendarFilterTranslator getCalendarFilterTranslater() {
        try {
            return (CalendarFilterTranslator) calendarFilterTranslator.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verify that event uid (event UID property in ical data) is unique
     * for the containing calendar.
     * @param calendar
     * @param event
     * @throws DuplicateEventUidException if an event with the same uid
     *         exists
     * @throws ModelValidationException if there is an error retrieving
     *         the uid from the even ics data
     */
    private void verifyEventUidIsUnique(CalendarCollectionItem calendar,
            CalendarEventItem event) {
        String uid = null;
        
        try {
            uid = event.getMasterEvent().getUid().getValue();
        } catch(ModelConversionException mce) {
            throw mce;
        } catch (Exception e) {
            log.error("error retrieving master event");
            throw new ModelValidationException("invalid event ics data");
        }
        
        Query hibQuery = getSession()
                .getNamedQuery("event.by.calendar.icaluid");
        hibQuery.setParameter("calendar", calendar);
        hibQuery.setParameter("uid", uid);
        List results = hibQuery.list();
        if (results.size() > 0)
            throw new DuplicateEventUidException("uid " + uid
                    + " already exists in calendar");
    }
}
