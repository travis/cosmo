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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.id.StringIdentifierGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.UnresolvableObjectException;
import org.osaf.cosmo.dao.ItemDao;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.CalendarPropertyIndex;
import org.osaf.cosmo.model.CalendarTimeRangeIndex;
import org.osaf.cosmo.model.CalendarItem;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.Ticket;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Implementation of ItemDao using Hibernate persistent objects.
 *
 */
public class ItemDaoImpl extends HibernateDaoSupport implements ItemDao {

    private static final Log log = LogFactory.getLog(ItemDaoImpl.class);

    private StringIdentifierGenerator idGenerator;

    private ItemPathTranslator itemPathTranslator = null;

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#findItemByPath(java.lang.String)
     */
    public Item findItemByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemByPath(path);
            return dbItem;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#findItemParentByPath(java.lang.String)
     */
    public Item findItemParentByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemParent(path);
            return dbItem;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#findItemByUid(java.lang.String)
     */
    public Item findItemByUid(String uid) {
        try {
            Query hibQuery = getSession().getNamedQuery("item.by.uid")
                    .setParameter("uid", uid);
            List results = hibQuery.list();
            if (results.size() > 0)
                return (Item) results.get(0);
            else
                return null;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#removeItem(org.osaf.cosmo.model.Item)
     */
    public void removeItem(Item item) {
        try {

            if(item==null)
                throw new IllegalArgumentException("item cannot be null");
            
            if(item instanceof HomeCollectionItem)
                throw new IllegalArgumentException("cannot remove root item");

            getSession().refresh(item);
            getSession().delete(item);
            getSession().flush();
            
        } catch(ObjectNotFoundException onfe) {
            throw new ItemNotFoundException("item not found");
        } catch(ObjectDeletedException ode) {
            throw new ItemNotFoundException("item not found");
        } catch(UnresolvableObjectException uoe) {
            throw new ItemNotFoundException("item not found");
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#getItemPath(org.osaf.cosmo.model.Item)
     */
    public String getItemPath(Item item) {
        try {
            return itemPathTranslator.getItemPath(item);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#getItemPath(java.lang.String)
     */
    public String getItemPath(String uid) {
        try {
            Item dbItem = findItemByUid(uid);
            return itemPathTranslator.getItemPath(dbItem);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.ItemDao#getRootItem(org.osaf.cosmo.model.User)
     */
    public HomeCollectionItem getRootItem(User user) {
        try {
            return findRootItem(user.getId());
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#createRootItem(org.osaf.cosmo.model.User)
     */
    public HomeCollectionItem createRootItem(User user) {
        try {

            if(user==null)
                throw new IllegalArgumentException("invalid user");

            if(findRootItem(user.getId())!=null)
                throw new RuntimeException("user already has root item");

            HomeCollectionItem newItem = new HomeCollectionItem();

            newItem.setOwner(user);
            newItem.setParent(null);
            newItem.setName(user.getUsername());
            setBaseItemProps(newItem);
            getSession().save(newItem);
            return newItem;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.TicketDao#createTicket(java.lang.String, org.osaf.cosmo.model.Ticket)
     */
    public void createTicket(String path, Ticket ticket) {

        if(ticket==null)
            throw new IllegalArgumentException("ticket cannot be null");

        if(path==null)
            throw new IllegalArgumentException("path cannot be null");

        try {
            User owner = ticket.getOwner();
            if (owner == null)
                throw new IllegalArgumentException("ticket must have owner");

            Item item = getItemPathTranslator().findItemByPath(path);

            if(item==null)
                throw new IllegalArgumentException("invalid item");

            if (ticket.getKey() == null)
                ticket.setKey(getIdGenerator().nextStringIdentifier());

            ticket.setCreated(new Date());
            item.addTicket(ticket);
            getSession().save(item);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.TicketDao#getTicket(java.lang.String, java.lang.String)
     */
    public Ticket getTicket(String path, String key) {

        if(key==null)
            throw new IllegalArgumentException("ticket key cannot be null");

        if(path==null)
            throw new IllegalArgumentException("path cannot be null");

        try {
            Item item = getItemPathTranslator().findItemByPath(path);

            if(item==null)
                throw new IllegalArgumentException("invalid item");

            Iterator tickets = item.getTickets().iterator();
            while (tickets.hasNext()) {
                Ticket ticket = (Ticket) tickets.next();
                if (ticket.getKey().equals(key))
                    return ticket;
            }
            return null;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.TicketDao#getTickets(java.lang.String)
     */
    public Set<Ticket> getTickets(String path) {

        if(path==null)
            throw new IllegalArgumentException("path cannot be null");

        try {
            HashSet tickets = new HashSet();
            Item item = getItemPathTranslator().findItemByPath(path);

            if(item==null)
                throw new IllegalArgumentException("invalid item");

            return item.getTickets();
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.TicketDao#removeTicket(java.lang.String, org.osaf.cosmo.model.Ticket)
     */
    public void removeTicket(String path, Ticket ticket) {
        if(ticket==null)
            throw new IllegalArgumentException("ticket cannot be null");

        if(path==null)
            throw new IllegalArgumentException("path cannot be null");

        try {
            Item item = getItemPathTranslator().findItemByPath(path);

            if(item==null)
                throw new IllegalArgumentException("invalid item");

            Iterator it = item.getTickets().iterator();
            Ticket toDelete = null;

            while (it.hasNext()) {
                Ticket nextTicket = (Ticket) it.next();
                if (ticket.getKey().equals(ticket.getKey())) {
                    toDelete = nextTicket;
                    break;
                }
            }

            if (toDelete != null) {
                item.getTickets().remove(toDelete);
                getSession().save(item);
            }
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<Ticket> getTickets(Item item) {
        if(item==null)
            throw new IllegalArgumentException("item cannot be null");

        try {
            getSession().refresh(item);
            return item.getTickets();
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void createTicket(Item item, Ticket ticket) {
        try {
            if(ticket.getKey()==null)
                ticket.setKey(idGenerator.nextStringIdentifier());
            getSession().refresh(item);
            ticket.setCreated(new Date());
            item.addTicket(ticket);
            getSession().save(item);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Ticket getTicket(Item item, String key) {
        try {
            getSession().refresh(item);
            for(Iterator it = item.getTickets().iterator();it.hasNext();) {
                Ticket ticket = (Ticket) it.next();
                if(ticket.getKey().equals(key))
                    return ticket;
            }

            return null;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeTicket(Item item, Ticket ticket) {
        try {
            getSession().refresh(item);
            item.getTickets().remove(ticket);
            getSession().save(item);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#removeItemByPath(java.lang.String)
     */
    public void removeItemByPath(String path) {
        try {
            Item item = itemPathTranslator.findItemByPath(path);
            removeItem(item);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#removeItemByUid(java.lang.String)
     */
    public void removeItemByUid(String uid) {
        try {
            Item item = findItemByUid(uid);
            removeItem(item);
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#copyItem(org.osaf.cosmo.model.Item, java.lang.String, boolean)
     */
    public void copyItem(Item item, String path, boolean deepCopy) {
        try {
            String copyName = itemPathTranslator.getItemName(path);
            
            if(copyName==null || "".equals(copyName))
                throw new IllegalArgumentException("path must include name");
            
            if(item.getParent()==null)
                throw new IllegalArgumentException("cannot copy root collection");
            
            CollectionItem parent = (CollectionItem) itemPathTranslator.findItemParent(path);
            
            if(parent==null)
                throw new ItemNotFoundException("parent collection not found");
            
            checkForDuplicateItemName(item.getOwner().getId(), 
                    parent.getId(), copyName);
            
            verifyNotInLoop(item, parent);
            
            Item newItem = copyItem(item,parent,deepCopy);
            newItem.setName(copyName);
            getSession().update(newItem);
            
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ItemDao#moveItem(org.osaf.cosmo.model.Item, java.lang.String)
     */
    public void moveItem(Item item, String path) {
        try {
            
            // Name of moved item
            String moveName = itemPathTranslator.getItemName(path);
            
            if(moveName==null || "".equals(moveName))
                throw new IllegalArgumentException("path must include name");
            
            // Parent of moved item
            CollectionItem parent = (CollectionItem) itemPathTranslator.findItemParent(path);
            
            if(parent==null)
                throw new ItemNotFoundException("parent collecion not found");
            
            if(item.getParent()==null)
                throw new IllegalArgumentException("cannot move root collection");
            
            checkForDuplicateItemName(item.getOwner().getId(), 
                    parent.getId(), moveName);
            
            verifyNotInLoop(item, parent);
            
            item.setName(moveName);
            item.setParent(parent);
            getSession().update(item);
            
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /**
     * Set the unique ID generator for new items
     *
     * @param idGenerator
     */
    public void setIdGenerator(StringIdentifierGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public StringIdentifierGenerator getIdGenerator() {
        return idGenerator;
    }

    public ItemPathTranslator getItemPathTranslator() {
        return itemPathTranslator;
    }

    /**
     * Set the path translator. The path translator is responsible for
     * translating a path to an item in the database.
     *
     * @param itemPathTranslator
     */
    public void setItemPathTranslator(ItemPathTranslator itemPathTranslator) {
        this.itemPathTranslator = itemPathTranslator;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.Dao#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.osaf.cosmo.dao.Dao#init()
     */
    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }

        if (itemPathTranslator == null) {
            throw new IllegalStateException("itemPathTranslator is required");
        }
    }

    protected Item copyItem(Item item, CollectionItem parent, boolean deepCopy) {
        
        Item item2 = null;
        
        // create new item instance (can be any subclass of item)
        try {
            item2 = item.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("unable to construct new item instance");
        }
        
        // copy base Item fields
        item2.setParent(parent);
        item2.setOwner(item.getOwner());
        item2.setName(item.getName());
        setBaseItemProps(item2);
        
        // copy attributes
        for(Entry<String, Attribute> entry: item.getAttributes().entrySet())
            item2.addAttribute(entry.getValue().copy());
        
       
        // copy calendar indexes
        if(item instanceof CalendarItem) {
            CalendarItem calendarItem = (CalendarItem) item;
            CalendarItem newCalendarItem = (CalendarItem) item2;
            for(CalendarTimeRangeIndex index: calendarItem.getTimeRangeIndexes())
                newCalendarItem.addTimeRangeIndex(index.copy());
            for(CalendarPropertyIndex index: calendarItem.getPropertyIndexes())
                newCalendarItem.addPropertyIndex(index.copy());
        }
        
        
        // save Item before attempting deep copy
        getSession().save(item2);
        
        // copy children if collection and deepCopy = true
        if(deepCopy==true && (item instanceof CollectionItem) ) {
            CollectionItem collection = (CollectionItem) item;
            for(Item child: collection.getChildren())
                copyItem(child,(CollectionItem) item2,true);       
        }
        
        return item2;
    }
    
    /**
     * Checks to see if a parent Item is currently a child of a target item. If
     * so, then this would put the hierarchy into a loop and is not allowed.
     *
     * @param dbItem
     * @param newParent
     * @throws ModelValidationException
     *             if newParent is child of item
     */
    protected void verifyNotInLoop(Item item, CollectionItem newParent) {
        // need to verify that the new parent is not a child
        // of the item, otherwise we get a loop
        if (item.getId().equals(newParent.getId()))
            throw new ModelValidationException(
                    "Invalid parent - will cause loop");
        
        // If item is not a collection then all is good
        if(!(item instanceof CollectionItem ))
            return;
        
        CollectionItem collection = (CollectionItem) item;
        getSession().refresh(collection);

        for (Item nextItem: collection.getChildren())
            verifyNotInLoop(nextItem, newParent);
    }

    /**
     * Find the DbItem with the specified dbId
     *
     * @param dbId
     *            dbId of DbItem to find
     * @return DbItem with specified dbId
     */
    protected Item findItemByDbId(Long dbId) {
        return (Item) getSession().get(Item.class, dbId);
    }


    // Set server generated item properties on new DbItem
    protected void setBaseItemProps(Item item) {
        item.setUid(idGenerator.nextStringIdentifier());
        item.setCreationDate(new Date());
        item.setModifiedDate(new Date());
    }

    protected Item findItemByParentAndName(Long userDbId, Long parentDbId,
            String name) {
        List results = null;
        if (parentDbId != null) {
            Query hibQuery = getSession().getNamedQuery(
                    "item.by.ownerId.parentId.name").setParameter("ownerid",
                    userDbId).setParameter("parentid", parentDbId)
                    .setParameter("name", name);
            results = hibQuery.list();
        } else {
            Query hibQuery = getSession().getNamedQuery(
                    "item.by.ownerId.nullParent.name").setParameter("ownerid",
                    userDbId).setParameter("name", name);
            results = hibQuery.list();
        }

        if (results.size() > 0)
            return (Item) results.get(0);
        else
            return null;
    }
    
    protected Item findItemByParentAndNameMinusItem(Long userDbId, Long parentDbId,
            String name, Long itemId) {
        List results = null;
        if (parentDbId != null) {
            Query hibQuery = getSession().getNamedQuery(
                    "item.by.ownerId.parentId.name.minusItem").setParameter("itemid", itemId)
                    .setParameter("ownerid",
                    userDbId).setParameter("parentid", parentDbId)
                    .setParameter("name", name);
            results = hibQuery.list();
        } else {
            Query hibQuery = getSession().getNamedQuery(
                    "item.by.ownerId.nullParent.name.minusItem").setParameter("itemid", itemId)
                    .setParameter("ownerid",
                    userDbId).setParameter("name", name);
            results = hibQuery.list();
        }

        if (results.size() > 0)
            return (Item) results.get(0);
        else
            return null;
    }

    protected HomeCollectionItem findRootItem(Long dbUserId) {
        Query hibQuery = getSession().getNamedQuery(
                "homeCollection.by.ownerId").setParameter("ownerid",
                dbUserId);
        List results = hibQuery.list();

        if (results.size() > 0)
            return (HomeCollectionItem) results.get(0);
        else
            return null;
    }

    protected void checkForDuplicateItemName(Long userDbId, Long parentDbId,
            String name) {
        if (findItemByParentAndName(userDbId, parentDbId, name) != null)
            throw new DuplicateItemNameException(name);
    }
    
    protected void checkForDuplicateItemNameMinusItem(Long userDbId, Long parentDbId,
            String name, Long itemId) {
        if (findItemByParentAndNameMinusItem(userDbId, parentDbId, name, itemId) != null)
            throw new DuplicateItemNameException(name);
    }

}
