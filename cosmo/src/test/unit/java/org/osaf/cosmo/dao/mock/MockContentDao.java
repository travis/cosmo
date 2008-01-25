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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.BaseModelObject;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockCollectionItem;
import org.osaf.cosmo.model.mock.MockContentItem;

import org.springframework.dao.ConcurrencyFailureException;

/**
 * Mock implementation of <code>ContentDao</code> useful for testing.
 *
 * @see ContentDao
 * @see ContentItem
 * @see CollectionItem
 */
public class MockContentDao extends MockItemDao implements ContentDao {
    private static final Log log = LogFactory.getLog(MockItemDao.class);

    public static boolean THROW_CONCURRENT_EXCEPTION = false;
    
    /**
     */
    public MockContentDao(MockDaoStorage storage) {
        super(storage);
    }

    // ContentDao methods

    /**
     * Create a new collection.
     *
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection) {
        if (parent == null)
            throw new IllegalArgumentException("parent cannot be null");
        if (collection == null)
            throw new IllegalArgumentException("collection cannot be null");
        if (getStorage().getItemByUid(collection.getUid()) != null)
            throw new UidInUseException(collection.getUid());

        collection.getParents().add(parent);

        getStorage().storeItem((Item) collection);

        return collection;
    }

    /**
     * Update an existing collection.
     *
     * @param collection
     *            collection to update
     * @return updated collection
     */
    public CollectionItem updateCollection(CollectionItem collection) {
        if (collection == null)
            throw new IllegalArgumentException("collection cannot be null");

        if (collection instanceof MockCollectionItem)
            ((MockCollectionItem) collection).setVersion(collection
                    .getVersion() + 1);
        
        getStorage().updateItem(collection);

        return collection;
    }

    /**
     * Find collection by hierachical path. Path must be in the form:
     * /username/parent1/parent2/collectionname
     *
     * @param path
     *            path of collection
     * @return collection represented by path
     */
    public CollectionItem findCollectionByPath(String path) {
        return (CollectionItem) findItemByPath(path);
    }

    /**
     * Find collection by uid.
     *
     * @param uid
     *            uid of collection
     * @return collection represented by uid
     */
    public CollectionItem findCollectionByUid(String uid) {
        return (CollectionItem) findItemByUid(uid);
    }

    /**
     * Find all children for collection. Children can consist of ContentItem and
     * CollectionItem objects.
     *
     * @param collection
     *            collection to find children for
     * @return collection of child objects for parent collection. Child objects
     *         can be either CollectionItem or ContentItem.
     */
    public Set findChildren(CollectionItem collection) {
        return getStorage().findItemChildren(collection);
    }

    /**
     * Find all top level children for user. Children can consist of ContentItem
     * and CollectionItem objects.
     *
     * @param collection
     *            collection to find children for
     * @return collection of child objects for parent collection. Child objects
     *         can be either CollectionItem or ContentItem.
     */
    public Set findChildren(User user) {
        return findRootChildren(user);
    }

    /**
     * Create new content item. A content item represents a piece of content or
     * file.
     *
     * @param parent
     *            parent collection of content. If null, content is assumed to
     *            live in the top-level user collection
     * @param content
     *            content to create
     * @return newly created content
     */
    public ContentItem createContent(CollectionItem parent,
                                     ContentItem content) {
        if (parent == null)
            throw new IllegalArgumentException("parent cannot be null");
        if (content == null)
            throw new IllegalArgumentException("collection cannot be null");

        if(THROW_CONCURRENT_EXCEPTION)
            throw new ConcurrencyFailureException("fail!");

        if (getStorage().getItemByUid(content.getUid()) != null)
            throw new UidInUseException("Uid " + content.getUid() + " already in use");
        
        content.getParents().add(parent);
        
        getStorage().storeItem((Item)content);

        return content;
    }
    
