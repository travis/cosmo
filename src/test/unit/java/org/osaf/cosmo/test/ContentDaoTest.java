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
package org.osaf.cosmo.test;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.osaf.cosmo.dao.hibernate.ContentDaoImpl;
import org.osaf.cosmo.dao.hibernate.ItemDaoImpl;
import org.osaf.cosmo.dao.hibernate.UserDaoImpl;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.DateAttribute;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.MultiValueStringAttribute;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

public class ContentDaoTest extends HibernateDaoTestCase {

    ContentDaoImpl contentDao = null;
    UserDaoImpl userDao = null;
    ItemDaoImpl itemDao = null;


    public ContentDaoTest()
    {
        contentDao = (ContentDaoImpl) ctx.getBean("contentDao");
        userDao = (UserDaoImpl) ctx.getBean("userDao");
        itemDao = (ItemDaoImpl) ctx.getBean("itemDao");
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        removeAllUsers(userDao);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testContentDaoCreateContent() throws Exception
    {
        User user = getUser(userDao,"testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = new ContentItem();
        item.setName("test");
        item.setDisplayName("this is a test item");
        item.setOwner(user);
        item.setContent(getBytes(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");
        item.addAttribute(new StringAttribute("customattribute", "customattributevalue"));

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(newItem.getId()>-1);
        Assert.assertTrue(newItem.getUid()!=null);

        clearSession();
        
        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());

        verifyItem(newItem, queryItem);
    }
    
    public void testContentAttributes() throws Exception
    {
        User user = getUser(userDao,"testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = new ContentItem();
        item.setName("test");
        item.setOwner(user);
        item.setContent(new FileInputStream(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");
        item.addStringAttribute("customattribute", "customattributevalue");
        item.addIntegerAttribute("intattribute", new Long(22));
        // TODO: figure out db date type is handled because i'm seeing
        // issues with accuracy
        //item.addAttribute(new DateAttribute("dateattribute", new Date()));
        
        HashSet<String> values = new HashSet<String>();
        values.add("value1");
        values.add("value2");
        item.addMultiValueStringAttribute("multistringattribute", values);
        
        HashMap<String, String> dictionary = new HashMap<String, String>();
        dictionary.put("key1", "value1");
        dictionary.put("key2", "value2");
        item.addDictionaryAttribute("dictionaryattribute", dictionary);
        
        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(newItem.getId()>-1);
        Assert.assertTrue(newItem.getUid()!=null);

        clearSession();
        
        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());

        Set<String> querySet = (Set<String>) queryItem.getAttributeValue("multistringattribute");
        Assert.assertTrue(querySet.contains("value1"));
        Assert.assertTrue(querySet.contains("value2"));
        
        Map<String, String> queryDictionary = (Map<String, String>) queryItem.getAttributeValue("dictionaryattribute");
        Assert.assertEquals("value1", queryDictionary.get("key1"));
        Assert.assertEquals("value2", queryDictionary.get("key2"));
        
        Attribute custom = queryItem.getAttribute("customattribute");
        Assert.assertEquals("customattributevalue", custom.getValue());
        
        verifyItem(newItem, queryItem);
        
        // set attribute value to null
        custom.setValue(null);
        
        querySet.add("value3");
        queryDictionary.put("key3", "value3");
        
        queryItem.removeAttribute("intattribute");
        
        contentDao.updateContent(queryItem);
        
        clearSession();
        
        queryItem = contentDao.findContentByUid(newItem.getUid());
        querySet = (Set) queryItem.getAttributeValue("multistringattribute");
        queryDictionary = (Map) queryItem.getAttributeValue("dictionaryattribute");
        Attribute queryAttribute = queryItem.getAttribute("customattribute");
        Assert.assertTrue(querySet.contains("value3"));
        Assert.assertEquals("value3", queryDictionary.get("key3"));
        Assert.assertNotNull(queryAttribute);
        Assert.assertNull(queryAttribute.getValue());
        Assert.assertNull(queryItem.getAttribute("intattribute"));
    }

    public void testCreateDuplicateRootItem() throws Exception
    {
        User testuser = getUser(userDao, "testuser");
        try
        {
            contentDao.createRootItem(testuser);
            Assert.fail("able to create duplicate root item");
        }
        catch(RuntimeException re) {}
    }

    public void testDeleteRootItem() throws Exception
    {
        User testuser = getUser(userDao, "testuser");
        Item root = contentDao.getRootItem(testuser);
        try
        {
            contentDao.removeItem(root);
            Assert.fail("able to delete root item");
        }
        catch(IllegalArgumentException iae) {}
    }

    public void testFindWithItemDao() throws Exception
    {
        User testuser2 = getUser(userDao, "testuser2");

        CollectionItem root = (CollectionItem) contentDao.getRootItem(testuser2);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        Item queryItem = itemDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        queryItem = itemDao.findItemByPath("/testuser2/a");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        ContentItem item = new ContentItem();
        item.setName("test");
        item.setOwner(getUser(userDao, "testuser"));
        item.setContent(getBytes(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");
        item.addAttribute(new StringAttribute("customattribute", "customattributevalue"));

        item = contentDao.createContent(a, item);
        
        clearSession();
        
        queryItem = itemDao.findItemByPath("/testuser2/a/test");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);
        
        clearSession();
        
        queryItem = itemDao.findItemParentByPath("/testuser2/a/test");
        Assert.assertNotNull(queryItem);
        Assert.assertEquals(a.getUid(), queryItem.getUid());
    }

    public void testContentDaoUpdateContent() throws Exception
    {
        User user = getUser(userDao,"testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = new ContentItem();
        item.setName("test");
        item.setDisplayName("this is a test item");
        item.setOwner(user);
        item.setContent(getBytes(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");
        item.addAttribute(new StringAttribute("customattribute", "customattributevalue"));

        ContentItem newItem = contentDao.createContent(root, item);
        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());

        verifyItem(newItem, queryItem);
        Assert.assertEquals(0, newItem.getVersion().intValue());

        queryItem.setName("test2");
        queryItem.setDisplayName("this is a test item2");
        queryItem.getAttributes().remove("customattribute");
        queryItem.setContentLanguage("es");
        queryItem.setContent(getBytes(baseDir + "/testdata2.txt"));

        contentDao.updateContent(queryItem);

        clearSession();
        
        ContentItem queryItem2 = contentDao.findContentByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem2.getVersion().intValue());
        verifyItem(queryItem, queryItem2);
    }
    
    public void testContentDaoUpdateError() throws Exception
    {
        User user = getUser(userDao,"testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item1 = new ContentItem();
        item1.setName("test");
        item1.setOwner(user);
        item1.setContent(getBytes(baseDir + "/testdata1.txt"));
        item1.setContentEncoding("UTF8");
        item1.setContentType("text/text");
        item1.setContentLanguage("en");
       
        ContentItem item2 = new ContentItem();
        item2.setName("test2");
        item2.setOwner(user);
        item2.setContent(getBytes(baseDir + "/testdata1.txt"));
        item2.setContentEncoding("UTF8");
        item2.setContentType("text/text");
        item2.setContentLanguage("en");
        

        ContentItem newItem1 = contentDao.createContent(root, item1);
        ContentItem newItem2 = contentDao.createContent(root, item2);
        
        clearSession();
        
        ContentItem queryItem2 = contentDao.findContentByUid(newItem2.getUid());
        
        queryItem2.setName("test");
        try {
            contentDao.updateContent(queryItem2);
            Assert.fail("able to update item with duplicate name");
        } catch(DuplicateItemNameException dine) {}
        
    }

    public void testContentDaoDeleteContent() throws Exception
    {
        User user = getUser(userDao,"testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = new ContentItem();
        item.setName("test");
        item.setOwner(user);
        item.setContent(getBytes(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");
        item.addAttribute(new StringAttribute("customattribute", "customattributevalue"));

        ContentItem newItem = contentDao.createContent(root, item);
        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());
        verifyItem(newItem, queryItem);

        contentDao.removeContent(queryItem);
        
        clearSession();
        
        queryItem = contentDao.findContentByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    public void testDeleteContentByPath() throws Exception
    {
        User user = getUser(userDao,"testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = new ContentItem();
        item.setName("test");
        item.setOwner(user);
        item.setContent(getBytes(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");
        item.addAttribute(new StringAttribute("customattribute", "customattributevalue"));

        ContentItem newItem = contentDao.createContent(root, item);
        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());
        verifyItem(newItem, queryItem);

        contentDao.removeItemByPath("/testuser/test");
        
        clearSession();
        
        queryItem = contentDao.findContentByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    public void testDeleteContentByUid() throws Exception
    {
        User user = getUser(userDao,"testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = new ContentItem();
        item.setName("test");
        item.setOwner(user);
        item.setContent(getBytes(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");
        item.addAttribute(new StringAttribute("customattribute", "customattributevalue"));

        ContentItem newItem = contentDao.createContent(root, item);
        ContentItem queryItem = contentDao.findContentByUid(newItem.getUid());
        verifyItem(newItem, queryItem);

        contentDao.removeItemByUid(queryItem.getUid());
        
        clearSession();
        
        queryItem = contentDao.findContentByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    public void testContentDaoCreateCollection() throws Exception
    {
        User user = getUser(userDao,"testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        Assert.assertTrue(a.getId() > -1);
        Assert.assertNotNull(a.getUid());

        clearSession();
        
        CollectionItem queryItem = contentDao.findCollectionByUid(a.getUid());
        verifyItem(a, queryItem);
    }

    public void testContentDaoUpdateCollection() throws Exception
    {
        User user = getUser(userDao,"testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        Assert.assertTrue(a.getId() > -1);
        Assert.assertNotNull(a.getUid());

        CollectionItem queryItem = contentDao.findCollectionByUid(a.getUid());
        verifyItem(a, queryItem);

        a.setName("b");
        contentDao.updateCollection(a);
        
        clearSession();
        
        queryItem = contentDao.findCollectionByUid(a.getUid());
        verifyItem(a, queryItem);
    }

    public void testContentDaoDeleteCollection() throws Exception
    {
        User user = getUser(userDao,"testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        clearSession();
        
        CollectionItem queryItem = contentDao.findCollectionByUid(a.getUid());
        Assert.assertNotNull(queryItem);

        contentDao.removeCollection(queryItem);
        
        clearSession();
        
        queryItem = contentDao.findCollectionByUid(a.getUid());
        Assert.assertNull(queryItem);
    }

    public void testContentDaoAdvanced() throws Exception
    {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(testuser2);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new CollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        ContentItem c = new ContentItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));
        c.setContent(getBytes(baseDir + "/testdata1.txt"));
        c.setContentEncoding("UTF8");
        c.setContentType("text/text");
        c.setContentLanguage("en");

        c = contentDao.createContent(b, c);

        ContentItem d = new ContentItem();
        d.setName("d");
        d.setOwner(getUser(userDao, "testuser2"));
        d.setContent(getBytes(baseDir + "/testdata1.txt"));
        d.setContentEncoding("UTF8");
        d.setContentType("text/text");
        d.setContentLanguage("en");

        d = contentDao.createContent(a, d);

        clearSession();
        
        // test duplicates
        CollectionItem a2 = new CollectionItem();
        a2.setName("a");
        a2.setOwner(getUser(userDao, "testuser2"));
        
        ContentItem d2 = new ContentItem();
        d2.setName("d");
        d2.setOwner(getUser(userDao, "testuser2"));
        d2.setContent(getBytes(baseDir + "/testdata1.txt"));
        d2.setContentEncoding("UTF8");
        d2.setContentType("text/text");
        d2.setContentLanguage("en");
        
        try
        {
            contentDao.createCollection(root, a2);
            Assert.fail("Should not be able to create duplicate collection");
        }
        catch(DuplicateItemNameException dine) {}

        try
        {
            contentDao.createContent(a, d2);
            Assert.fail("Should not be able to create duplicate content");
        }
        catch(DuplicateItemNameException dine) {}

        
        a = contentDao.findCollectionByUid(a.getUid());
        b = contentDao.findCollectionByUid(b.getUid());
        c = contentDao.findContentByUid(c.getUid());
        d = contentDao.findContentByUid(d.getUid());
        root = contentDao.getRootItem(testuser2);
        
        Assert.assertNotNull(a);
        Assert.assertNotNull(b);
        Assert.assertNotNull(d);
        Assert.assertNotNull(root);
        
        // test children
        Collection children = contentDao.findChildren(a);
        Assert.assertEquals(2, children.size());
        verifyContains(children, b);
        verifyContains(children, d);

        children = contentDao.findChildren(root);
        Assert.assertEquals(1, children.size());
        verifyContains(children, a);

        // test get by path
        ContentItem queryC = contentDao.findContentByPath("/testuser2/a/b/c");
        Assert.assertNotNull(queryC);
        verifyInputStream(new FileInputStream(baseDir + "/testdata1.txt"), queryC.getContent());
        Assert.assertEquals("c", queryC.getName());

        // test get path
        String cPath1 = contentDao.getItemPath(c);
        String cPath2 = contentDao.getItemPath(c.getUid());

        Assert.assertEquals("/testuser2/a/b/c", cPath1);
        Assert.assertEquals(cPath1, cPath2);

        // test get path/uid abstract
        Item queryItem = contentDao.findItemByPath("/testuser2/a/b/c");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);

        queryItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        // test delete
        contentDao.removeContent(c);
        queryC = contentDao.findContentByUid(c.getUid());
        Assert.assertNull(queryC);

        contentDao.removeCollection(a);

        CollectionItem queryA = contentDao.findCollectionByUid(a.getUid());
        Assert.assertNull(queryA);

        ContentItem queryD = contentDao.findContentByUid(d.getUid());
        Assert.assertNull(queryD);
    }

    public void testContentDaoMove() throws Exception
    {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(testuser2);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new CollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        CollectionItem c = new CollectionItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));

        c = contentDao.createCollection(b, c);

        ContentItem d = new ContentItem();
        d.setName("d");
        d.setOwner(getUser(userDao, "testuser2"));
        d.setContent(getBytes(baseDir + "/testdata1.txt"));
        d.setContentEncoding("UTF8");
        d.setContentType("text/text");
        d.setContentLanguage("en");

        d = contentDao.createContent(c, d);

        CollectionItem e = new CollectionItem();
        e.setName("e");
        e.setOwner(getUser(userDao, "testuser2"));

        e = contentDao.createCollection(a, e);

        clearSession();
        
        // verify can't move root collection
        try {
            contentDao.moveContent(a, root);
            Assert.fail("able to move root collection");
        }
        catch(IllegalArgumentException iae) {}

        // verify can't move to root collection
        try {
            contentDao.moveContent(null, e);
            Assert.fail("able to move to root collection");
        }
        catch(IllegalArgumentException iae) {}

        // verify can't create loop
        try {
            contentDao.moveContent(c, b);
            Assert.fail("able to create loop");
        }
        catch(ModelValidationException iae) {}


        // verify that move works
        e = contentDao.findCollectionByUid(e.getUid());
        b = contentDao.findCollectionByPath("/testuser2/a/b");
        Assert.assertNotNull(b);

        contentDao.moveContent(e, b);

        clearSession();
        
        CollectionItem queryCollection = contentDao.findCollectionByPath("/testuser2/a/e/b");
        Assert.assertNotNull(queryCollection);
    }
    
    public void testHomeCollection() throws Exception
    {
        User testuser2 = getUser(userDao, "testuser2");
        HomeCollectionItem root = contentDao.getRootItem(testuser2);
        
        Assert.assertNotNull(root);
        root.setName("alsfjal;skfjasd");
        Assert.assertEquals(root.getName(), "testuser2");
        
    }
    
    public void testItemDaoMove() throws Exception
    {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(testuser2);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new CollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        CollectionItem c = new CollectionItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));

        c = contentDao.createCollection(b, c);

        ContentItem d = new ContentItem();
        d.setName("d");
        d.setOwner(getUser(userDao, "testuser2"));
        d.setContent(getBytes(baseDir + "/testdata1.txt"));
        d.setContentEncoding("UTF8");
        d.setContentType("text/text");
        d.setContentLanguage("en");

        d = contentDao.createContent(c, d);

        CollectionItem e = new CollectionItem();
        e.setName("e");
        e.setOwner(getUser(userDao, "testuser2"));

        e = contentDao.createCollection(a, e);

        clearSession();
        
        root = (CollectionItem) contentDao.getRootItem(testuser2);
        e = contentDao.findCollectionByUid(e.getUid());
        b = contentDao.findCollectionByUid(b.getUid());
        
        // verify can't move root collection
        try {
            contentDao.moveItem(root, "/testuser2/a/blah");
            Assert.fail("able to move root collection");
        }
        catch(IllegalArgumentException iae) {}

        // verify can't move to root collection
        try {
            contentDao.moveItem(e, "/testuser2");
            Assert.fail("able to move to root collection");
        }
        catch(ItemNotFoundException infe) {}

        // verify can't create loop
        try {
            contentDao.moveItem(b, "/testuser2/a/b/c/new");
            Assert.fail("able to create loop");
        }
        catch(ModelValidationException iae) {}

        clearSession();

        // verify that move works
        b = contentDao.findCollectionByPath("/testuser2/a/b");
        
        contentDao.moveItem(b, "/testuser2/a/e/b");

        clearSession();
        
        CollectionItem queryCollection = contentDao.findCollectionByPath("/testuser2/a/e/b");
        Assert.assertNotNull(queryCollection);
        
        contentDao.moveItem(queryCollection,"/testuser2/a/e/bnew" );
        
        clearSession();
        queryCollection = contentDao.findCollectionByPath("/testuser2/a/e/bnew");
        Assert.assertNotNull(queryCollection);
        
        Item queryItem = contentDao.findItemByPath("/testuser2/a/e/bnew/c/d");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);
    }

    public void testItemDaoCopy() throws Exception
    {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(testuser2);

        CollectionItem a = new CollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new CollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        CollectionItem c = new CollectionItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));

        c = contentDao.createCollection(b, c);

        ContentItem d = new ContentItem();
        d.setName("d");
        d.setOwner(getUser(userDao, "testuser2"));
        d.setContent(getBytes(baseDir + "/testdata1.txt"));
        d.setContentEncoding("UTF8");
        d.setContentType("text/text");
        d.setContentLanguage("en");

        d = contentDao.createContent(c, d);

        CollectionItem e = new CollectionItem();
        e.setName("e");
        e.setOwner(getUser(userDao, "testuser2"));

        e = contentDao.createCollection(a, e);

        clearSession();
        
        root = (CollectionItem) contentDao.getRootItem(testuser2);
        e = contentDao.findCollectionByUid(e.getUid());
        b = contentDao.findCollectionByUid(b.getUid());
        
        // verify can't copy root collection
        try {
            contentDao.copyItem(root, "/testuser2/a/blah",true);
            Assert.fail("able to copy root collection");
        }
        catch(IllegalArgumentException iae) {}

        // verify can't move to root collection
        try {
            contentDao.copyItem(e, "/testuser2",true);
            Assert.fail("able to move to root collection");
        }
        catch(ItemNotFoundException infe) {}

        // verify can't create loop
        try {
            contentDao.copyItem(b, "/testuser2/a/b/c/new",true);
            Assert.fail("able to create loop");
        }
        catch(ModelValidationException iae) {}

        clearSession();

        // verify that copy works
        b = contentDao.findCollectionByPath("/testuser2/a/b");
        
        contentDao.copyItem(b, "/testuser2/a/e/bcopy",true);

        clearSession();
        
        CollectionItem queryCollection = contentDao.findCollectionByPath("/testuser2/a/e/bcopy");
        Assert.assertNotNull(queryCollection);
        
        queryCollection = contentDao.findCollectionByPath("/testuser2/a/e/bcopy/c");
        Assert.assertNotNull(queryCollection);
        
        d = contentDao.findContentByUid(d.getUid());
        ContentItem dcopy = contentDao.findContentByPath("/testuser2/a/e/bcopy/c/d");
        Assert.assertNotNull(dcopy);
        Assert.assertEquals(d.getName(), dcopy.getName());
        Assert.assertNotSame(d.getUid(), dcopy.getUid());
        verifyBytes(d.getContent(), dcopy.getContent());
        
        clearSession();
        
        b = contentDao.findCollectionByPath("/testuser2/a/b");
        
        contentDao.copyItem(b, "/testuser2/a/e/bcopyshallow",false);

        clearSession();
        
        queryCollection = contentDao.findCollectionByPath("/testuser2/a/e/bcopyshallow");
        Assert.assertNotNull(queryCollection);
        
        queryCollection = contentDao.findCollectionByPath("/testuser2/a/e/bcopyshallow/c");
        Assert.assertNull(queryCollection);
        
        clearSession();
        d = contentDao.findContentByUid(d.getUid());
        contentDao.copyItem(d, "/testuser2/dcopy",true);
        
        dcopy = contentDao.findContentByPath("/testuser2/dcopy");
        Assert.assertNotNull(dcopy);
    }
    
    public void testTickets() throws Exception
    {
        User testuser = getUser(userDao, "testuser");
        String name = "ticketable:" + System.currentTimeMillis();
        ContentItem item = new ContentItem();
        item.setName(name);
        item.setOwner(testuser);
        item.setContent(getBytes(baseDir + "/testdata1.txt"));
        item.setContentEncoding("UTF8");
        item.setContentType("text/text");
        item.setContentLanguage("en");

        CollectionItem root = (CollectionItem) contentDao.getRootItem(testuser);
        ContentItem newItem = contentDao.createContent(root, item);

        Ticket ticket1 = new Ticket();
        ticket1.setKey("ticket1");
        ticket1.setTimeout(10);
        ticket1.setOwner(testuser);
        HashSet privs = new HashSet();
        privs.add("priv1");
        privs.add("privs2");
        ticket1.setPrivileges(privs);

        contentDao.createTicket("/testuser/" + name ,ticket1);

        Ticket ticket2 = new Ticket();
        ticket2.setKey("ticket2");
        ticket2.setTimeout(100);
        ticket2.setOwner(testuser);
        privs = new HashSet();
        privs.add("priv3");
        privs.add("priv4");
        ticket2.setPrivileges(privs);

        contentDao.createTicket("/testuser/" + name ,ticket2);

        clearSession();
        
        Ticket queryTicket1 = contentDao.getTicket("/testuser/" + name, "ticket1");
        Assert.assertNotNull(queryTicket1);
        verifyTicket(queryTicket1, ticket1);

        Collection tickets = contentDao.getTickets("/testuser/"+ name);
        Assert.assertEquals(2, tickets.size());
        verifyTicketInCollection(tickets, ticket1.getKey() );
        verifyTicketInCollection(tickets, ticket2.getKey() );

        contentDao.removeTicket("/testuser/" + name, ticket1);
        tickets = contentDao.getTickets("/testuser/"+ name);
        Assert.assertEquals(1, tickets.size());
        verifyTicketInCollection(tickets, ticket2.getKey() );

        queryTicket1 = contentDao.getTicket("/testuser/" + name, "ticket1");
        Assert.assertNull(queryTicket1);

        Ticket queryTicket2 = contentDao.getTicket("/testuser/" + name, "ticket2");
        Assert.assertNotNull(queryTicket2);
        verifyTicket(queryTicket2, ticket2);

        contentDao.removeTicket("/testuser/" + name, ticket2);

        tickets = contentDao.getTickets("/testuser/" + name);
        Assert.assertEquals(0, tickets.size());
    }

    private void verifyTicket(Ticket ticket1, Ticket ticket2)
    {
        Assert.assertEquals(ticket1.getKey(), ticket2.getKey());
        Assert.assertEquals(ticket1.getTimeout(), ticket2.getTimeout());
        Assert.assertEquals(ticket1.getOwner().getUsername(), ticket2.getOwner().getUsername());
        Iterator it1 = ticket1.getPrivileges().iterator();
        Iterator it2 = ticket2.getPrivileges().iterator();

        Assert.assertEquals(ticket1.getPrivileges().size(), ticket1.getPrivileges().size());

        while(it1.hasNext())
            Assert.assertEquals(it1.next(), it2.next());
    }

    private void verifyTicketInCollection(Collection tickets, String name)
    {
        for(Iterator it = tickets.iterator();it.hasNext();)
        {
            Ticket ticket = (Ticket) it.next();
            if(ticket.getKey().equals(name))
                return;
        }

        Assert.fail("could not find ticket: " + name);
    }

    private void verifyContains(Collection items, CollectionItem collection)
    {
        for(Iterator it=items.iterator();it.hasNext();)
        {
            Item item = (Item) it.next();
            if(item instanceof CollectionItem && item.getName().equals(collection.getName()))
                return;
        }
        Assert.fail("collection not found");
    }

    private void verifyContains(Collection items, ContentItem content)
    {
        for(Iterator it=items.iterator();it.hasNext();)
        {
            Item item = (Item) it.next();
            if(item instanceof ContentItem && item.getName().equals(content.getName()))
                return;
        }
        Assert.fail("content not found");
    }

    private User getUser(UserDao userDao, String username)
    {
        return getUser(userDao,contentDao,username);
    }

}
