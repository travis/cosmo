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

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import javax.transaction.TransactionManager;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate3.support.ClobStringType;


/**
 * Custom Hibernate type that persists ical4j Calendar object
 * to CLOB field in database.
 */
public class CalendarClobType
        extends ClobStringType {
    private static final Log log = LogFactory.getLog(CalendarClobType.class);

    /**
     * Constructor used by Hibernate: fetches config-time LobHandler and
     * config-time JTA TransactionManager from LocalSessionFactoryBean.
     *
     * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#getConfigTimeLobHandler
     * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#getConfigTimeTransactionManager
     */
    public CalendarClobType() {
        super();
    }

    /**
     * Constructor used for testing: takes an explicit LobHandler
     * and an explicit JTA TransactionManager (can be null).
     */
    protected CalendarClobType(LobHandler lobHandler, TransactionManager jtaTransactionManager) {
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

        Reader reader = lobHandler.getClobAsCharacterStream(resultSet, columns[0]);
        if(reader==null)
            return null;
        
        Calendar calendar = null;
        
        try {
            calendar = CalendarUtils.parseCalendar(reader);
        } catch (ParserException e) {
            log.error("error parsing icalendar from db", e);
            // shouldn't happen because we always persist valid data
            throw new HibernateException("cannot parse icalendar stream");
        } catch(IOException ioe) {
            throw new HibernateException("cannot read icalendar stream");
        }
        finally {
            if (reader != null)
                try {reader.close();} catch(Exception e) {}
        }
        
        return calendar;
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
        
        String icalStr = null;
        if(value!=null)
            icalStr = ((Calendar) value).toString();
        super.nullSafeSetInternal(statement, index, icalStr, lobCreator);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null)
            return null;
        try {
            return new Calendar((Calendar) value);
        } catch (IOException e) {
            throw new HibernateException("Unable to read original calendar", e);
        } catch (ParseException e) {
            log.error("parse error with following ics:" + ((Calendar) value).toString());
            throw new HibernateException("Unable to parse original calendar", e);
        } catch (URISyntaxException e) {
            throw new HibernateException("Unknown syntax exception", e);
        }
    }

    public Class returnedClass() {
        return Calendar.class;
    }

    public boolean isMutable() {
        return true;
    }
}
