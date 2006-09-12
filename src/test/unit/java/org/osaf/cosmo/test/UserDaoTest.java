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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.osaf.cosmo.dao.hibernate.UserDaoImpl;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PageCriteria;
import org.osaf.cosmo.util.PagedList;

/**
 * Test UserDao funtionality
 */
public class UserDaoTest extends HibernateDaoTestCase {
	UserDaoImpl userDao = null;
	
	public UserDaoTest()
	{
		userDao = new UserDaoImpl();
        userDao.setSessionFactory(sessionFactory);
        userDao.init();
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
	
	public void testCreateUser()
	{
		User user1 = new User();
		user1.setUsername("user1");
		user1.setFirstName("User");
		user1.setLastName("1");
		user1.setEmail("user1@user1.com");
		user1.setPassword("user1password");
		user1.setAdmin(Boolean.TRUE);
		
		userDao.createUser(user1);
		
		User user2 = new User();
		user2.setUsername("user2");
		user2.setFirstName("User2");
		user2.setLastName("2");
		user2.setEmail("user2@user2.com");
		user2.setPassword("user2password");
		user2.setAdmin(Boolean.FALSE);
		
		userDao.createUser(user2);
		
        clearSession();
        
		// get users
		User queryUser1 = userDao.getUser("user1");
		Assert.assertNotNull(queryUser1);
		verifyUser(user1, queryUser1);
		
		Set users = userDao.getUsers();
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());
		verifyUserInSet(user1,users);
		verifyUserInSet(user2,users);
		
        clearSession();
        
		// try to create duplicate
		User user3 = new User();
		user3.setUsername("user2");
		user3.setFirstName("User");
		user3.setLastName("1");
		user3.setEmail("user1@user1.com");
		user3.setPassword("user1password");
		user3.setAdmin(Boolean.TRUE);
		
		try{
			userDao.createUser(user3);
			Assert.fail("able to create user with duplicate username");
		}
		catch(DuplicateUsernameException due) {}
		
		user3.setUsername("user3");
		try{
			userDao.createUser(user3);
			Assert.fail("able to create user with duplicate email");
		}
		catch(DuplicateEmailException dee) {}
        
        
        // delete user
        userDao.removeUser("user2");
	}
    
    public void testPaginatedUsers() throws Exception
    {
        User user1 = createDummyUser(userDao, 1);
        User user2 = createDummyUser(userDao, 2);
        User user3 = createDummyUser(userDao, 3);
        User user4 = createDummyUser(userDao, 4); 
        
        clearSession();
        
        PageCriteria pageCriteria = new PageCriteria();

        pageCriteria.setPageNumber(1);
        pageCriteria.setPageSize(2);
        pageCriteria.setSortAscending(true);
        pageCriteria.setSortTypeString(User.NAME_SORT_STRING);
        
        PagedList pagedList = userDao.getUsers(pageCriteria);
        List results = pagedList.getList();
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(4, pagedList.getTotal());
        Assert.assertTrue(results.contains(user1));
        Assert.assertTrue(results.contains(user2));
        
        clearSession();
        
        pageCriteria.setPageNumber(2);
        pagedList = userDao.getUsers(pageCriteria);
        results = pagedList.getList();
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(4, pagedList.getTotal());
        Assert.assertTrue(results.contains(user3));
        Assert.assertTrue(results.contains(user4));
        
        pageCriteria.setSortAscending(false);
        pageCriteria.setSortTypeString(User.NAME_SORT_STRING);
        pageCriteria.setPageNumber(1);
        
        pagedList = userDao.getUsers(pageCriteria);
        results = pagedList.getList();
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(4, pagedList.getTotal());
        Assert.assertTrue(results.contains(user3));
        Assert.assertTrue(results.contains(user4)); 
    }
    
    public void testDeleteUser() throws Exception
    {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("1");
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");
        user1.setAdmin(Boolean.TRUE);
        
        userDao.createUser(user1);
        User queryUser1 = userDao.getUser("user1");
        Assert.assertNotNull(queryUser1);
        userDao.removeUser(user1.getUsername());
        queryUser1 = userDao.getUser("user1");
        Assert.assertNull(queryUser1);
    }
	
	private void verifyUser(User user1, User user2)
	{
		Assert.assertEquals(user1.getUsername(), user2.getUsername());
		Assert.assertEquals(user1.getAdmin(), user2.getAdmin());
		Assert.assertEquals(user1.getEmail(), user2.getEmail());
		Assert.assertEquals(user1.getFirstName(), user2.getFirstName());
		Assert.assertEquals(user1.getLastName(), user2.getLastName());
		Assert.assertEquals(user1.getPassword(), user2.getPassword());
	}
	
	private void verifyUserInSet(User user, Set users)
	{
		Iterator it = users.iterator();
		while(it.hasNext())
		{
			User nextUser = (User) it.next();
			if(nextUser.getUsername().equals(user.getUsername()))
			{
				verifyUser(user,nextUser);
				return;
			}
		}
		Assert.fail("specified User doesn't exist in Set: " + user.getUsername());
	}
}
