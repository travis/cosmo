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
package org.osaf.cosmo.ui.config;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

/**
 * Gathers information from Spring configuration and the user database
 * and sets servlet context attributes (see individual methods for
 * details).
 */
public class ServletContextConfigurer {
    private static final Log log =
        LogFactory.getLog(ServletContextConfigurer.class);

    private ServletContext servletContext;
    private String serverVersion;
    private UserDAO userDao;

    /**
     * An entry point for configuration of the servlet context.
     * Calls the following methods:
     *
     * <ul>
     * <li>{@link #setServerVersion}</li>
     * <li>{@link #setServerAdmin}</li>
     * </ul>
     */
    public void configure(ServletContext sc) {
        this.servletContext = sc;
        setServerVersion();
        setServerAdmin();
    }

    /**
     * Sets the {@link CosmoConstants#SC_ATTR_SERVER_VERSION} servlet context
     * attribute.
     */
    public void setServerVersion() {
        servletContext.setAttribute(CosmoConstants.SC_ATTR_SERVER_VERSION,
                                    serverVersion);
    }

    /**
     * Sets the {@link CosmoConstants#SC_ATTR_SERVER_ADMIN} servlet context
     * attribute by looking up the root user's email address.
     */
    public void setServerAdmin() {
        User rootUser =
            userDao.getUserByUsername(CosmoSecurityManager.USER_ROOT);
        servletContext.setAttribute(CosmoConstants.SC_ATTR_SERVER_ADMIN,
                                    rootUser.getEmail());
    }

    /**
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     */
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     */
    public UserDAO getUserDao() {
        return userDao;
    }

    /**
     */
    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }
}
