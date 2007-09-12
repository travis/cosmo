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

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavResourceIterator;

import org.osaf.cosmo.MockHelper;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.dav.acl.resource.DavUserPrincipal;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.StandardResourceFactory;
import org.osaf.cosmo.dav.impl.DavHomeCollection;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.PathUtil;

public class DavTestHelper extends MockHelper
    implements ExtendedDavConstants {
    private static final Log log = LogFactory.getLog(DavTestHelper.class);

    private StandardResourceFactory resourceFactory;
    private StandardResourceLocatorFactory locatorFactory;
    private User user;
    private DavResourceLocator homeLocator;
    private DavHomeCollection homeResource;

    public DavTestHelper() {
        super();

        resourceFactory =
            new StandardResourceFactory(getContentService(),
                                        getUserService(),
                                        getSecurityManager());
        locatorFactory =
            new StandardResourceLocatorFactory(getServiceLocatorFactory());
    }

    public void setUp() throws Exception {
        super.setUp();
        homeLocator =
            locatorFactory.createResourceLocator("", "/" + getUser().getUsername());
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
                                     resourceFactory);
    }

    public DavResource getMember(DavCollection parent,
                                 String name)
        throws Exception {
        for (DavResourceIterator i = parent.getMembers(); i.hasNext();) {
            org.apache.jackrabbit.webdav.DavResource m = i.nextResource();
            log.debug("member path: " + m.getResourcePath());
            if (PathUtil.getBasename(m.getResourcePath()).equals(name))
                return (DavResource) m;
        }
        return null;
    }

    public DavUserPrincipal getPrincipal(User user)
        throws Exception {
        DavResourceLocator locator = locatorFactory.
            createResourceLocator("", TEMPLATE_USER.bind(user.getUsername()));
        return new DavUserPrincipal(user, locator, resourceFactory);
    }
}
