/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.query.CalendarQueryProcessor;
import org.osaf.cosmo.calendar.query.impl.StandardCalendarQueryProcessor;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockTicketPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardTriageStatusQueryProcessor;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.service.lock.SingleVMLockManager;

/**
 */
public class MockHelper extends TestHelper {
    private static final Log log = LogFactory.getLog(MockHelper.class);

    private MockEntityFactory entityFactory;
    private MockSecurityManager securityManager;
    private ServiceLocatorFactory serviceLocatorFactory;
    private StandardContentService contentService;
    private StandardUserService userService;
    private StandardCalendarQueryProcessor calendarQueryProcessor;
    private User user;
    private HomeCollectionItem homeCollection;

    public MockHelper() {
        super();

        securityManager = new MockSecurityManager();

        serviceLocatorFactory = new ServiceLocatorFactory();
        serviceLocatorFactory.setAtomPrefix("/atom");
        serviceLocatorFactory.setCmpPrefix("/cmp");
        serviceLocatorFactory.setDavPrefix("/dav");
        serviceLocatorFactory.setMorseCodePrefix("/mc");
        serviceLocatorFactory.setPimPrefix("/pim");
        serviceLocatorFactory.setWebcalPrefix("/webcal");
        serviceLocatorFactory.setSecurityManager(securityManager);
        serviceLocatorFactory.init();

        MockDaoStorage storage = new MockDaoStorage();
        MockCalendarDao calendarDao = new MockCalendarDao(storage);
        MockContentDao contentDao = new MockContentDao(storage);
        MockUserDao userDao = new MockUserDao();
        SingleVMLockManager lockManager = new SingleVMLockManager();
        
        entityFactory = new MockEntityFactory();
        
        contentService = new StandardContentService();
        contentService.setCalendarDao(calendarDao);
        contentService.setContentDao(contentDao);
        contentService.setLockManager(lockManager);
        contentService.setTriageStatusQueryProcessor(new StandardTriageStatusQueryProcessor());
       
        contentService.init();

        calendarQueryProcessor = new StandardCalendarQueryProcessor();
        calendarQueryProcessor.setCalendarDao(calendarDao);
        
        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.init();

        user = userService.getUser("test");
        if (user == null) {
            user = makeDummyUser("test", "password");
            userService.createUser(user);
        }
        homeCollection = contentService.getRootItem(user);
    }

    public void setUp() throws Exception {}

    public void tearDown() throws Exception {}

    public void logIn() {
        logInUser(user);
    }

    public void logInUser(User u) {
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(u));
    }

    public void logInTicket(Ticket t) {
        securityManager.setUpMockSecurityContext(new MockTicketPrincipal(t));
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    public ContentService getContentService() {
        return contentService;
    }
    
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }
    
    public CalendarQueryProcessor getCalendarQueryProcessor() {
        return calendarQueryProcessor;
    }

    public UserService getUserService() {
        return userService;
    }

    public User getUser() {
        return user;
    }

    public HomeCollectionItem getHomeCollection() {
        return homeCollection;
    }

    public CollectionItem makeAndStoreDummyCollection()
        throws Exception {
        return makeAndStoreDummyCollection(homeCollection);
    }

    public CollectionItem makeAndStoreDummyCollection(CollectionItem parent)
        throws Exception {
        CollectionItem c = makeDummyCollection(user);
        return contentService.createCollection(parent, c);
    }
    
    public CollectionItem makeAndStoreDummyCalendarCollection()
        throws Exception {
        return makeAndStoreDummyCalendarCollection(null);
    }

    public CollectionItem makeAndStoreDummyCalendarCollection(String name)
            throws Exception {
        CollectionItem c = makeDummyCalendarCollection(user, name);
        return contentService.createCollection(homeCollection, c);
    }

    public void lockCollection(CollectionItem collection) {
        contentService.getLockManager().lockCollection(collection);
    }

    public ContentItem makeAndStoreDummyContent()
        throws Exception {
        return makeAndStoreDummyContent(homeCollection);
    }

    public ContentItem makeAndStoreDummyContent(CollectionItem parent)
        throws Exception {
        ContentItem c = makeDummyContent(user);
        return contentService.createContent(parent, c);
    }

    public NoteItem makeAndStoreDummyItem()
        throws Exception {
        return makeAndStoreDummyItem(homeCollection);
    }

    public NoteItem makeAndStoreDummyItem(CollectionItem parent)
        throws Exception {
        return makeAndStoreDummyItem(parent, null);
    }

    public NoteItem makeAndStoreDummyItem(CollectionItem parent,
                                          String name)
        throws Exception {
        NoteItem i = makeDummyItem(user, name);
        return (NoteItem) contentService.createContent(parent, i);
    }

    public Ticket makeAndStoreDummyTicket(CollectionItem collection)
        throws Exception {
        Ticket ticket = makeDummyTicket(user);
        contentService.createTicket(collection, ticket);
        return ticket;
    }

    public CollectionSubscription makeAndStoreDummySubscription()
        throws Exception {
        CollectionItem collection = makeAndStoreDummyCollection();
        Ticket ticket = makeAndStoreDummyTicket(collection);
        return makeAndStoreDummySubscription(collection, ticket);
    }

    public CollectionSubscription
        makeAndStoreDummySubscription(CollectionItem collection,
                                      Ticket ticket)
        throws Exception {
        CollectionSubscription sub = makeDummySubscription(collection, ticket);
        user.addSubscription(sub);
        userService.updateUser(user);
        return sub;
    }

    public Preference makeAndStoreDummyPreference()
        throws Exception {
        Preference pref = makeDummyPreference();
        user.addPreference(pref);
        userService.updateUser(user);
        return pref;
    }

    public NoteItem findItem(String uid) {
        return (NoteItem) contentService.findItemByUid(uid);
    }

    public CollectionItem findCollection(String uid) {
        return (CollectionItem) contentService.findItemByUid(uid);
    }

    public CollectionSubscription findSubscription(String displayName) {
        return user.getSubscription(displayName);
    }
}
