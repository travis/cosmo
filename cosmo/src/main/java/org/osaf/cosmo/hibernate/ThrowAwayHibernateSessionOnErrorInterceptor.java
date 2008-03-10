/*
 * Copyright 2008 Open Source Applications Foundation
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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Interceptor that catches RuntimeException and throws 
 * away the currently bound Hibernate Session if necessary.
 * If a session is thrown away, a new one is bound to allow
 * retry attempts.  It turns out just clearing the sesion
 * using session.clear() doesn't always work, and a new 
 * session should be used for additional retry attempts.
 */
public class ThrowAwayHibernateSessionOnErrorInterceptor implements MethodInterceptor{

    private SessionFactory sessionFactory = null;
   
    private static final Log log =
        LogFactory.getLog(ThrowAwayHibernateSessionOnErrorInterceptor.class);
   
    public Object invoke(MethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (RuntimeException e) {
            handleException();
            throw e;
        }
    }

    private void handleException() {
        
        // If session is bound to transaction, close it and create/bind
        // new session to prevent stale data when retrying transaction
        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            
            if(log.isDebugEnabled())
                log.debug("throwing away bad session and binding new one");
            
            // Get current session and close
            SessionHolder sessionHolder =
                (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
          
            SessionFactoryUtils.closeSession(sessionHolder.getSession());
            
            // Open new session and bind (this session should be closed and
            // unbound elsewhere, for example OpenSessionInViewFilter)
            Session session = SessionFactoryUtils.getSession(sessionFactory, true);
            session.setFlushMode(FlushMode.MANUAL);
            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        }
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
