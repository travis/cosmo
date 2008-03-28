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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.hibernate.SimpleConnectionProvider;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.ServerProperty;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.ServerPropertyService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.ui.UIConstants;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * A helper class that initializes the Cosmo database schema and populates the
 * database with seed data.
 */
public class DbInitializer {
    private static final Log log = LogFactory.getLog(DbInitializer.class);

    private UserService userService;

    private ServerPropertyService serverPropertyService;
    private EntityFactory entityFactory;

    private LocalSessionFactoryBean localSessionFactory;

    private String rootLoginUrl;
    
    private DataSource datasource;
    
    private boolean validateSchema = true;

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
            log.info("Initializing database");
            addOverlord();
            return true;
        } else {
            // Verify that db schema is supported by server
            // TODO: here is where we will eventually put auto-update
            // scripts. For now, the server will not start if an
            // unsupported version is found.
            checkSchemaVersion();
            
            // More thorough schema validation
            if(validateSchema==true)
                validateSchema();
            
            return false;
        }
    }

    /** */
    public UserService getUserService() {
        return userService;
    }

    /** */
    public void setUserService(UserService dao) {
        userService = dao;
    }
    
    public void setDataSource(DataSource datasource) {
        this.datasource = datasource;
    }
    
    public void setValidateSchema(boolean validateSchema) {
        this.validateSchema = validateSchema;
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
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = datasource.getConnection();
            ps = conn.prepareStatement("select count(*) from server_properties");
            ps.executeQuery();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    private void checkSchemaVersion() {
        String schemaVersion = serverPropertyService
                .getServerProperty(ServerProperty.PROP_SCHEMA_VERSION);

        log.info("found schema version " + schemaVersion);
        if (!CosmoConstants.SCHEMA_VERSION.equals(schemaVersion)) {
            log.error("Schema version does not match (" + schemaVersion + ":"
                    + CosmoConstants.SCHEMA_VERSION);
            throw new RuntimeException(
                    "Schema version found in database does not match schema version required by server");
        }
    }
    
    private void validateSchema() {
        
        try {
            // Workaround for Spring 2.0.1 LocalSessionFactoryBean not  having
            // access to DataSource.  Should be fixed in Spring 2.0.2.
            SimpleConnectionProvider.setConnection(datasource.getConnection());
            Configuration config = localSessionFactory.getConfiguration();
            config.setProperty(Environment.CONNECTION_PROVIDER, "org.osaf.cosmo.hibernate.SimpleConnectionProvider");
            log.info("validating schema");
            new SchemaValidator(config).validate();
            log.info("schema validation passed");
        } catch (SQLException sqle) {
            log.error("could not get database metadata", sqle);
            throw new RuntimeException("Error retreiving database metadata");
        } catch (RuntimeException rte) {
            log.error("error validating schema", rte);
            throw rte;
        }
    }

    private void addOverlord() {
        if (log.isDebugEnabled()) {
            log.debug("adding overlord");
        }

        User overlord = entityFactory.createUser();
        overlord.setUsername(User.USERNAME_OVERLORD);
        overlord.setFirstName("Cosmo");
        overlord.setLastName("Administrator");
        overlord.setPassword("cosmo");
        overlord.setEmail("root@localhost");
        overlord.setAdmin(Boolean.TRUE);

        Preference loginUrlPref =
            entityFactory.createPreference(UIConstants.PREF_KEY_LOGIN_URL, rootLoginUrl);
        overlord.addPreference(loginUrlPref);

        userService.createUser(overlord);
    }

    private void addServerProperties() {
        serverPropertyService.setServerProperty(
                ServerProperty.PROP_SCHEMA_VERSION,
                CosmoConstants.SCHEMA_VERSION);
    }

    public void setRootLoginUrl(String rootLoginUrl) {
        this.rootLoginUrl = rootLoginUrl;
    }

    public String getRootLoginUrl() {
        return rootLoginUrl;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
    
}
