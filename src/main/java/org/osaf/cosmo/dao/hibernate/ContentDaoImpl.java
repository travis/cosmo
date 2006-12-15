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

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DuplicateEventUidException;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelConversionException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.User;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * Implementation of ContentDao using hibernate persistence objects
 * 
 */
public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    private static final Log log = LogFactory.getLog(ContentDaoImpl.class);

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
     * @see org.osaf.cosmo.dao.ContentDao#createCollection(org.osaf.cosmo.model.CollectionItem,
     *      org.osaf.cosmo.model.CollectionItem)
     */
    public CollectionItem createCollection(CollectionItem parent,
            CollectionItem collection) {

        if(parent==null)
            throw new IllegalArgumentException("parent cannot be null");
        
        if (collection == null)
            throw new IllegalArgumentException("collection cannot be null");

        if (collection.getId()!=-1)
            throw new IllegalArgumentException("invalid collection id (expected -1)");
        
        
        try {
            if (collection.getOwner() == null)
                throw new IllegalArgumentException("collection must have owner");

            User owner = collection.getOwner();
            
            // verify uid not in use
            checkForDuplicateUid(collection);
            
            // We need to enforce a content hierarchy to support WebDAV
            // In a hierarchy, can't have two items with same name with
            // same owner and parent
            checkForDuplicateItemName(owner.getId(), parent.getId(), collection
                    .getName());
            
            setBaseItemProps(collection);
            collection.setParent(parent);
            
            // validate item
            collection.validate();
            getSession().save(collection);
            
            return collection;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#createContent(org.osaf.cosmo.model.CollectionItem,
     *      org.osaf.cosmo.model.ContentItem)
     */
    public ContentItem createContent(CollectionItem parent, ContentItem content) {

        if(parent==null)
            throw new IllegalArgumentException("parent cannot be null");
        
        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (content.getId()!=-1)
            throw new IllegalArgumentException("invalid content id (expected -1)");
        
        try {
            User owner = content.getOwner();

            if (owner == null)
                throw new IllegalArgumentException("content must have owner");

            // verify uid not in use
            checkForDuplicateUid(content);
            
            // Enforce hiearchy for WebDAV support
            // In a hierarchy, can't have two items with same name with
            // same parent
            checkForDuplicateItemName(owner.getId(), parent.getId(), content.getName());

            setBaseItemProps(content);
            content.setParent(parent);
            
            // validate content
            content.validate();
            
            boolean isEvent = content.getStamp(EventStamp.class) != null;
            
            if(isEvent) {
                EventStamp event = EventStamp.getStamp(content);
                verifyEventUidIsUnique(parent,event);
                getCalendarIndexer().indexCalendarEvent(getSession(), event);
                try {
                    // Set the content length (required) based on the length of the
                    // icalendar string
                    content.setContentLength((long) event.getCalendar().toString()
                            .getBytes("UTF-8").length);
                } catch (UnsupportedEncodingException e) {
                    // should never happen
                    throw new RuntimeException("error converting to utf8");
                }
            }
            
            getSession().save(content);
            return content;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#findChildren(org.osaf.cosmo.model.CollectionItem)
     */
    public Set<Item> findChildren(CollectionItem collection) {

        try {
            getSession().refresh(collection);
            return collection.getChildren();
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#findCollectionByUid(java.lang.String)
     */
    public CollectionItem findCollectionByUid(String uid) {
        try {
            return (CollectionItem) getSession().getNamedQuery(
                    "collectionItem.by.uid").setParameter("uid", uid)
                    .uniqueResult();
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#findCollectionByPath(java.lang.String)
     */
    public CollectionItem findCollectionByPath(String path) {
        try {
            Item item = getItemPathTranslator().findItemByPath(path);
            if (item == null || !(item instanceof CollectionItem) )
                return null;

            return (CollectionItem) item;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#findContentByPath(java.lang.String)
     */
    public ContentItem findContentByPath(String path) {
        try {
            Item item = getItemPathTranslator().findItemByPath(path);
            if (item == null || !(item instanceof ContentItem) )
                return null;

            return (ContentItem) item;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#findContentByUid(java.lang.String)
     */
    public ContentItem findContentByUid(String uid) {
        try {
            return (ContentItem) getSession().getNamedQuery(
                    "contentItem.by.uid").setParameter("uid", uid).uniqueResult();
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#updateCollection(org.osaf.cosmo.model.CollectionItem)
     */
    public CollectionItem updateCollection(CollectionItem collection) {
        try {
            
            if (collection == null)
                throw new IllegalArgumentException("collection cannot be null");
            
            // In a hierarchy, can't have two items with same name with
            // same parent
            if (collection.getParent() != null)
                checkForDuplicateItemNameMinusItem(collection.getOwner().getId(), 
                    collection.getParent().getId(), collection.getName(), collection.getId());
            
            updateBaseItemProps(collection);
            
            // validate item
            collection.validate();
            
            getSession().update(collection);
            return collection;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#updateContent(org.osaf.cosmo.model.ContentItem)
     */
    public ContentItem updateContent(ContentItem content) {
        try {     
            
            if (content == null)
                throw new IllegalArgumentException("content cannot be null");
            
            if(content.getParent()==null)
                throw new IllegalArgumentException("parent cannot be null");
                        
            // validate content
            content.validate();
            
            // In a hierarchy, can't have two items with same name with
            // same parent
            checkForDuplicateItemNameMinusItem(content.getOwner().getId(), 
                    content.getParent().getId(), content.getName(), content.getId());
            
            updateBaseItemProps(content);
            
            boolean isEvent = content.getStamp(EventStamp.class) != null;
            
            if(isEvent) {
                EventStamp event = EventStamp.getStamp(content);
                // TODO: verify icaluid is unique
                getCalendarIndexer().indexCalendarEvent(getSession(), event);
                try {
                    // Set the content length (required) based on the length of the
                    // icalendar string
                    content.setContentLength((long) event.getCalendar().toString()
                            .getBytes("UTF-8").length);
                } catch (UnsupportedEncodingException e) {
                    // should never happen
                    throw new RuntimeException("error converting to utf8");
                }
            }
            
            getSession().update(content);
            return content;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#removeCollection(org.osaf.cosmo.model.CollectionItem)
     */
    public void removeCollection(CollectionItem collection) {
        removeItem(collection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#removeContent(org.osaf.cosmo.model.ContentItem)
     */
    public void removeContent(ContentItem content) {
        try {
            content.setIsActive(false);
            content.setName("DELETED:" + content.getName());
            content.clearContent();
            content.getAttributes().clear();
            content.getStamps().clear();
            updateBaseItemProps(content);
            getSession().update(content);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#findChildren(org.osaf.cosmo.model.User)
     */
    public Set<Item> findChildren(User user) {
        try {
            CollectionItem rootItem = getRootItem(user);
            if(rootItem!=null)
                return rootItem.getChildren();
            else
                return new HashSet<Item>(0);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#moveContent(org.osaf.cosmo.model.CollectionItem,
     *      org.osaf.cosmo.model.Item)
     */
    public void moveContent(CollectionItem parent, Item content) {
        try {
            
            if (parent == null)
                throw new IllegalArgumentException("Invalid parent collection");

            if (content == null)
                throw new IllegalArgumentException("Invalid Item");

            Item item = findItemByUid(content.getUid());

            Item currentParent = item.getParent();

            // can't move root collection
            if (currentParent == null)
                throw new IllegalArgumentException("Invalid parent collection");

            // need to verify we aren't creating a loop
            verifyNotInLoop(item, parent);

            content.setParent(parent);
            getSession().update(content);
            getSession().flush();
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } 
    }
    
    @Override
    public void removeItem(Item item) {
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else
            super.removeItem(item);
    }

    @Override
    public void removeItemByPath(String path) {
        Item item = this.findItemByPath(path);
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else
            super.removeItem(item);
    }

    @Override
    public void removeItemByUid(String uid) {
        Item item = this.findItemByUid(uid);
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else
            super.removeItem(item);
    }

    /**
     * Initializes the DAO, sanity checking required properties and defaulting
     * optional properties.
     */
    public void init() {
        super.init();
        
        if (calendarIndexer == null)
            throw new IllegalStateException("calendarIndexer is required");
    }
    
    /**
     * Verify that event uid (event UID property in ical data) is unique
     * for the containing calendar.
     * @param collection collection to search
     * @param event event to verify
     * @throws DuplicateEventUidException if an event with the same uid
     *         exists
     * @throws ModelValidationException if there is an error retrieving
     *         the uid from the even ics data
     */
    private void verifyEventUidIsUnique(CollectionItem collection,
            EventStamp event) {
        String uid = null;
        
        try {
            uid = event.getIcalUid();
        } catch(ModelConversionException mce) {
            throw mce;
        } catch (Exception e) {
            log.error("error retrieving master event");
            throw new ModelValidationException("invalid event ics data");
        }
        
        Query hibQuery = getSession()
                .getNamedQuery("event.by.calendar.icaluid");
        hibQuery.setParameter("calendar", collection);
        hibQuery.setParameter("uid", uid);
        if (hibQuery.uniqueResult()!=null)
            throw new DuplicateEventUidException("uid " + uid
                    + " already exists in calendar");
    }
    
    
}
