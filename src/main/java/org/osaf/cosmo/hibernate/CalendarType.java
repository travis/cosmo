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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

/**
 * Custom Hibernate type that maps a java.util.Calendar 
 * to two columns.  One column stores the timestamp (datetime)
 * and the other stores the timezone.  This provides support
 * for datetimes with timezone.
 */
public class CalendarType implements CompositeUserType {

    public Object assemble(Serializable cached, SessionImplementor session,
            Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    public Object deepCopy(Object obj) throws HibernateException {
        if(obj==null)
            return null;
        return ((Calendar) obj).clone();
    }

    public Serializable disassemble(Object value, SessionImplementor session)
            throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    public boolean equals(Object val1, Object val2) throws HibernateException {
        if(val1==val2)
            return true;
        
        if(val1==null || val2==null)
            return false;
        
        Calendar cal1 = (Calendar) val1;
        Calendar cal2 = (Calendar) val2;
        
        return cal1.equals(cal2);
    }

    public String[] getPropertyNames() {
        return new String[] {"date", "timezone" };
    }

    public Type[] getPropertyTypes() {
        return new Type[] {Hibernate.CALENDAR, Hibernate.TIMEZONE};
    }

    public Object getPropertyValue(Object component, int property)
            throws HibernateException {
        if(property == 0) 
            return (Calendar) component;
        if(property == 1) 
            return ((Calendar) component).getTimeZone().getID();
        
        return null;
    }

    public int hashCode(Object obj) throws HibernateException {
        return obj.hashCode();
    }

    public boolean isMutable() {
        return true;
    }

    public Object nullSafeGet(ResultSet rs, String[] names,
            SessionImplementor session, Object owner) throws HibernateException,
            SQLException {
        
        TimeZone tz = (TimeZone) Hibernate.TIMEZONE.nullSafeGet(rs, names[1]);
        if(tz==null)
            tz = TimeZone.getDefault();
        Calendar cal = new GregorianCalendar(tz);
        
        Timestamp ts = rs.getTimestamp(names[0], cal);
        
        if (ts!=null) {
            if ( Environment.jvmHasTimestampBug() ) {
                cal.setTime( new Date( ts.getTime() + ts.getNanos() / 1000000 ) );
            }
            else {
                cal.setTime(ts);
            }
            return cal;
        }
        else {
            return null;
        }
    }

    public void nullSafeSet(PreparedStatement st, Object obj, int index,
            SessionImplementor arg3) throws HibernateException, SQLException {
        
        if(obj==null) {
            Hibernate.CALENDAR.nullSafeSet(st, obj, index);
            Hibernate.TIMEZONE.nullSafeSet(st, obj, index+1);
        } else {
            Hibernate.CALENDAR.nullSafeSet(st, obj, index);
            Hibernate.TIMEZONE.nullSafeSet(st, ((Calendar) obj).getTimeZone(), index+1);
        }
    }

    public Object replace(Object original, Object target, SessionImplementor session,
            Object owner) throws HibernateException {
        return deepCopy(original);
    }

    public Class returnedClass() {
        return Calendar.class;
    }

    public void setPropertyValue(Object component, int property, Object value)
            throws HibernateException {
        Calendar cal = (Calendar) component;
        if(property == 0)
            cal.setTime(((Calendar) value).getTime());
        else if(property == 1) 
            cal.setTimeZone((TimeZone) value);
    }

}
