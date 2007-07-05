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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
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
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampHandler;

/**
 * Implementation of ContentDao using hibernate persistence objects
 * 
 */
public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    private static final Log log = LogFactory.getLog(ContentDaoImpl.class);
    private HashMap<String, StampHandler> stampHandlers = new HashMap<String, StampHandler>();

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
            // verify uid not in use
            checkForDuplicateUid(collection);
            
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
        
        try {
            updateCollectionInternal(collection);
            
            // Either create, update, or delete each item
            for (ContentItem item : children) {
                
                // create item
                if(item.getId()==-1) {
                    createContentInternal(collection, item);
                }
                // delete item
                else if(item.getIsActive()==false) {
                    removeItemFromCollectionInternal(item, collection);
                }
                // update item
                else {
                    if(!item.getParents().contains(collection)) {
                        addItemToCollectionInternal(item, collection);
                    }
                    updateContentInternal(item);
                }
            }
            
            getSession().flush();
            
            // clear the session to improve subsequent flushes
            getSession().clear();
            
            // load collection to get it back into the session
            getSession().load(collection, collection.getId());
            
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
        
        try {
            createContentInternal(parent, content);
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
            
            applyStampHandlerCreate(content);
            
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
        
        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (content.getId()!=-1)
            throw new IllegalArgumentException("invalid content id (expected -1)");
        
        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");
        
        try {
            if(parents.size()==0)
                throw new IllegalArgumentException("content must have at least one parent");
            
            // verify uid not in use
            checkForDuplicateUid(content);
            
            setBaseItemProps(content);
            for(CollectionItem parent: parents) {
                content.getParents().add(parent);
                if(parent.removeTombstone(content)==true)
                    getSession().update(parent);
            }
            
            applyStampHandlerCreate(content);
            
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
            Query hibQuery = getSession()
                    .getNamedQuery("collectionItem.by.uid").setParameter("uid",uid);
            hibQuery.setFlushMode(FlushMode.MANUAL);
            return (CollectionItem) hibQuery.uniqueResult();
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
            Query hibQuery = getSession().getNamedQuery("contentItem.by.uid")
                    .setParameter("uid", uid);
            hibQuery.setFlushMode(FlushMode.MANUAL);
            return (ContentItem) hibQuery.uniqueResult();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

    public CollectionItem updateCollectionTimestamp(CollectionItem collection) {
        try {
            if(!getSession().contains(collection))
                collection = (CollectionItem) getSession().merge(collection);
            collection.setModifiedDate(new Date());
            getSession().flush();
            return collection;
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
            
            updateCollectionInternal(collection);
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
            updateContentInternal(content);
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

            // use custom HQL query that will eager fetch all associations
            if (timestamp == null)
                query = getSession().getNamedQuery("contentItem.by.parent")
                        .setParameter("parent", collection);
            else
                query = getSession().getNamedQuery("contentItem.by.parent.timestamp")
                        .setParameter("parent", collection).setParameter(
                                "timestamp", timestamp);
            
            query.setFlushMode(FlushMode.MANUAL);
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
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        if(item instanceof NoteItem)
            removeNoteItemFromCollection((NoteItem) item, collection);
        else
            super.removeItemFromCollection(item, collection);
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
    
       
    public HashMap<String, StampHandler> getStampHandlers() {
        return stampHandlers;
    }

    public void setStampHandlers(HashMap<String, StampHandler> stampHandlers) {
        this.stampHandlers = stampHandlers;
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
    
    
    /**
     * Remove NoteItem from a collection.  This includes removing any modificaions.
     */
    public void removeNoteItemFromCollection(NoteItem note, CollectionItem collection) {
        try {
            removeNoteItemFromCollectionInternal(note, collection);
            getSession().flush();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        } 
    }
    
    private void removeNoteItemFromCollectionInternal(NoteItem note, CollectionItem collection) {
        getSession().update(collection);
        getSession().update(note);
        
        // do nothing if item doesn't belong to collection
        if(!note.getParents().contains(collection))
            return;
        
        collection.addTombstone(new ItemTombstone(collection, note));
        note.getParents().remove(collection);
        
        for(NoteItem mod: note.getModifications())
            removeNoteItemFromCollectionInternal(mod, collection);
        
        // If the item belongs to no collection, then it should
        // be purged.
        if(note.getParents().size()==0)
            getSession().delete(note);
        
    }
    
    private void applyStampHandlerCreate(Item item) {
        
        if(stampHandlers.size()==0)
            return;
        
        for(Stamp stamp : item.getStamps()) {
            StampHandler sh = stampHandlers.get(stamp.getClass().getName());
            if(sh != null) {
                sh.onCreateItem(stamp);
                continue;
            }
        }
    }
    
    private void applyStampHandlerUpdate(Item item) {
        
        if(stampHandlers.size()==0)
            return;
        
        for(Stamp stamp : item.getStamps()) {
            StampHandler sh = stampHandlers.get(stamp.getClass().getName());
            if(sh != null) {
                sh.onUpdateItem(stamp);
                continue;
            }
        }
    }
    
    protected void createContentInternal(CollectionItem parent, ContentItem content) {

        if(parent==null)
            throw new IllegalArgumentException("parent cannot be null");
        
        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (content.getId()!=-1)
            throw new IllegalArgumentException("invalid content id (expected -1)");
        
        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");
        
        // verify uid not in use
        checkForDuplicateUid(content);
        
        setBaseItemProps(content);
        
        // add parent to new content
        content.getParents().add(parent);
        
        // remove tomstone (if it exists) from parent
        if(parent.removeTombstone(content)==true)
            getSession().update(parent);
        
        applyStampHandlerCreate(content);
        
        getSession().save(content);    
    }
    
    protected void updateContentInternal(ContentItem content) {
        
        if (content == null)
            throw new IllegalArgumentException("content cannot be null");
         
        getSession().update(content);
        
        if (content.getOwner() == null)
            throw new IllegalArgumentException("content must have owner");
        
        content.setModifiedDate(new Date());
        
        applyStampHandlerUpdate(content); 
    }
    
    protected void updateCollectionInternal(CollectionItem collection) {
        if (collection == null)
            throw new IllegalArgumentException("collection cannot be null");
        
        getSession().update(collection);
        
        if (collection.getOwner() == null)
            throw new IllegalArgumentException("collection must have owner");
        
        collection.setModifiedDate(new Date());
    }
    
    @Override
    protected void removeItemFromCollectionInternal(Item item, CollectionItem collection) {
        if(item instanceof NoteItem)
            removeNoteItemFromCollectionInternal((NoteItem) item, collection);
        else
            super.removeItemFromCollectionInternal(item, collection);
    }
    
}
