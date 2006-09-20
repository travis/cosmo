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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.ItemDao;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PathUtil;

/**
 * Mock implementation of <code>ItemDao</code> useful for testing.
 *
 * @see ItemDao
 * @see Item
 */
public class MockItemDao implements ItemDao {
    private static final Log log = LogFactory.getLog(MockItemDao.class);

    private HashMap<String, Item> itemsByPath;
    private HashMap<String, Item> itemsByUid;
    private HashMap<Long, String> rootUids;
    private HashMap<String, Set> tickets;
    private SessionIdGenerator idGenerator;

    /**
     */
    public MockItemDao() {
        itemsByPath = new HashMap<String, Item>();
        itemsByUid = new HashMap<String, Item>();
        rootUids = new HashMap<Long, String>();
        tickets = new HashMap<String, Set>();
        idGenerator = new SessionIdGenerator();
    }

    // ItemDao methods

    /**
     * Find an item with the specified uid. The return type will be one of
     * Item, CollectionItem, CalendarCollectionItem, CalendarItem.
     * 
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    public Item findItemByUid(String uid) {
        return itemsByUid.get(uid);
    }

    /**
     * Find an item with the specified path. The return type will be one of
     * Item, CollectionItem, CalendarCollectionItem, CalendarItem.
     * 
     * @param path
     *            path of item to find
     * @return item represented by path
     */
    public Item findItemByPath(String path) {
        return itemsByPath.get(path);
    }

    /**
     * Return the path to an item. The path has the format:
     * /username/parent1/parent2/itemname
     * 
     * @param item
     *            the item to calculate the path for
     * @return hierarchical path to item
     */
    public String getItemPath(Item item) {
        StringBuffer path = new StringBuffer();
        LinkedList<String> hierarchy = new LinkedList<String>();
        hierarchy.addFirst(item.getName());

        Item currentItem = item;
        while (currentItem.getParent() != null) {
            currentItem = itemsByUid.get(currentItem.getParent().getUid());
            hierarchy.addFirst(currentItem.getName());
        }

        // hierarchy
        for (String part : hierarchy)
            path.append("/" + part);

        return path.toString();
    }

    /**
     * Return the path to an item. The path has the format:
     * /username/parent1/parent2/itemname
     * 
     * @param uid
     *            the uid of the item to calculate the path for
     * @return hierarchical path to item
     */
    public String getItemPath(String uid) {
        return getItemPath(itemsByUid.get(uid));
    }

    /**
     * Get the root item for a user
     * 
     * @param user
     * @return
     */
    public HomeCollectionItem getRootItem(User user) {
        if (user == null)
            throw new IllegalArgumentException("null user");

        if (log.isDebugEnabled()) {
            log.debug("getting root item for user " + user.getUsername());
        }

        String rootUid = rootUids.get(user.getId());
        if (rootUid == null) {
            throw new IllegalStateException("user does not have a root item");
        }

        return (HomeCollectionItem) itemsByUid.get(rootUid);
    }

    /**
     * Create the root item for a user.
     * @param user
     * @return
     */
    public HomeCollectionItem createRootItem(User user) {
        if (user == null)
            throw new IllegalArgumentException("null user");

        if (log.isDebugEnabled()) {
            log.debug("creating root item for user " + user.getUsername());
        }

        HomeCollectionItem rootCollection = new HomeCollectionItem();
        rootCollection.setName(user.getUsername());
        rootCollection.setOwner(user);
        rootCollection.setUid(calculateUid());
        rootCollection.setCreationDate(new Date());
        rootCollection.setModifiedDate(rootCollection.getCreationDate());

        itemsByUid.put(rootCollection.getUid(), rootCollection);
        itemsByPath.put("/" + rootCollection.getName(), rootCollection);
        rootUids.put(user.getId(), rootCollection.getUid());

        if (log.isDebugEnabled()) {
            log.debug("root item uid is " + rootCollection.getUid());
        }

        return rootCollection;
    }

    /**
     * Copy an item to the given path
     * @param item item to copy
     * @param path path to copy item to
     * @param deepCopy true for deep copy, else shallow copy will
     *                 be performed
     * @throws org.osaf.cosmo.model.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.model.DuplicateItemNameException
     *         if path points to an item with the same path
     */
    public void copyItem(Item item,
                         String path,
                         boolean deepCopy) {
        if (item == null)
            throw new IllegalArgumentException("item cannot be null");
        if (path == null)
            throw new IllegalArgumentException("path cannot be null");

        String parentPath = PathUtil.getParentPath(path);
        CollectionItem parent = (CollectionItem)
            itemsByPath.get(PathUtil.getParentPath(path));
        if (parent == null)
            throw new ItemNotFoundException("parent collection not found");

        Item copy =
            copyItem(item, PathUtil.getBasename(path), parent, deepCopy);
    }

