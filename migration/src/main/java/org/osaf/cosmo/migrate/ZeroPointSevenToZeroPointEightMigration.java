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
package org.osaf.cosmo.migrate;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Migration implementation that migrates Cosmo 0.7.x (schema ver 110)
 * to Cosmo 0.8 (schema ver 120)
 * 
 * Supports MySQL5 and Derby dialects only.
 *
 */
public class ZeroPointSevenToZeroPointEightMigration extends AbstractMigration {
    
    private static final Log log = LogFactory.getLog(ZeroPointSevenToZeroPointEightMigration.class);
    private static final Base64 etagEncoder = new Base64();
    private static final MessageDigest etagDigest;
    
    static {
        try {
            etagDigest = MessageDigest.getInstance("sha1");
        } catch (Exception e) {
            throw new RuntimeException("Platform does not support sha1?", e);
        }
    }
    
    static {
        // use custom timezone registry
        System.setProperty("net.fortuna.ical4j.timezone.registry", "org.osaf.cosmo.calendar.CosmoTimeZoneRegistryFactory");
    }
    
    @Override
    public String getFromVersion() {
        return "110";
    }

    @Override
    public String getToVersion() {
        // switching to different schema version format
        return "120";
    }

    
    @Override
    public List<String> getSupportedDialects() {
        ArrayList<String> dialects = new ArrayList<String>();
        dialects.add("Derby");
        dialects.add("MySQL5");
        return dialects;
    }

    public void migrateData(Connection conn, String dialect) throws Exception {
        
        log.debug("starting migrateData()");
        migrateItems(conn);
        migrateSubscriptions(conn);
        migratePreferences(conn);
        migrateUsers(conn);
    }
    
    
    /**
     * add etag to items
     */
    private void migrateItems(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        
        ResultSet rs = null;
        
        long count = 0;
        
        log.debug("starting migrateItems()");
        
        try {
            // get all data to migrate
            stmt = conn.prepareStatement("select id, uid, modifydate from item");
            // migration statment
            updateStmt = conn.prepareStatement("update item set etag=? where id=?");
            
            rs = stmt.executeQuery();
            
            // migrate each event_stamp row
            while(rs.next()) {
                long id = rs.getLong(1);
                String uid = rs.getString(2);
                long modifyDate = rs.getLong(3);
                
                updateStmt.setString(1, calculateItemEtag(uid, modifyDate));
                updateStmt.setLong(2, id);
                
                updateStmt.executeUpdate();
                count++;
            }
            
            
        } finally {
            close(stmt);
            close(updateStmt);
        }
        
        log.debug("processed " + count + " items");
    }
    
    /**
     * add etag to subscriptions
     */
    private void migrateSubscriptions(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        
        ResultSet rs = null;
        
        long count = 0;
        
        log.debug("starting migrateSubscriptions()");
        
        try {
            // get all to migrate
            stmt = conn.prepareStatement("select s.id, u.uid, s.displayName, s.modifydate from subscription s, users u where s.ownerid=u.id");
            // migration statment
            updateStmt = conn.prepareStatement("update subscription set etag=? where id=?");
            
            rs = stmt.executeQuery();
            
            // migrate each subscription row
            while(rs.next()) {
                long id = rs.getLong(1);
                String userUid = rs.getString(2);
                String displayName = rs.getString(3);
                long modifyDate = rs.getLong(4);
                
                updateStmt.setString(1, calculateSubscriptionEtag(userUid, displayName, modifyDate));
                updateStmt.setLong(2, id);
                
                updateStmt.executeUpdate();
                count++;
            }
            
            
        } finally {
            close(stmt);
            close(updateStmt);
        }
        
        log.debug("processed " + count + " subscriptions");
    }
    
    /**
     * add etag to user preferences
     */
    private void migratePreferences(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        
        ResultSet rs = null;
        
        long count = 0;
        
        log.debug("starting migratePreferences()");
        
        try {
            // get all to migrate
            stmt = conn.prepareStatement("select p.id, u.uid, p.preferencename, p.modifydate from preference p, users u where p.userid=u.id");
            // migration statment
            updateStmt = conn.prepareStatement("update subscription set etag=? where id=?");
            
            rs = stmt.executeQuery();
            
            // migrate each preference row
            while(rs.next()) {
                long id = rs.getLong(1);
                String userUid = rs.getString(2);
                String key = rs.getString(3);
                long modifyDate = rs.getLong(4);
                
                updateStmt.setString(1, calculatePreferenceEtag(userUid, key, modifyDate));
                updateStmt.setLong(2, id);
                
                updateStmt.executeUpdate();
                count++;
            }
            
            
        } finally {
            close(stmt);
            close(updateStmt);
        }
        
        log.debug("processed " + count + " preferences");
    }
    
    /**
     * add etag to users
     */
    private void migrateUsers(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        
        ResultSet rs = null;
        
        long count = 0;
        
        log.debug("starting migrateUsers()");
        
        try {
            // get all to migrate
            stmt = conn.prepareStatement("select id, username, modifydate from users");
            // migration statment
            updateStmt = conn.prepareStatement("update users set etag=? where id=?");
            
            rs = stmt.executeQuery();
            
            // migrate each preference row
            while(rs.next()) {
                long id = rs.getLong(1);
                String userName = rs.getString(2);
                long modifyDate = rs.getLong(3);
                
                updateStmt.setString(1, calculateUserEtag(userName, modifyDate));
                updateStmt.setLong(2, id);
                
                updateStmt.executeUpdate();
                count++;
            }
            
            
        } finally {
            close(stmt);
            close(updateStmt);
        }
        
        log.debug("processed " + count + " users");
    }
    
    
    private String calculateItemEtag(String uid, long modifyDate) {
        String etag = uid + ":" + modifyDate;
        return new String(etagEncoder.encode(etagDigest.digest(etag.getBytes())));
    }
    
    private String calculateSubscriptionEtag(String userUid, String name,
            long modifyDate) {
        String etag = userUid + ":" + name + ":" + modifyDate;
        return new String(etagEncoder
                .encode(etagDigest.digest(etag.getBytes())));
    }
    
    private String calculatePreferenceEtag(String userUid, String key,
            long modifyDate) {
        String etag = userUid + ":" + key + ":" + modifyDate;
        return new String(etagEncoder
                .encode(etagDigest.digest(etag.getBytes())));
    }
    
    private String calculateUserEtag(String userName, long modifyDate) {
        String etag = userName + ":" + modifyDate;
        return new String(etagEncoder
                .encode(etagDigest.digest(etag.getBytes())));
    }
}