    /**
     * Create new content item. A content item represents a piece of content or
     * file.
     *
     * @param content
     *            content to create
     * @return newly created content
     */
    public ContentItem createContent(ContentItem content) {
       
        if (content == null)
            throw new IllegalArgumentException("collection cannot be null");

        if(THROW_CONCURRENT_EXCEPTION)
            throw new ConcurrencyFailureException("fail!");
        
        if (getStorage().getItemByUid(content.getUid()) != null)
            throw new UidInUseException("Uid " + content.getUid() + " already in use");
        
        getStorage().storeItem((Item)content);

        return content;
    }

    /**
     * Update an existing content item.
     *
     * @param content
     *            content item to update
     * @return updated content item
     */
    public ContentItem updateContent(ContentItem content) {
        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if(THROW_CONCURRENT_EXCEPTION)
            throw new ConcurrencyFailureException("fail!");

        Item stored = getStorage().getItemByUid(content.getUid());
        if (stored != null && stored != content)
            throw new UidInUseException("Uid " + content.getUid() + " already in use");
        
        if(content instanceof MockContentItem)
            ((MockContentItem) content).setVersion(content.getVersion()+1);
        
        getStorage().updateItem((Item) content);

        return content;
    }

    /**
     * Move an Item to a different collection
     *
     * @param parent
     *            collection to add item to
     * @param item
     *            item to move
     * @throws ModelValidationException
     *             if parent is invalid
     */
    public void moveContent(CollectionItem parent,
                            Item item) {
        if (parent == null)
            throw new IllegalArgumentException("parent cannot be null");
        if (item == null)
            throw new IllegalArgumentException("item cannot be null");

        item.getParents().add(parent);
        getStorage().updateItem(item);
    }

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     *
     * @param path
     *            path of content to find
     * @return content item represented by hierachical path
     */
    public ContentItem findContentByPath(String path) {
        return (ContentItem) findItemByPath(path);
    }

    /**
     * Find content item by uid.
     *
     * @param uid
     *            uid of content to find
     * @return content item represented by uid
     */
    public ContentItem findContentByUid(String uid) {
        return (ContentItem) findItemByUid(uid);
    }

    /**
     * Remove content item
     *
     * @param content
     *            content item to remove
     */
    public void removeContent(ContentItem content) {
        removeItem(content);
    }

    public void removeUserContent(User user) {
        // do nothing for now
    }

    /**
     * Remove collection item
     *
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection) {
        removeItem(collection);
    }

    public ContentItem createContent(Set<CollectionItem> parents, ContentItem content) {
        if (parents == null || parents.size()==0)
            throw new IllegalArgumentException("parents cannot be null or empty");
        if (content == null)
            throw new IllegalArgumentException("collection cannot be null");

        if(THROW_CONCURRENT_EXCEPTION)
            throw new ConcurrencyFailureException("fail!");
        
        if (getStorage().getItemByUid(content.getUid()) != null)
            throw new UidInUseException("Uid " + content.getUid() + " already in use");
        
        content.getParents().addAll(parents);
          
        getStorage().storeItem((Item)content);

        return content;
    }

    public CollectionItem updateCollection(CollectionItem collection, Set<ContentItem> children) {
        for(ContentItem child : children) {
            if(child.getCreationDate()==null) {
                createContent(collection, child);
            }
            else if(child.getIsActive()==false) {
                removeContent(child);
            } else {
                if(!child.getParents().contains(collection)) {
                    addItemToCollection(child, collection);
                    updateContent(child);
                }
            }
            
        }
        return collection;
    }

    public CollectionItem updateCollectionTimestamp(CollectionItem collection) {
        ((MockCollectionItem) collection).setModifiedDate(new Date());
        if (collection instanceof MockCollectionItem)
            ((MockCollectionItem) collection).setVersion(collection
                    .getVersion() + 1);
        return collection;
    }

    public Set<ContentItem> loadChildren(CollectionItem collection, Date timestamp) {
        Set<ContentItem> items = new HashSet<ContentItem>();
        for(Item item : collection.getChildren()) {
            if(item instanceof ContentItem) {
                if(timestamp==null || item.getModifiedDate().after(timestamp))
                    items.add((ContentItem) item);
            }
        }
        return items;
    }
    
}
