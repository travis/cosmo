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
package org.osaf.cosmo.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.ServerPropertyDao;
import org.osaf.cosmo.service.ServerPropertyService;

/**
 * Standard implementation of {@link ServerPropertyService}.
 */
public class StandardServerPropertyService implements ServerPropertyService {
    private static final Log log = LogFactory
            .getLog(StandardServerPropertyService.class);

    private ServerPropertyDao serverPropertyDao = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.service.ServerPropertyService#getServerProperty(java.lang.String)
     */
    public String getServerProperty(String property) {
        return serverPropertyDao.getServerProperty(property);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.service.ServerPropertyService#setServerProperty(java.lang.String,
     *      java.lang.String)
     */
    public void setServerProperty(String property, String value) {
        serverPropertyDao.setServerProperty(property, value);
    }

    /**
     * Initializes the service, sanity checking required properties and
     * defaulting optional properties.
     */
    public void init() {
        if (serverPropertyDao == null) {
            throw new IllegalStateException("serverPropertyDao is required");
        }
    }

    /**
     * Readies the service for garbage collection, shutting down any resources
     * used.
     */
    public void destroy() {
        // does nothing
    }

    public ServerPropertyDao getServerPropertyDao() {
        return serverPropertyDao;
    }

    public void setServerPropertyDao(ServerPropertyDao serverPropertyDao) {
        this.serverPropertyDao = serverPropertyDao;
    }
}
