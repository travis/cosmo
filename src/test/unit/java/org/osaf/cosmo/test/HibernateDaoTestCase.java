/*
 * Copyright (c) 2006 SimDesk Technologies, Inc.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * SimDesk Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with SimDesk Technologies.
 *
 * SIMDESK TECHNOLOGIES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
 * THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  SIMDESK TECHNOLOGIES
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.osaf.cosmo.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class HibernateDaoTestCase extends SpringTestCase {
	
	protected String baseDir = "src/test/unit/resources/testdata";
    protected SessionFactory sessionFactory = null;
    protected Session session = null;
    
    public HibernateDaoTestCase() {
        super(new String[] {
            "applicationContext.xml",
            "applicationContext-test.xml",
        });
        sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");
    }
    
    protected void clearSession() {
        session.flush();
        session.clear();
    }
    
    protected void setUp() throws Exception
	{
        super.setUp();
	    session = SessionFactoryUtils.getSession(sessionFactory, true);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        Session s = holder.getSession(); 
        s.flush();
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.releaseSession(s, sessionFactory);
    }
	
	protected byte[] getBytes(String fileName) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(fileName);
		IOUtils.copy(fis, baos);
		return baos.toByteArray();
	}
	
	protected void verifyInputStream(InputStream is1, byte[] content) throws Exception
	{
		byte[] buf1 = new byte[4096];
		byte[] buf2 = new byte[4096];
		
		ByteArrayInputStream is2 = new ByteArrayInputStream(content);
		
		int read1 = is1.read(buf1);
		int read2 = is2.read(buf2);
		while(read1 > 0 || read2 > 0)
		{
			Assert.assertEquals(read1, read2);
			
			for(int i=0;i<read1;i++)
				Assert.assertEquals(buf1[i], buf2[i]);
			
			read1 = is1.read(buf1);
			read2 = is2.read(buf2);
		}
	}
    
    protected void verifyBytes(byte[] content1, byte[] content2) throws Exception
    {
        ByteArrayInputStream is = new ByteArrayInputStream(content1);
        verifyInputStream(is,content2);
    }
	
	protected User getUser(UserDao userDao, ContentDao contentDao, String username)
	{
		User user = userDao.getUser(username);
		if(user==null)
		{
			user = new User();
			user.setUsername(username);
			user.setEmail(username + "@testem");
			user.setAdmin(Boolean.TRUE);
			user.setFirstName("testfn");
			user.setLastName("testln");
			userDao.createUser(user);
			user = userDao.getUser(username);
            
            // create root item
            contentDao.createRootItem(user);
		}
		return user;
	}
    
    protected User createDummyUser(UserDao userDao, int index)
    {
        User user = new User();
        user.setUsername("user" + index);
        user.setEmail("user" + index + "@test" + index);
        user.setAdmin(Boolean.TRUE);
        user.setFirstName("fristname" + index);
        user.setLastName("lastname" + index);
        userDao.createUser(user);
        user = userDao.getUser("user" + index);
        return user;
    }
    
    // Will essentially clean the db
    protected void removeAllUsers(UserDao userDao)
    {
        Set users = userDao.getUsers();
        for(Iterator it = users.iterator();it.hasNext();)
        {
            User user = (User) it.next();
            userDao.removeUser(user.getUsername());
        }
    }
    
    protected void verifyItemInCollection(Collection items, Item item) throws Exception
    {
        for(Iterator it = items.iterator();it.hasNext();)
        {
            Item nextItem = (Item)  it.next();
            if(nextItem.getUid().equals(item.getUid()))
                return;
        }
        Assert.fail("Item not in collection");
    }
    
    protected void verifyItem(Item item1, Item item2) throws Exception
    {
        Assert.assertEquals(item1.getName(),item2.getName());
        Assert.assertEquals(item1.getId(), item2.getId());
        Assert.assertEquals(item1.getUid(), item2.getUid());
        Assert.assertEquals(item1.getAttributes().size(), item2.getAttributes().size());
        for(Iterator it = item1.getAttributes().keySet().iterator();it.hasNext();)
        {
            String key = (String) it.next();
            Object val1 = item1.getAttributeValue(key);
            Object val2 = item2.getAttributeValue(key);
            verifyAttributeValue(val1,val2);
        }
        
    }
    
    protected void verifyMap(Map val1, Map val2)
    {
        Assert.assertEquals(val1.size(), val2.size());
        for(Iterator keys = val1.keySet().iterator();keys.hasNext();)
        {
            String key = (String) keys.next();
            Assert.assertEquals(val1.get(key), val2.get(key));
        }
    }
    
    protected void verifySet(Set val1, Set val2)
    {
        Assert.assertEquals(val1.size(), val2.size());
        for(Iterator elems = val1.iterator();elems.hasNext();)
            Assert.assertTrue(val2.contains(elems.next()));
    }
    
    protected void verifyDate(Date val1, Date val2)
    {
        Assert.assertEquals(val1.getTime(), val2.getTime());
    }
    
    protected void verifyAttributeValue(Object val1, Object val2) throws Exception
    {
        if(val1 instanceof String)
            Assert.assertEquals(val1, val2);
        else if(val1 instanceof byte[])
            verifyBytes((byte[]) val1, (byte[]) val2);
        else if(val1 instanceof Set)
            verifySet((Set)val1, (Set)val2);
        else if(val1 instanceof Map)
            verifyMap((Map) val1, (Map) val2);
        else if(val1 instanceof Date)
            verifyDate((Date) val1, (Date) val2);
        else if(!val1.equals(val2))
            Assert.fail("attributes not equal");
    }
	
}
