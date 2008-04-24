/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.MockHelper;
import org.osaf.cosmo.dav.acl.resource.DavUserPrincipal;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavEvent;
import org.osaf.cosmo.dav.impl.DavHomeCollection;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.UriTemplate;

public class DavTestHelper extends MockHelper
    implements ExtendedDavConstants {
    private static final Log log = LogFactory.getLog(DavTestHelper.class);

    private StandardResourceFactory resourceFactory;
    private StandardResourceLocatorFactory locatorFactory;
    private DavResourceLocator homeLocator;

    private URL baseUrl;

    public DavTestHelper() {
        super();

        resourceFactory =
            new StandardResourceFactory(getContentService(),
                                        getUserService(),
                                        getSecurityManager(),
                                        getEntityFactory(),
                                        getCalendarQueryProcessor(),
                                        getClientFilterManager());
        locatorFactory = new StandardResourceLocatorFactory();
        try {
            baseUrl = new URL("http", "localhost", -1, "/dav");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setUp() throws Exception {
        super.setUp();

        String path = TEMPLATE_HOME.bind(false, getUser().getUsername());
        homeLocator =
            locatorFactory.createResourceLocatorByPath(baseUrl, path);
    }

    
    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public DavResourceLocatorFactory getResourceLocatorFactory() {
        return locatorFactory;
    }

    public DavResourceLocator getHomeLocator() {
        return homeLocator;
    }

    public DavHomeCollection initializeHomeResource()
        throws DavException {
        return new DavHomeCollection(getHomeCollection(), homeLocator,
                                     resourceFactory, getEntityFactory());
    }

    public DavUserPrincipal getPrincipal(User user)
        throws Exception {
        String path = TEMPLATE_USER.bind(false, user.getUsername());
        DavResourceLocator locator =
            locatorFactory.createResourceLocatorByPath(baseUrl, path);
        return new DavUserPrincipal(user, locator, resourceFactory);
    }

    public DavTestContext createTestContext() {
        return new DavTestContext(locatorFactory);
    }

    public DavResourceLocator createLocator(String path) {
        return locatorFactory.
            createResourceLocatorByPath(homeLocator.getContext(), path);
    }

    public DavResourceLocator createMemberLocator(DavResourceLocator locator,
                                                  String segment) {
        String path = locator.getPath() + "/" + segment;
        return locatorFactory.
            createResourceLocatorByPath(locator.getContext(), path);
    }

    public DavResource findMember(DavCollection collection,
                                  String segment)
        throws DavException {
        String href = collection.getResourceLocator().getHref(false) + "/" +
            UriTemplate.escapeSegment(segment);
        return collection.findMember(href);
    }

    public DavCalendarCollection initializeDavCalendarCollection(String name)
        throws Exception {
        CollectionItem collection = (CollectionItem)
            getHomeCollection().getChildByName(name);
        if (collection == null)
            collection = makeAndStoreDummyCalendarCollection(name);
        DavResourceLocator locator =
            createMemberLocator(homeLocator, collection.getName());
        return new DavCalendarCollection(collection, locator,
                                         resourceFactory, getEntityFactory());
    }

    public DavEvent initializeDavEvent(DavCalendarCollection parent,
                                       String name)
        throws Exception {
        CollectionItem collection = (CollectionItem) parent.getItem();
        NoteItem item = (NoteItem) collection.getChildByName(name);
        if (item == null)
            item = makeAndStoreDummyItem(collection, name);
        DavResourceLocator locator =
            createMemberLocator(parent.getResourceLocator(), item.getName());
        return new DavEvent(item, locator, resourceFactory, getEntityFactory());
    }
}
