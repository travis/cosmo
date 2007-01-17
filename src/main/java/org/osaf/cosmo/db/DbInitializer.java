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
package org.osaf.cosmo.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.ServerProperty;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.ServerPropertyService;
import org.osaf.cosmo.service.UserService;
import org.springframework.orm.hibernate3.HibernateJdbcException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * A helper class that initializes the Cosmo database schema and populates the
 * database with seed data.
 */
public class DbInitializer {
    private static final Log log = LogFactory.getLog(DbInitializer.class);

    private UserService userService;

    private ServerPropertyService serverPropertyService;

    private LocalSessionFactoryBean localSessionFactory;

    private String loginUrlKey;

    private String rootLoginUrl;

    /**
     * Performs initialization tasks if required.
     * 
     * @return <code>true</code> if initialization was required, *
     *         <code>false</code> otherwise>.
     */
    public boolean initialize() {

        // Create DB schema if not present
        if (!isSchemaInitialized()) {
            log.info("Creating database");
            localSessionFactory.createDatabaseSchema();
            addServerProperties();
        }

        // Verify that db schema is supported by server
        // TODO: here is where we will eventually put auto-update
        // scripts. For now, the server will not start if an
        // unsupported version is found.
        checkSchemaVersion();

        // Once schema is present, check that db is initialized
        if (isAlreadyInitialized())
            return false;

        log.info("Initializing database");
        addOverlord();

        return true;
    }

    /** */
    public UserService getUserService() {
        return userService;
    }

    /** */
    public void setUserService(UserService dao) {
        userService = dao;
    }

    public void setLocalSessionFactory(
            LocalSessionFactoryBean localSessionFactory) {
        this.localSessionFactory = localSessionFactory;
    }

    public void setServerPropertyService(
            ServerPropertyService serverPropertyService) {
        this.serverPropertyService = serverPropertyService;
    }

    private boolean isSchemaInitialized() {
        try {
            userService.getUser(User.USERNAME_OVERLORD);
            return true;
        } catch (HibernateJdbcException e) {
            return false;
        }
    }

    private void checkSchemaVersion() {
        String schemaVersion = serverPropertyService
                .getServerProperty(ServerProperty.PROP_SCHEMA_VERSION);

        log.info("found schema version " + schemaVersion);
        if (!CosmoConstants.PRODUCT_VERSION.equals(schemaVersion)) {
            log.error("Schema version does not match (" + schemaVersion + ":"
                    + CosmoConstants.PRODUCT_VERSION);
            throw new RuntimeException(
                    "Schema version does not match server version");
        }
    }

    private boolean isAlreadyInitialized() {
        return userService.getUser(User.USERNAME_OVERLORD) != null;
    }

    private void addOverlord() {
        if (log.isDebugEnabled()) {
            log.debug("adding overlord");
        }

        User overlord = new User();
        overlord.setUsername(User.USERNAME_OVERLORD);
        overlord.setFirstName("Cosmo");
        overlord.setLastName("Administrator");
        overlord.setPassword("cosmo");
        overlord.setEmail("root@localhost");
        overlord.setAdmin(Boolean.TRUE);
        
        overlord.setPreference(loginUrlKey, rootLoginUrl);

        userService.createUser(overlord);
    }

    private void addServerProperties() {
        serverPropertyService.setServerProperty(
                ServerProperty.PROP_SCHEMA_VERSION,
                CosmoConstants.PRODUCT_VERSION);
    }

    public void setLoginUrlKey(String loginUrlKey) {
        this.loginUrlKey = loginUrlKey;
    }

    public String getLoginUrlKey() {
        return loginUrlKey;
    }

    public void setRootLoginUrl(String rootLoginUrl) {
        this.rootLoginUrl = rootLoginUrl;
    }

    public String getRootLoginUrl() {
        return rootLoginUrl;
    }
}
