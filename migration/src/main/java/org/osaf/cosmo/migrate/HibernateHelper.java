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
import java.sql.SQLException;



/**
 * Contains methods to mimic Hibernate actions, useful
 * in data migration.
 */
public class HibernateHelper {
    
    private long hi;
    private int lo;
    private int maxLo;
    
    public HibernateHelper() {
        maxLo = Short.MAX_VALUE;
        lo = maxLo + 1;
    }
    
    /**
     * Generates id using table created by hibernate, using hibernate's
     * hi/lo algorithm.  Needed for Derby dialect.
     */
    public long getNexIdUsingHiLoGenerator(Connection conn) throws Exception {
        String sql = "select next_hi from hibernate_unique_key";
        int result;
        PreparedStatement qps = conn.prepareStatement(sql);
        try {
            ResultSet rs = qps.executeQuery();
            if ( !rs.next() ) {
                throw new RuntimeException("Unable to get next_hi!");
            }
            result = rs.getInt(1);
            rs.close();
        }
        catch (SQLException sqle) {
            throw sqle;
        }
        finally {
            qps.close();
        }

        sql = "update hibernate_unique_key set next_hi=? where next_hi=?";
        
        PreparedStatement ups = conn.prepareStatement(sql);
        try {
            ups.setInt( 1, result + 1 );
            ups.setInt( 2, result );
            ups.executeUpdate();
        }
        catch (SQLException sqle) {
            throw sqle;
        }
        finally {
            ups.close();
        }
        
        if (maxLo < 1) {
            return (long) result;
        }
        if (lo>maxLo) {
            int hival = result;
            lo = (hival == 0) ? 1 : 0;
            hi = hival * (maxLo+1);
        }

        return (hi + lo++);
        
    }
}
