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
package org.osaf.cosmo.dav;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.dav.impl.DavCollection;
import org.osaf.cosmo.dav.impl.DavHomeCollection;
import org.osaf.cosmo.dav.impl.NoOpDavSession;
import org.osaf.cosmo.dav.impl.NoOpDavSessionProvider;
import org.osaf.cosmo.dav.impl.StandardDavResourceFactory;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.security.mock.MockTicketPrincipal;
import org.osaf.cosmo.security.mock.MockUserPrincipal;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.account.AutomaticAccountActivator;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.impl.StandardUserService;
import org.osaf.cosmo.util.PathUtil;

/**
 */
public class DavTestHelper extends TestHelper {
    private static final Log log = LogFactory.getLog(DavTestHelper.class);

    private MockSecurityManager securityManager;
    private StandardContentService contentService;
    private StandardUserService userService;
    private StandardDavResourceFactory resourceFactory;
    private LocatorFactoryImpl locatorFactory;
    private NoOpDavSessionProvider sessionProvider;
    private NoOpDavSession session;
    private User user;
    private HomeCollectionItem homeCollection;
    private DavResourceLocator homeLocator;
    private DavHomeCollection homeResource;

    /** */
    public DavTestHelper() {
        this("");
    }

    /** */
    public DavTestHelper(String repositoryPrefix) {
        super();

        securityManager = new MockSecurityManager();

        MockDaoStorage storage = new MockDaoStorage();
        MockCalendarDao calendarDao = new MockCalendarDao(storage);
        MockContentDao contentDao = new MockContentDao(storage);
        MockUserDao userDao = new MockUserDao();

        contentService = new StandardContentService();
        contentService.setCalendarDao(calendarDao);
        contentService.setContentDao(contentDao);
        contentService.init();

        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        userService.setPasswordGenerator(new SessionIdGenerator());
        userService.init();

        resourceFactory = new StandardDavResourceFactory();
        resourceFactory.setContentService(contentService);
        resourceFactory.setSecurityManager(securityManager);

        locatorFactory = new LocatorFactoryImpl(repositoryPrefix);

        sessionProvider = new NoOpDavSessionProvider();
        session = new NoOpDavSession();
    }

    /** */
    public void setUp() throws Exception {
        user = makeDummyUser();
        userService.createUser(user);

        homeCollection = contentService.getRootItem(user);
        homeLocator =
            locatorFactory.createResourceLocator("", "/" + user.getUsername());
    }

    /** */
    protected void tearDown() throws Exception {
        userService.removeUser(user);

        userService.destroy();
        contentService.destroy();
    }

    /**
     */
    public void logIn() {
        logInUser(user);
    }

    /**
     */
    public void logInUser(User u) {
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(u));
    }

    /**
     */
    public void logInTicket(Ticket t) {
        securityManager.setUpMockSecurityContext(new MockTicketPrincipal(t));
    }

    /** */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /** */
    public ContentService getContentService() {
        return contentService;
    }

    /** */
    public UserService getUserService() {
        return userService;
    }

    /** */
    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    /** */
    public DavLocatorFactory getLocatorFactory() {
        return locatorFactory;
    }

    /** */
    public DavSessionProvider getSessionProvider() {
        return sessionProvider;
    }

    /** */
    public DavSession getSession() {
        return session;
    }

    /** */
    public User getUser() {
        return user;
    }

    /** */
    public HomeCollectionItem getHomeCollection() {
        return homeCollection;
    }

    /** */
    public DavResourceLocator getHomeLocator() {
        return homeLocator;
    }

    /** */
    public DavHomeCollection initializeHomeResource() {
        return new DavHomeCollection(homeCollection, homeLocator,
                                     resourceFactory, session);
    }

    /** */
    public CollectionItem makeAndStoreDummyCollection(CollectionItem parent)
        throws Exception {
        CollectionItem c = makeDummyCollection(user);
        return contentService.createCollection(parent, c);
    }

    /** */
    public DavResource getMember(DavCollection parent,
                                 String name)
        throws Exception {
        for (DavResourceIterator i = parent.getMembers(); i.hasNext();) {
            DavResource m = i.nextResource();
            if (PathUtil.getBasename(m.getResourcePath()).equals(name))
                return m;
        }
        return null;
    }
}
