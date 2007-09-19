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
import java.util.Date;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.osaf.cosmo.model.AuditableObject;

/**
 * Hibernate Interceptor that updates modifiedDate each
 * time a dirty object is flushed, and sets creationDate
 * when an object is created.
 */
public class CosmoInterceptor extends EmptyInterceptor {

 
    @Override
    public boolean onFlushDirty(Object object, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if(! (object instanceof AuditableObject))
            return false;
        
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "modifiedDate".equals( propertyNames[i] ) ) {
                currentState[i] = new Date(System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onSave(Object object, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        
        if(! (object instanceof AuditableObject))
            return false;
        
        boolean changed = false;
        Date currDate = new Date();
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "creationDate".equals(propertyNames[i]) ||
                  "modifiedDate".equals(propertyNames[i]) ) {
                state[i] = currDate;
                changed = true;
            }
        }
        return changed;
    }

}
