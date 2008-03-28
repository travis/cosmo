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

import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.transaction.TransactionManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.osaf.cosmo.xml.DomReader;
import org.osaf.cosmo.xml.DomWriter;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate3.support.ClobStringType;
import org.w3c.dom.Element;

/**
 * Custom Hibernate type that persists a XML DOM Element to a CLOB field in
 * the database.
 */
public class XmlClobType extends ClobStringType {
    private static final Log log = LogFactory.getLog(XmlClobType.class);

    public XmlClobType() {
        super();
    }

    protected XmlClobType(LobHandler lobHandler,
                          TransactionManager jtaTransactionManager) {
        super(lobHandler, jtaTransactionManager);
    }

    protected Object nullSafeGetInternal(ResultSet resultSet,
                                         String[] columns,
                                         Object owner,
                                         LobHandler lobHandler)
        throws SQLException, HibernateException {
        if (columns == null || columns.length != 1)
            throw new HibernateException("Only one column name can be used for the " + getClass() + " user type");

        Reader reader = lobHandler.getClobAsCharacterStream(resultSet, columns[0]);
        if (reader == null)
            return null;

        // don't throw an exception if the clob can't be parsed, because
        // otherwise this item will not be able to be loaded (thus, it won't be
        // able to be deleted)

        String clob = null;
        try {
            clob = IOUtils.toString(reader);
        } catch (Exception e) {
            log.error("Error reading XML clob", e);
            return null;
        }

        try {
            return DomReader.read(clob);
        } catch (Exception e) {
            log.error("Error deserializing XML clob '" + clob + "'", e);
            return null;
        } finally {
            if (reader != null)
                try {reader.close();} catch(Exception e) {}
        }
    }

    protected void nullSafeSetInternal(PreparedStatement statement,
                                       int index,
                                       Object value,
                                       LobCreator lobCreator)
        throws SQLException, HibernateException {
        String xml = null;
        if (value != null) {
            try {
                xml = DomWriter.write((Element) value);
            } catch (Exception e) {
                log.error("Error serializing XML clob", e);
                throw new HibernateException("Error serializing XML clob: " + e.getMessage());
            }
        }

        super.nullSafeSetInternal(statement, index, xml, lobCreator);
    }

    @Override
    public Object deepCopy(Object value)
        throws HibernateException {
        if (value == null)
            return null;
        return ((Element)value).cloneNode(true);
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if(x==null || y==null)
            return false;
        
        return ((Element) x).isEqualNode((Element) y);
    }

    public Class returnedClass() {
        return Element.class;
    }

    public boolean isMutable() {
        return true;
    }
}
