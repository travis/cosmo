/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.event.aop;

import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockEventLogDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ItemChangeRecord;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.ItemChangeRecord.Action;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.security.mock.MockSecurityContext;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockTicketPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardTriageStatusQueryProcessor;
import org.osaf.cosmo.service.lock.SingleVMLockManager;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

/**
 * Test Case for <code>EventLogAdvice/code>
 */
public class EventLogAdviceTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(EventLogAdviceTest.class);
    
    private StandardContentService service;
    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockEventLogDao eventLogDao;
    private MockDaoStorage storage;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    private ContentService proxyService;
    private MockSecurityManager securityManager;
    
   
    /** */
    protected void setUp() throws Exception {
        testHelper = new TestHelper();
        securityManager = new MockSecurityManager();
        storage = new MockDaoStorage();
        calendarDao = new MockCalendarDao(storage);
        contentDao = new MockContentDao(storage);
        eventLogDao = new MockEventLogDao();
        service = new StandardContentService();
        lockManager = new SingleVMLockManager();
        service.setCalendarDao(calendarDao);
        service.setContentDao(contentDao);
        service.setLockManager(lockManager);
        service.setTriageStatusQueryProcessor(new StandardTriageStatusQueryProcessor());
        service.init();
        
        // create a factory that can generate a proxy for the given target object
        AspectJProxyFactory factory = new AspectJProxyFactory(service); 

        // add aspect
        EventLogAdvice eva = new EventLogAdvice();
        eva.setEnabled(true);
        eva.setSecurityManager(securityManager);
        eva.setEventLogDao(eventLogDao);
        eva.init();
        factory.addAspect(eva);

        // now get the proxy object...
        proxyService = factory.getProxy();
    }

    /** */
    public void testEventLogAspectWithUser() throws Exception {
        
        Date startDate = new Date();
        
        User user1 = testHelper.makeDummyUser("user1", "password");
        User user2 = testHelper.makeDummyUser("user2", "password");
        CollectionItem rootCollection = contentDao.createRootItem(user1);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(rootCollection, dummyContent);
        
        // login as user1
        initiateContext(user1);
        
        // update content
        proxyService.updateContent(dummyContent);
        
        Date endDate = new Date();
        
        
        // query ItemChangeRecords
        List<ItemChangeRecord> records = eventLogDao.findChangesForCollection(rootCollection, startDate, endDate);
        Assert.assertEquals(1, records.size());
        
        ItemChangeRecord record = records.get(0);
        Assert.assertEquals(dummyContent.getUid(), record.getItemUuid());
        Assert.assertEquals(dummyContent.getDisplayName(), record.getItemDisplayName());
        Assert.assertEquals(user1.getEmail(), record.getModifiedBy());
        Assert.assertEquals(Action.ITEM_CHANGED, record.getAction());
    }
    
    /** */
    public void testSecuredApiWithTicket() throws Exception {
        Date startDate = new Date();
        
        
        User user1 = testHelper.makeDummyUser("user1", "password");
        User user2 = testHelper.makeDummyUser("user2", "password");
        CollectionItem rootCollection = contentDao.createRootItem(user1);
        CollectionItem collection = testHelper.makeDummyCollection(user1);
        collection.setUid("col");
        
        // create RO and RW tickets on collection
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("T2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        collection.getTickets().add(rwTicket);
        
        collection = contentDao.createCollection(rootCollection, collection);
        
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(collection, dummyContent);
        
        
        // login as RW ticket
        initiateContext(rwTicket);
        
        // update content
        proxyService.updateContent(dummyContent);
        
        Date endDate = new Date();
        
        
        // query ItemChangeRecords
        List<ItemChangeRecord> records = eventLogDao.findChangesForCollection(collection, startDate, endDate);
        Assert.assertEquals(1, records.size());
        
        ItemChangeRecord record = records.get(0);
        Assert.assertEquals(dummyContent.getUid(), record.getItemUuid());
        Assert.assertEquals(dummyContent.getDisplayName(), record.getItemDisplayName());
        Assert.assertEquals("ticket: anonymous", record.getModifiedBy());
        Assert.assertEquals(Action.ITEM_CHANGED, record.getAction());
       
    }
    
    private void initiateContext(User user) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockUserPrincipal(user)));
    }
    
    private void initiateContextWithTickets(User user, Set<Ticket> tickets) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockUserPrincipal(user), tickets));
    }
    
    private void initiateContext(Ticket ticket) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockTicketPrincipal(ticket)));
    } 
}
