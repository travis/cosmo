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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.validator.InvalidStateException;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemTombstone;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;

/**
 * Implementation of ContentDao using hibernate persistence objects
 * 
 */
public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    private static final Log log = LogFactory.getLog(ContentDaoImpl.class);

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

        if (collection.getOwner() == null)
            throw new IllegalArgumentException("collection must have owner");
        
        if (collection.getId()!=-1)
            throw new IllegalArgumentException("invalid collection id (expected -1)");
        
        
        try {
            User owner = collection.getOwner();
            
            // verify uid not in use
            checkForDuplicateUid(collection);
            
            // We need to enforce a content hierarchy to support WebDAV
            // In a hierarchy, can't have two items with same name with
            // same owner and parent
            checkForDuplicateItemName(owner.getId(), parent.getId(), collection
                    .getName());
            
            setBaseItemProps(collection);
            collection.getParents().add(parent);
            
            getSession().save(collection);
            getSession().flush();
            
            return collection;
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } catch (InvalidStateException ise) {
            logInvalidStateException(ise);
            throw ise;
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#updateCollection(org.osaf.cosmo.model.CollectionItem, java.util.Set)
     */
    public CollectionItem updateCollection(CollectionItem collection, Set<ContentItem> children) {
        
        int count = 0;
        int batchSize = 20;
        
        try {
            collection = updateCollection(collection);
            
            // Either create, update, or delete each item
            for (ContentItem item : children) {
                
                // periodically clear the session to improve performance
                count++;
                if(count%batchSize==0)
                    getSession().clear();
                
                // create item
                if(item.getId()==-1) {
                    item = createContent(collection, item);
                }
                // delete item
                else if(item.getIsActive()==false) {
                    removeItemFromCollection(item, collection);
                }
                // update item
                else {
                    // Here is the tricky part.  If the session has
                    // been cleared, then in order to prevent Hibernate
                    // exceptions like "found two representations of the
                    // same collection..", we need to merge the transient
                    // item state with the persistent state, and pass the
                    // peristent object into updateContent().
                    if(!getSession().contains(item)) {
                        item = (ContentItem) getSession().merge(item);
                    }
                    if(!item.getParents().contains(collection))
                        addItemToCollection(item, collection);
                    updateContent(item);
                }
            }
            
            return collection;
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
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
        
        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");
        
        try {
            User owner = content.getOwner();

            // verify uid not in use
            checkForDuplicateUid(content);
            
            // Enforce hiearchy for WebDAV support
            // In a hierarchy, can't have two items with same name with
            // same parent
            checkForDuplicateItemName(owner.getId(), parent.getId(), content.getName());

            setBaseItemProps(content);
            
            // add parent to new content
            content.getParents().add(parent);
            
            // remove tomstone (if it exists) from parent
            if(parent.removeTombstone(content)==true)
                getSession().update(parent);
            
            getSession().save(content);
            getSession().flush();
            return content;
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } catch (InvalidStateException ise) {
            logInvalidStateException(ise);
            throw ise;
        }
    }
    
    public ContentItem createContent(ContentItem content) {

        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (content.getId()!=-1)
            throw new IllegalArgumentException("invalid content id (expected -1)");
        
        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");
        
        try {
            // verify uid not in use
            checkForDuplicateUid(content);
            
            setBaseItemProps(content);
            
            getSession().save(content);
            getSession().flush();
            return content;
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } catch (InvalidStateException ise) {
            logInvalidStateException(ise);
            throw ise;
        }
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#createContent(java.util.Set, org.osaf.cosmo.model.ContentItem)
     */
    public ContentItem createContent(Set<CollectionItem> parents, ContentItem content) {

        if(parents==null)
            throw new IllegalArgumentException("parent cannot be null");
        
        if(parents.size()==0)
            throw new IllegalArgumentException("content must have at least one parent");
        
        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (content.getId()!=-1)
            throw new IllegalArgumentException("invalid content id (expected -1)");
        
        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");
        
        try {
            User owner = content.getOwner();

            // verify uid not in use
            checkForDuplicateUid(content);
            
            // Enforce hiearchy for WebDAV support
            // In a hierarchy, can't have two items with same name with
            // same parent
            for(Item parent: parents)
                checkForDuplicateItemName(owner.getId(), parent.getId(), content.getName());

            setBaseItemProps(content);
            for(CollectionItem parent: parents) {
                content.getParents().add(parent);
                if(parent.removeTombstone(content)==true)
                    getSession().update(parent);
            }
            
            getSession().save(content);
            getSession().flush();
            return content;
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } catch (InvalidStateException ise) {
            logInvalidStateException(ise);
            throw ise;
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
            throw convertHibernateAccessException(e);
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
            throw convertHibernateAccessException(e);
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
            throw convertHibernateAccessException(e);
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
            throw convertHibernateAccessException(e);
        }
    }

    public void updateCollectionTimestamp(String collectionUid) {
        try {
            CollectionItem collection = findCollectionByUid(collectionUid);
            collection.setModifiedDate(new Date());
            getSession().flush();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
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
            
            getSession().update(collection);
            
            if (collection.getOwner() == null)
                throw new IllegalArgumentException("collection must have owner");
            
            // In a hierarchy, can't have two items with same name with
            // same parent
            if (collection.getParents().size() > 0)
                checkForDuplicateItemNameMinusItem(collection.getOwner().getId(), 
                    collection.getParents(), collection.getName(), collection.getId());
            
            collection.setModifiedDate(new Date());
            
            getSession().flush();
            
            return collection;
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } catch (InvalidStateException ise) {
            logInvalidStateException(ise);
            throw ise;
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
             
            getSession().update(content);
            
            if (content.getOwner() == null)
                throw new IllegalArgumentException("content must have owner");
            
            // In a hierarchy, can't have two items with same name with
            // same parent
            checkForDuplicateItemNameMinusItem(content.getOwner().getId(), 
                    content.getParents(), content.getName(), content.getId());
            
            content.setModifiedDate(new Date());
            
            getSession().flush();
            
            return content;
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } catch (InvalidStateException ise) {
            logInvalidStateException(ise);
            throw ise;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#removeCollection(org.osaf.cosmo.model.CollectionItem)
     */
    public void removeCollection(CollectionItem collection) {
        
        if(collection==null)
            throw new IllegalArgumentException("collection cannot be null");
        
        try {
            getSession().refresh(collection);
            removeCollectionRecursive(collection);
            getSession().flush();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dao.ContentDao#removeContent(org.osaf.cosmo.model.ContentItem)
     */
    public void removeContent(ContentItem content) {
        
        if(content==null)
            throw new IllegalArgumentException("content cannot be null");
        
        try {
            getSession().refresh(content);
            removeContentRecursive(content);
            getSession().flush();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ContentDao#loadChildren(org.osaf.cosmo.model.CollectionItem, java.util.Date)
     */
    public Set<ContentItem> loadChildren(CollectionItem collection, Date timestamp) {
        try {
            Set<ContentItem> children = new HashSet<ContentItem>();
            Query query = null;

            if (timestamp == null)
                query = getSession().getNamedQuery("contentItem.by.parent")
                        .setParameter("parent", collection);
            else
                query = getSession().getNamedQuery("contentItem.by.parent.timestamp")
                        .setParameter("parent", collection).setParameter(
                                "timestamp", timestamp);

            List results = query.list();
            for (Iterator it = results.iterator(); it.hasNext();) {
                ContentItem content = (ContentItem) it.next();
                initializeItem(content);
                children.add(content);
            }

            return children;
            
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }


    @Override
    public void initializeItem(Item item) {
        super.initializeItem(item);
        
        // Initialize master NoteItem if applicable
        try {
           if(item instanceof NoteItem) {
               NoteItem note = (NoteItem) item;
               if(note.getModifies()!=null) {
                   Hibernate.initialize(note.getModifies());
                   initializeItem(note.getModifies());
               }
           }
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
        
    }

    @Override
    public void removeItem(Item item) {
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else if(item instanceof CollectionItem)
            removeCollection((CollectionItem) item);
        else
            super.removeItem(item);
    }

    @Override
    public void removeItemByPath(String path) {
        Item item = this.findItemByPath(path);
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else if(item instanceof CollectionItem)
            removeCollection((CollectionItem) item);
        else
            super.removeItem(item);
    }

    @Override
    public void removeItemByUid(String uid) {
        Item item = this.findItemByUid(uid);
        if(item instanceof ContentItem)
            removeContent((ContentItem) item);
        else if(item instanceof CollectionItem)
            removeCollection((CollectionItem) item);
        else
            super.removeItem(item);
    }
    
    /**
     * Initializes the DAO, sanity checking required properties and defaulting
     * optional properties.
     */
    public void init() {
        super.init();
    }

    private void removeContentRecursive(ContentItem content) {
        // Add a tombstone to each parent collection to track
        // when the removal occurred.
        for(CollectionItem parent : content.getParents()) {
            parent.addTombstone(new ItemTombstone(parent, content));
            getSession().update(parent);
        }
        
        getSession().delete(content);
    }
    
    private void removeCollectionRecursive(CollectionItem collection) {
        // Removing a collection does not automatically remove
        // its children.  Instead, the association to all the
        // children is removed, and any children who have no
        // parent collection are then removed.
        for(Item item: collection.getChildren()) {
            if(item instanceof CollectionItem) {
                removeCollectionRecursive((CollectionItem) item);
            } else if(item instanceof ContentItem) {                    
                item.getParents().remove(collection);
                if(item.getParents().size()==0)
                    getSession().delete(item);
            } else {
                getSession().delete(item);
            }
        }
        
        getSession().delete(collection);
    }
}
