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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Migration implementation that migrates Cosmo 0.8 (schema ver 120)
 * to Cosmo 0.9 (schema ver 130)
 * 
 * Supports MySQL5 and Derby dialects only.
 *
 */
public class ZeroPointEightToZeroPointNineMigration extends AbstractMigration {
    
    private static final Log log = LogFactory.getLog(ZeroPointEightToZeroPointNineMigration.class);
    
    @Override
    public String getFromVersion() {
        return "120";
    }

    @Override
    public String getToVersion() {
        // switching to different schema version format
        return "130";
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
        migrateModifications(conn);
    }
    
    
    /**
     * Fix modification items that are out of sync with the parent item.
     */
    private void migrateModifications(Connection conn) throws Exception {
        
        PreparedStatement stmt = null;
        PreparedStatement updateItemStmt = null;
        PreparedStatement insertCollectionItemStmt = null;
        PreparedStatement parentsStmt = null;
        
        ResultSet rs = null;
        
        long count = 0;
        
        log.debug("starting migrateModifications()");
        
        try {
            // get all stamp/item data to migrate
            stmt = conn.prepareStatement("select id, modifiesitemid from item where modifiesitemid is not null");
            // update timestamp
            updateItemStmt = conn.prepareStatement("update item set modifydate=?, version=version+1 where id=?");
            insertCollectionItemStmt = conn.prepareStatement("insert into collection_item(collectionid, itemid) values (?,?)");
            parentsStmt = conn.prepareStatement("select collectionid from collection_item where itemid=?");
            
            rs = stmt.executeQuery();
            
            HashMap<Long, Set<Long>> parentMap = new HashMap<Long, Set<Long>>();
            
            // examine each modification and fix if necessary
            while(rs.next()) {
                long itemId = rs.getLong(1);
                long modifiesItemId = rs.getLong(2);
                
                Set<Long> modParents = getParents(parentsStmt, itemId);
                Set<Long> masterParents = parentMap.get(modifiesItemId);
                
                // cache the set of parents as it doesn't change
                if(masterParents==null) {
                    masterParents = getParents(parentsStmt, modifiesItemId);
                    parentMap.put(modifiesItemId, masterParents);
                }
                
                // If both sets of parents are equal, we are good
                if(modParents.equals(masterParents))
                    continue;
                
                log.debug("found out-of-sync modification: id " + itemId);
                
                // otherwise add modification to each parent that
                // master is in
                for(Long parent: masterParents) {
                    
                    // Only care about collections that item is not in
                    if(modParents.contains(parent))
                        continue;
                    
                    log.debug("adding out-of-sync modification id " + itemId + " to parent " + parent);
                    
                    // insert into parent
                    insertCollectionItemStmt.setLong(1, parent);
                    insertCollectionItemStmt.setLong(2, itemId);
                    if(insertCollectionItemStmt.executeUpdate()!=1)
                        throw new RuntimeException("insert into collection_item failed");
                    
                    // update parent and item version/timestamps
                    updateItemStmt.setLong(1, System.currentTimeMillis());
                    updateItemStmt.setLong(2, itemId);
                    if(updateItemStmt.executeUpdate()!=1)
                        throw new RuntimeException("update of item failed");
                    
                    updateItemStmt.setLong(1, System.currentTimeMillis());
                    updateItemStmt.setLong(2, parent);
                    if(updateItemStmt.executeUpdate()!=1)
                        throw new RuntimeException("update of item failed");   
                }
                
                
                count++;
            }
            
            
        } finally {
            close(stmt);
            close(updateItemStmt);
            close(insertCollectionItemStmt);
            close(parentsStmt);
            close(rs);
        }
        
        log.debug("processed " + count + " out of sync item modifications");
    }
    
    private Set<Long> getParents(PreparedStatement ps, long itemId)
            throws Exception {
        HashSet<Long> parents = new HashSet<Long>();
        ps.setLong(1, itemId);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            parents.add(rs.getLong(1));

        rs.close();

        return parents;
    }
}
