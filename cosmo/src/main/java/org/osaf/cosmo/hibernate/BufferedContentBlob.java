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

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.osaf.cosmo.io.BufferedContent;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate3.support.AbstractLobType;


/**
 * Custom Hibernate type that persists BufferedContent to BLOB field.
 */
public class BufferedContentBlob
        extends AbstractLobType {
    private static final Log log = LogFactory.getLog(BufferedContentBlob.class);

    /**
     * Constructor used by Hibernate: fetches config-time LobHandler and
     * config-time JTA TransactionManager from LocalSessionFactoryBean.
     *
     * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#getConfigTimeLobHandler
     * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#getConfigTimeTransactionManager
     */
    public BufferedContentBlob() {
        super();
    }

    /**
     * Constructor used for testing: takes an explicit LobHandler
     * and an explicit JTA TransactionManager (can be null).
     */
    protected BufferedContentBlob(LobHandler lobHandler, TransactionManager jtaTransactionManager) {
        super(lobHandler, jtaTransactionManager);
    }

    /**
     * @param resultSet a JDBC result set
     * @param columns the column names
     * @param owner the containing entity
     * @param lobHandler the LobHandler to use
     */
    protected Object nullSafeGetInternal(ResultSet resultSet, String[] columns, Object owner, LobHandler lobHandler)
            throws SQLException, HibernateException {
        
        // we only handle one column, so panic if it isn't so
        if (columns == null || columns.length != 1)
            throw new HibernateException("Only one column name can be used for the " + getClass() + " user type");

        InputStream is = lobHandler.getBlobAsBinaryStream(resultSet, columns[0]);
        if(is==null)
            return null;
        
        BufferedContent content = null;
        
        try {
            content = new BufferedContent(is);
        } catch(IOException ioe) {
            throw new HibernateException("cannot read binary stream");
        }
        finally {
            if (is != null)
                try {is.close();} catch(Exception e) {}
        }
        
        return content;
    }

    
    /**
     * @param statement the PreparedStatement to set on
     * @param index the statement parameter index
     * @param value the file
     * @param lobCreator the LobCreator to use
     * @throws SQLException if thrown by JDBC methods
     */
    protected void nullSafeSetInternal(PreparedStatement statement, int index, Object value, LobCreator lobCreator)
            throws SQLException, HibernateException {
        
        BufferedContent content = (BufferedContent) value;
       
        if(content!=null)
            lobCreator.setBlobAsBinaryStream(statement, index, content.getInputStream(), (int) content.getLength());
        else
            lobCreator.setBlobAsBinaryStream(statement, index, null, 0);
    }

    public Class returnedClass() {
        return BufferedContent.class;
    }

    public boolean isMutable() {
        return true;
    }

    public int[] sqlTypes() {
        return new int[] { Types.BLOB };
    }
}
