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
package org.osaf.cosmo.hibernate;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;

/**
 * LobHandler that uses java.sql.Blob to work with PostgreSQL.
 * Hibernate creates BLOB columns as OID in Postgres and 
 * DefaultLobHandler uses setBinaryStream(), which works fine
 * with MySQL and Derby BLOB, but not Postgres OID.
 */
public class CosmoLobHandler extends DefaultLobHandler {

    @Override
    public byte[] getBlobAsBytes(ResultSet rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index); 
        return blob.getBytes(1, (int) blob.length()); 
    }

    @Override
    public LobCreator getLobCreator() {
        return new CosmoLobCreator();
    }
    
    protected class CosmoLobCreator extends DefaultLobCreator {

        public void setBlobAsBytes(PreparedStatement ps, int paramIndex, byte[] content)
                throws SQLException {

            ps.setBlob(paramIndex, Hibernate.createBlob(content));
            if (logger.isDebugEnabled()) {
                logger.debug(content != null ? "Set bytes for BLOB with length " + content.length :
                        "Set BLOB to null");
            }
        }
    }

}
