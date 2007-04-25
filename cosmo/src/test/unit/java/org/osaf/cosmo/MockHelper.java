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

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockTicketPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.service.lock.SingleVMLockManager;

/**
 */
public class MockHelper extends TestHelper {
    private static final Log log = LogFactory.getLog(MockHelper.class);

    private MockSecurityManager securityManager;
    private ServiceLocatorFactory serviceLocatorFactory;
    private StandardContentService contentService;
    private StandardUserService userService;
    private User user;
    private HomeCollectionItem homeCollection;

    public MockHelper() {
        super();

        securityManager = new MockSecurityManager();

        serviceLocatorFactory = new ServiceLocatorFactory();
        serviceLocatorFactory.setAtomPrefix("/atom");
        serviceLocatorFactory.setCmpPrefix("/cmp");
        serviceLocatorFactory.setDavPrefix("/dav");
        serviceLocatorFactory.setDavPrincipalPrefix("/dav");
        serviceLocatorFactory.setDavCalendarHomePrefix("/dav");
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

        contentService = new StandardContentService();
        contentService.setCalendarDao(calendarDao);
        contentService.setContentDao(contentDao);
        contentService.setLockManager(lockManager);
        contentService.init();

        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.init();
    }

    public void setUp() throws Exception {
        user = makeDummyUser();
        userService.createUser(user);
        homeCollection = contentService.getRootItem(user);
    }

    public void tearDown() throws Exception {
        userService.removeUser(user);
        userService.destroy();
        contentService.destroy();
    }

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
        NoteItem i = makeDummyItem(user);
        return (NoteItem) contentService.createContent(parent, i);
    }

    public NoteItem findItem(String uid) {
        return (NoteItem) contentService.findItemByUid(uid);
    }
}
