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
import java.util.HashSet;
import java.util.Set;

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
        return "120";
    }

    
    @Override
    public Set<String> getSupportedDialects() {
        HashSet<String> dialects = new HashSet<String>();
        dialects.add("Derby");
        dialects.add("MySQL5");
        dialects.add("PostgreSQL");
        return dialects;
    }

    public void migrateData(Connection conn, String dialect) throws Exception {
        
        log.debug("starting migrateData()");
        fixDuplicateIcalUids(conn);
        migrateItems(conn);
        migrateSubscriptions(conn);
        migratePreferences(conn);
        migrateUsers(conn);
    }
    
    /**
     * ensure collections do not contain duplicate icaluids
     */
    private void fixDuplicateIcalUids(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement selectByIcalUid = null;
        PreparedStatement selectNumDuplicates = null;
        PreparedStatement updateStmt = null;
        PreparedStatement updateColStmt = null;
        
        ResultSet rs = null;
        
        long itemCount = 0;
        HashSet<Long> collectionIds = new HashSet<Long>();
       
        log.debug("starting fixDuplicateIcalUids()");
        
        try {
            // get all to migrate
            stmt = conn.prepareStatement("select * from (SELECT ci.collectionid, i.icaluid, count(*) as counticaluid from item i, collection_item ci where ci.itemid=i.id and i.icaluid is not null and i.modifiesitemid is null group by i.icaluid, ci.collectionid) as ss where counticaluid > 1");
            selectNumDuplicates = conn.prepareStatement("select count(*) from item i, collection_item ci where ci.collectionid=? and ci.itemid=i.id and i.icaluid=?");
            selectByIcalUid = conn.prepareStatement("select i.id from item i, collection_item ci where ci.collectionid=? and ci.itemid=i.id and i.icaluid=? and upper(i.icaluid)!=upper(i.uid)");
            // migration statements
            updateStmt = conn.prepareStatement("update item set icaluid=upper(uid), modifydate=?, version=version+1 where id=?");
            updateStmt.setLong(1, System.currentTimeMillis());
            
            updateColStmt = conn.prepareStatement("update item set modifydate=?, version=version+1 where id=?");
            updateColStmt.setLong(1, System.currentTimeMillis());
            
            rs = stmt.executeQuery();
            
            
            // migrate each duplicate icaluid
            while(rs.next()) {
                long collectionId = rs.getLong(1);
                String icalUid = rs.getString(2);
                
                collectionIds.add(collectionId);
                
                selectNumDuplicates.setLong(1, collectionId);
                selectNumDuplicates.setString(2, icalUid);
                ResultSet duplicatesRs = selectNumDuplicates.executeQuery();
                duplicatesRs.next();
                int numDuplicates = duplicatesRs.getInt(1);
                duplicatesRs.close();
                
                // While there are more than one item with the same
                // icaluid in the collection, attempt to fix by updating
                // a single item in the list of duplicates.
                while(numDuplicates>1) {
                    log.debug("found " + numDuplicates + " for icaluid " + icalUid + " collectionid " + collectionId);
                    log.debug("fixing collection " + collectionId + " icaluid " + icalUid);
                    
                    selectByIcalUid.setLong(1, collectionId);
                    selectByIcalUid.setString(2, icalUid);
                    ResultSet toFix = selectByIcalUid.executeQuery();
                    toFix.next();
                    
                    // fix icaluid to be uid, update timestamp
                    long itemId = toFix.getLong(1);
                    log.debug("fixing item " + itemId + " by setting icalUid to uid");
                    updateStmt.setLong(2, itemId);
                    updateStmt.executeUpdate();
                    toFix.close();
                    
                    // update collection timestamp
                    updateColStmt.setLong(2, collectionId);
                    updateColStmt.executeUpdate();
                    
                    itemCount++;
                    
                    duplicatesRs = selectNumDuplicates.executeQuery();
                    duplicatesRs.next();
                    numDuplicates = duplicatesRs.getInt(1);
                }
                
            }
            
            
        } finally {
            close(stmt);
            close(updateStmt);
            close(selectNumDuplicates);
            close(selectByIcalUid);
        }
        
        log.debug("fixed " + collectionIds.size() + " collections that contained duplicate icaluids");
        log.debug("fixed " + itemCount + " items with duplicate icaluids");
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
            // migration statement
            updateStmt = conn.prepareStatement("update item set etag=? where id=?");
            
            rs = stmt.executeQuery();
            
            // migrate each item row
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
            stmt = conn.prepareStatement("select s.id, u.uid, s.displayname, s.modifydate from subscription s, users u where s.ownerid=u.id");
            // migration statement
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
            stmt = conn.prepareStatement("select p.id, u.uid, p.preferencename, p.modifydate from user_preferences p, users u where p.userid=u.id");
            // migration statment
            updateStmt = conn.prepareStatement("update user_preferences set etag=? where id=?");
            
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
