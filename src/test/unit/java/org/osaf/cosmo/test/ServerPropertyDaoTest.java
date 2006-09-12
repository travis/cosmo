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

import junit.framework.Assert;

import org.osaf.cosmo.dao.hibernate.ServerPropertyDaoImpl;

/**
 * Test UserDao funtionality
 */
public class ServerPropertyDaoTest extends HibernateDaoTestCase {
	ServerPropertyDaoImpl serverPropertyDao = null;
	
	public ServerPropertyDaoTest()
	{
        serverPropertyDao = new ServerPropertyDaoImpl();
        serverPropertyDao.setSessionFactory(sessionFactory);
        serverPropertyDao.init();
	}
    
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
	
	public void testServerProperties() throws Exception {
	    serverPropertyDao.setServerProperty("testprop1", "testvalue1");
        clearSession();
        String propValue = serverPropertyDao.getServerProperty("testprop1");
        Assert.assertEquals("testvalue1", propValue);
        clearSession();
        serverPropertyDao.setServerProperty("testprop1", "testvalue2");
        clearSession();
        propValue = serverPropertyDao.getServerProperty("testprop1");
        Assert.assertEquals("testvalue2", propValue);
    }
}
