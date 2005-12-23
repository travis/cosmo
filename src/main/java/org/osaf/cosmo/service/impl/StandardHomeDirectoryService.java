/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.service.impl;

import org.osaf.cosmo.dao.HomeDirectoryDao;
import org.osaf.cosmo.service.HomeDirectoryService;
import org.osaf.cosmo.model.DavResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Standard implementation of {@link HomeDirectoryService}.
 */
public class StandardHomeDirectoryService implements HomeDirectoryService {
    private static final Log log =
        LogFactory.getLog(StandardHomeDirectoryService.class);

    private HomeDirectoryDao homeDirectoryDao;

    // HomeDirectoryService methods

    /**
     * Returns the resource at the specified path within the
     * repository.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */
    public DavResource getResource(String path) {
        return homeDirectoryDao.getResource(path);
    }

    // Service methods

    /**
     * Initializes the service, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        // does nothing
    }

    /**
     * Readies the service for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /**
     */
    public HomeDirectoryDao getHomeDirectoryDao() {
        return this.homeDirectoryDao;
    }

    /**
     */
    public void setHomeDirectoryDao(HomeDirectoryDao homeDirectoryDao) {
        this.homeDirectoryDao = homeDirectoryDao;
    }
}
