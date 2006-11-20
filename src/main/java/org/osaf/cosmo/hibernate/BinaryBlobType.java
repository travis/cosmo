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
/**
 * 
 */
package org.osaf.cosmo.hibernate;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.type.MutableType;

/**
 * @author rletness
 *
 */
public class BinaryBlobType extends MutableType implements Comparator {

    @Override
    protected Object deepCopyNotNull(Object value) throws HibernateException {
        byte[] bytes = (byte[]) value;
        byte[] result = new byte[bytes.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        return result;
    }

    @Override
    public Object fromStringValue(String arg0) throws HibernateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
        Blob blob = rs.getBlob(name); 
        return blob.getBytes(1, (int) blob.length()); 
    }


    public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        st.setBlob(index, Hibernate.createBlob((byte[]) value));
    }

    @Override
    public int sqlType() {
        return Types.BLOB;
    }

    public Comparator getComparator() {
        return this;
    }

    public int compare(Object o1, Object o2) {
        return compare( o1, o2, null );
    }
    
    public boolean isEqual(Object x, Object y) {
        return x==y || ( x!=null && y!=null && java.util.Arrays.equals( (byte[]) x, (byte[]) y ) );
    }
    
    public int getHashCode(Object x, EntityMode entityMode) {
        byte[] bytes = (byte[]) x;
        int hashCode = 1;
        for ( int j=0; j<bytes.length; j++ ) {
            hashCode = 31 * hashCode + bytes[j];
        }
        return hashCode;
    }

    public int compare(Object x, Object y, EntityMode entityMode) {
        byte[] xbytes = (byte[]) x;
        byte[] ybytes = (byte[]) y;
        if ( xbytes.length < ybytes.length ) return -1;
        if ( xbytes.length > ybytes.length ) return 1;
        for ( int i=0; i<xbytes.length; i++ ) {
            if ( xbytes[i] < ybytes[i] ) return -1;
            if ( xbytes[i] > ybytes[i] ) return 1;
        }
        return 0;
    }
    
    public String toString(Object val) throws HibernateException {
        byte[] bytes = ( byte[] ) val;
        StringBuffer buf = new StringBuffer();
        for ( int i=0; i<bytes.length; i++ ) {
            String hexStr = Integer.toHexString( bytes[i] - Byte.MIN_VALUE );
            if ( hexStr.length()==1 ) buf.append('0');
            buf.append(hexStr);
        }
        return buf.toString();
    }

    public String getName() {
        // TODO Auto-generated method stub
        return "byte[]";
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    public Class getReturnedClass() {
        return byte[].class;
    }
}