    private Item copyItem(Item item,
                          String copyName,
                          CollectionItem parent,
                          boolean deepCopy) {
        Item copy = null;
        try {
            copy = item.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("unable to construct new instance of " + item.getClass());
        }

        if (copyName == null)
            copyName = item.getName();
        copy.setName(copyName);
        copy.setParent(parent);
        copy.setOwner(item.getOwner());

        for(Map.Entry<String, Attribute> entry :
                item.getAttributes().entrySet())
            copy.addAttribute(entry.getValue().copy());

        // XXX: ignoring calendar indexes

        storeItem(copy);

        if (deepCopy && (item instanceof CollectionItem)) {
            CollectionItem collection = (CollectionItem) item;
            for (Item child: collection.getChildren())
                copyItem(child, null, (CollectionItem) copy, true);
        }

        return copy;
    }
    
  
    /**
     * Move item to the given path
     * @param item item to move
     * @param path path to move item to
     * @throws org.osaf.cosmo.model.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.model.DuplicateItemNameException
     *         if path points to an item with the same path
     */
    public void moveItem(Item item,
                         String path) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove an item.
     * 
     * @param item
     *            item to remove
     */
    public void removeItem(Item item) {
        item.getParent().getChildren().remove(item);

        itemsByUid.remove(item.getUid());
        itemsByPath.remove(getItemPath(item));
        if (rootUids.get(item.getOwner().getId()).equals(item.getUid())) {
            rootUids.remove(item.getOwner().getId());
        }
    }

    /**
     * Creates a ticket on an item.
     *
     * @param item the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(Item item,
                             Ticket ticket) {
        ticket.setKey(calculateTicketKey());
        item.getTickets().add(ticket);
        findItemTickets(item).add(ticket);
    }

    /**
     * Returns all tickets on the given item.
     *
     * @param item the item to be ticketed
     */
    public Set getTickets(Item item) {
        return findItemTickets(item);
    }

    /**
     * Returns the identified ticket on the given item, or
     * <code>null</code> if the ticket does not exists. Tickets are
     * inherited, so if the specified item does not have the ticket
     * but an ancestor does, it will still be returned.
     *
     * @param item the ticketed item
     * @param key the ticket to return
     */
    public Ticket getTicket(Item item,
                            String key) {
        for (Iterator i=findItemTickets(item).iterator(); i.hasNext();) {
            Ticket t = (Ticket) i.next();
            if (t.getKey().equals(key)) {
                return t;
            }
        }
        // the ticket might be on an ancestor, so check the parent
        if (item.getParent() != null) {
            return getTicket(itemsByUid.get(item.getParent().getUid()), key);
        }
        // this is the root item; the ticket simply doesn't exist
        // anywhere in the given path
        return null;
    }

    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param ticket the ticket to remove
     */
    public void removeTicket(Item item,
                             Ticket ticket) {
        Set itemTickets = findItemTickets(item);
        if (itemTickets.contains(ticket)) {
            item.getTickets().remove(ticket);
            itemTickets.remove(ticket);
            return;
        }
        // the ticket might be on an ancestor, so check the parent
        if (item.getParent() != null) {
            removeTicket(itemsByUid.get(item.getParent().getUid()), ticket);
        }
        // this is the root item; the ticket simply doesn't exist
        // anywhere in the given path
        return;
    }

    public void removeItemByPath(String path) {
        Item item = findItemByPath(path);
        removeItem(item);
    }

    public void removeItemByUid(String uid) {
        Item item = findItemByUid(uid);
        removeItem(item);
    }
    
    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }

    // our methods


    /** */
    protected void storeItem(Item item) {
        if (item.getName() == null)
            throw new IllegalArgumentException("name cannot be null");
        if (item.getParent() == null)
            throw new IllegalArgumentException("parent cannot be null");
        Item parentItem = item.getParent();
        
        if (item.getOwner() == null)
            throw new IllegalArgumentException("owner cannot be null");

        item.setUid(calculateUid());
        item.setCreationDate(new Date());
        item.setModifiedDate(item.getCreationDate());

        for (Item sibling : item.getParent().getChildren()) {
            if (sibling.getName().equals(item.getName()))
                throw new DuplicateItemNameException();
        }

        item.getParent().getChildren().add(item);

        itemsByUid.put(item.getUid(), item);
        itemsByPath.put(getItemPath(parentItem) + "/" + item.getName(), item);
    }

    /** */
    protected void updateItem(Item item) {
        if (itemsByUid.get(item.getUid()) != item)
            throw new IllegalArgumentException("item to be updated does not match stored item");
        if (item.getName() == null)
            throw new IllegalArgumentException("name cannot be null");
        if (item.getOwner() == null)
            throw new IllegalArgumentException("owner cannot be null");

        CollectionItem parentItem = item.getParent();

        if (parentItem != null) {
            for (Item sibling : parentItem.getChildren()) {
                if (sibling.getName().equals(item.getName()) &&
                    ! (sibling.getUid().equals(item.getUid())))
                    throw new DuplicateItemNameException();
            }
        }

        item.setModifiedDate(item.getCreationDate());

        String path = "";
        if (parentItem != null)
            path += getItemPath(parentItem);
        path += "/" + item.getName();

        // XXX if the item name changed during the update, then we
        // leave a dangling map entry
        itemsByPath.put(path, item);
    }

    /** */
    protected Set findItemChildren(Item item) {
        HashSet children = new HashSet();

        for (Iterator<Item> i=itemsByUid.values().iterator(); i.hasNext();) {
            Item child = i.next();
            if (child.getParent().getUid().equals(item.getUid())) {
                children.add(child);
            }
        }

        return Collections.unmodifiableSet(children);
    }

    /** */
    protected Set findRootChildren(User user) {
        return
            findItemChildren(itemsByUid.get(rootUids.get(user.getId())));
    }

    private String calculateUid() {
        return idGenerator.nextStringIdentifier();
    }

    private String calculateTicketKey() {
        return idGenerator.nextStringIdentifier();
    }

    private Set findItemTickets(Item item) {
        Set itemTickets = (Set) tickets.get(item.getUid());
        if (itemTickets == null) {
            itemTickets = new HashSet();
            tickets.put(item.getUid(), itemTickets);
        }
        return itemTickets;
    }
}
