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
package org.osaf.cosmo.hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.TimestampType;

/**
 * Custom Hibernate type that maps a java.util.Date
 * to a SQL BIGINT column, storing the number of 
 * milliseconds that have passed since Jan 1, 1970 GMT.
 *
 */
public class LongTimestampType extends TimestampType {

   
    @Override
    public String objectToSQLString(Object value, Dialect dialect)
            throws Exception {
        return "" + ((Date) value).getTime();
    }

    @Override
    public Object get(ResultSet rs, String index) throws HibernateException, SQLException {
        return new Date(rs.getLong(index));
    }

    @Override
    public void set(PreparedStatement ps, Object value, int index) throws HibernateException, SQLException {
        ps.setLong(index, ((Date)value).getTime());
    }

    public String getName() {
        return "long_timestamp";
    }
    
    public int sqlType() {
        return Types.BIGINT;
    }
}
