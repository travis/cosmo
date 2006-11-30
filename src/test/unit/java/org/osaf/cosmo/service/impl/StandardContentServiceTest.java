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
package org.osaf.cosmo.service.impl;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockContentItem;
import org.osaf.cosmo.TestHelper;

/**
 * Test Case for <code>StandardContentService</code> which uses mock
 * data access objects.
 *
 * @see StandardContentService
 * @see MockContentDao
 */
public class StandardContentServiceTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(StandardContentServiceTest.class);

    private StandardContentService service;
    private MockContentDao contentDao;
    private TestHelper testHelper;

    /** */
    protected void setUp() throws Exception {
        testHelper = new TestHelper();
        contentDao = new MockContentDao(new MockDaoStorage());
        service = new StandardContentService();
        service.setContentDao(contentDao);
        service.init();
    }

    /** */
    public void testFindItemByPath() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new ContentItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNotNull(item);
        assertEquals(dummyContent, item);

        contentDao.removeContent(dummyContent);
    }

    /** */
    public void testFindNonExistentItemByPath() throws Exception {
        String path = "/foo/bar/baz";
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }

    /** */
    public void testRemoveItem() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new ContentItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        contentDao.removeItem(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }

    /** */
    public void testCreateContent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        ContentItem content = new ContentItem();
        content.setName("foo");
        content.setOwner(user);
        content = contentDao.createContent(rootCollection, content);

        assertNotNull(content);
        assertEquals("foo", content.getName());
        assertEquals(user, content.getOwner());

        contentDao.removeContent(content);
    }

    /** */
    public void testRemoveContent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new ContentItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        contentDao.removeContent(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        assertNull(item);
    }

    public void testCreateCollectionWithChildren() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new CollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        ContentItem dummyContent = new ContentItem();
        dummyContent.setName("bar");
        dummyContent.setOwner(user);
        
        HashSet<Item> children = new HashSet<Item>();
        children.add(dummyContent);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection, children);
        
        assertNotNull(dummyCollection);
        assertEquals(1, dummyCollection.getChildren().size());
        assertEquals("bar", 
                dummyCollection.getChildren().iterator().next().getName());
    }
    
    public void testUpdateCollectionWithChildren() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new CollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        ContentItem dummyContent1 = new MockContentItem();
        dummyContent1.setName("bar1");
        dummyContent1.setOwner(user);
        
        ContentItem dummyContent2 = new MockContentItem();
        dummyContent2.setName("bar2");
        dummyContent2.setOwner(user);
        
        HashSet<Item> children = new HashSet<Item>();
        children.add(dummyContent1);
        children.add(dummyContent2);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection, children);
        
        assertEquals(2, dummyCollection.getChildren().size());
        
        ContentItem bar1 = 
            getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        ContentItem bar2 = 
            getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        assertNotNull(bar1);
        assertNotNull(bar2);
        
        bar1.setIsActive(false);
        bar2.addStringAttribute("foo", "bar");
        
        ContentItem bar3 = new MockContentItem();
        bar3.setName("bar3");
        bar3.setOwner(user);
        
        children.clear();
        children.add(bar1);
        children.add(bar2);
        children.add(bar3);
        
        dummyCollection = service.updateCollection(dummyCollection, children);
          
        assertEquals(2, dummyCollection.getChildren().size());
        
        bar1 = getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        bar2 = getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        bar3 = getContentItemFromSet(dummyCollection.getChildren(), "bar3");
        
        assertNull(bar1);
        assertNotNull(bar2);
        assertEquals(1,bar2.getAttributes().size());
        assertEquals("bar", bar2.getAttributeValue("foo"));
        assertNotNull(bar3);
    }
    
    /** */
    public void testNullContentDao() throws Exception {
        service.setContentDao(null);
        try {
            service.init();
            fail("Should not be able to initialize service without contentDao");
        } catch (IllegalStateException e) {
            // expected
        }
    }
    
    private ContentItem getContentItemFromSet(Set<Item> items, String name) {
        for(Item item : items)
            if(item.getName().equals(name))
                return (ContentItem) item;
        return null;
    }
}
