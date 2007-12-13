/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.migrate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages migration from the current version
 * of Cosmo to the latest version.  Requires a 
 * DataSource to the Cosmo database, a List of Migration
 * objects, and a dialect.
 * 
 * The MigrationManager will retrieve the current
 * schema version from the DataSource, then determin
 * which Migration objects to run in the correct
 * order.  The result will be a Cosmo database that
 * is migrated to the latest version included in the
 * list of Migration objects.
 *
 */
public class MigrationManager {
    
    private static final Log log = LogFactory.getLog(MigrationManager.class);
    
    private DataSource datasource = null;
    private String dialect = null;
    private List<Migration> migrations = null;
    
    public DataSource getDatasource() {
        return datasource;
    }
    
    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }
    
    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public List<Migration> getMigrations() {
        return migrations;
    }

    public void setMigrations(List<Migration> migrations) {
        this.migrations = migrations;
    }
    
    /**
     * Applies migrations based on the current version
     * of Cosmo.
     */
    public void migrate() throws Exception {
        Connection conn = datasource.getConnection();
        try {
            conn.setAutoCommit(false);
            String currentVersion = getCurrentVersion(conn);
            
            log.info("Found Cosmo schema version " + currentVersion);
            
            List<Migration> toApply = getMigrationsToApply(currentVersion);
            
            if(toApply.size()==0)
                log.info("No migrations found for version " + currentVersion + " and dialect " + dialect);
            else
                log.info("Found " + toApply.size() + " migrations using dialect: " + dialect);
            
            for(Migration migration: toApply) {
                log.info("Migrating " + migration.getFromVersion() + " to " + migration.getToVersion());
                migration.migrate(conn, dialect);
                log.info("Migrated to " + migration.getToVersion());
            }
            
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }
    
    protected String getCurrentVersion(Connection conn) throws Exception {
        PreparedStatement stmt = conn.prepareStatement("select propertyvalue from server_properties where propertyname=?");
        stmt.setString(1, "cosmo.schemaVersion");
        ResultSet rs = stmt.executeQuery();
        String result = null;
        if(rs.next())
            result = rs.getString(1);
        
        rs.close();
        stmt.close();
        return result;
    }
    
    private List<Migration> getMigrationsToApply(String version) {
        List<Migration> toApply = new ArrayList<Migration>();
        
        Migration migration = getMigration(version);
        while(migration!=null) {
            toApply.add(migration);
            migration = getMigration(migration.getToVersion());
        }
        
        return toApply;
    }
    
    private Migration getMigration(String from) {
        for(Migration m: migrations)
            if(m.getFromVersion().equals(from) &&
               m.getSupportedDialects().contains(dialect) )
                return m;
        
        return null;
    }

}
